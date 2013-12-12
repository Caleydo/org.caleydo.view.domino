/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.internal.ui.DominoLayoutInfo;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
class NodeLayoutElement implements INodeUI {
	private final IGLLayoutElement elem;
	private final DominoLayoutInfo info;
	private boolean setLocation = false;

	public NodeLayoutElement(IGLLayoutElement elem) {
		this.elem = elem;
		this.info = elem.getLayoutDataAs(DominoLayoutInfo.class,
				GLLayoutDatas.<DominoLayoutInfo> throwInvalidException());
	}

	public BlockInfo getBlock() {
		return info.getBlock();
	}

	public void setBlock(BlockInfo block) {
		info.setBlock(block);
	}

	/**
	 * @return
	 */
	@Override
	public INode asNode() {
		return elem.getLayoutDataAs(INode.class, GLLayoutDatas.<INode> throwInvalidException());
	}

	@Override
	public GLElement asGLElement() {
		return elem.asElement();
	}

	@Override
	public void setData(EDimension dim, TypedList data) {
		((INodeUI) elem.asElement()).setData(dim, data);
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

	/**
	 * @return
	 */
	public boolean isDragged() {
		return info.isDragged();
	}

	/**
	 * @param dir
	 * @param v
	 */
	public void setSize(EDimension dir, float v) {
		float w = dir.select(v, elem.getWidth());
		float h = dir.select(elem.getHeight(), v);
		elem.setSize(w, h);
	}

	public void setLocation(float x, float y) {
		setLocation = true;
		elem.setLocation(x, y);
	}

	public Vec2f getLocation() {
		return elem.getLocation();
	}
}
