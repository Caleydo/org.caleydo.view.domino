/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.DominoGraph;
import org.caleydo.view.domino.api.model.graph.Nodes;
import org.caleydo.view.domino.internal.dnd.ANodeDragInfo;
import org.caleydo.view.domino.internal.dnd.GraphDragInfo;
import org.caleydo.view.domino.internal.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.internal.event.ShowPlaceHoldersEvent;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoBackgroundLayer extends PickableGLElement implements IDropGLTarget {

	protected final DominoNodeLayer nodes;
	protected final DominoGraph graph;

	/**
	 * @param nodes
	 * @param graph
	 */
	public DominoBackgroundLayer(DominoNodeLayer nodes, DominoGraph graph) {
		this.nodes = nodes;
		this.graph = graph;
	}

	@Override
	protected void onMouseWheel(Pick pick) {
		for (INodeElement elem : nodes.getNodes())
			elem.pick(pick);
	}

	@Override
	protected void onMouseOver(Pick pick) {
		context.getMouseLayer().addDropTarget(this);
	}

	@Override
	protected void onMouseOut(Pick pick) {
		context.getMouseLayer().removeDropTarget(this);
	}

	@Override
	protected void onMouseReleased(Pick pick) {
		nodes.select(null, SelectionType.SELECTION, false, true);
	}

	@Override
	public void onItemChanged(IDnDItem item) {

	}


	@Override
	public void onDrop(IDnDItem item) {
		Vec2f pos = toRelative(item.getMousePos());
		INode n = Nodes.extractPrimary(item);
		if (item.getInfo() instanceof ANodeDragInfo) {
			pos.sub(((ANodeDragInfo) item.getInfo()).getOffset());
		}
		n.setLayoutData(pos);
		if (!graph.contains(n)) {
			graph.addVertex(n);
		} else {
			graph.moveToAlone(n);
		}
		if (item.getInfo() instanceof GraphDragInfo) {
			((GraphDragInfo) item.getInfo()).dragGraph(graph, n, item.getType());
		}
		EventPublisher.trigger(new HidePlaceHoldersEvent().to(nodes));
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		boolean r = Nodes.canExtract(item);
		if (r)
			EventPublisher.trigger(new ShowPlaceHoldersEvent(item).to(this));
		return r;
	}

	@ListenTo(sendToMe = true)
	private void onShowPlaceHoldersEvent(ShowPlaceHoldersEvent event) {
		if (graph.hasPlaceholders()) // already there
			return;
		EventPublisher.trigger(new ShowPlaceHoldersEvent(event).to(nodes));
	}
}
