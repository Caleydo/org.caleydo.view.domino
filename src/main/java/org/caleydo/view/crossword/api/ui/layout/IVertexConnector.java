/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.ui.layout;

import org.caleydo.core.data.collection.EDimension;

/**
 * a vertex connector is the connection of a {@link IGraphEdge} to a {@link IGraphVertex}
 *
 * @author Samuel Gratzl
 *
 */
public interface IVertexConnector {
	/**
	 *
	 * @return the type of connector whether in record or column dimension
	 */
	EDimension getDimension();

	/**
	 * in percent
	 *
	 * @return
	 */
	float getRadius();

	/**
	 * in percent
	 *
	 * @return
	 */
	float getCenter();
}