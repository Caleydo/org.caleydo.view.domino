/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.layout;

import gleem.linalg.Vec2f;

import java.util.Set;

/**
 * @author Samuel Gratzl
 *
 */
public interface IGraphVertex {
	Vec2f getLocation();

	Vec2f getSize();

	void setBounds(Vec2f location, Vec2f size);

	/**
	 * move item
	 *
	 * @param x
	 * @param y
	 */
	void move(float x, float y);

	/**
	 * get the edges of this item
	 * 
	 * @return
	 */
	Set<? extends IGraphEdge> getEdges();
}
