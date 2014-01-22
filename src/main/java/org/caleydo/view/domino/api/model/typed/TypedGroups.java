/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.domino.api.model.typed.util.RepeatingList;

/**
 * @author Samuel Gratzl
 *
 */
public final class TypedGroups {
	private static final String UNMAPPED = "Unmapped";
	private static final String UNGROUPED = "Ungrouped";
	private static final Color COLOr = Color.NEUTRAL_GREY;

	private TypedGroups() {

	}

	public static TypedListGroup createUnmappedGroup(IDType idType, int size) {
		return new TypedListGroup(new TypedList(RepeatingList.repeat(TypedCollections.INVALID_ID, size), idType),
				UNMAPPED, COLOr);
	}

	public static TypedListGroup createUngroupedGroup(IDType idType, int size) {
		return new TypedListGroup(new TypedList(RepeatingList.repeat(TypedCollections.INVALID_ID, size), idType),
				UNGROUPED, COLOr);
	}

	public static TypedListGroup createUngroupedGroup(TypedList list) {
		return new TypedListGroup(list, UNGROUPED, COLOr);
	}

	public static TypedSetGroup createUngroupedGroup(TypedSet set) {
		return new TypedSetGroup(set, UNGROUPED, COLOr);
	}

	public static boolean isUngrouped(ITypedGroup group) {
		return group.getLabel() == UNGROUPED;
	}

	public static boolean isUnmapped(ITypedGroup group) {
		return group.getLabel() == UNMAPPED;
	}
}
