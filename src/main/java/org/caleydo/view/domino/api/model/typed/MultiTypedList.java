/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import static org.caleydo.view.domino.api.model.typed.TypedCollections.INVALID_ID;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.api.model.typed.util.RepeatingList;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * a {@link IMultiTypedCollection} with an underlying list
 * 
 * @author Samuel Gratzl
 * 
 */
public class MultiTypedList extends AbstractList<int[]> implements IMultiTypedCollection {
	private final IDType[] idTypes;
	private final List<int[]> ids;

	public MultiTypedList(IDType[] idTypes, List<int[]> ids) {
		this.idTypes = idTypes;
		this.ids = ids;
	}

	public int depth() {
		return idTypes.length;
	}

	@Override
	public MultiTypedList asList() {
		return this;
	}

	@Override
	public List<TypedID> asInhomogenous() {
		if (ids instanceof Single)
			return new SingleTypedIDList(((Single) ids).data);
		ImmutableList.Builder<TypedID> b = ImmutableList.builder();
		for (int i = 0; i < depth(); ++i) {
			IDType idType = idTypes[i];
			// select just the slice and map to typed id
			b.addAll(Collections2.transform(ids, Functions.compose(TypedID.toTypedId(idType), slice(i))));
		}
		return b.build();
	}

	@Override
	public int[] get(int index) {
		return ids.get(index);
	}

	public int get(IDType idType, int index) {
		int jindex = index(idType);
		if (jindex < 0)
			return INVALID_ID;
		return get(index)[jindex];
	}

	public TypedList slice(IDType idType) {
		int index = index(idType);
		if (index < 0)
			return new TypedList(RepeatingList.repeat(INVALID_ID, size()), idType);
		if (depth() == 1 && ids instanceof Single)
			return ((Single) ids).data;
		return new TypedList(Lists.transform(ids, slice(index)), idType);
	}

	@Override
	public IDType[] getIDTypes() {
		return idTypes;
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

	private static final class Single extends AbstractList<int[]> implements Function<Integer, int[]> {
		private final TypedList data;

		public Single(TypedList set) {
			this.data = set;
		}
		@Override
		public int size() {
			return data.size();
		}
		@Override
		public int[] apply(Integer input) {
			return new int[] {input.intValue()};
		}


		@Override
		public Iterator<int[]> iterator() {
			return Iterators.transform(data.iterator(), this);
		}

		@Override
		public int[] get(int index) {
			return apply(data.get(index));
		}
	}

	public static MultiTypedList single(TypedList set) {
		return new MultiTypedList(new IDType[] { set.getIdType() }, new Single(set));
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
