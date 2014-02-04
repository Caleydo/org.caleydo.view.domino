/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.util.gleem.IColored;

/**
 * @author Samuel Gratzl
 *
 */
public enum EBandMode implements IColored {
	OVERVIEW, GROUPS, DETAIL;

	@Override
	public Color getColor() {
		switch (this) {
		case OVERVIEW:
			return new Color(0, 0, 0, 0.08f);
		case GROUPS:
			return new Color(0, 0, 0, 0.1f);
		case DETAIL:
			return new Color(0, 0, 0, 0.4f);
		}
		throw new IllegalStateException();
	}

	/**
	 * @return
	 */
	public static float alpha(int nrItems) {
		float alpha = Math.min((float) (6 / Math.sqrt(nrItems)), 0.8f);
		return alpha;
	}
}
