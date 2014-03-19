/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class LabelDataValues implements IDataValues {
	private final IDCategory category;
	private final TypedGroupSet data;
	private final IIDTypeMapper<Integer, String> mapper;

	public LabelDataValues(IDCategory category) {
		this.category = category;
		data = TypedGroupSet.createUngrouped(TypedCollections.empty(category.getPrimaryMappingType()));

		this.mapper = IDMappingManagerRegistry.get().getIDMappingManager(category)
				.getIDTypeMapper(data.getIdType(), category.getHumanReadableIDType());

	}

	@Override
	public String getLabel() {
		return category.getCategoryName().toUpperCase();
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim.isHorizontal() ? data : TypedGroupSet.createUngrouped(TypedCollections.INVALID_SET);
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		String av = get(a);
		String bv = get(b);
		return Objects.compare(av, bv, String.CASE_INSENSITIVE_ORDER);
	}

	@Override
	public String getExtensionID() {
		return "label";
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData, boolean[] existNeigbhor) {
		TypedList g = TypedCollections.isInvalid(dimData.getIdType()) ? recData : dimData;
		EDimension dim = EDimension.get(g == dimData);
		b.put(TypedList.class, g);
		b.put(EDimension.class, dim);
		b.set("labels.boxHighlights");

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
	public Collection<String> getDefaultVisualization() {
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

	public String get(int id) {
		Set<String> r = mapper.apply(id);
		if (r == null)
			return "Unnamed";
		return StringUtils.join(r, ", ");
	}
}
