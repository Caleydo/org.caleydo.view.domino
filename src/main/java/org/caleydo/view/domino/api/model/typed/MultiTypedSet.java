/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import static org.caleydo.view.domino.api.model.typed.TypedCollections.INVALID_ID;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.collection.Pair;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

/**
 * @author Samuel Gratzl
 *
 */
public class MultiTypedSet extends AbstractSet<int[]> implements IMultiTypedCollection {
	private final IDType[] idTypes;
	private final Set<int[]> ids;

	public MultiTypedSet(IDType[] idTypes, Set<int[]> ids) {
		this.idTypes = idTypes;
		this.ids = ids;
	}


	@Override
	public MultiTypedList asList() {
		return new MultiTypedList(idTypes, ImmutableList.copyOf(ids));
	}

	@Override
	public Set<TypedID> asInhomogenous() {
		if (ids instanceof Single)
			return new SingleTypedIDSet(((Single) ids).set);
		ImmutableSet.Builder<TypedID> b = ImmutableSet.builder();
		for (int i = 0; i < depth(); ++i) {
			IDType idType = idTypes[i];
			// select just the slice and map to typed id
			b.addAll(Collections2.transform(ids, Functions.compose(TypedID.toTypedId(idType), slice(i))));
		}
		return b.build();
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

	@Override
	public IDType[] getIDTypes() {
		return idTypes;
	}

	public boolean hasIDType(IDType idType) {
		for (IDType type : idTypes) {
			if (type == idType)
				return true;
		}
		return false;
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


	/**
	 * expands the current {@link MultiTypedSet} to be able to represent all given id types
	 *
	 * @param types
	 * @return a new set
	 */
	public MultiTypedSet expand(Collection<? extends IHasIDType> types) {
		List<IDType> toAdd = new ArrayList<>();
		for (IHasIDType idtype : types) {
			if (toAdd.contains(idtype.getIdType()) &&!hasIDType(idtype.getIdType()))
				toAdd.add(idtype.getIdType());
		}
		if (toAdd.isEmpty()) // nothing missing
			return this;

		final LoadingCache<Pair<IDType, IDType>, IIDTypeMapper<Integer, Integer>> cache = MappingCaches.create();
		// select the best mapping (in the best case a 1:1 mapping)
		List<Pair<Integer, IIDTypeMapper<Integer, Integer>>> converters = new ArrayList<>(toAdd.size());
		for (IDType idType : toAdd) {
			IDType source = selectBestIDType(cache, idType);
			int index = index(source);
			converters.add(Pair.make(index, cache.getUnchecked(Pair.make(source, idType))));
		}

		final int oldLength = idTypes.length;
		final int newLength =oldLength+toAdd.size();

		IDType[] r  = Arrays.copyOf(idTypes, newLength);
		for(int i = oldLength; i < newLength; ++i)
			r[i] = toAdd.get(i-oldLength);

		ImmutableSet.Builder<int[]> r_s = ImmutableSet.builder();
		for(int[] entry : this.ids) {
			int[] new_ = Arrays.copyOf(entry, newLength);
			// map all missing entries
			for(int i = oldLength; i < newLength; ++i) {
				int j = i - oldLength;
				Pair<Integer, IIDTypeMapper<Integer, Integer>> p = converters.get(j);
				new_[i] = TypedCollections.mapSingle(p.getSecond(), new_[p.getFirst()]);
			}
			r_s.add(new_);
		}
		return new MultiTypedSet(r, r_s.build());
	}

	/**
	 * @param cache
	 * @param idType
	 */
	private IDType selectBestIDType(LoadingCache<Pair<IDType, IDType>, IIDTypeMapper<Integer, Integer>> cache,
			IDType idType) {
		IIDTypeMapper<Integer, Integer> mapper;
		for (IDType act : idTypes) {
			mapper = cache.getUnchecked(Pair.make(act, idType));
			if (mapper == null)
				continue;
			if (mapper.isOne2OneMapping())
				return act;
		}
		return idTypes[0]; // any is possible

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
