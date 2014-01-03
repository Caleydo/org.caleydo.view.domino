/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.ui;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.AGLButton;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.picking.Pick;

import v2.Node;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ADragButton extends AGLButton implements IDragGLSource {
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);

		if (hovered)
			hoverEffect.render(g, w, h, this);
		if (armed)
			armedEffect.render(g, w, h, this);
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
	public IDragInfo startSWTDrag(IDragEvent event) {
		return new v2.NodeDragInfo(event.getMousePos(), createNode());
	}

	/**
	 * @return
	 */
	protected abstract Node createNode();

	@Override
	public void onDropped(IDnDItem info) {

	}

	@Override
	public GLElement createUI(IDragInfo info) {
		return null;
	}
}
