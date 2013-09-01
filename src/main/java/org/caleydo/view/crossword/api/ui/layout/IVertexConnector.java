/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.ui.layout;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.geom.Rect;

/**
 * a vertex connector is the connection of a {@link IGraphEdge} to a {@link IGraphVertex}
 *
 * @author Samuel Gratzl
 *
 */
public interface IVertexConnector {
	/**
	 *
	 * @return the type of connector whether in record or column dimension
	 */
	EConnectorType getConnectorType();

	/**
	 * in percent
	 *
	 * @return
	 */
	float getRadius();

	/**
	 * in percent
	 *
	 * @return
	 */
	float getCenter();

	public static enum EConnectorType {
		RECORD, COLUMN;

		/**
		 * extract the position,size for the dimension described by this element
		 *
		 * @param sRect
		 * @return
		 */
		public Vec2f extract(Rect rect) {
			switch (this) {
			case COLUMN:
				return new Vec2f(rect.x(), rect.width());
			case RECORD:
				return new Vec2f(rect.y(), rect.height());
			}
			throw new IllegalStateException();
		}

		public EConnectorType inverse() {
			return this == RECORD ? COLUMN : RECORD;
		}
	}
}