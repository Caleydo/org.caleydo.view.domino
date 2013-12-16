/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.renderer.Borders;
import org.caleydo.core.view.opengl.layout2.renderer.Borders.IBorderGLRenderer;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.internal.event.ShowPlaceHoldersEvent;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeElement extends ANodeElement {
	private final IBorderGLRenderer border;

	public NodeElement(INode node) {
		super(node);
		this.border = Borders.createBorder(Color.BLACK);
		this.add(new GLElement(border));
	}

	private final IDragGLSource source = new IDragGLSource() {

		@Override
		public IDragInfo startSWTDrag(IDragEvent event) {
			EventPublisher.trigger(new ShowPlaceHoldersEvent(node).to(findParent(DominoNodeLayer.class)));
			return new NodeDragInfo(node, event.getMousePos());
		}

		@Override
		public void onDropped(IDnDItem info) {
			EventPublisher.trigger(new HidePlaceHoldersEvent().to(findParent(DominoNodeLayer.class)));
		}

		@Override
		public GLElement createUI(IDragInfo info) {
			NodeDragInfo d = ((NodeDragInfo) info);
			GLElement e = d.createUI();
			return e;
		}
	};

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDragSource(source);
		super.takeDown();
	}


	@Override
	protected void onMainPick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDragSource(source);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(source);
			break;
		case MOUSE_RELEASED:
			node.getUIState().setSelected(!node.getUIState().isSelected());
			if (!node.getUIState().isSelected())
				border.setColor(SelectionType.MOUSE_OVER.getColor());
			else
				border.setColor(SelectionType.SELECTION.getColor());
			break;
		default:
			break;
		}
	}

	@Override
	public void pick(Pick pick) {
		super.pick(pick);
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!node.getUIState().isSelected())
				border.setColor(SelectionType.MOUSE_OVER.getColor());
			get(1).repaint();
			node.getUIState().setHovered(true);
			break;
		case MOUSE_OUT:
			if (!node.getUIState().isSelected())
				border.setColor(Color.BLACK);
			node.getUIState().setHovered(false);
			get(1).repaint();
			break;
		default:
			break;
		}
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// g.color(Color.BLACK).drawRect(0, 0, w, h);
		super.renderImpl(g, w, h);
		g.incZ();
		g.incZ();
		float wi = Math.max(100, w);
		float x = (w - wi) * 0.5f;
		g.drawText(node.getLabel(), x, (h - 10) * .5f, wi, 10, VAlign.CENTER);
		g.decZ();
		g.decZ();
	}


	@Override
	public Vec2f getMinSize() {
		Vec2f s = getNodeSize();
		s.add(new Vec2f(BORDER * 2, BORDER * 2));
		return s;
	}
}
