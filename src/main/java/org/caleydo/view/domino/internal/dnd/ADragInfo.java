/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.domino.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ADragInfo implements IDragInfo {
	private final Vec2f mousePos;

	public ADragInfo(Vec2f mousePos) {
		this.mousePos = mousePos;
	}

	/**
	 * @return the offset, see {@link #mousePos}
	 */
	public Vec2f getMousePos() {
		return mousePos;
	}

	protected abstract Vec2f getSize();


	/**
	 * @param findDomino
	 * @return
	 */
	public GLElement createUI(Domino domino) {
		Vec2f size = getSize();
		return new DragElement(getLabel(), size, domino, this);
	}
}
