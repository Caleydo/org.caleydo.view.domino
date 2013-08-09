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
public class SharedBandEdge extends ABandEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6400784633888705942L;

	/**
	 * @param sHor
	 * @param tHor
	 */
	public SharedBandEdge(boolean sHor, boolean tHor) {
		super(sHor, tHor, new Color(0.9f, 0.9f, 0.9f, 0.5f));
	}

}
