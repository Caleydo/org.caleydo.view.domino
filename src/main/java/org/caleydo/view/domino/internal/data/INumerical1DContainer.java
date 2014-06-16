/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

/**
 * @author Samuel Gratzl
 *
 */
public interface INumerical1DContainer extends IDataValues {

	/**
	 * @param id
	 * @return
	 */
	Float getRaw(int id);

	/**
	 * @param id
	 * @return
	 */
	float getNormalized(int id);

}
