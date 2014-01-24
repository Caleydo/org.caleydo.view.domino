/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.core.data.collection.EDimension;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public enum EDirection {
	NORTH, EAST, SOUTH, WEST;

	public EDimension asDim() {
		return EDimension.get(isHorizontal());
	}

	/**
	 * @return
	 */
	public boolean isHorizontal() {
		return this == WEST || this == EAST;
	}

	public boolean isVertical() {
		return !isHorizontal();
	}

	public EDirection opposite() {
		return shift(2);
	}

	public EDirection shift(int by) {
		return EDirection.values()[(ordinal() + by) % 4];
	}

	public EDirection rot90() {
		return shift(1);
	}

	public boolean isPrimaryDirection() {
		return this == WEST || this == NORTH;
	}

	/**
	 * @param dimension
	 * @return
	 */
	public int asInt(EDimension dim) {
		switch (this) {
		case NORTH:
			return dim.select(0, -1);
		case SOUTH:
			return dim.select(0, 1);
		case WEST:
			return dim.select(-1, 0);
		case EAST:
			return dim.select(1, 0);
		}
		throw new IllegalStateException();
	}

	/**
	 * @return a set of all directions within in the given dimension
	 */
	public static ImmutableSet<EDirection> get(EDimension dim) {
		if (dim.isHorizontal())
			return Sets.immutableEnumSet(EDirection.WEST, EDirection.EAST);
		else
			return Sets.immutableEnumSet(EDirection.NORTH, EDirection.SOUTH);
	}

	/**
	 * @return a set of all directions within in the given dimension
	 */
	public static EDirection getPrimary(EDimension dim) {
		if (dim.isHorizontal())
			return EDirection.WEST;
		else
			return EDirection.NORTH;
	}

	public static Function<EDirection,EDirection> TO_OPPOSITE = new Function<EDirection,EDirection>() {
		@Override
		public EDirection apply(EDirection input) {
			return input.opposite();
		}
	};
}
