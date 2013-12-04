/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.dnd.IUIDragInfo;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ui.DndNodeElement;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeDragInfo implements IUIDragInfo {
	private final INode node;
	private final Vec2f offset;

	public NodeDragInfo(INode node, Vec2f offset) {
		this.node = node;
		this.offset = offset;
	}

	@Override
	public String getLabel() {
		return node.getLabel();
	}

	/**
	 * @return the offset, see {@link #offset}
	 */
	public Vec2f getOffset() {
		return offset;
	}

	@Override
	public GLElement createUI() {
		DndNodeElement elem = new DndNodeElement(node);
		Vec2f s = elem.getInfo().getSize();
		elem.setSize(s.x(), s.y());
		elem.setLocation(-offset.x(), -offset.y());
		elem.setVisibility(EVisibility.VISIBLE);
		return elem;
	}

	/**
	 * @return the node, see {@link #node}
	 */
	public INode getNode() {
		return node;
	}

}
