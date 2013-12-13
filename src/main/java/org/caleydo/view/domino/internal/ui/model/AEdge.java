/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.model;

import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.jgrapht.graph.DefaultEdge;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AEdge extends DefaultEdge implements IEdge, Cloneable {
	private static final long serialVersionUID = 5377583849522419114L;

	@Override
	public INode getSource() {
		return (INode) super.getSource();
	}

	@Override
	public INode getOpposite(INode node) {
		return node == getSource() ? getTarget() : getSource();
	}


	@Override
	public INode getTarget() {
		return (INode) super.getTarget();
	}

	@Override
	public String toString() {
		return getSource() + "->(" + getDirection(getSource()) + ")->" + getTarget();
	}
}
