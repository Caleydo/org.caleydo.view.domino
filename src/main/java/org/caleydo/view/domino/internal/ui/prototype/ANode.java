/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.view.domino.api.model.TypedCollections;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ANode implements INode {
	protected final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	private Object layoutData;
	private String label;

	public ANode(String label) {
		this.label = label;
	}

	public ANode(ANode clone) {
		this.label = clone.label;
	}

	/**
	 * @param label
	 *            setter, see {@link label}
	 */
	@Override
	public final void setLabel(String label) {
		if (Objects.equals(label, this.label))
			return;
		propertySupport.firePropertyChange(PROP_LABEL, this.label, this.label = label);
	}

	/**
	 * @return the label, see {@link #label}
	 */
	@Override
	public final String getLabel() {
		return label;
	}

	@Override
	public final ANode setLayoutData(Object layoutData) {
		if (Objects.equals(this.layoutData, layoutData))
			return this;
		this.layoutData = layoutData;
		return this;
	}

	@Override
	public final <T> T getLayoutDataAs(Class<T> clazz, T default_) {
		return getLayoutDataAs(clazz, Suppliers.ofInstance(default_));
	}

	@Override
	public <T> T getLayoutDataAs(Class<T> clazz, Supplier<? extends T> default_) {
		if (clazz.isInstance(this))
			return clazz.cast(this);
		return GLLayouts.resolveLayoutData(clazz, layoutData, default_);
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
