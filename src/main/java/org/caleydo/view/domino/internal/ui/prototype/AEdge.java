/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.jgrapht.graph.DefaultEdge;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AEdge extends DefaultEdge implements IEdge, Cloneable {
	private static final long serialVersionUID = 5377583849522419114L;
	private EDirection direction;
	private boolean swapped = false;

	public AEdge(EDirection direction) {
		this.direction = direction;
	}

	@Override
	public EDirection getDirection() {
		return swapped ? direction.opposite() : direction;
	}

	@Override
	public void transpose() {
		this.direction = direction.rot90();
		if (!this.getDirection().isPrimaryDirection()) {
			swapped = !swapped;
		}
	}

	@Override
	public INode getSource() {
		return get(!swapped);
	}

	private INode get(boolean source) {
		return source ? getRawSource() : getRawTarget();
	}

	@Override
	public INode getRawSource() {
		return (INode) super.getSource();
	}

	@Override
	public INode getTarget() {
		return get(swapped);
	}

	@Override
	public INode getRawTarget() {
		return (INode) super.getTarget();
	}

	@Override
	public IEdge reverse() {
		AEdge clone = (AEdge) clone();
		clone.swapped = true;
		return clone;
	}

	@Override
	public String toString() {
		return getSource() + "->(" + getDirection() + ")->" + getTarget();
	}

	/**
	 * mapping function to unify edges, such that the given vertex will be the source
	 *
	 * @param vertex
	 * @return
	 */
	public static Function<IEdge, IEdge> unify(final INode vertex) {
		return new Function<IEdge, IEdge>() {
			@Override
			public IEdge apply(IEdge input) {
				if (input == null)
					return null;
				if (vertex == input.getSource())
					return input;
				return input.reverse();
			}
		};
	}

}
