/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.ui.layout;

import java.util.Set;


/**
 * @author Samuel Gratzl
 *
 */
public interface IGraphEdge {

	IGraphVertex getSource();

	IGraphVertex getTarget();

	IVertexConnector getSourceConnector();

	IVertexConnector getTargetConnector();

	EEdgeType getType();

	Set<Integer> getIntersection();
}
