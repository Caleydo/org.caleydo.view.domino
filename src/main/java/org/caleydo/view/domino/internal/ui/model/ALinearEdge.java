/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.model;

import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class ALinearEdge extends AEdge {
	private EDirection direction;

	public ALinearEdge(EDirection direction) {
		this.direction = direction;
	}

	@Override
	public EDirection getDirection(INode source) {
		return getSource() == source ? getDirection() : getDirection().opposite();
	}

	@Override
	public void swapDirection(INode to) {
		this.direction = direction.opposite();
	}

	public EDirection getDirection() {
		return direction;
	}

}
