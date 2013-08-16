/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.layout;

import org.caleydo.view.crossword.internal.ui.CrosswordElement;

/**
 * @author Samuel Gratzl
 *
 */
public interface IGraphEdge {
	CrosswordElement getSource();

	CrosswordElement getTarget();

	void relayout();
}
