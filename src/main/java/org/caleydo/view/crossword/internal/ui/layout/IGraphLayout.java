/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.layout;

import java.util.List;
import java.util.Set;

/**
 * @author Samuel Gratzl
 *
 */
public interface IGraphLayout {
	/**
	 * performs the layouting
	 *
	 * @param vertices
	 * @return whether another layouting round is needed
	 */
	boolean doLayout(List<? extends IGraphVertex> vertices, Set<? extends IGraphEdge> edges);
}
