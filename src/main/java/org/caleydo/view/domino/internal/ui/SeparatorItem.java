/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ScaleLogic;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.ItemDragInfo;
import org.caleydo.view.domino.internal.toolbar.ItemTools;
import org.caleydo.view.domino.internal.undo.ZoomItemCmd;

/**
 * @author Samuel Gratzl
 *
 */
public class SeparatorItem extends AItem {

	public SeparatorItem(UndoStack undo) {
		super(undo);
		if (dim.isHorizontal())
			setSize(200, 20);
		else
			setSize(20, 200);
	}


	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		renderBaseAxis(g, w, h);
		super.renderImpl(g, w, h);
	}


	private void renderBaseAxis(GLGraphics g, float w, float h) {
		g.color(Color.LIGHT_GRAY);
		g.lineWidth(hovered ? 4 : 2);
		if (dim.isHorizontal()) {
			g.drawLine(0, h * 0.5f, w, h * 0.5f);
		} else {
			g.drawLine(w * 0.5f, 0, w * 0.5f, h);
		}
		g.lineWidth(1);
	}
}
