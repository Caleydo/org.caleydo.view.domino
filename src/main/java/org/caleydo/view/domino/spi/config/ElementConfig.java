/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.spi.config;

import org.apache.commons.lang.BitField;

/**
 * @author Samuel Gratzl
 *
 */
public class ElementConfig {

	public static final int FLAG_CAN_SPLIT_X = 1 << 1;
	public static final int FLAG_CAN_SPLIT_Y = 2 << 1;
	public static final int FLAG_CAN_SPLIT = FLAG_CAN_SPLIT_X | FLAG_CAN_SPLIT_Y;

	public static final int FLAG_CAN_CHANGE_X = 1 << 3;
	public static final int FLAG_CAN_CHANGE_Y = 2 << 3;
	public static final int FLAG_CAN_CHANGE = FLAG_CAN_CHANGE_X | FLAG_CAN_CHANGE_Y;

	public static final int FLAG_CAN_MOVE = 1 << 5;
	public static final int FLAG_CAN_SCALE = 1 << 6;
	public static final int FLAG_CAN_CHANGE_VIS = 1 << 7;
	public static final int FLAG_CAN_CLOSE = 1 << 8;

	public static final int FLAG_CAN_ALL = FLAG_CAN_MOVE | FLAG_CAN_SCALE | FLAG_CAN_CHANGE_VIS | FLAG_CAN_CLOSE
			| FLAG_CAN_CHANGE | FLAG_CAN_SPLIT;

	public static final ElementConfig ALL = new ElementConfig(FLAG_CAN_ALL);

	private final BitField mask;

	public ElementConfig(int mask) {
		this(new BitField(mask));
	}
	public ElementConfig(BitField mask) {
		this.mask = mask;
	}


	public boolean canScale() {
		return mask.isSet(FLAG_CAN_SCALE);
	}

	public boolean canMove() {
		return mask.isSet(FLAG_CAN_MOVE);
	}

	/**
	 * @return
	 */
	public boolean canChangeVis() {
		return mask.isSet(FLAG_CAN_CHANGE_VIS);
	}

	public boolean canClose() {
		return mask.isSet(FLAG_CAN_CLOSE);
	}


	public boolean canSplit(boolean dim) {
		return mask.isSet(dim ? FLAG_CAN_SPLIT_X : FLAG_CAN_SPLIT_Y);
	}


	public boolean canChange(boolean dim) {
		return mask.isSet(dim ? FLAG_CAN_CHANGE_X : FLAG_CAN_CHANGE_Y);
	}
}
