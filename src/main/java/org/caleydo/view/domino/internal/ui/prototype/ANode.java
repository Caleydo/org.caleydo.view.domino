/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.api.model.TypedCollections;

import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ANode implements INode {
	protected final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	public ANode() {
	}

	public ANode(ANode clone) {

	}

	@Override
	public abstract ANode clone();

	@Override
	public final IDType getIDType(EDimension dim) {
		return getData(dim).getIdType();
	}

	@Override
	public final boolean hasDimension(EDimension dim) {
		return !TypedCollections.isInvalid(getIDType(dim));
	}

	@Override
	public final Set<EDimension> dimensions() {
		Set<EDimension> dims = EnumSet.noneOf(EDimension.class);
		for (EDimension dim : EDimension.values())
			if (hasDimension(dim))
				dims.add(dim);
		return Sets.immutableEnumSet(dims);
	}

	@Override
	public final int getSize(EDimension dim) {
		return getData(dim).size();
	}

	@Override
	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	@Override
	public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	@Override
	public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getLabel();
	}
}
