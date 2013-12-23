/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorBrewer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;

import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class Utils {
	public static void renderCategorical(GLGraphics g, float w, float h, GroupList groups, final int total,
			boolean horizontal, List<Color> colors) {
		if (horizontal) {
			float di = w / total;
			float x = 0;
			for (int i = 0; i < groups.size(); ++i) {
				Group group = groups.get(i);
				float wi = di * group.getSize();
				g.color(colors.get(i)).fillRect(x, 0, wi, h);
				x += wi;
			}
		} else {
			float di = h / total;
			float y = 0;
			for (int i = 0; i < groups.size(); ++i) {
				Group group = groups.get(i);
				float hi = di * group.getSize();
				g.color(colors.get(i)).fillRect(0, y, w, hi);
				y += hi;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static List<CategoryProperty<?>> resolveCategories(Integer singleID, ATableBasedDataDomain dataDomain,
			EDimension dim) {
		final Table table = dataDomain.getTable();

		Object spec = table.getDataClassSpecificDescription(dim.select(singleID.intValue(), 0),
				dim.select(0, singleID.intValue()));
		if (spec instanceof CategoricalClassDescription<?>) {
			List<?> tmp = ((CategoricalClassDescription<?>) spec).getCategoryProperties();
			return ImmutableList.copyOf((List<CategoryProperty<?>>) tmp);
		}
		return Collections.emptyList();
	}

	/**
	 * @param referenceId
	 * @param virtualArray
	 * @return
	 */
	public static List<TypedListGroup> extractGroups(Perspective p, Integer referenceId, EDimension mainDim) {
		VirtualArray va = p.getVirtualArray();
		GroupList groups = va.getGroupList();
		List<Color> colors = getGroupColors(referenceId, (ATableBasedDataDomain) p.getDataDomain(), groups, mainDim);
		List<TypedListGroup> r = new ArrayList<>();
		for (int i = 0; i < groups.size(); ++i) {
			r.add(new TypedListGroup(ImmutableList.copyOf(va.getIDsOfGroup(i)), va.getIdType(), groups
					.get(i)
					.getLabel(), colors.get(i)));
		}
		return ImmutableList.copyOf(r);
	}

	private static List<Color> getGroupColors(Integer referenceId, ATableBasedDataDomain dataDomain, GroupList groups,
			EDimension mainDim) {
		if (referenceId == null) {
			return ColorBrewer.Set2.getColors(groups.size());
		}
		// lookup the colors from the properties
		List<CategoryProperty<?>> categories = Utils.resolveCategories(referenceId, dataDomain, mainDim.opposite());
		List<Color> colors = new ArrayList<>(groups.size());
		for (Group group : groups) {
			String label = group.getLabel();
			CategoryProperty<?> prop = findProp(label, categories);
			colors.add(prop == null ? Color.NEUTRAL_GREY : prop.getColor());
		}
		return colors;
	}

	/**
	 * @param label
	 * @return
	 */
	private static CategoryProperty<?> findProp(String label, List<CategoryProperty<?>> categories) {
		for (CategoryProperty<?> prop : categories) {
			if (prop.getCategoryName().equals(label))
				return prop;
		}
		return null;
	}

}
