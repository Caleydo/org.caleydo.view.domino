/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ANodeDragInfo implements IDragInfo {
	private final Vec2f mousePos;
	private Vec2f offset = new Vec2f(0, 0);

	public ANodeDragInfo(Vec2f mousePos) {
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

	/**
	 * @return the offset, see {@link #mousePos}
	 */
	public Vec2f getMousePos() {
		return mousePos;
	}
}
