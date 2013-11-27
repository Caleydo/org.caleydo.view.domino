/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorBrewer;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationNode extends ANode {
	private final Perspective data;
	private final TypedSet ids;
	private EDimension dim;

	public StratificationNode(Perspective data, EDimension dim) {
		this.data = data;
		this.ids = TypedSet.of(data.getVirtualArray());
		this.dim = dim;
	}

	@Override
	public void transpose() {
		this.dim = dim.opposite();
		propertySupport.firePropertyChange(PROP_TRANSPOSE, !this.dim.isVertical(), this.dim.isVertical());
	}

	/**
	 * @return the data, see {@link #data}
	 */
	public Perspective getData() {
		return data;
	}

	public int size() {
		return data.getVirtualArray().size();
	}

	public GroupList getGroups() {
		return getData().getVirtualArray().getGroupList();
	}

	/**
	 * @return the ids, see {@link #ids}
	 */
	@Override
	public TypedSet getData(EDimension dim) {
		return dim == this.dim ? ids : TypedSet.INVALID;
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends GLElement {
		private final StratificationNode node;

		public UI(StratificationNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			GroupList groups = node.getGroups();
			List<Color> colors = ColorBrewer.Set2.getColors(groups.size());
			final int total = node.size();
			if (node.dim.isHorizontal()) {
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
			super.renderImpl(g, w, h);
		}
	}
}
