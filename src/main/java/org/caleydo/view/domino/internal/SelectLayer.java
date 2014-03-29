/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;

/**
 * renders and handles a selection rectangle
 *
 * @author Samuel Gratzl
 *
 */
public class SelectLayer extends GLElement {

	private final Rect rect = new Rect();

	/**
	 * @param xy
	 */
	public SelectLayer(Vec2f xy) {
		this.rect.xy(xy);
	}


	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.lineStippled(4, 0xAAAA);
		Rect r = unifyRect();
		g.incZ(2);
		g.color(Color.GRAY).drawRect(r.x(), r.y(), r.width(), r.height());
		g.incZ(-2);
		g.lineStippled(false);
		super.renderImpl(g, w, h);
	}

	/**
	 * @return
	 */
	private Rect unifyRect() {
		float x = rect.x();
		float y = rect.y();
		float w = rect.width();
		float h = rect.height();
		if (w < 0) {
			x += w;
			w *= -1;
		}
		if (h < 0) {
			y += h;
			h *= -1;
		}
		return new Rect(x, y, w, h);
	}

	/**
	 * @param dx
	 * @param dy
	 */
	public void dragTo(float dx, float dy, boolean ctrlDown) {
		if (dx == 0 && dy == 0)
			return;
		rect.width(rect.width() + dx);
		rect.height(rect.height() + dy);
		repaint();
		updateSelection(ctrlDown);
	}

	/**
	 * @param ctrlDown
	 *
	 */
	private void updateSelection(boolean ctrlDown) {
		Domino domino = findParent(Domino.class);
		domino.selectByBounds(unifyRect(), !ctrlDown);
	}
}
