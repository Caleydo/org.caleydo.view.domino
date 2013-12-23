/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.view.domino.spi.model.graph.INode;


/**
 * @author Samuel Gratzl
 *
 */
public abstract class ALinearEdge extends AEdge {
	private static final long serialVersionUID = 7503595941233324714L;
	private final EDirection direction;

	public ALinearEdge(EDirection direction) {
		this.direction = direction;
	}

	@Override
	public EDirection getDirection(INode source) {
		return getSource() == source ? getDirection() : getDirection().opposite();
	}

	public EDirection getDirection() {
		return direction;
	}

}
