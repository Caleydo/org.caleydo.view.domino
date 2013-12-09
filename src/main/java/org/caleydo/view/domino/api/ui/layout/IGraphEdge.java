/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.ui.layout;

import org.caleydo.view.domino.api.model.typed.TypedSet;


/**
 * a representation of a edge within the element graph
 *
 * @author Samuel Gratzl
 *
 */
public interface IGraphEdge {

	IGraphVertex getSource();

	IGraphVertex getTarget();

	IVertexConnector getSourceConnector();

	IVertexConnector getTargetConnector();

	EEdgeType getType();

	/**
	 * return the shared ids
	 *
	 * @return
	 */
	TypedSet getIntersection();
}
