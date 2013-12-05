/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.Nodes;
import org.caleydo.view.domino.internal.ui.prototype.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;

/**
 * @author Samuel Gratzl
 *
 */
public class PlaceholderNodeElement extends ANodeElement implements IDropGLTarget {
	public PlaceholderNodeElement(PlaceholderNode node) {
		super(node);
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDropTarget(this);
		super.takeDown();
	}

	@Override
	protected void onMainPick(Pick pick) {
		super.onMainPick(pick);
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDropTarget(this);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDropTarget(this);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		boolean b = Nodes.canExtract(item);
		return b;
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}

	@Override
	public void onDrop(IDnDItem item) {
		INode n = Nodes.extract(item);
		DominoGraph graph = findGraph();
		if (!graph.contains(n)) {
			graph.addVertex(n);
		}
		graph.move(n, (PlaceholderNode) this.node);
		EventPublisher.trigger(new HidePlaceHoldersEvent().to(findParent(DominoNodeLayer.class)));
	}

	@Override
	public void onItemChanged(IDnDItem item) {

	}

	@Override
	public Vec2f getMinSize() {
		Vec2f s = getNodeSize();
		s.add(new Vec2f(BORDER * 2, BORDER * 2));
		return s;
	}
}
