/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.id.IDType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

/**
 * a reaonly list of integers with their id type
 *
 * @author Samuel Gratzl
 *
 */
public class TypedList extends AbstractList<Integer> implements ITypedCollection {
	private final List<Integer> wrappee;
	private final IDType idType;

	public TypedList(List<Integer> wrappee, IDType idType) {
		this.wrappee = wrappee instanceof TypedList ? ((TypedList) wrappee).wrappee : Preconditions
				.checkNotNull(wrappee);
		this.idType = Preconditions.checkNotNull(idType);
	}

	public TypedList(TypedList clone) {
		this.wrappee = clone.wrappee;
		this.idType = clone.idType;
	}

	public static TypedList of(VirtualArray per) {
		return new TypedList(per.getIDs(), per.getIdType());
	}

	@Override
	public TypedList asList() {
		return this;
	}

	@Override
	public TypedList subList(int fromIndex, int toIndex) {
		return new TypedList(super.subList(fromIndex, toIndex), idType);
	}

	/**
	 * @return the wrappee, see {@link #wrappee}
	 */
	final List<Integer> getWrappee() {
		return wrappee;
	}

	/**
	 * @return the idType, see {@link #idType}
	 */
	@Override
	public final IDType getIdType() {
		return idType;
	}

	@Override
	public final Integer get(int index) {
		return wrappee.get(index);
	}

	@Override
	public final int size() {
		return wrappee.size();
	}

	@Override
	public final boolean isEmpty() {
		return wrappee.isEmpty();
	}

	@Override
	public final boolean contains(Object o) {
		return wrappee.contains(o);
	}

	@Override
	public final Iterator<Integer> iterator() {
		return Iterators.unmodifiableIterator(wrappee.iterator());
	}

	@Override
	public final Object[] toArray() {
		return wrappee.toArray();
	}

	@Override
	public final <T> T[] toArray(T[] a) {
		return wrappee.toArray(a);
	}

	@Override
	public final boolean containsAll(Collection<?> c) {
		return wrappee.containsAll(c);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((idType == null) ? 0 : idType.hashCode());
		result = prime * result + ((wrappee == null) ? 0 : wrappee.hashCode());
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
		TypedList other = (TypedList) obj;
		return Objects.equals(idType, other.idType) && Objects.equals(wrappee, other.wrappee);
	}
}
