/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;

/**
 * @author Samuel Gratzl
 *
 */
public enum EDirection {
	ABOVE, LEFT_OF, BELOW, RIGHT_OF;

	public EDimension asDim() {
		return EDimension.get(isHorizontal());
	}

	/**
	 * @return
	 */
	public boolean isHorizontal() {
		return this == LEFT_OF || this == RIGHT_OF;
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

}
