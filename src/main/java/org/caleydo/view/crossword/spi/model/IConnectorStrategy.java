/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.spi.model;

import java.util.Set;

import org.caleydo.view.crossword.api.model.CenterRadius;
import org.caleydo.view.crossword.api.ui.layout.IVertexConnector;

/**
 * a connector strategy computes for a given set of ids and the given overlap how to attach that to the vertex, i.e. it
 * implements the logic behind the {@link IVertexConnector}
 *
 * @author Samuel Gratzl
 *
 */
public interface IConnectorStrategy {
	/**
	 * @param ids
	 * @param intersection
	 * @return the center and radius attaching the vertex
	 */
	CenterRadius update(Set<Integer> ids, Set<Integer> intersection);

}
