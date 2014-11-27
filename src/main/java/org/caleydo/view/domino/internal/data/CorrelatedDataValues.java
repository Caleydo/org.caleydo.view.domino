/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import static org.caleydo.view.domino.internal.data.ADataDomainDataValues.isInvalid;

import java.util.Collection;
import java.util.Collections;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.DoubleFunctions;
import org.caleydo.core.util.function.Function2;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.IInvertableDoubleFunction;
import org.caleydo.core.util.function.MappedDoubleList;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.internal.Constants;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.Floats;
import com.jogamp.common.util.IntObjectHashMap;

/**
 * @author Samuel Gratzl
 *
 */
public class CorrelatedDataValues implements IDataValues, Function2<Integer, Integer, Color> {
	private final Numerical2DDataDomainValues wrappee;
	private final EDimension along;
	private final ECorrelation correlation;
	private final IInvertableDoubleFunction normalize = DoubleFunctions.normalize(-1, 1);
	private final TypedList other;

	private final IntObjectHashMap dimFullCompareCache = new IntObjectHashMap();
	private final IntObjectHashMap recFullCompareCache = new IntObjectHashMap();

	private final LoadingCache<Pair<Integer, Integer>, Double> cache = CacheBuilder.newBuilder().build(
			new CacheLoader<Pair<Integer, Integer>, Double>() {
				@Override
				public Double load(Pair<Integer, Integer> key) throws Exception {
					return compute(key.getFirst(), key.getSecond());
				}
	});

	private final Function2<Integer, Integer, Float> toRaw = new Function2<Integer, Integer, Float>() {
		@Override
		public Float apply(Integer dimensionID, Integer recordID) {
			Double r = getRaw(dimensionID, recordID);
			return r == null ? Float.NaN : r.floatValue();
		}
	};
	private final Function2<Integer, Integer, String> cell2Label = new Function2<Integer, Integer, String>() {
		@Override
		public String apply(Integer input1, Integer input2) {
			return getLabel(input1, input2);
		}
	};

	public CorrelatedDataValues(Numerical2DDataDomainValues wrappee, ECorrelation proj, EDimension along,
			TypedList other) {
		this.other = other;
		assert wrappee.getIDType(along) == other.getIdType();
		this.wrappee = wrappee;
		this.along = along;
		this.correlation = proj;
	}

	/**
	 * @param first
	 * @return
	 */
	private IDoubleList asList(final Integer id) {
		return new MappedDoubleList<>(other, new Function<Integer, Double>() {
			@Override
			public Double apply(Integer input) {
				Integer dimensionID = along.select(id, input);
				Integer recordID = along.select(input, id);
				Float f = wrappee.getRaw(dimensionID, recordID);
				return f == null ? Double.NaN : f.doubleValue();
			}
		});
	}

	protected Double compute(Integer first, Integer second) {
		IDoubleList a = asList(first);
		IDoubleList b = asList(second);
		if (a == null || b == null)
			return Double.NaN;
		return correlation.apply(a, b);
	}

	public Double getRaw(Integer dimensionID, Integer recordID) {
		if (isInvalid(dimensionID) || isInvalid(recordID))
			return null;
		Integer a = dimensionID.compareTo(recordID) < 0 ? dimensionID : recordID;
		Integer b = dimensionID.compareTo(recordID) < 0 ? recordID : dimensionID;
		Double r = cache.getUnchecked(Pair.make(a, b));
		return r;
	}

	protected String getLabel(Integer recordID, Integer dimensionID) {
		Object raw = getRaw(dimensionID, recordID);
		return ADataDomainDataValues.raw2string(raw);
	}

	public float getNormalized(Integer dimensionID, Integer recordID) {
		if (isInvalid(dimensionID) || isInvalid(recordID))
			return Float.NaN;
		Double raw = getRaw(dimensionID, recordID);
		if (raw == null)
			return Float.NaN;
		Double r = normalize.apply(raw);
		return r == null ? Float.NaN : r.floatValue();
	}

	@Override
	public Color apply(Integer recordID, Integer dimensionID) {
		if (isInvalid(recordID) || isInvalid(dimensionID))
			return Color.NOT_A_NUMBER_COLOR;
		float vs = getNormalized(dimensionID, recordID);
		if (Float.isNaN(vs))
			return Color.NOT_A_NUMBER_COLOR;
		return Constants.colorMapping(vs);
	}

	/**
	 * @return the correlation, see {@link #correlation}
	 */
	public ECorrelation getCorrelation() {
		return correlation;
	}
	/**
	 * @return the along, see {@link #along}
	 */
	public EDimension getAlong() {
		return along;
	}

	@Override
	public Color getColor() {
		return wrappee.getColor();
	}

	@Override
	public String getLabel() {
		return String.format("%s %s correlated using %s", wrappee.getLabel(), correlation.getLabel(), along.name());
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return wrappee.getDefaultGroups(this.along.opposite());
	}

	@Override
	public int compare(EDimension dim, int a, int b, ITypedCollection otherData) {
		switch (otherData.size()) {
		case 0:
			return a - b;
		case 1:
			Integer other = otherData.iterator().next();
			return Floats.compare(getNormalized(a, other), getNormalized(b, other));
		default:
			//
			float a_sum = getCached(dim, a, otherData);
			float b_sum = getCached(dim, b, otherData);
			return Floats.compare(a_sum, b_sum);
		}
	}

	private float getCached(EDimension dim, int a, ITypedCollection otherData) {
		IntObjectHashMap cache = dim.select(dimFullCompareCache, recFullCompareCache);
		int size = getDefaultGroups(dim.opposite()).size();
		if (otherData.size() != size)
			return sum(dim, a, otherData);
		if (cache.containsKey(a))
			return (Float) cache.get(a);
		float sum = sum(dim, a, otherData);
		cache.put(a, sum);
		return sum;
	}

	private float sum(EDimension dim, int a, ITypedCollection otherData) {
		// mean values
		float a_sum = 0;
		for (Integer other : otherData) {
			a_sum += getNormalized(a, other);
		}
		return a_sum;
	}

	@Override
	public String getExtensionID() {
		return "numerical.2projected";
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData, boolean[] existNeigbhor, boolean mediumTranspose) {
		b.put("id2double", toRaw);
		b.put("dimensions", dimData);
		b.put("dimensions.idType", dimData.getIdType());
		b.put("records", recData);
		b.put("records.idType", recData.getIdType());
		b.put(Function2.class, this);
		b.put("cell2color", this);
		b.put("cell2label", this.cell2Label);
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return Collections.singleton("heatmap");
	}

	@Override
	public boolean apply(String input) {
		return !"labels".equals(input) && !"distribution.bar".equals(input) && !"distribution.pie".equals(input);
	}

	@Override
	public void onSelectionChanged(boolean selected) {
		wrappee.onSelectionChanged(selected);
	}

}
