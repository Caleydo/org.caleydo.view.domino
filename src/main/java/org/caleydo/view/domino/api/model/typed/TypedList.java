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

import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.id.IDType;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedList extends AbstractList<Integer> implements ITypedCollection {
	private final List<Integer> wrappee;
	private final IDType idType;

	public TypedList(List<Integer> wrappee, IDType idType) {
		this.wrappee = Preconditions.checkNotNull(wrappee);
		this.idType = Preconditions.checkNotNull(idType);
	}

	public static TypedList of(VirtualArray per) {
		return new TypedList(per.getIDs(), per.getIdType());
	}

	@Override
	public TypedList asList() {
		return this;
	}

	/**
	 * @return the idType, see {@link #idType}
	 */
	@Override
	public IDType getIdType() {
		return idType;
	}

	@Override
	public Integer get(int index) {
		return wrappee.get(index);
	}

	@Override
	public int size() {
		return wrappee.size();
	}

	@Override
	public boolean isEmpty() {
		return wrappee.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return wrappee.contains(o);
	}

	@Override
	public Iterator<Integer> iterator() {
		return Iterators.unmodifiableIterator(wrappee.iterator());
	}

	@Override
	public Object[] toArray() {
		return wrappee.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return wrappee.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return wrappee.containsAll(c);
	}
}
