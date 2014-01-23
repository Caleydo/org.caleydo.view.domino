/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Collection;
import java.util.Collections;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.graph.EProximityMode;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class LabelDataValues implements IDataValues {
	private final IDCategory category;
	private final TypedGroupSet data;

	public LabelDataValues(IDCategory category) {
		this.category = category;
		data = TypedGroupSet.createUngrouped(TypedCollections.empty(category.getPrimaryMappingType()));
	}

	@Override
	public String getLabel() {
		return "Labels of " + category.getCategoryName();
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return data;
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		return 0;
	}

	@Override
	public String getExtensionID() {
		return "label";
	}

	@Override
	public void fill(Builder b, TypedListGroup dimData, TypedListGroup recData) {
		TypedListGroup g = dimData.isEmpty() ? recData : dimData;
		EDimension dim = EDimension.get(g == dimData);
		b.put(TypedListGroup.class, g);
		b.put(EDimension.class, dim);
	}

	@Override
	public Collection<String> getDefaultVisualization(EProximityMode mode) {
		return Collections.singleton("labels");
	}

	@Override
	public Color getColor() {
		return Color.LIGHT_GRAY;
	}

	@Override
	public boolean apply(String input) {
		return true;
	}

	@Override
	public void onSelectionChanged(boolean selected) {

	}
}
