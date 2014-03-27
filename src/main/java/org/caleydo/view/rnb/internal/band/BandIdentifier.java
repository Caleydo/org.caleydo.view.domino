/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.band;

import java.util.Objects;

import org.caleydo.view.rnb.internal.LinearBlock;

/**
 * @author Samuel Gratzl
 *
 */
public class BandIdentifier extends ABandIdentifier {
	private final LinearBlock bS, bT;
	private final String visTypeS, visTypeT;

	public static BandIdentifier id(LinearBlock bS, boolean leftS, LinearBlock bT, boolean leftT) {
		return new BandIdentifier(bS, leftS, bT, leftT);
	}

	public BandIdentifier(LinearBlock bS, boolean leftS, LinearBlock bT, boolean leftT) {
		super(leftS, leftT);
		this.bS = bS;
		this.bT = bT;
		this.visTypeS = bS.getNode(leftS).getVisualizationType(true);
		this.visTypeT = bT.getNode(leftT).getVisualizationType(true);
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
		result = prime * result + ((visTypeS == null) ? 0 : visTypeS.hashCode());
		result = prime * result + ((visTypeT == null) ? 0 : visTypeT.hashCode());
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
		if (!Objects.equals(visTypeS, other.visTypeS) || !Objects.equals(visTypeT, other.visTypeT))
			return false;
		return bS == other.bS && bT == other.bT;
	}

	/**
	 * @return
	 */
	@Override
	public BandIdentifier swap() {
		return id(bT, leftT, bS, leftS);
	}

	/**
	 * @param b
	 * @param c
	 */
	@Override
	public BandIdentifier with(boolean leftS, boolean leftT) {
		return id(bS, leftS, bT, leftT);
	}
}
