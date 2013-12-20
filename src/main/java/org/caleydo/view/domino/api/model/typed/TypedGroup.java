/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroup extends AbstractCollection<Integer> implements ITypedGroup, ITypedCollection {
	private final Color color;
	private final String label;
	private final ITypedCollection data;

	public TypedGroup(Set<Integer> data, IDType idType, Color color, String label) {
		this(new TypedSet(data, idType), color, label);
	}

	public TypedGroup(List<Integer> data, IDType idType, Color color, String label) {
		this(new TypedList(data, idType), color, label);
	}
	public TypedGroup(ITypedCollection data, Color color, String label) {
		this.data = data;
		this.color = color;
		this.label = label;
	}

	@Override
	public IDType getIdType() {
		return data.getIdType();
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
	public TypedList asList() {
		return data.asList();
	}

	@Override
	public Iterator<Integer> iterator() {
		return data.iterator();
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public boolean contains(Object o) {
		return data.contains(o);
	}

	@Override
	public final Object[] toArray() {
		return data.toArray();
	}

	@Override
	public final <T> T[] toArray(T[] a) {
		return data.toArray(a);
	}

	@Override
	public final boolean containsAll(Collection<?> c) {
		return data.containsAll(c);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedGroup other = (TypedGroup) obj;
		return Objects.equals(color, other.color) && Objects.equals(label, other.label)
				&& Objects.equals(data, other.data);
	}

}
