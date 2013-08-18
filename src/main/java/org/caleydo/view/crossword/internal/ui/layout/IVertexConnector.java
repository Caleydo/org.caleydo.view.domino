/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.layout;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.geom.Rect;

/**
 * @author Samuel Gratzl
 *
 */
public interface IVertexConnector {
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
	}
}