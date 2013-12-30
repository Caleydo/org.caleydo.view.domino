/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.spi.model.graph;

import java.util.Collection;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.api.model.graph.ISortableNode;

/**
 * @author Samuel Gratzl
 *
 */
public interface IDominoGraphListener {
	void vertexAdded(INode vertex, Collection<IEdge> edges);

	void vertexRemoved(INode vertex, Collection<IEdge> edges);

	void vertexSortingChanged(ISortableNode vertex, EDimension dim, boolean stratisfied);
}