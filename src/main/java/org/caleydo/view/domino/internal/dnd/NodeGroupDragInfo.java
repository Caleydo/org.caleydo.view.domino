/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.NodeGroup;

import gleem.linalg.Vec2f;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeGroupDragInfo extends ADragInfo {
	private final NodeGroup group;

	public NodeGroupDragInfo(Vec2f mousePos, NodeGroup group) {
		super(mousePos);
		this.group = group;
	}

	@Override
	public String getLabel() {
		return group.getLabel();
	}

	/**
	 * @return the group, see {@link #group}
	 */
	public NodeGroup getGroup() {
		return group;
	}

	public Node getNode() {
		return group.getNode();
	}

	@Override
	protected Vec2f getSize() {
		return group.getSize();
	}
}
