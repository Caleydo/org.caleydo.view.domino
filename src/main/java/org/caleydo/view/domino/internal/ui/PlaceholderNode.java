/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.domino.api.model.graph.ADataNode;
import org.caleydo.view.domino.api.model.graph.ANode;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class PlaceholderNode extends ADataNode {

	public PlaceholderNode(INode node, boolean transposed) {
		super("", node.getData(EDimension.DIMENSION), node.getData(EDimension.RECORD));
		this.transposed = transposed;
		setLayoutData(node.getLayoutDataAs(Object.class, null));
	}

	public PlaceholderNode(PlaceholderNode clone) {
		super(clone);
	}

	@Override
	public INodeUI createUI() {
		return new UI(this);
	}

	@Override
	public ANode clone() {
		return new PlaceholderNode(this);
	}

	static class UI extends GLElement implements INodeUI {
		private final PlaceholderNode node;
		private TypedGroupList dimData = TypedCollections.INVALID_GROUP_LIST;
		private TypedGroupList recData = TypedCollections.INVALID_GROUP_LIST;

		private boolean armed = false;

		public UI(PlaceholderNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		public INode asNode() {
			return node;
		}

		/**
		 * @param armed
		 *            setter, see {@link armed}
		 */
		public void setArmed(boolean armed) {
			if (this.armed == armed)
				return;
			this.armed = armed;
			repaint();
		}

		@Override
		public GLElement getToolBar() {
			return null;
		}

		@Override
		public GLElement asGLElement() {
			return this;
		}

		@Override
		public boolean setData(EDimension dim, TypedGroupList data) {
			int old = dim.select(dimData, recData).size();
			if (dim.isHorizontal())
				dimData = data;
			else
				recData = data;
			return old != data.size();
		}

		@Override
		public double getSize(EDimension dim) {
			TypedList l = dim.select(dimData, recData);
			if (!l.isEmpty())
				return l.size();
			return 80;
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(Color.DARK_BLUE).fillRect(0, 0, w, h);
			g.gl.glEnable(GL2.GL_LINE_STIPPLE);
			g.gl.glLineStipple(2, (short) 0xAAAA);
			g.lineWidth(2);
			g.color(armed ? 0.80f : 0.95f).fillRoundedRect(0, 0, w, h, 5);
			g.color(Color.GRAY).drawRoundedRect(0, 0, w, h, 5);
			g.gl.glDisable(GL2.GL_LINE_STIPPLE);
			g.lineWidth(1);
			super.renderImpl(g, w, h);
		}
	}
}
