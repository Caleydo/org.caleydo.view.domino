/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import static org.caleydo.view.domino.api.model.MappingCaches.findMapper;
import static org.caleydo.view.domino.api.model.TypedCollections.INVALID_ID;
import static org.caleydo.view.domino.api.model.TypedCollections.mapSingle;
import static org.caleydo.view.domino.api.model.TypedCollections.toSingleOrInvalid;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.id.MappingType;
import org.caleydo.core.util.collection.Pair;

import com.google.common.base.Function;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.primitives.Ints;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedSets {


	public static MultiTypedSet union(TypedSet... sets) {
		if (sets.length == 0)
			return new MultiTypedSet(new IDType[0], Collections.<int[]>emptySet());
		sets = compress(sets);
		final int l = sets.length;
		if (l == 1) { // single id type
			return MultiTypedSet.single(sets[0]);
		}

		final LoadingCache<Pair<IDType, IDType>, IIDTypeMapper<Integer, Integer>> cache = MappingCaches.create();

		sets = expandMapped(sets, cache);

		Set<List<Integer>> r = new HashSet<>();
		IDType[] t = new IDType[l];
		for(int i = 0; i < sets.length; ++i) {
			t[i] = sets[i].getIdType();
			fill(sets, i, cache, r);
		}

		// convert to right structure
		ImmutableSet.Builder<int[]> r_s = ImmutableSet.builder();
		for(List<Integer> ri : r)
			r_s.add(Ints.toArray(ri));
		return new MultiTypedSet(t, r_s.build());
	}

	/**
	 * expands te given sets, such that all mapped instances between the ids are part of the sets, i.e. for each set map
	 * all ids to all others ids and add missing entries
	 *
	 * @param sets
	 * @param cache
	 * @return
	 */
	private static TypedSet[] expandMapped(TypedSet[] sets,
			LoadingCache<Pair<IDType, IDType>, IIDTypeMapper<Integer, Integer>> cache) {
		Set<TypedID> todo = new HashSet<>();
		Set<TypedID> done = new HashSet<>();

		for (TypedSet s : sets)
			// init
			todo.addAll(new SingleTypedIDSet(s));

		while (!todo.isEmpty()) {
			Iterator<TypedID> it = todo.iterator();
			TypedID r = it.next();
			it.remove();
			done.add(r);

			for (TypedSet s : sets) { // map to all other types
				IDType s_idType = s.getIdType();
				if (s_idType == r.getIdType())
					continue;
				IIDTypeMapper<Integer, Integer> m = cache.getUnchecked(Pair.make(r.getIdType(), s_idType));
				if (m == null)
					continue;
				Set<Integer> apply = m.apply(r.getId());
				if (apply == null)
					continue;
				for (Integer s_r : apply) { // produce new todo items it not already done
					TypedID i = new TypedID(s_r, s_idType);
					if (done.contains(i))
						continue;
					todo.add(i);
				}
			}
		}

		Collection<TypedSet> new_ = toTypedSets(done);
		return new_.toArray(new TypedSet[0]);
	}

	public static Collection<TypedSet> toTypedSets(Set<TypedID> set) {
		if (set instanceof SingleTypedIDSet) { // its just a wrapper
			return Collections.singleton(((SingleTypedIDSet) set).wrappee);
		}
		// compress to typed sets
		ListMultimap<IDType, TypedID> index = Multimaps.index(set, TypedCollections.toIDType);
		Collection<TypedSet> new_ = new ArrayList<>(index.keySet().size());
		for (IDType idType : index.keySet()) {
			List<TypedID> same = index.get(idType);
			new_.add(new TypedSet(ImmutableSet.copyOf(Lists.transform(same, TypedID.TO_ID)), idType));
		}
		return new_;
	}

	public static void main(String[] args) {
		final IDCategory cat = IDCategory.registerCategory("test");
		final IDType a = IDType.registerType("A", cat, EDataType.INTEGER);
		final IDType b = IDType.registerType("B", cat, EDataType.INTEGER);
		final IDType c = IDType.registerType("C", cat, EDataType.INTEGER);
		cat.setHumanReadableIDType(a);
		final int bA = 1;
		final int bB = 2;
		final int bC = 3;
		final int bD = 4;
		final int bE = 5;
		final int bF = 6;
		final IDMappingManager manager = IDMappingManagerRegistry.get().getIDMappingManager(cat);
		// 1:1
		// MappingType a2b = manager.createMap(a, b, false, false);
		// MappingType b2a = manager.createMap(b, a, false, false);
		// manager.addMapping(a2b, 2, bA);
		// manager.addMapping(a2b, 3, bB);
		// manager.addMapping(b2a, bA, 2);
		// manager.addMapping(b2a, bB, 3);
		// 1:n
		// MappingType a2b = manager.createMap(a, b, true, false);
		// MappingType b2a = manager.createMap(b, a, false, false);
		// manager.addMapping(a2b, 2, bB);
		// manager.addMapping(a2b, 2, bC);
		// manager.addMapping(a2b, 3, bE);
		// manager.addMapping(b2a, bB, 2);
		// manager.addMapping(b2a, bC, 2);
		// manager.addMapping(b2a, bE, 3);

		// n:m
		MappingType a2b = manager.createMap(a, b, true, false);
		MappingType b2a = manager.createMap(b, a, true, false);
		manager.addMapping(a2b, 2, bB);
		manager.addMapping(a2b, 2, bC);
		manager.addMapping(a2b, 3, bB);
		manager.addMapping(a2b, 3, bE);
		manager.addMapping(a2b, 4, bC);
		manager.addMapping(a2b, 5, bE);
		manager.addMapping(a2b, 5, bF);
		manager.addMapping(a2b, 6, bE);
		manager.addMapping(b2a, bB, 2);
		manager.addMapping(b2a, bB, 3);
		manager.addMapping(b2a, bC, 2);
		manager.addMapping(b2a, bC, 4);
		manager.addMapping(b2a, bE, 3);
		manager.addMapping(b2a, bE, 5);
		manager.addMapping(b2a, bE, 6);
		manager.addMapping(b2a, bF, 5);

		TypedSet a_s = new TypedSet(ImmutableSet.of(1, 2, 3), a);
		TypedSet b_s = new TypedSet(ImmutableSet.of(bB, bC, bD), b);

		MultiTypedSet r = union(a_s, b_s);
		System.out.println(r);
	}

	/**
	 * compress the sets such that one idtype occurs only ones
	 *
	 * @param sets
	 * @return
	 */
	private static TypedSet[] compress(TypedSet[] sets) {
		ListMultimap<IDType, TypedSet> index = Multimaps.index(Arrays.asList(sets), TypedCollections.toIDType);

		if (index.size() == sets.length) // nothing to compress
			return sets;
		TypedSet[] new_ = new TypedSet[index.keySet().size()];
		int i = 0;
		for (IDType idType : index.keySet()) {
			List<TypedSet> same = index.get(idType);
			new_[i++] = TypedSet.union(same);
		}
		return new_;
	}

	/**
	 * creates a multi typed sets pairs for a {@link TypedSet}
	 *
	 * @param sets
	 * @param act_i
	 * @param cache
	 * @param r
	 */
	private static void fill(TypedSet[] sets, int act_i,
			LoadingCache<Pair<IDType, IDType>, IIDTypeMapper<Integer, Integer>> cache, Set<List<Integer>> r) {
		TypedSet act = sets[act_i];
		final int l = sets.length;
		List<Iterator<Set<Integer>>> mapped = new ArrayList<>(l);
		final RepeatingList<Set<Integer>> invalidList = new RepeatingList<Set<Integer>>(
				Collections.<Integer> emptySet(), act.size());
		for(int i = 0; i < l; ++i) {
			TypedSet s = sets[i];
			IIDTypeMapper<Integer, Integer> m = cache.getUnchecked(Pair.make(act.getIdType(), s.getIdType()));
			Collection<Set<Integer>> m_r = m == null ? null : m.applySeq(act);
			if (m_r == null) {
				mapped.add(invalidList.iterator());
			} else
				mapped.add(m_r.iterator());
		}

		List<Set<Integer>> acts = new ArrayList<>(mapped.size());
		List<Integer> multi = new ArrayList<>();

		for (int j = 0; j < act.size(); ++j) {
			next(mapped, acts);
			multi.clear();
			int[] singles = new int[l]; // store just the singles
			for (int i = 0; i < l; ++i) {
				boolean isMulti = acts.get(i).size() > 1;
				if (isMulti)
					multi.add(i);
				else {
					Set<Integer> a = acts.get(i);
					singles[i] = a.isEmpty() ? INVALID_ID : a.iterator().next();
				}
			}
			if (multi.isEmpty()) {// just single mappings
				r.add(Ints.asList(singles));
			} else {
				// some multi mappings, we need to create the product of all combinations
				product(multi, 0, singles, acts, r);
			}
		}
	}


	private static void product(List<Integer> multi, int start, int[] singles, List<Set<Integer>> acts,
			Set<List<Integer>> r) {
		if (multi.size() <= start) { // flush
			r.add(Ints.asList(Arrays.copyOf(singles, singles.length)));
			return;
		}
		int next = multi.get(start);
		Set<Integer> all = acts.get(next);
		for (Integer ai : all) {
			singles[next] = ai;
			product(multi, start + 1, singles, acts, r);
		}
	}

	/**
	 * @param mapped
	 * @param acts
	 * @return
	 */
	private static List<Set<Integer>> next(List<Iterator<Set<Integer>>> mapped, List<Set<Integer>> acts) {
		acts.clear();
		for (Iterator<Set<Integer>> it : mapped)
			acts.add(it.next());
		return acts;
	}


	/**
	 * maps the given inhomogenous input list to a homogenous list using {@link TypedCollections#INVALID_ID} for missing
	 * entries
	 *
	 * @param in
	 * @param target
	 * @return
	 */
	public static TypedList map(List<TypedID> in, IDType target) {
		if (in instanceof SingleTypedIDList) {
			SingleTypedIDList l = (SingleTypedIDList) in;
			if (l.getIdType() == target)
				return l.wrappee;
			IIDTypeMapper<Integer, Integer> m = findMapper(l.getIdType(), target);
			if (m == null) // not mappable all invalid
				return allInvalid(in, target);
			Collection<Set<Integer>> r = m.applySeq(l.wrappee);
			if (r == null)
				return allInvalid(in, target);
			// map result to a list
			return new TypedList(ImmutableList.copyOf(Collections2.transform(r, toSingleOrInvalid)), target);
		}
		// we have multiple types

		// use a cache for better performance
		LoadingCache<IDType, IIDTypeMapper<Integer, Integer>> cache = MappingCaches.create(null, target);

		return new TypedList(ImmutableList.copyOf(Lists.transform(in, map(cache))), target);
	}

	/**
	 * sort the inhomogenous set using the given comparators
	 *
	 * @param in
	 * @param comparators
	 * @return
	 */
	public static List<TypedID> sort(Collection<TypedID> in, ITypedComparator... comparators) {
		// nothing to sort
		if (comparators.length == 0 || in.size() <= 1)
			return in instanceof List ? ((List<TypedID>) in) : ImmutableList.copyOf(in);

		if (in instanceof ISingleTypedIDCollection) // single id type, optimization
			return sort(((ISingleTypedIDCollection) in), comparators);

		TypedID[] r = in.toArray(new TypedID[0]);
		Arrays.sort(r, MappingComparators.of(comparators));
		return ImmutableList.copyOf(r);
	}

	/**
	 * optimized version for {@link ISingleTypedIDCollection} as input
	 *
	 * @param in
	 * @param comparators
	 * @return
	 */
	public static List<TypedID> sort(ISingleTypedIDCollection in, ITypedComparator... comparators) {
		Integer[] r = in.getData().toArray(new Integer[0]);
		Arrays.sort(r, MappingComparators.of(in.getIdType(), comparators));
		return new SingleTypedIDList(new TypedList(Arrays.asList(r), in.getIdType()));
	}

	public static MultiTypedList sort(IMultiTypedCollection in, ITypedComparator... comparators) {
		// nothing to sort
		if (comparators.length == 0 || in.size() <= 1)
			return in.asList();

		int[][] r = in.toArray(new int[0][]);
		Arrays.sort(r, MappingComparators.of(in.getIDTypes(), comparators));
		return new MultiTypedList(in.getIDTypes(), ImmutableList.copyOf(r));
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

	private interface ISingleTypedIDCollection extends IHasIDType, Collection<TypedID> {
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
}
