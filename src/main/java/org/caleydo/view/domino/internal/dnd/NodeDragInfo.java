/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IUIDragInfo;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeDragInfo implements IUIDragInfo {
	private final INode node;
	private final Vec2f mousePos;
	private Vec2f offset = new Vec2f(0, 0);

	public NodeDragInfo(INode node, Vec2f mousePos) {
		this.node = node;
		this.mousePos = mousePos;
	}

	/**
	 * @param offset
	 *            setter, see {@link offset}
	 */
	public void setOffset(Vec2f offset) {
		this.offset = offset;
	}

	/**
	 * @return the offset, see {@link #offset}
	 */
	public Vec2f getOffset() {
		return offset;
	}

	@Override
	public String getLabel() {
		return node.getLabel();
	}

	/**
	 * @return the offset, see {@link #mousePos}
	 */
	public Vec2f getMousePos() {
		return mousePos;
	}

	@Override
	public GLElement createUI() {
		// DndNodeElement elem = new DndNodeElement(node);
		// elem.setLocation(-offset.x(), -offset.y());
		// elem.setVisibility(EVisibility.VISIBLE);
		return null;
	}

	/**
	 * @return the node, see {@link #node}
	 */
	public INode getNode() {
		return node;
	}

}
