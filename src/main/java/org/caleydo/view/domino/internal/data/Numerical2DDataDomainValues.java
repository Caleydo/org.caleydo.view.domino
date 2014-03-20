/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.table.NumericalTable;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.Function2;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.api.model.typed.util.BitSetSet;
import org.caleydo.view.domino.internal.Constants;
import org.caleydo.view.domino.internal.prefs.MyPreferences;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Numerical2DDataDomainValues extends ADataDomainDataValues {
	private final TypedGroupSet recGroups;
	private final TypedGroupSet dimGroups;
	private final boolean isInteger;

	private final Function2<Integer, Integer, Float> toRaw = new Function2<Integer, Integer, Float>() {

		@Override
		public Float apply(Integer dimensionID, Integer recordID) {
			return getRaw(dimensionID, recordID);
		}
	};
	/**
	 * @param t
	 */
	public Numerical2DDataDomainValues(TablePerspective t) {
		super(t.getDataDomain().getLabel(), t);
		// Pair<TypedGroupSet, TypedGroupSet> r = extractGroups(t);
		this.recGroups = TypedGroupSet.createUngrouped(TypedSet.of(t.getRecordPerspective().getVirtualArray()));
		this.dimGroups = TypedGroupSet.createUngrouped(TypedSet.of(t.getDimensionPerspective().getVirtualArray()));
		this.isInteger = DataSupportDefinitions.dataClass(EDataClass.NATURAL_NUMBER).apply(t);
	}

	@Override
	public String getExtensionID() {
		return "numerical.2d";
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData, boolean[] existNeigbhor) {
		super.fillHeatMap(b, dimData, recData);

		if (dimData.getIdType() != getIDType(EDimension.DIMENSION)) { // swapped
			b.put("id2double", Functions2s.swap(toRaw));
		} else
			b.put("id2double", toRaw);
	}

	@Override
	public Float getRaw(Integer dimensionID, Integer recordID) {
		Object r = super.getRaw(dimensionID, recordID);
		if (r instanceof Float)
			return (Float) r;
		if (r instanceof Integer && isInteger) {
			Integer i = (Integer) r;
			if (i.intValue() == Integer.MIN_VALUE)
				return Float.NaN;
			return i.floatValue();
		}
		if (r instanceof Number)
			return ((Number) r).floatValue();
		return Float.NaN;
	}

	@Override
	public Color apply(Integer recordID, Integer dimensionID) {
		if (!MyPreferences.isUseNumericalColorMapping())
			return super.apply(recordID, dimensionID);
		if (isInvalid(recordID) || isInvalid(dimensionID))
			return Color.NOT_A_NUMBER_COLOR;
		float vs = getNormalized(dimensionID, recordID);
		return Constants.colorMapping(vs);
	}

	/**
	 * @param t2
	 * @return
	 */
	private Pair<TypedGroupSet, TypedGroupSet> extractGroups(TablePerspective t) {
		Set<Integer> invalidDim = new BitSetSet();
		Set<Integer> invalidRec = new BitSetSet();
		TypedSet d = TypedSet.of(t.getDimensionPerspective().getVirtualArray());
		TypedSet r = TypedSet.of(t.getRecordPerspective().getVirtualArray());
		for (Integer dim : d) {
			for(Integer rec : r) {
				float v = getNormalized(dim, rec);
				if (Float.isInfinite(v) || Float.isNaN(v)) {
					invalidRec.add(rec);
					invalidDim.add(dim);
				}
			}
		}

		return Pair.make(resolve(invalidDim, d), resolve(invalidRec, r));
	}

	private TypedGroupSet resolve(Set<Integer> invalid, TypedSet d) {
		if (invalid.isEmpty())
			return TypedGroupSet.createUngrouped(d);

		TypedSetGroup normal = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(Sets.difference(d, invalid)),
				d.getIdType()), "Normal", getDataDomain().getColor());
		TypedSetGroup invalidG = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(invalid), d.getIdType()), "NaN",
				Color.NOT_A_NUMBER_COLOR);
		return new TypedGroupSet(normal, invalidG);
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim.select(dimGroups, recGroups);
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return Collections.singleton("heatmap");
	}

	public double getMin() {
		NumericalTable t = (NumericalTable) getDataDomain().getTable();
		return t.getMin();
	}

	public double getMax() {
		NumericalTable t = (NumericalTable) getDataDomain().getTable();
		return t.getMax();
	}

}
