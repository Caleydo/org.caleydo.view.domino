/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

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
	static List<CategoryProperty<?>> resolveCategories(Integer singleID, ATableBasedDataDomain dataDomain,
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

}
