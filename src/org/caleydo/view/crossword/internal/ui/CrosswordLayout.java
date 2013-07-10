/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
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
		GLLayouts.flowHorizontal(2).doLayout(children, w, h);

		List<LayoutHelper> elems = new ArrayList<>(Collections2.transform(children, toLayoutHelper));

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
	}
}
