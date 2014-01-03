/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

import v2.Node;

/**
 * @author Samuel Gratzl
 *
 */
public class DragElement extends GLElement {

	private final String label;

	/**
	 * @param label
	 */
	public DragElement(String label, Vec2f size) {
		this.label = label;
		size = Node.initialSize(size.x(), size.y());
		setSize(size.x(), size.y());
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		float ri = Math.min(5, Math.min(w, h) * 0.45f);
		g.color(1, 1, 1, 0.75f).fillRoundedRect(0, 0, w, h, ri);
		g.color(Color.BLACK).drawRoundedRect(0, 0, w, h, ri);
		float hi = Math.min(h, 12);
		g.drawText(label, -100, (h - hi) * 0.5f, w + 200, hi, VAlign.CENTER);
		super.renderImpl(g, w, h);
	}

}
