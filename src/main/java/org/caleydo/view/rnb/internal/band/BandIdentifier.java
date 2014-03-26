/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.band;

import org.caleydo.view.rnb.internal.LinearBlock;

/**
 * @author Samuel Gratzl
 *
 */
public class BandIdentifier extends ABandIdentifier {
	private final LinearBlock bS, bT;

	public static BandIdentifier id(LinearBlock bS, boolean leftS, LinearBlock bT, boolean leftT) {
		return new BandIdentifier(bS, leftS, bT, leftT);
	}

	public BandIdentifier(LinearBlock bS, boolean leftS, LinearBlock bT, boolean leftT) {
		super(leftS, leftT);
		this.bS = bS;
		this.bT = bT;
	}

	/**
	 *
	 */
	public void updateBandInfo() {
		if (leftS)
			bS.setHasRightBand(true);
		else
			bS.setHasLeftBand(true);

		if (leftT)
			bT.setHasRightBand(true);
		else
			bT.setHasLeftBand(true);
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
		BandIdentifier other = (BandIdentifier) obj;
		if (leftS != other.leftS)
			return false;
		if (leftT != other.leftT)
			return false;
		return bS == other.bS && bT == other.bT;
	}

	/**
	 * @return
	 */
	public BandIdentifier swap() {
		return new BandIdentifier(bT, leftT, bS, leftS);
	}

	/**
	 * @param b
	 * @param c
	 */
	public BandIdentifier with(boolean leftS, boolean leftT) {
		return new BandIdentifier(bS, leftS, bT, leftT);
	}
}
