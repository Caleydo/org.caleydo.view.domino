/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;


/**
 * @author Samuel Gratzl
 *
 */
public interface IEdge {
	EDirection getDirection();

	INode getSource();

	INode getTarget();
	/**
	 *
	 */
	void transpose();

	/**
	 * @return
	 */
	IEdge reverse();

	/**
	 * @return
	 */
	INode getRawSource();

	/**
	 * @return
	 */
	INode getRawTarget();
}