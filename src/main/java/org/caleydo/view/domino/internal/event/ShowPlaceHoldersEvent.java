/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.event;

import java.util.EnumSet;
import java.util.Set;

import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.graph.Nodes;
import org.caleydo.view.domino.internal.dnd.GraphDragInfo;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class ShowPlaceHoldersEvent extends ADirectedEvent {

	private final INode node;
	private final Set<EDirection> directions;

	/**
	 * @param item
	 */
	public ShowPlaceHoldersEvent(IDnDItem item) {
		this.node = Nodes.extractPrimary(item);
		if (item.getInfo() instanceof GraphDragInfo) {
			this.directions = ((GraphDragInfo)item.getInfo()).getFreePrimaryDirections();
		} else
			this.directions = EnumSet.allOf(EDirection.class);
	}

	/**
	 * @param event
	 */
	public ShowPlaceHoldersEvent(ShowPlaceHoldersEvent clone) {
		this.node = clone.node;
		this.directions = clone.directions;
	}

	/**
	 * @return the node, see {@link #node}
	 */
	public INode getNode() {
		return node;
	}

	/**
	 * @return the directions, see {@link #directions}
	 */
	public Set<EDirection> getDirections() {
		return directions;
	}

}
