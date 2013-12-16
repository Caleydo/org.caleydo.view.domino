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
public class BandEdge extends AEdge implements ISortBarrier {
	private static final long serialVersionUID = 418966282805280479L;
	private EDimension sourceDir;
	private EDimension targetDir;

	public BandEdge(EDimension sourceDir, EDimension targetDir) {
		this.sourceDir = sourceDir;
		this.targetDir = targetDir;
	}

	@Override
	public EProximityMode asMode() {
		return EProximityMode.FREE;
	}

	@Override
	public EDirection getDirection(INode of) {
		return EDirection.getPrimary(getDimension(of));
	}

	private EDimension getDimension(INode of) {
		if (getSource() == of)
			return sourceDir;
		else
			return targetDir;
	}

	@Override
	public void swapDirection(INode of) {
		if (getSource() == of)
			sourceDir = sourceDir.opposite();
		else
			targetDir = targetDir.opposite();
	}

}
