/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import static org.caleydo.view.domino.api.model.typed.TypedCollections.mapSingle;

import java.util.Comparator;
import java.util.Map;

import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.collection.Pair;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * set of comparator that handle conversion of ids for {@link ITypedComparator} correctly
 *
 * @author Samuel Gratzl
 *
 */
public class MappingComparators {


	public static Comparator<TypedID> of(ITypedComparator... comparators) {
		return new Complex(comparators);
	}

	public static Comparator<Integer> of(IDType idType, ITypedComparator... comparators) {
		return new Single(idType, comparators);
	}

	/**
	 * comparator for a {@link IMultiTypedCollection} based comparison
	 *
	 * @param idTypes
	 * @param comparators
	 * @return
	 */
	public static Comparator<int[]> of(IDType[] idTypes, ITypedComparator... comparators) {
		return new Multi(idTypes, comparators);
	}

	private static final class Complex implements Comparator<TypedID> {
		private final ITypedComparator[] comparators;
		private final LoadingCache<Pair<IDType, IDType>, IIDTypeMapper<Integer, Integer>> cache = MappingCaches.create();

		/**
		 * @param comparators
		 */
		public Complex(ITypedComparator... comparators) {
			this.comparators = comparators;
		}

		@Override
		public int compare(TypedID o1, TypedID o2) {
			for (ITypedComparator c : comparators) {
				IDType target = c.getIdType();
				Integer id1 = map(o1, target);
				Integer id2 = map(o2, target);
				int r = c.compare(id1, id2);
				if (r != 0)
					return r;
			}
			return 0;
		}

		private Integer map(TypedID id, IDType target) {
			IDType source = id.getIdType();
			if (source.equals(target)) // optimize
				return id.getId();

			IIDTypeMapper<Integer, Integer> m = cache.apply(Pair.make(source, target));
			return mapSingle(m, id.getId());
		}
	}

	private static final class Multi implements Comparator<int[]> {
		private final ITypedComparator[] comparators;
		private final Map<IDType, Integer> lookup;

		/**
		 * @param idTypes
		 * @param comparators
		 */
		public Multi(IDType[] idTypes, ITypedComparator... comparators) {
			this.comparators = comparators;
			Builder<IDType, Integer> builder = ImmutableMap.builder();
			for (int i = 0; i < idTypes.length; ++i)
				builder.put(idTypes[i], i);
			this.lookup = builder.build();
		}

		@Override
		public int compare(int[] o1, int[] o2) {
			for (ITypedComparator c : comparators) {
				IDType target = c.getIdType();
				Integer index = lookup.get(target);
				if (index == null) // nothing to map
					continue;
				Integer id1 = o1[index];
				Integer id2 = o2[index];
				int r = c.compare(id1, id2);
				if (r != 0)
					return r;
			}
			return 0;
		}
	}

	private static final class Single implements Comparator<Integer> {
		private final ITypedComparator[] comparators;
		private final LoadingCache<IDType, IIDTypeMapper<Integer, Integer>> cache;
		private final IDType source;

		/**
		 * @param comparators
		 */
		public Single(IDType idType, ITypedComparator... comparators) {
			this.source = idType;
			this.comparators = comparators;
			cache = MappingCaches.create(idType, null);
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			for (ITypedComparator c : comparators) {
				IDType target = c.getIdType();
				// map to target type
				Integer id1 = map(o1, target);
				Integer id2 = map(o2, target);
				int r = c.compare(id1, id2);
				if (r != 0)
					return r;
			}
			return 0;
		}

		private Integer map(Integer id, IDType target) {
			if (source.equals(target)) // identity
				return id;
			IIDTypeMapper<Integer, Integer> m = cache.apply(target);
			return mapSingle(m, id);
		}
	}

}
