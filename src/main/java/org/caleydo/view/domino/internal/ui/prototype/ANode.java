/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ANode implements INode {
	protected final static String PROP_TRANSPOSE = "transpose";
	protected final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	@Override
	public final IDType getIDType(EDimension dim) {
		return getData(dim).getIdType();
	}

	@Override
	public final int getSize(EDimension dim) {
		return getData(dim).size();
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

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getLabel();
	}
}
