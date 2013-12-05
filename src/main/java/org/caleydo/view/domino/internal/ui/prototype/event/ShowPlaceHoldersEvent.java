/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.event;

import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class ShowPlaceHoldersEvent extends ADirectedEvent {

	private final INode node;

	/**
	 * @param node
	 */
	public ShowPlaceHoldersEvent(INode node) {
		this.node = node;

	}

	/**
	 * @return the node, see {@link #node}
	 */
	public INode getNode() {
		return node;
	}

}
