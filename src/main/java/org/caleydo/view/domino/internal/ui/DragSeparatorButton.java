/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.AGLButton;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.SeparatorDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class DragSeparatorButton extends AGLButton implements IDragGLSource {

	public DragSeparatorButton() {
		setRenderer(GLRenderers.fillImage(Resources.ICON_SEPARATOR));
		setTooltip("Add Separator Line");
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);

		if (hovered)
			hoverEffect.render(g, w, h, this);
		if (armed)
			armedEffect.render(g, w, h, this);
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		return new SeparatorDragInfo(event.getMousePos(), new Separator(findParent(Domino.class).getUndo()));
	}

	@Override
	protected void onMouseOver(Pick pick) {
		context.getMouseLayer().addDragSource(this);
		super.onMouseOver(pick);
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDragSource(this);
		super.takeDown();
	}

	@Override
	protected void onMouseOut(Pick pick) {
		context.getMouseLayer().removeDragSource(this);
		super.onMouseOut(pick);
	}

	@Override
	public GLElement createUI(IDragInfo info) {
		if (info instanceof ADragInfo)
			return ((ADragInfo) info).createUI(findParent(Domino.class));
		return null;
	}

	@Override
	public void onDropped(IDnDItem info) {

	}
}
