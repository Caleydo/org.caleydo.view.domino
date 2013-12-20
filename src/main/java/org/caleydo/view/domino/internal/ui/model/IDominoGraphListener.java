/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.model;

import java.util.Collection;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.caleydo.view.domino.internal.ui.prototype.IStratisfyingableNode;

/**
 * @author Samuel Gratzl
 *
 */
public interface IDominoGraphListener {
	void vertexAdded(INode vertex, Collection<IEdge> edges);

	void vertexRemoved(INode vertex, Collection<IEdge> edges);

	void vertexSortingChanged(ISortableNode vertex, EDimension dim);

	void vertexStratificationChanged(IStratisfyingableNode vertex, EDimension dim);
}
