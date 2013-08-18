/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.model;

import java.util.Set;

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
