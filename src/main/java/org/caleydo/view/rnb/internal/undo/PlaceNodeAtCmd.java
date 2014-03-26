/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.api.model.EDirection;
import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.Node;

/**
 * @author Samuel Gratzl
 *
 */
public class PlaceNodeAtCmd implements ICmd {

	private Node node;
	private Node neighbor;
	private EDirection dir;

	public PlaceNodeAtCmd(Node node, Node neighbor, EDirection dir) {
		this.node = node;
		this.neighbor = neighbor;
		this.dir = dir;
	}

	@Override
	public String getLabel() {
		return "Add Node: " + node.getLabel();
	}

	@Override
	public ICmd run(RnB rnb) {
		rnb.placeAt(neighbor, dir, node);
		return new RemoveNodeCmd(node);
	}

}
