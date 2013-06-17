/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
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
