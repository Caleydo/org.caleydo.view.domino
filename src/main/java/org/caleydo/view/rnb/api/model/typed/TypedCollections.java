/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.api.model.typed;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * utilities for {@link ITypedCollection}
 *
 * @author Samuel Gratzl
 *
 */
public final class TypedCollections {
	public static final IDType INVALID_IDTYPE;

	public static final TypedSet INVALID_SET;
	public static final TypedList INVALID_LIST;
	public static final TypedSet INVALID_SINGLETON_SET;
	public static final TypedList INVALID_SINGLETON_LIST;
	public static final TypedGroupList INVALID_GROUP_LIST;
	public static final ITypedComparator NATURAL_ORDER;

	public final static Integer INVALID_ID = Integer.valueOf(-1);

	public static Function<IHasIDType, IDType> TO_IDTYPE = new Function<IHasIDType, IDType>() {
		@Override
		public IDType apply(IHasIDType input) {
			return input == null ? null : input.getIdType();
		}
	};

	static {
		INVALID_IDTYPE = IDType.registerType("INVALID",
				IDCategory.registerInternalCategory("INVALID"), EDataType.STRING);
		INVALID_SET = new TypedSet(ImmutableSet.<Integer> of(), INVALID_IDTYPE);
		INVALID_LIST = new TypedList(ImmutableList.<Integer> of(), INVALID_IDTYPE);
		INVALID_SINGLETON_SET = new TypedSet(ImmutableSet.<Integer> of(INVALID_ID), INVALID_IDTYPE);
		INVALID_SINGLETON_LIST = new TypedList(ImmutableList.<Integer> of(INVALID_ID), INVALID_IDTYPE);
		NATURAL_ORDER = new TypedComparator(TypedComparator.NATURAL, INVALID_IDTYPE);
		INVALID_GROUP_LIST = TypedGroupList.createUngrouped(INVALID_LIST);
	}

	public static final Function<Set<Integer>, Integer> toSingleOrInvalid = new Function<Set<Integer>, Integer>() {
		@Override
		public Integer apply(Set<Integer> input) {
			if (input == null || input.isEmpty())
				return INVALID_ID;
			return input.iterator().next();
		}
	};

	public static boolean isInvalid(IHasIDType col) {
		return col != null && isInvalid(col.getIdType());
	}
	public static boolean isInvalid(IDType idType) {
		return idType.equals(INVALID_IDTYPE);
	}

	public static Integer mapSingle(IIDTypeMapper<Integer, Integer> mapper, Integer id) {
		if (mapper == null)
			return INVALID_ID;
		Set<Integer> r = mapper.apply(id);
		if (r == null || r.isEmpty())
			return INVALID_ID;
		return r.iterator().next();
	}

	/**
	 * @param singleID
	 */
	public static TypedList singletonList(TypedID singleID) {
		return singletonList(singleID.getId(), singleID.getIdType());
	}

	public static TypedList singletonList(Integer id, IDType idType) {
		if (INVALID_ID.equals(id) && isInvalid(idType))
			return INVALID_SINGLETON_LIST;
		return new TypedList(Collections.singletonList(id), idType);
	}

	public static TypedSet singleton(TypedID singleID) {
		return singleton(singleID.getId(), singleID.getIdType());
	}

	public static TypedSet singleton(Integer id, IDType idType) {
		if (INVALID_ID.equals(id) && isInvalid(idType))
			return INVALID_SINGLETON_SET;
		return new TypedSet(Collections.singleton(id), idType);
	}

	public static TypedSet empty(IDType idType) {
		if (isInvalid(idType))
			return INVALID_SET;
		return new TypedSet(ImmutableSet.<Integer> of(), idType);
	}

	public static TypedList emptyList(IDType idType) {
		if (isInvalid(idType))
			return INVALID_LIST;
		return new TypedList(ImmutableList.<Integer> of(), idType);
	}

	public static ITypedComparator wrap(Comparator<Integer> c, IDType idType) {
		return new TypedComparator(c, idType);
	}

	public static ITypedComparator reverseOrder(final ITypedComparator c) {
		return new ITypedComparator() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return -1 * c.compare(o1, o2);
			}

			@Override
			public IDType getIdType() {
				return c.getIdType();
			}
		};
	}

}
