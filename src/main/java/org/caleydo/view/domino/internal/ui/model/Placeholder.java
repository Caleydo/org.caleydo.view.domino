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
public class Placeholder {

	private final EDirection dir;
	private final INode node;
	private final boolean transposed;

	public Placeholder(INode v, EDirection dir, boolean transposed) {
		this.node = v;
		this.dir = dir;
		this.transposed = transposed;
	}

	/**
	 * @return the transpose, see {@link #transposed}
	 */
	public boolean isTransposed() {
		return transposed;
	}

	/**
	 * @return the node, see {@link #node}
	 */
	public INode getNode() {
		return node;
	}

	/**
	 * @return the dir, see {@link #dir}
	 */
	public EDirection getDir() {
		return dir;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Placeholder [node=");
		builder.append(node);
		builder.append(", dir=");
		builder.append(dir);
		builder.append(", transpose=");
		builder.append(transposed);
		builder.append("]");
		return builder.toString();
	}

}
