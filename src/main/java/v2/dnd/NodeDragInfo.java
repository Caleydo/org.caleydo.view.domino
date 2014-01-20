/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.dnd;

import v2.Node;
import gleem.linalg.Vec2f;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeDragInfo extends ADragInfo {
	private final Node node;

	public NodeDragInfo(Vec2f mousePos, Node node) {
		super(mousePos);
		this.node = node;
	}

	@Override
	public String getLabel() {
		return node.getLabel();
	}

	/**
	 * @return the node, see {@link #node}
	 */
	public Node getNode() {
		return node;
	}

	@Override
	protected Vec2f getSize() {
		return node.getSize();
	}
}
