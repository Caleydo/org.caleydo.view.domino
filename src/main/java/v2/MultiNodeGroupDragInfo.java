/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.util.Set;

import org.caleydo.core.util.base.Labels;

/**
 * @author Samuel Gratzl
 *
 */
public class MultiNodeGroupDragInfo extends ADragInfo {
	private final Set<NodeGroup> groups;
	private final NodeGroup primary;

	public MultiNodeGroupDragInfo(Vec2f mousePos, NodeGroup primary, Set<NodeGroup> groups) {
		super(mousePos);
		this.primary = primary;
		this.groups = groups;
	}

	@Override
	public String getLabel() {
		return Labels.join(groups, ", ");
	}

	/**
	 * @return the groups, see {@link #groups}
	 */
	public Set<NodeGroup> getGroups() {
		return groups;
	}

	/**
	 * @return the primary, see {@link #primary}
	 */
	public NodeGroup getPrimary() {
		return primary;
	}

	@Override
	public Node getBaseNode() {
		return primary.getNode();
	}

	@Override
	protected Vec2f getSize() {
		return primary.getSize();
	}
}
