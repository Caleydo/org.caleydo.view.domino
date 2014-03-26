/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.rnb.api.model.EDirection;
import org.caleydo.view.rnb.api.model.typed.TypedGroupSet;
import org.caleydo.view.rnb.api.model.typed.TypedList;
import org.caleydo.view.rnb.api.model.typed.TypedSet;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class String1DDataDomainValues extends A1DDataDomainValues {
	private final TypedGroupSet groups;

	public String1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data, main);
		Perspective p = main.select(data.getDimensionPerspective(), data.getRecordPerspective());
		this.groups = TypedGroupSet.createUngrouped(TypedSet.of(p.getVirtualArray()));
	}

	@Override
	public String getExtensionID() {
		return "string.1d";
	}

	@Override
	protected TypedGroupSet getGroups() {
		return groups;
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return Collections.singleton("labels");
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData, boolean[] existNeigbhor) {
		super.fill(b, dimData, recData, existNeigbhor);
		EDimension dim = main;
		TypedList data = main.select(dimData, recData);
		boolean transposed = data.getIdType() == this.singleGroup.getIdType();
		if (transposed) {
			dim = dim.opposite();
		}
		VAlign align;
		boolean leftN = existNeigbhor[dim.select(EDirection.SOUTH, EDirection.WEST).ordinal()];
		boolean rightN = existNeigbhor[dim.select(EDirection.NORTH, EDirection.EAST).ordinal()];
		if (rightN && leftN)
			align = VAlign.CENTER;
		else if (rightN)
			align = VAlign.RIGHT;
		else
			align = VAlign.LEFT;
		b.put("align", align);
	}

	@Override
	protected void fill(Builder b, TypedList data, EDimension dim, boolean[] existNeigbhor) {
		super.fill(b, data, dim, existNeigbhor);
		final Function<Integer, String> toString = new Function<Integer, String>() {
			@Override
			public String apply(Integer input) {
				return Objects.toString(getRaw(input.intValue()), "");
			}
		};
		b.put("id2string", toString);
		b.set("boxHighlights");
	}

	@Override
	public boolean apply(String input) {
		return !"distribution.bar".equals(input);
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		if (dim != main)
			return 0;
		return Objects.compare(getRaw(a), getRaw(b), String.CASE_INSENSITIVE_ORDER);
	}

	@Override
	public String getRaw(int id) {
		Object r = super.getRaw(id);
		return r == null ? "" : r.toString();
	}
}
