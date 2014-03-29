/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.AGLButton;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.RulerDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class DragRulerButton extends AGLButton implements IDragGLSource {

	private final SelectionManager manager;
	private boolean dragMode = true;

	public DragRulerButton(SelectionManager manager) {
		this.manager =manager;
		setRenderer(GLRenderers.fillImage(Resources.ICON_RULER));
		setTooltip("Show / Hide Ruler for " + getLabel());
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);

		if (hovered)
			hoverEffect.render(g, w, h, this);
		if (armed)
			armedEffect.render(g, w, h, this);
		if (!dragMode) {
			g.color(1, 1, 1, 0.25f).fillRoundedRect(0, 0, w, h, Math.min(w, h) * 0.25f);
		}
	}

	private String getLabel() {
		return getIDCategory().getCategoryName();
	}

	public IDCategory getIDCategory() {
		return manager.getIDType().getIDCategory();
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		return new RulerDragInfo(event.getMousePos(), new Ruler(manager, findParent(Domino.class).getUndo()));
	}

	@Override
	protected void onMouseOver(Pick pick) {
		if (dragMode)
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
	public void onDropped(IDnDItem info) {
		if (info.getType() != EDnDType.NONE)
			setDragMode(false);
	}

	@Override
	protected void onMouseReleased(Pick pick) {
		if (dragMode)
			return;
		findParent(Domino.class).scrollRulerIntoView(getIDCategory());
	}

	/**
	 * @param b
	 */
	public void setDragMode(boolean enabled) {
		if (dragMode == enabled)
			return;
		this.dragMode = enabled;
		if (!enabled && context != null)
			context.getMouseLayer().removeDragSource(this);
	}

	/**
	 * @return the manager, see {@link #manager}
	 */
	public SelectionManager getManager() {
		return manager;
	}

	@Override
	public GLElement createUI(IDragInfo info) {
		if (info instanceof ADragInfo)
			return ((ADragInfo) info).createUI(findParent(Domino.class));
		return null;
	}
}
