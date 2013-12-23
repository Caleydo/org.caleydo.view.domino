/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import gleem.linalg.Vec2f;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeUIState {
	protected final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	public static final String PROP_ZOOM = "zoom";
	public static final String PROP_PROXIMITY_MODE = "proximityMode";

	private final Vec2f zoom = new Vec2f(1, 1);

	private EProximityMode proximityMode = EProximityMode.FREE;

	/**
	 * @param clone
	 */
	public void init(NodeUIState clone) {
		this.zoom.set(clone.zoom);
	}
	/**
	 * @return the proximityMode, see {@link #proximityMode}
	 */
	public EProximityMode getProximityMode() {
		return proximityMode;
	}

	/**
	 * @param proximityMode
	 *            setter, see {@link proximityMode}
	 */
	public void setProximityMode(EProximityMode proximityMode) {
		propertySupport.firePropertyChange(PROP_PROXIMITY_MODE, this.proximityMode, this.proximityMode = proximityMode);
	}

	public boolean setZoom(float zoomFactorX, float zoomFactorY) {
		if (this.zoom.x() == zoomFactorX && this.zoom.y() == zoomFactorY)
			return false;
		final Vec2f old = zoom.copy();
		zoom.set(zoomFactorX, zoomFactorY);
		propertySupport.firePropertyChange(PROP_ZOOM, old, zoom);
		return true;
	}

	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
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
		// float shiftX = dim == 0 ? 0 : event.getWheelRotation() * 5;
		// float shiftY = rec == 0 ? 0 : event.getWheelRotation() * 5;
		// setZoom(zoom.x() + shiftX, zoom.y() + shiftY);
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
