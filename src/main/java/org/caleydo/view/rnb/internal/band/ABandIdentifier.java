/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.band;


/**
 * @author Samuel Gratzl
 *
 */
public abstract class ABandIdentifier {
	protected final boolean leftS, leftT;

	public ABandIdentifier(boolean leftS, boolean leftT) {
		this.leftS = leftS;
		this.leftT = leftT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (leftS ? 1231 : 1237);
		result = prime * result + (leftT ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ABandIdentifier other = (ABandIdentifier) obj;
		if (leftS != other.leftS)
			return false;
		if (leftT != other.leftT)
			return false;
		return true;
	}

	public abstract ABandIdentifier swap();

	public abstract ABandIdentifier with(boolean leftS, boolean leftT);
}
