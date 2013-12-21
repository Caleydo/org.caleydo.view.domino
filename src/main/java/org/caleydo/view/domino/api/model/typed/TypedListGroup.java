/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.List;
import java.util.Objects;

import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedListGroup extends TypedList implements ITypedGroup {
	private final Color color;
	private final String label;

	public TypedListGroup(List<Integer> data, IDType idType, String label, Color color) {
		super(data, idType);
		this.color = color;
		this.label = label;
	}

	public TypedListGroup(TypedList data, String label, Color color) {
		super(data);
		this.color = color;
		this.label = label;
	}

	@Override
	public TypedListGroup subList(int fromIndex, int toIndex) {
		return new TypedListGroup(super.subList(fromIndex, toIndex), label, color);
	}

	@Override
	public TypedListGroup asList() {
		return this;
	}

	/**
	 * @return the label, see {@link #label}
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @return the color, see {@link #color}
	 */
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedListGroup other = (TypedListGroup) obj;
		return Objects.equals(color, other.color) && Objects.equals(label, other.label);
	}


}
