/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.jgrapht.graph.DefaultEdge;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AEdge extends DefaultEdge implements IEdge {
	private static final long serialVersionUID = 5377583849522419114L;
	private final EDirection direction;

	public AEdge(EDirection direction) {
		this.direction = direction;
	}

	@Override
	public EDirection getDirection() {
		return direction;
	}

	@Override
	public String toString() {
		return getSource() + "->(" + direction + ")->" + getTarget();
	}

}
