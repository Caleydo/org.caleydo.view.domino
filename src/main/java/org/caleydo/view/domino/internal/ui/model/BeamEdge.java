/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.model;

import org.caleydo.view.domino.internal.ui.prototype.EDirection;


/**
 * @author Samuel Gratzl
 *
 */
public class BeamEdge extends ALinearEdge implements IStratificationBarrier {
	private static final long serialVersionUID = 418966282805280479L;

	public BeamEdge(EDirection direction) {
		super(direction);
	}

}