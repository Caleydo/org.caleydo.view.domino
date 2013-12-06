/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import static org.caleydo.view.domino.api.model.TypedCollections.mapSingle;

import java.util.Comparator;

import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.collection.Pair;

import com.google.common.cache.LoadingCache;

/**
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
