/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.List;

import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

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


}
