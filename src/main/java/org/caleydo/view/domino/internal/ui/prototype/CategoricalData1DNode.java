/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;

/**
 * @author Samuel Gratzl
 *
 */
public class CategoricalData1DNode extends Data1DNode {

	/**
	 * @param data
	 */
	public CategoricalData1DNode(TablePerspective data) {
		super(data);
		assert DataSupportDefinitions.categoricalColumns.apply(data);
	}

}
