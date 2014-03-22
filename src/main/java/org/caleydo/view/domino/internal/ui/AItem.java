/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
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
public class AItem extends GLElementContainer implements IDragGLSource, IPickingListener, IGLLayout2 {
	protected EDimension dim = EDimension.RECORD;
	protected boolean hovered = false;

	public AItem(UndoStack undo) {
		setLayout(this);

		setVisibility(EVisibility.PICKABLE);
		onPick(this);

		this.add(createToolBar(undo));
	}

	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	/**
	 * @return
	 */
	private GLElement createToolBar(UndoStack undo) {
		ItemTools tools = new ItemTools(undo, this);
		tools.setSize(tools.getWidth(24), 24);
		return tools;
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		final IGLLayoutElement toolbar = children.get(0);
		if (hovered) {
			float wi = toolbar.getSetWidth();
			if (wi > w)
				toolbar.setBounds((w - wi) * 0.5f, -24, wi, 24);
			else
				toolbar.setBounds(w - wi, -24, wi, 24);
		} else
			toolbar.hide();
		return false;
	}


	public void transpose() {
		Vec2f old = getSize();
		this.dim = this.dim.opposite();
		setSize(old.y(), old.x());
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_WHEEL:
			final Vec2f bak = getSize();
			Vec2f shift = ScaleLogic.shiftLogic(((IMouseEvent) pick), bak);
			float change = dim.select(shift);
			if (change == 0)
				return;
			UndoStack undo = findParent(Domino.class).getUndo();
			undo.push(new ZoomItemCmd(this, change));
			break;
		case MOUSE_OVER:
			context.getMouseLayer().addDragSource(this);
			hovered = true;
			relayout();
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
			hovered = false;
			relayout();
			break;
		case RIGHT_CLICKED:
			context.getSWTLayer().showContextMenu(((ItemTools) get(0)).asContextMenu());
			break;
		default:
			break;
		}
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDragSource(this);
		super.takeDown();
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		return new ItemDragInfo(event.getMousePos(), this);
	}

	@Override
	public void onDropped(IDnDItem info) {

	}

	@Override
	public GLElement createUI(IDragInfo info) {
		if (info instanceof ADragInfo) {
			return ((ADragInfo) info).createUI(findParent(Domino.class));
		}
		return null;
	}

	/**
	 * @param shift
	 */
	public void shiftLocation(Vec2f shift) {
		Vec2f loc = getLocation();
		setLocation(loc.x() + shift.x(), loc.y() + shift.y());
	}

	/**
	 * @param shift
	 */
	public void zoom(float change) {
		if (change == 0)
			return;
		float old = dim.select(getSize());
		float new_ = Math.max(old + change, 20);
		if (dim.isHorizontal())
			setSize(new_, 20);
		else
			setSize(20, new_);
		relayout();
	}
}
