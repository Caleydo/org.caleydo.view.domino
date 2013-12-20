/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin.ISelectionMixinCallback;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
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
import org.caleydo.view.domino.internal.ui.model.DominoGraph;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeElement extends ANodeElement implements ISelectionMixinCallback {
	private final IBorderGLRenderer border;

	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

	public NodeElement(INode node) {
		super(node);
		this.border = Borders.createBorder(Color.BLACK);
		this.add(new GLElement(border));
		selections.add(DominoGraph.newNodeSelectionManager());
	}

	private final IDragGLSource source = new IDragGLSource() {

		@Override
		public IDragInfo startSWTDrag(IDragEvent event) {
			// EventPublisher.trigger(new ShowPlaceHoldersEvent(node).to(findParent(DominoNodeLayer.class)));
			return new NodeDragInfo(node, event.getMousePos());
		}

		@Override
		public void onDropped(IDnDItem info) {
			EventPublisher.trigger(new HidePlaceHoldersEvent().to(findParent(DominoNodeLayer.class)));
		}

		@Override
		public GLElement createUI(IDragInfo info) {
			NodeDragInfo d = ((NodeDragInfo) info);
			Vec2f mousePos = d.getMousePos();
			Vec2f loc = getAbsoluteLocation();
			Vec2f offset = mousePos.minus(loc);
			d.setOffset(offset);
			return d.createUI();
		}
	};

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDragSource(source);
		super.takeDown();
	}

	protected boolean isContentPickable() {
		return content.getVisibility() == EVisibility.PICKABLE;
	}


	@Override
	protected void onMainPick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!isContentPickable())
				context.getMouseLayer().addDragSource(source);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(source);
			break;
		case MOUSE_RELEASED:
			select(SelectionType.SELECTION, !isSelected(SelectionType.SELECTION), !((IMouseEvent) pick).isCtrlDown());
			break;
		case RIGHT_CLICKED:
			findGraph().remove(node);
			break;
		case DOUBLE_CLICKED:
			// graph.remove(node);
			content.setVisibility(content.getVisibility() == EVisibility.PICKABLE ? EVisibility.VISIBLE
					: EVisibility.PICKABLE);
			System.out.println(content.getVisibility());
			if (isContentPickable()) {
				border.setColor(Color.RED);
				context.getMouseLayer().removeDragSource(source);
			} else {
				border.setColor(Color.BLUE);
				context.getMouseLayer().addDragSource(source);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		SelectionType s = manager.getHighestSelectionType(node.getID());
		border.setColor(s == null ? Color.BLACK : s.getColor());
		get(1).repaint();
	}

	@Override
	public void pick(Pick pick) {
		super.pick(pick);
		IMouseEvent event = ((IMouseEvent) pick);
		DominoGraph graph = findGraph();
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			select(SelectionType.MOUSE_OVER, true, true);
			break;
		case MOUSE_OUT:
			select(SelectionType.MOUSE_OVER, false, true);
			break;
		default:
			break;
		}
	}

	public boolean isSelected(SelectionType type) {
		return selections.get(0).checkStatus(type, node.getID());
	}

	/**
	 * @param mouseOver
	 * @param b
	 */
	private void select(SelectionType type, boolean enable, boolean clear) {
		SelectionManager m = selections.get(0);
		if (clear)
			m.clearSelection(type);
		if (enable)
			m.addToType(type, node.getID());
		else
			m.removeFromType(type, node.getID());
		selections.fireSelectionDelta(m);
		onSelectionUpdate(m);
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
