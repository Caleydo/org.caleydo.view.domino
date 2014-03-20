/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Collection;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.io.NumericalProperties;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.DoubleStatistics;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.internal.Constants;

/**
 * @author Samuel Gratzl
 *
 */
public class Numerical1DDataDomainValues extends A1DDataDomainValues implements INumerical1DContainer {
	private final TypedGroupSet groups;
	private final boolean isInteger;
	private final Numerical1DMixin mixin;

	public Numerical1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data, main);
		Perspective p = main.select(data.getDimensionPerspective(), data.getRecordPerspective());
		this.groups = Numerical1DMixin.extractGroups(p, this);
		this.isInteger = DataSupportDefinitions.dataClass(EDataClass.NATURAL_NUMBER).apply(data);

		Object desc = getDescription();
		Float min = null, max = null;
		if (desc instanceof NumericalProperties) {
			min = ((NumericalProperties) desc).getMin();
			max = ((NumericalProperties) desc).getMax();
		}
		if (min == null || min.isNaN() || max == null || max.isNaN()) {
			DoubleStatistics stats = createStats(this, groups);
			if (min == null || min.isNaN())
				min = (float) stats.getMin();
			if (max == null || max.isNaN())
				max = (float) stats.getMax();
		}
		this.mixin = new Numerical1DMixin(this, groups, min.floatValue(), max.floatValue());
	}

	private static DoubleStatistics createStats(INumerical1DContainer c, TypedGroupSet groups) {
		DoubleStatistics.Builder b = DoubleStatistics.builder();
		for (Integer id : groups)
			b.add(c.getRaw(id));
		return b.build();
	}

	@Override
	public String getExtensionID() {
		return "numerical.1d";
	}

	@Override
	protected TypedGroupSet getGroups() {
		return groups;
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return mixin.getDefaultVisualization();
	}

	@Override
	public boolean apply(String input) {
		return super.apply(input) && !"distribution.pie".equals(input);
	}


	@Override
	protected void fill(Builder b, TypedList data, EDimension dim, boolean[] existNeigbhor) {
		super.fill(b, data, dim, existNeigbhor);
		mixin.fill(b, data, dim, existNeigbhor);
	}

	@Override
	public Color apply(Integer recordID, Integer dimensionID) {
		if (isInvalid(recordID) || isInvalid(dimensionID))
			return Color.NOT_A_NUMBER_COLOR;
		float vs = getNormalized(dimensionID, recordID);
		return Constants.colorMapping(vs);
	}

	@Override
	public Float getRaw(int id) {
		Object r = super.getRaw(id);
		if (r instanceof Float)
			return ((Float)r);
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
}
