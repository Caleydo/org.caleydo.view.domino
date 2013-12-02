/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.internal.ui.DominoLayoutInfo;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
class NodeLayoutElement {
	private final IGLLayoutElement elem;
	private final DominoLayoutInfo info;

	public NodeLayoutElement(IGLLayoutElement elem) {
		this.elem = elem;
		this.info = elem.getLayoutDataAs(DominoLayoutInfo.class,
				GLLayoutDatas.<DominoLayoutInfo> throwInvalidException());
	}

	/**
	 * @return
	 */
	public INode asNode() {
		return elem.getLayoutDataAs(INode.class, GLLayoutDatas.<INode> throwInvalidException());
	}

	public IGLLayoutElement asElem() {
		return elem;
	}

	public Vec2f getSize() {
		return info.getSize();
	}

	public void setBounds(float x, float y, float w, float h) {
		elem.setBounds(x, y, w, h);
	}

	public Rect getRectBounds() {
		return elem.getRectBounds();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeLayoutElement [").append(asNode().getLabel());
		builder.append("]");
		return builder.toString();
	}

}
