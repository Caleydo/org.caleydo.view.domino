/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;

/**
 * @author Samuel Gratzl
 *
 */
public class Nodes {

	/**
	 * @param tablePerspective
	 * @return
	 */
	public static INode create(TablePerspective t) {
		ATableBasedDataDomain dataDomain = t.getDataDomain();
		if (DataSupportDefinitions.categoricalTables.apply(dataDomain))
			return new CategoricalData2DNode(dataDomain);
		if (DataSupportDefinitions.numericalTables.apply(dataDomain))
			return new NumericalData2DNode(dataDomain);
		if (DataSupportDefinitions.categoricalColumns.apply(t))
			return new CategoricalData1DNode(t, EDimension.RECORD);
		if (DataSupportDefinitions.homogenousColumns.apply(t))
			return new NumericalData1DNode(t, EDimension.RECORD);
		return null;
	}

}
