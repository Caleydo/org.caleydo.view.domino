/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.domino.internal.ui.prototype.ADataNode;
import org.caleydo.view.domino.internal.ui.prototype.ANode;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class PlaceholderNode extends ADataNode {

	public PlaceholderNode(INode node, boolean transposed) {
		super(node.getData(EDimension.DIMENSION), node.getData(EDimension.RECORD));
		this.transposed = transposed;
	}

	public PlaceholderNode(PlaceholderNode clone) {
		super(clone);
	}

	@Override
	public String getLabel() {
		return "";
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	@Override
	public ANode clone() {
		return new PlaceholderNode(this);
	}

	private static class UI extends GLElement {
		private final PlaceholderNode node;

		public UI(PlaceholderNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.gl.glEnable(GL2.GL_LINE_STIPPLE);
			g.gl.glLineStipple(2, (short) 0xAAAA);
			g.lineWidth(2);
			g.color(0.95f).fillRoundedRect(0, 0, w, h, 5);
			g.color(Color.GRAY).drawRoundedRect(0, 0, w, h, 5);
			g.gl.glDisable(GL2.GL_LINE_STIPPLE);
			g.lineWidth(1);
			super.renderImpl(g, w, h);
		}
	}
}
