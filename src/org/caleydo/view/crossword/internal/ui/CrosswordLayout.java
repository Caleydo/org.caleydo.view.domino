/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

/**
 * layout implementation
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordLayout implements IGLLayout {

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		float acc = 10;
		for(IGLLayoutElement elem : children) {
			CrosswordLayoutInfo helper = elem.getLayoutDataAs(CrosswordLayoutInfo.class, null);
			assert helper != null;
			Vec2f loc = helper.getLocation(elem);
			loc.setX(Float.isNaN(loc.x()) ? acc : loc.x());
			loc.setY(Float.isNaN(loc.y()) ? acc : loc.y());
			Vec2f msize = helper.getMinSize(elem);
			helper.scale(msize);
			helper.setBounds(elem, loc,msize);
			acc += msize.x() + 10;
		}

		// FIXME
	}

	public static void doElementLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		IGLLayoutElement content = children.get(0);
		IGLLayoutElement buttons = children.get(1);
		IGLLayoutElement border = children.get(2);
		content.setBounds(1, 1, w - 2, h - 16 - 2);
		buttons.setBounds(1, h - 16 - 2, w - 2, 16);
		border.setBounds(0, 0, w, h);
	}
}
