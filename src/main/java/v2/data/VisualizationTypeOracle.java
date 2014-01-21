/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import org.caleydo.core.view.opengl.layout2.manage.EVisScaleType;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementMetaData;

/**
 * @author Samuel Gratzl
 *
 */
public class VisualizationTypeOracle {

	public static boolean stratifyByDefault(String visualizationType) {
		IGLElementMetaData metaData = GLElementFactories.getMetaData(visualizationType);
		return metaData != null && metaData.getScaleType() == EVisScaleType.DATA_DEPENDENT;
	}
}
