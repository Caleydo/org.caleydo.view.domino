/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.Nodes;
import org.caleydo.view.domino.internal.ui.prototype.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.internal.ui.prototype.event.ShowPlaceHoldersEvent;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoBackgroundLayer extends PickableGLElement implements IDropGLTarget {

	private final DominoNodeLayer nodes;
	private final DominoGraph graph;

	/**
	 * @param nodes
	 * @param graph
	 */
	public DominoBackgroundLayer(DominoNodeLayer nodes, DominoGraph graph) {
		this.nodes = nodes;
		this.graph = graph;
		setzDelta(-0.1f);
	}

	@Override
	protected void onMouseWheel(Pick pick) {
		for (ANodeElement elem : nodes.getNodes())
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
	public void onItemChanged(IDnDItem item) {

	}

	@Override
	public void onDrop(IDnDItem item) {
		Vec2f pos = toRelative(item.getMousePos());
		INode n = Nodes.extract(item);
		if (!graph.contains(n)) {
			n.setLayoutData(pos);
			graph.addVertex(n);
		} else {
			ANodeElement elem = nodes.apply(n);
			if (elem != null)
				elem.setLocation(pos.x(), pos.y());
			graph.moveToAlone(n);
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
		if (r && !item.isInternal())
			EventPublisher.trigger(new ShowPlaceHoldersEvent(Nodes.extract(item)).to(this));
		return r;
	}

	@ListenTo(sendToMe = true)
	private void onShowPlaceHoldersEvent(ShowPlaceHoldersEvent event) {
		if (graph.hasPlaceholders()) // already there
			return;
		EventPublisher.trigger(new ShowPlaceHoldersEvent(event.getNode()).to(nodes));
	}
}
