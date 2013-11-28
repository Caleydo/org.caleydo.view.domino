/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.graph;

import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class Placeholder {

	private final EDirection dir;
	private final INode node;

	public Placeholder(INode v, EDirection dir) {
		this.node = v;
		this.dir = dir;
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

}
