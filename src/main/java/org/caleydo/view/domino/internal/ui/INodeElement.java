/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public interface INodeElement extends IPickingListener {
	boolean setData(EDimension dim, TypedGroupList data);

	TypedGroupList getData(EDimension dim);

	INode asNode();

	/**
	 * @return
	 */
	Rectangle2D getRectangleBounds();

	/**
	 * @return
	 */
	Vec2f getSize();

	/**
	 * @return
	 */
	double getSize(EDimension dim);

	/**
	 * @return
	 */
	Vec2f getLocation();
}
