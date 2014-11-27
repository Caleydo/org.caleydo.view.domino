/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.AbstractSet;
import java.util.Iterator;

import org.caleydo.core.id.IDType;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

/**
 * utility class for a {@link TypedID} based set based on a single {@link TypedSet}
 *
 * @author Samuel Gratzl
 *
 */
class SingleTypedIDSet extends AbstractSet<TypedID> implements ISingleTypedIDCollection {
	private final TypedSet wrappee;
	private final Function<Integer, TypedID> f;

	public SingleTypedIDSet(TypedSet wrappee) {
		this.wrappee = wrappee;
		f = TypedID.toTypedId(wrappee.getIdType());
	}

	@Override
	public TypedSet getData() {
		return wrappee;
	}

	@Override
	public Iterator<TypedID> iterator() {
		return Iterators.transform(wrappee.iterator(), f);
	}

	@Override
	public IDType getIdType() {
		return wrappee.getIdType();
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
	public int size() {
		return wrappee.size();
	}
}
