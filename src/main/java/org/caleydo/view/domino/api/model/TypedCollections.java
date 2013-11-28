/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import java.util.Collections;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedCollections {
	private static final IDType IDTYPE;

	public static final TypedSet INVALID_SET;
	public static final TypedList INVALID_LIST;
	public static final ITypedComparator NATURAL_ORDER;

	static {
		IDTYPE = IDType.registerType("INVALID",
				IDCategory.registerInternalCategory("INVALID"), EDataType.STRING);
		INVALID_SET = new TypedSet(Collections.<Integer> emptySet(), IDTYPE);
		INVALID_LIST = new TypedList(Collections.<Integer> emptyList(), IDTYPE);
		NATURAL_ORDER = new TypedComparator(TypedComparator.NATURAL, IDTYPE);
	}

	public static boolean isInvalid(IHasIDType col) {
		return col.getIdType().equals(IDTYPE);
	}
}
