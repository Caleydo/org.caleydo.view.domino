/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.spi.config;

import org.caleydo.view.domino.api.ui.layout.IGraphVertex;


/**
 * @author Samuel Gratzl
 *
 */
public class MultiConfig {

	/**
	 * @return
	 */
	public ElementConfig getDefaultElementConfig() {
		return ElementConfig.ALL;
	}

	public ElementConfig getSplittedElementConfig(IGraphVertex parent) {
		return getDefaultElementConfig();
	}
}
