/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;

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
	public void fill(Builder b, TypedList dimData, TypedList recData) {
		super.fill(b, dimData, recData);

		TypedList data = main.select(dimData, recData);
		boolean transposed = data.getIdType() == this.singleGroup.getIdType();
		if (transposed) {
			data = main.opposite().select(dimData, recData);
		}

		final Function<Integer, String> toString = new Function<Integer, String>() {
			@Override
			public String apply(Integer input) {
				return Objects.toString(getRaw(input.intValue()), "");
			}
		};
		b.put("id2string", toString);
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
