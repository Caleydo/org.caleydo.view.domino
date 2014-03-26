/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.band;

import org.caleydo.view.rnb.internal.Node;

/**
 * @author Samuel Gratzl
 *
 */
public class MediumBandIdentifier extends ABandIdentifier {
	private final Node bS, bT;

	public static MediumBandIdentifier id(Node bS, boolean leftS, Node bT, boolean leftT) {
		return new MediumBandIdentifier(bS, leftS, bT, leftT);
	}

	public MediumBandIdentifier(Node bS, boolean leftS, Node bT, boolean leftT) {
		super(leftS, leftT);
		this.bS = bS;
		this.bT = bT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bS == null) ? 0 : bS.hashCode());
		result = prime * result + (leftS ? 1231 : 1237);
		result = prime * result + (leftT ? 1231 : 1237);
		result = prime * result + ((bT == null) ? 0 : bT.hashCode());
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
		MediumBandIdentifier other = (MediumBandIdentifier) obj;
		if (leftS != other.leftS)
			return false;
		if (leftT != other.leftT)
			return false;
		return bS == other.bS && bT == other.bT;
	}

	/**
	 * @return
	 */
	public MediumBandIdentifier swap() {
		return new MediumBandIdentifier(bT, leftT, bS, leftS);
	}

	/**
	 * @param b
	 * @param c
	 */
	public MediumBandIdentifier with(boolean leftS, boolean leftT) {
		return new MediumBandIdentifier(bS, leftS, bT, leftT);
	}
}
