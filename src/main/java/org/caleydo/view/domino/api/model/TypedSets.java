/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import static org.caleydo.view.domino.api.model.TypedCollections.INVALID_ID;
import static org.caleydo.view.domino.api.model.TypedCollections.mapSingle;
import static org.caleydo.view.domino.api.model.TypedCollections.toSingleOrInvalid;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;

import com.google.common.base.Function;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedSets {


	public static Set<TypedID> union(TypedSet a, TypedSet b) {
		if (a.getIdType().equals(b.getIdType()))
			return new SingleTypedIDSet(a.union(b));
		IIDTypeMapper<Integer, Integer> b2a = findMapper(b.getIdType(), a.getIdType());
		if (b2a == null) // no shared elements
			return Sets.union(new SingleTypedIDSet(a), new SingleTypedIDSet(b));

		return Collections.emptySet();
	}

	private static IIDTypeMapper<Integer, Integer> findMapper(IDType from, IDType to) {
		IDMappingManager m = IDMappingManagerRegistry.get().getIDMappingManager(from);
		if (m == null)
			return null;
		return m.getIDTypeMapper(from, to);
	}

	public static TypedList map(List<TypedID> in, IDType target) {
		if (in instanceof SingleTypedIDList) {
			SingleTypedIDList l = (SingleTypedIDList) in;
			if (l.getIdType() == target)
				return l.wrappee;
			IIDTypeMapper<Integer, Integer> m = findMapper(l.getIdType(), target);
			if (m == null) // not mappable all invalid
				return allInvalid(in, target);
			List<Set<Integer>> r = m.applyList(l.wrappee);
			if (r == null)
				return allInvalid(in, target);
			// map result to a list
			return new TypedList(ImmutableList.copyOf(Lists.transform(r, toSingleOrInvalid)), target);
		}
		// we have multiple types

		// use a cache for better performance
		LoadingCache<IDType, IIDTypeMapper<Integer, Integer>> cache = MappingCaches.create(null, target);

		return new TypedList(ImmutableList.copyOf(Lists.transform(in, map(cache))), target);
	}

	public static List<TypedID> sort(Collection<TypedID> in, ITypedComparator... comparators) {
		// nothing to sort
		if (comparators.length == 0 || in.size() <= 1)
			return in instanceof List ? ((List<TypedID>) in) : ImmutableList.copyOf(in);

		if (in instanceof ISingleTypedIDCollection) // single id type, optimization
			return sortSingle(((ISingleTypedIDCollection) in).getData(), ((ISingleTypedIDCollection) in).getIdType(),
					comparators);

		TypedID[] r = in.toArray(new TypedID[0]);
		Arrays.sort(r, MappingComparators.of(comparators));
		return ImmutableList.copyOf(r);
	}

	private static List<TypedID> sortSingle(ITypedCollection in, IDType idType, ITypedComparator... comparators) {
		Integer[] r = in.toArray(new Integer[0]);
		Arrays.sort(r, MappingComparators.of(idType, comparators));
		return new SingleTypedIDList(new TypedList(Arrays.asList(r), idType));
	}

	/**
	 * @param cache
	 * @return
	 */
	private static Function<TypedID, Integer> map(final LoadingCache<IDType, IIDTypeMapper<Integer, Integer>> cache) {
		return new Function<TypedID, Integer>() {
			@Override
			public Integer apply(TypedID input) {
				if (input == null)
					return INVALID_ID;
				IIDTypeMapper<Integer, Integer> m = cache.getUnchecked(input.getIdType());
				return mapSingle(m, input.getId());
			}
		};
	}

	private static TypedList allInvalid(List<TypedID> in, IDType target) {
		return new TypedList(new RepeatingList<Integer>(INVALID_ID, in.size()), target);
	}

	private interface ISingleTypedIDCollection extends IHasIDType {
		ITypedCollection getData();
	}

	private static class SingleTypedIDSet extends AbstractSet<TypedID> implements Function<Integer, TypedID>,
			ISingleTypedIDCollection {
		private final TypedSet wrappee;

		public SingleTypedIDSet(TypedSet wrappee) {
			this.wrappee = wrappee;
		}

		@Override
		public TypedSet getData() {
			return wrappee;
		}

		@Override
		public Iterator<TypedID> iterator() {
			return Iterators.transform(wrappee.iterator(), this);
		}

		@Override
		public IDType getIdType() {
			return wrappee.getIdType();
		}

		@Override
		public TypedID apply(Integer input) {
			return new TypedID(input == null ? -1 : input.intValue(), wrappee.getIdType());
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

	private static class SingleTypedIDList extends AbstractList<TypedID> implements Function<Integer, TypedID>,
			ISingleTypedIDCollection {
		private final TypedList wrappee;

		public SingleTypedIDList(TypedList wrappee) {
			this.wrappee = wrappee;
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
			return Iterators.transform(wrappee.iterator(), this);
		}

		@Override
		public TypedID apply(Integer input) {
			return new TypedID(input == null ? -1 : input.intValue(), wrappee.getIdType());
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
			return apply(wrappee.get(index));
		}
	}

	private static class RepeatingList<T> extends AbstractList<T> {
		private final T value;
		private final int size;

		public RepeatingList(T value, int size) {
			this.value = value;
			this.size = size;
		}

		@Override
		public T get(int index) {
			return value;
		}

		@Override
		public int indexOf(Object o) {
			if (Objects.equals(o, value))
				return 0;
			return -1;
		}

		@Override
		public boolean contains(Object o) {
			return Objects.equals(o, value);
		}

		@Override
		public int size() {
			return size;
		}

	}
}
