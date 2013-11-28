/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.view.domino.internal.ui.prototype.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoNodeLayer extends GLElementContainer implements GraphListener<INode, IEdge> {

	/**
	 * @param graph
	 * @param graphElement
	 */
	public DominoNodeLayer(IGLLayout2 layout, DominoGraph graph) {
		super(layout);
		graph.addGraphListener(this);
		for (INode node : graph.vertexSet()) {
			add(node);
		}
	}

	@Override
	public void vertexAdded(GraphVertexChangeEvent<INode> e) {
		add(e.getVertex());
	}

	@Override
	public void vertexRemoved(GraphVertexChangeEvent<INode> e) {
		remove(e.getVertex());
	}

	@Override
	public void edgeAdded(GraphEdgeChangeEvent<INode, IEdge> e) {
		relayout();
	}

	@Override
	public void edgeRemoved(GraphEdgeChangeEvent<INode, IEdge> e) {
		relayout();
	}

	/**
	 * @param vertex
	 */
	private void remove(INode vertex) {
		for (NodeElement elem : Iterables.filter(this, NodeElement.class)) {
			if (elem.getNode() == vertex) {
				remove(elem);
				break;
			}
		}
	}

	/**
	 * @param node
	 */
	private void add(INode node) {
		this.add(new NodeElement(node));
	}

	/**
	 * @return
	 */
	public Iterable<NodeElement> getNodes() {
		return Iterables.filter(this, NodeElement.class);
	}

}
