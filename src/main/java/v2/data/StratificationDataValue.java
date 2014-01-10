/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import java.util.Collections;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.Function2;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.internal.util.Utils;

import com.google.common.base.Function;

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
		b.put("heatmap.dimensions", dimData);
		b.put("heatmap.dimensions.idType", dimData.getIdType());
		b.put("heatmap.records", recData);
		b.put("heatmap.records.idType", recData.getIdType());
		final boolean swapped = dimData.getIdType() != getDefaultGroups(EDimension.DIMENSION).getIdType();
		if (swapped) { // swapped
			b.put(Function2.class, Functions2s.swap(this));
		} else
			b.put(Function2.class, this);

		final EDimension dir = swapped ? main.opposite() : main;
		b.put(EDimension.class, dir);
		b.put("axis.data", dir.select(dimData, recData));
		b.put("axis.f", new Function<Integer, Double>() {
			@Override
			public Double apply(Integer input) {
				return (double) indexOf(input);
			}
		});
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

	public int indexOf(Integer id) {
		int i = 0;
		for (TypedSetGroup g : groups.getGroups()) {
			if (g.contains(id))
				return i;
			i++;
		}
		return -1;
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
