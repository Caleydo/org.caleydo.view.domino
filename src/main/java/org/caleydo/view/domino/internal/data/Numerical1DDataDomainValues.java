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
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;

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

		this.mixin = new Numerical1DMixin(this, groups);
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
	protected void fill(Builder b, TypedList data) {
		super.fill(b, data);
		mixin.fill(b, data);
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
