/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.Collections;
import java.util.Set;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedCollections {
	private static final IDType IDTYPE;

	public static final TypedSet INVALID_SET;
	public static final TypedList INVALID_LIST;
	public static final ITypedComparator NATURAL_ORDER;

	public final static Integer INVALID_ID = Integer.valueOf(-1);

	public static Function<IHasIDType, IDType> toIDType = new Function<IHasIDType, IDType>() {
		@Override
		public IDType apply(IHasIDType input) {
			return input == null ? null : input.getIdType();
		}
	};

	static {
		IDTYPE = IDType.registerType("INVALID",
				IDCategory.registerInternalCategory("INVALID"), EDataType.STRING);
		INVALID_SET = new TypedSet(Collections.<Integer> emptySet(), IDTYPE);
		INVALID_LIST = new TypedList(Collections.<Integer> emptyList(), IDTYPE);
		NATURAL_ORDER = new TypedComparator(TypedComparator.NATURAL, IDTYPE);
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
		return isInvalid(col.getIdType());
	}
	public static boolean isInvalid(IDType idType) {
		return idType.equals(IDTYPE);
	}

	public static Integer mapSingle(IIDTypeMapper<Integer, Integer> mapper, Integer id) {
		if (mapper == null)
			return INVALID_ID;
		Set<Integer> r = mapper.apply(id);
		if (r == null || r.isEmpty())
			return INVALID_ID;
		return r.iterator().next();
	}
}
