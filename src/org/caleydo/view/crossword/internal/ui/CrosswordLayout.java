/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import static org.caleydo.core.view.opengl.layout2.layout.GLLayouts.defaultValue;
import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;

/**
 * layout implementation
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordLayout implements IGLLayout {

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		List<LayoutHelper> elems = new ArrayList<>(Collections2.transform(children, toLayoutHelper));

		float acc = 10;
		for (LayoutHelper helper : elems) {
			Vec2f loc = helper.getLocation();
			loc.setX(defaultValue(loc.x(), acc));
			loc.setY(defaultValue(loc.y(), 10));
			Vec2f msize = helper.getMinSize();
			msize.scale((float)helper.getZoomFactor());
			helper.setBounds(loc,msize);
			acc += msize.x() + 10;
		}

		// FIXME
	}

	private static final Function<IGLLayoutElement, LayoutHelper> toLayoutHelper = new Function<IGLLayoutElement, LayoutHelper>() {
		@Override
		public LayoutHelper apply(IGLLayoutElement in) {
			return new LayoutHelper(in);
		}
	};

	private static class LayoutHelper {
		private final IGLLayoutElement elem;
		private final CrosswordLayoutInfo info;

		public LayoutHelper(IGLLayoutElement elem) {
			this.elem = elem;
			this.info = Preconditions.checkNotNull(elem.getLayoutDataAs(CrosswordLayoutInfo.class, null));
		}

		Vec2f getLocation() {
			return elem.getSetLocation().copy();
		}

		Vec2f getMinSize() {
			IHasMinSize minSize = elem.getLayoutDataAs(IHasMinSize.class, null);
			if (minSize != null)
				return minSize.getMinSize();
			return elem.getLayoutDataAs(Vec2f.class, new Vec2f(100,100));
		}

		double getZoomFactor() {
			return info.getZoomFactor();
		}

		void setBounds(Vec2f loc, Vec2f size) {
			elem.setBounds(loc.x(), loc.y(), size.x(), size.y());
		}
	}
}
