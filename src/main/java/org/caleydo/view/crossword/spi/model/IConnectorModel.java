/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.spi.model;

import java.util.Set;

import org.caleydo.view.crossword.api.model.CenterRadius;

/**
 * @author Samuel Gratzl
 *
 */
public interface IConnectorModel {
	/**
	 * @param ids
	 * @param intersection
	 * @return
	 */
	CenterRadius update(Set<Integer> ids, Set<Integer> intersection);

}
