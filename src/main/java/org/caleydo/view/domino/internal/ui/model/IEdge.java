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
public interface IEdge {
	EDirection getDirection(INode of);

	INode getOpposite(INode node);

	/**
	 * @return
	 */
	INode getSource();

	/**
	 * @return
	 */
	INode getTarget();

	EProximityMode asMode();
}
