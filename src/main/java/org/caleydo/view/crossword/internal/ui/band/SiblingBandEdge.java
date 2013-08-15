/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.band;

import org.caleydo.core.util.color.Color;

/**
 * @author Samuel Gratzl
 *
 */
public class SiblingBandEdge extends ABandEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8802455065322429993L;

	/**
	 * @param sHor
	 * @param tHor
	 */
	public SiblingBandEdge(boolean sHor, boolean tHor) {
		super(sHor, tHor, new Color(0, 0.9f, 0f, 0.5f));
	}

}
