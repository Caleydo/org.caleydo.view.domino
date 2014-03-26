/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.api.model.typed;

import java.util.AbstractList;
import java.util.Iterator;

import org.caleydo.core.id.IDType;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

/**
 * @author Samuel Gratzl
 *
 */
class SingleTypedIDList extends AbstractList<TypedID> implements ISingleTypedIDCollection {
	private final TypedList wrappee;
	private final Function<Integer, TypedID> f;

	public SingleTypedIDList(TypedList wrappee) {
		this.wrappee = wrappee;
		f = TypedID.toTypedId(wrappee.getIdType());
	}

	@Override
	public TypedList getData() {
		return wrappee;
	}

	@Override
	public IDType getIdType() {
		return wrappee.getIdType();
	}

	@Override
	public Iterator<TypedID> iterator() {
		return Iterators.transform(wrappee.iterator(), f);
	}

	@Override
	public int size() {
		return wrappee.size();
	}

	@Override
	public int indexOf(Object o) {
		if (!(o instanceof TypedID))
			return -1;
		TypedID t = (TypedID) o;
		if (t.getIdType() != wrappee.getIdType())
			return -1;
		return wrappee.indexOf(t.getId());
	}

	@Override
	public boolean contains(Object o) {
		if (!(o instanceof TypedID))
			return false;
		TypedID t = (TypedID) o;
		if (t.getIdType() != wrappee.getIdType())
			return false;
		return wrappee.contains(t.getId());
	}

	@Override
	public TypedID get(int index) {
		return f.apply(wrappee.get(index));
	}
}
