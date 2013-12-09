/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import static org.caleydo.view.domino.api.model.TypedCollections.INVALID_ID;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.caleydo.core.id.IDType;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * @author Samuel Gratzl
 *
 */
public class MultiTypedSet extends AbstractSet<int[]> {
	private final IDType[] idTypes;
	private final Set<int[]> ids;

	public MultiTypedSet(IDType[] idTypes, Set<int[]> ids) {
		this.idTypes = idTypes;
		this.ids = ids;
	}

	public int depth() {
		return idTypes.length;
	}

	public TypedSet slice(IDType idType) {
		int index = index(idType);
		if (index < 0)
			return new TypedSet(ImmutableSet.<Integer> of(), idType);
		if (depth() == 1 && ids instanceof Single)
			return ((Single) ids).set;
		return new TypedSet(ImmutableSet.copyOf(Collections2.transform(ids, slice(index))), idType);
	}

	public TypedList sliceList(IDType idType) {
		int index = index(idType);
		if (index < 0)
			return new TypedList(new RepeatingList<Integer>(INVALID_ID, size()), idType);
		return new TypedList(ImmutableList.copyOf(Collections2.transform(ids, slice(index))), idType);
	}

	public Iterable<IDType> getIDTypes() {
		return Iterables.unmodifiableIterable(Arrays.asList(idTypes));
	}

	private Function<int[], Integer> slice(final int index) {
		return new Function<int[], Integer>() {
			@Override
			public Integer apply(int[] input) {
				return input == null ? null : input[index];
			}
		};
	}

	private int index(IDType idType) {
		for (int i = 0; i < idTypes.length; ++i)
			if (idTypes[i] == idType)
				return i;
		return -1;
	}

	@Override
	public int size() {
		return ids.size();
	}

	@Override
	public Iterator<int[]> iterator() {
		return Iterators.unmodifiableIterator(ids.iterator());
	}

	private static final class Single extends AbstractSet<int[]> implements Function<Integer,int[]> {
		private final TypedSet set;

		public Single(TypedSet set) {
			this.set = set;
		}
		@Override
		public int size() {
			return set.size();
		}
		@Override
		public int[] apply(Integer input) {
			return new int[] {input.intValue()};
		}

		@Override
		public Iterator<int[]> iterator() {
			return Iterators.transform(set.iterator(), this);
		}
	}

	public static MultiTypedSet single(TypedSet set) {
		return new MultiTypedSet(new IDType[] { set.getIdType() }, new Single(set));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MultiTypedSet [idTypes=");
		builder.append(Arrays.toString(idTypes));
		builder.append(", ids=");
		for (int[] v : ids)
			builder.append(Arrays.toString(v)).append(',');
		builder.append("]");
		return builder.toString();
	}

}
