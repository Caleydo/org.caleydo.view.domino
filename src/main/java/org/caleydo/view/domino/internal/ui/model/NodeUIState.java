/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.model;

import gleem.linalg.Vec2f;

import java.beans.PropertyChangeSupport;

import org.apache.commons.lang.BitField;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeUIState {
	protected final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	public static final String PROP_ZOOM = "zoom";
	public static final String PROP_STATE = "state";

	private final BitField states = new BitField(0);
	private final Vec2f zoom = new Vec2f(1, 1);

	private final static int STATE_SELECTED = 1 << 0;
	private final static int STATE_HOVERED = 1 << 1;
	private final static int STATE_DRAGGED = 1 << 2;

	public boolean setZoom(float zoomFactorX, float zoomFactorY) {
		if (this.zoom.x() == zoomFactorX && this.zoom.y() == zoomFactorY)
			return false;
		final Vec2f old = zoom.copy();
		zoom.set(zoomFactorX, zoomFactorY);
		propertySupport.firePropertyChange(PROP_ZOOM, old, zoom);
		return true;
	}

	/**
	 * @return the zoom, see {@link #zoom}
	 */
	public Vec2f getZoom() {
		return zoom.copy();
	}

	public boolean zoom(float factor) {
		return zoom(factor, factor);
	}

	/**
	 * @param factor
	 * @param factorY
	 */
	public boolean zoom(float factorX, float factorY) {
		if (isInvalid(factorX) || isInvalid(factorY))
			return false;
		return setZoom(zoom.x() * factorX, zoom.y() * factorY);
	}

	/**
	 * @param factorY
	 * @return
	 */
	private static boolean isInvalid(float factor) {
		return Double.isNaN(factor) || Double.isInfinite(factor) || factor <= 0;
	}

	/**
	 * zoom implementation of the given picking event
	 *
	 * @param event
	 */
	public void zoom(IMouseEvent event) {
		if (event.getWheelRotation() == 0)
			return;
		int dim = toDirection(event, EDimension.DIMENSION);
		int rec = toDirection(event, EDimension.RECORD);

		float factor = (float) Math.pow(1.2, event.getWheelRotation());
		float factorX = dim == 0 ? 1 : factor;
		float factorY = rec == 0 ? 1 : factor;
		zoom(factorX, factorY);
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

	/**
	 * @param hovered
	 *            setter, see {@link hovered}
	 */
	public void setHovered(boolean hovered) {
		set(STATE_HOVERED, hovered);
	}

	/**
	 * @param b
	 */
	public void setSelected(boolean selected) {
		set(STATE_SELECTED, selected);
	}

	/**
	 * @param dragged
	 *            setter, see {@link dragged}
	 */
	public void setDragged(boolean dragged) {
		set(STATE_DRAGGED, dragged);
	}

	/**
	 * @param stateDragged
	 * @param dragged
	 */
	private void set(int state, boolean value) {
		if (states.isSet(state) == value)
			return;
		states.setBoolean(state, value);
		propertySupport.fireIndexedPropertyChange(PROP_STATE, state, !value, value);
	}

	/**
	 * @return the dragged, see {@link #dragged}
	 */
	public boolean isDragged() {
		return states.isSet(STATE_DRAGGED);
	}

	/**
	 * @return the selected, see {@link #selected}
	 */
	public boolean isSelected() {
		return states.isSet(STATE_SELECTED);
	}

	/**
	 * @return the hovered, see {@link #hovered}
	 */
	public boolean isHovered() {
		return states.isSet(STATE_HOVERED);
	}
}
