/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.graph;

import org.caleydo.view.domino.internal.ui.prototype.EDirection;


/**
 *
 * @author Samuel Gratzl
 * 
 */
public class BandEdge extends AEdge implements ISortBarrier {
	private static final long serialVersionUID = 418966282805280479L;

	public BandEdge(EDirection direction) {
		super(direction);
	}

}
