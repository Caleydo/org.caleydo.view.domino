/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.api.model.typed;

import java.util.Comparator;

import org.caleydo.core.id.IDType;

import com.google.common.collect.Ordering;

/**
 * @author Samuel Gratzl
 *
 */
class TypedComparator implements ITypedComparator {
	public static final Comparator<Integer> NATURAL = Ordering.natural();
	private final IDType idType;
	private final Comparator<Integer> c;

	public TypedComparator(Comparator<Integer> c, IDType idType) {
		this.c = c;
		this.idType = idType;
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		return c.compare(o1, o2);
	}

	@Override
	public IDType getIdType() {
		return idType;
	}

}
