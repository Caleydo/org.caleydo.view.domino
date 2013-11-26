/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.ui.layout;

import gleem.linalg.Vec2f;

import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.crossword.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public interface IGraphVertex {

	Vec2f getLocation();

	Vec2f getSize();

	/**
	 * @return
	 */
	Rect getBounds();

	void setBounds(Vec2f location, Vec2f size);

	/**
	 * move item
	 *
	 * @param x
	 * @param y
	 */
	void move(float x, float y);

	/**
	 * return the set of outgoing edges
	 *
	 * @return
	 */
	Set<? extends IGraphEdge> getEdges();

	/**
	 * whether this vertex has an edge of the given type
	 *
	 * @param type
	 * @return
	 */
	boolean hasEdge(EEdgeType type);

	TypedSet getIDs(EDimension type);

}
