/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.Function2;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.internal.util.Utils;

import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationDataValue implements IDataValues, Function2<Integer, Integer, Color> {
	private final String label;
	private final EDimension main;

	private final TypedGroupSet singleGroup;
	private final TypedGroupSet groups;


	public StratificationDataValue(Perspective data, EDimension dim, Integer referenceId) {
		this.main = dim;
		this.label = data.getLabel();
		this.singleGroup = new TypedGroupSet(TypedGroupList.createUngroupedGroup(new TypedSet(Collections
				.singleton(TypedCollections.INVALID_ID), TypedCollections.INVALID_IDTYPE)));
		this.groups = new TypedGroupSet(Utils.extractSetGroups(data, referenceId, dim));
	}

	public StratificationDataValue(String label, TypedSet data, EDimension main) {
		this.main = main;
		this.label = label;
		this.singleGroup = new TypedGroupSet(TypedGroupList.createUngroupedGroup(new TypedSet(Collections
				.singleton(TypedCollections.INVALID_ID), TypedCollections.INVALID_IDTYPE)));
		this.groups = new TypedGroupSet(TypedGroupList.createUngroupedGroup(data));
	}


	@Override
	public String getExtensionID() {
		return "stratification";
	}

	@Override
	public void fill(Builder b, TypedListGroup dimData, TypedListGroup recData) {
		final EDimension dimension = main;
		b.put(EDimension.class, dimension);
		final TypedList data = dimension.select(dimData, recData);
		final List<Integer> op = ImmutableList.of(0);
		String prim = dimension.select("dimensions", "records");
		String sec = dimension.select("records", "dimensions");
		b.put("heatmap." + prim, data);
		b.put("heatmap." + prim + ".idType", groups.getIdType());
		b.put("heatmap." + sec, op);
		b.put("heatmap." + sec + ".idType", TypedCollections.INVALID_IDTYPE);
		b.put(Function2.class, this);
	}

	@Override
	public Color apply(Integer record, Integer dimension) {
		Integer id = main.select(dimension, record);
		for (TypedSetGroup g : groups.getGroups()) {
			if (g.contains(id))
				return g.getColor();
		}
		return Color.NOT_A_NUMBER_COLOR;
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim == main ? groups : singleGroup;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		return a - b;
	}

}
