/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.model;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.INode;


/**
 *
 * @author Samuel Gratzl
 *
 */
public class BandEdge extends AEdge implements ISortBarrier, IStratificationBarrier {
	private static final long serialVersionUID = 418966282805280479L;
	private final EDimension sourceDim;
	private final EDimension targetDim;

	public BandEdge(EDimension sourceDim, EDimension targetDim) {
		this.sourceDim = sourceDim;
		this.targetDim = targetDim;
	}

	@Override
	public EProximityMode asMode() {
		return EProximityMode.FREE;
	}

	@Override
	public EDirection getDirection(INode of) {
		final EDirection dir = EDirection.getPrimary(getDimension(of));
		return getSource() == of ? dir : dir.opposite();
	}

	public EDimension getDimension(INode of) {
		if (getSource() == of)
			return sourceDim;
		else
			return targetDim;
	}
}
