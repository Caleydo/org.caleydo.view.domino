/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.internal.ui.prototype.event.ShowPlaceHoldersEvent;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;
import org.caleydo.view.domino.internal.ui.prototype.graph.Placeholder;
import org.caleydo.vis.lineup.ui.GLPropertyChangeListeners;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoNodeLayer extends GLElementContainer {
	private final PropertyChangeListener repaint = GLPropertyChangeListeners.repaintOnEvent(this);
	private final PropertyChangeListener relayout = GLPropertyChangeListeners.relayoutOnEvent(this);

	private final DominoGraph graph;
	/**
	 * @param graph
	 * @param graphElement
	 */
	public DominoNodeLayer(IGLLayout2 layout, DominoGraph graph) {
		super(layout);
		this.graph = graph;
		graph.addPropertyChangeListener(DominoGraph.PROP_EDGES, repaint);
		graph.addPropertyChangeListener(DominoGraph.PROP_TRANSPOSED, relayout);
		graph.addPropertyChangeListener(DominoGraph.PROP_VERTICES, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() != null)
					add((INode) evt.getNewValue());
				if (evt.getOldValue() != null)
					remove((INode) evt.getOldValue());
			}
		});
		for (INode node : graph.vertexSet()) {
			add(node);
		}
	}

	@ListenTo(sendToMe = true)
	private void onShowPlaceHoldersEvent(ShowPlaceHoldersEvent event) {
		Set<Placeholder> placeholders = graph.findPlaceholders(event.getNode());
		graph.insertPlaceholders(placeholders, event.getNode());
	}

	@ListenTo(sendToMe = true)
	private void onHidePlaceHoldersEvent(HidePlaceHoldersEvent event) {
		graph.removePlaceholders(ImmutableSet.copyOf(Iterables.filter(graph.vertexSet(), PlaceholderNode.class)));
	}

	/**
	 * @param vertex
	 */
	private void remove(INode vertex) {
		for (ANodeElement elem : Iterables.filter(this, ANodeElement.class)) {
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
		if (node instanceof PlaceholderNode)
			this.add(new PlaceholderNodeElement((PlaceholderNode) node));
		else
			this.add(new NodeElement(node));
	}

	/**
	 * @return
	 */
	public Iterable<ANodeElement> getNodes() {
		return Iterables.filter(this, ANodeElement.class);
	}

	/**
	 * @param n
	 * @return
	 */
	public ANodeElement find(INode n) {
		for (ANodeElement elem : getNodes())
			if (elem.getNode() == n)
				return elem;
		return null;
	}

}
