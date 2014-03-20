/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Collection;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.DoubleFunctions;
import org.caleydo.core.util.function.DoubleStatistics;
import org.caleydo.core.util.function.IInvertableDoubleFunction;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.Constants;

import com.google.common.base.Function;
import com.jogamp.common.util.IntObjectHashMap;

/**
 * @author Samuel Gratzl
 *
 */
public class ProjectedDataValues implements IDataValues, INumerical1DContainer,Function<Integer, Color> {
	private final Numerical2DDataDomainValues wrappee;
	private final EDimension along;
	private final EProjection proj;
	private final IntObjectHashMap data;
	private final Numerical1DMixin mixin;
	private final TypedGroupSet singleGroup = TypedGroupSet.createUngrouped(TypedCollections.INVALID_SINGLETON_SET);
	private final IInvertableDoubleFunction normalize;

	private ProjectedDataValues(Numerical2DDataDomainValues wrappee, EProjection proj, EDimension along,
			TypedGroupSet other) {
		assert wrappee.getIDType(along) == other.getIdType();
		this.wrappee = wrappee;
		this.along = along;
		this.proj = proj;
		TypedGroupSet rows = wrappee.getDefaultGroups(along.opposite());
		Pair<IntObjectHashMap, DoubleStatistics> r = project(rows, wrappee, along, proj, other);
		this.data = r.getFirst();
		DoubleStatistics stats = r.getSecond();
		this.normalize = DoubleFunctions.normalize(stats.getMin(), stats.getMax());
		this.mixin = new Numerical1DMixin(this, getDefaultGroups(along.opposite()), (float) r.getSecond().getMin(),
				(float) r.getSecond().getMax());

	}

	private static Pair<IntObjectHashMap, DoubleStatistics> project(TypedGroupSet rows,
			Numerical2DDataDomainValues wrappee, EDimension along, EProjection proj, TypedGroupSet projs) {
		DoubleStatistics.Builder b = DoubleStatistics.builder();
		IntObjectHashMap r = new IntObjectHashMap(rows.size());
		for (Integer id : rows) {
			DoubleStatistics.Builder idb = DoubleStatistics.builder();
			if (along.isDimension()) {
				for(Integer col : projs)
					idb.add(wrappee.getRaw(col, id));
			} else {
				for(Integer row : projs)
					idb.add(wrappee.getRaw(id, row));
			}
			float v = (float) proj.select(idb.build());
			r.put(id, v);
			b.add(v);
		}
		return Pair.make(r, b.build());
	}

	/**
	 * @return the proj, see {@link #proj}
	 */
	public EProjection getProj() {
		return proj;
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
		return String.format("%s %s projected along %s", wrappee.getLabel(), proj.getLabel(), along.name());
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		if (dim == this.along)
			return singleGroup;
		return wrappee.getDefaultGroups(dim);
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		if (dim == this.along)
			return 0;

		return 0;
	}

	@Override
	public String getExtensionID() {
		return "numerical.1projected";
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData, boolean[] existNeigbhor) {
		EDimension dim = along.opposite();
		TypedList data = along.opposite().select(dimData, recData);
		boolean transposed = data.getIdType() == this.singleGroup.getIdType();
		if (transposed) {
			dim = dim.opposite();
			data = along.select(dimData, recData);
		}
		b.put(EDimension.class, dim);
		fill(b, data, dim, existNeigbhor);
	}

	protected void fill(Builder b, TypedList data, EDimension dim, boolean[] existNeigbhor) {
		b.put(TypedList.class, data);
		b.put(IDType.class, data.getIdType());
		b.put("idType", data.getIdType());
		b.put("id2color", this);
		mixin.fill(b, data, dim, existNeigbhor);
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return mixin.getDefaultVisualization();
	}

	@Override
	public boolean apply(String input) {
		return !"labels".equals(input) && !"distribution.bar".equals(input) && !"distribution.pie".equals(input);
	}

	@Override
	public void onSelectionChanged(boolean selected) {
		wrappee.onSelectionChanged(selected);
	}

	@Override
	public Float getRaw(int id) {
		Float d = (Float) this.data.get(id);
		if (d == null)
			return Float.NaN;
		return d;
	}

	@Override
	public float getNormalized(int id) {
		return (float) normalize.apply(getRaw(id));
	}

	@Override
	public Color apply(Integer id) {
		if (ADataDomainDataValues.isInvalid(id))
			return Color.NOT_A_NUMBER_COLOR;
		float v = getNormalized(id);
		return Constants.colorMapping(v);
	}
}
