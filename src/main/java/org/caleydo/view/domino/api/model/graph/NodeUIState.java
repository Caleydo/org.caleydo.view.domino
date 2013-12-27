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

	public static final String PROP_SIZE_CHANGE = "sizeChange";
	public static final String PROP_PROXIMITY_MODE = "proximityMode";

	private final Vec2f sizeChange = new Vec2f(0, 0);

	private EProximityMode proximityMode = EProximityMode.FREE;

	/**
	 * @param clone
	 */
	public void init(NodeUIState clone) {
		this.sizeChange.set(clone.sizeChange);
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

	public boolean setSizeChange(float sizeX, float sizeY) {
		if (this.sizeChange.x() == sizeX && this.sizeChange.y() == sizeY)
			return false;
		final Vec2f old = sizeChange.copy();
		sizeChange.set(sizeX, sizeY);
		propertySupport.firePropertyChange(PROP_SIZE_CHANGE, old, sizeChange);
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
	 * @return the size, see {@link #sizeChange}
	 */
	public Vec2f getSizeChange() {
		return sizeChange;
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

		float shiftX = dim == 0 ? 0 : event.getWheelRotation() * 5;
		float shiftY = rec == 0 ? 0 : event.getWheelRotation() * 5;
		setSizeChange(sizeChange.x() + shiftX, sizeChange.y() + shiftY);
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
