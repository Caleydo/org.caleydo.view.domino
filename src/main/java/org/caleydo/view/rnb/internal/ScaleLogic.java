/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;

/**
 * @author Samuel Gratzl
 *
 */
public class ScaleLogic {
	/**
	 * @param event
	 * @param s
	 * @return
	 */
	public static Vec2f shiftLogic(IMouseEvent event, Vec2f s) {
		int dim = toDirection(event, EDimension.DIMENSION);
		int rec = toDirection(event, EDimension.RECORD);
		float shiftX = dim == 0 ? 0 : event.getWheelRotation() * sizeFactor(s.x());
		float shiftY = rec == 0 ? 0 : event.getWheelRotation() * sizeFactor(s.y());
		return new Vec2f(shiftX, shiftY);
	}

	/**
	 * @param x
	 * @return
	 */
	private static int sizeFactor(float x) {
		if (x < 100)
			return 5;
		if (x < 500)
			return 10;
		if (x < 1000)
			return 20;
		return 50;
	}

	/**
	 * convert a {@link IMouseEvent} to a direction information
	 * 
	 * @param event
	 * @param dim
	 * @return -1 smaller, +1 larger, and 0 nothing
	 */
	private static int toDirection(IMouseEvent event, EDimension dim) {
		final int w = event.getWheelRotation();
		if (w == 0)
			return 0;
		int factor = w > 0 ? 1 : -1;
		return event.isCtrlDown() || dim.select(event.isAltDown(), event.isShiftDown()) ? factor : 0;
	}

}
