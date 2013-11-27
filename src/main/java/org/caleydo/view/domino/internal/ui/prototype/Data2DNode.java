/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class Data2DNode implements INode {
	private final ATableBasedDataDomain data;
	private final TypedSet rec;
	private final TypedSet dim;

	public Data2DNode(ATableBasedDataDomain data) {
		this.data = data;
		TablePerspective t = data.getDefaultTablePerspective();
		this.rec = TypedSet.of(t.getRecordPerspective().getVirtualArray());
		this.dim = TypedSet.of(t.getDimensionPerspective().getVirtualArray());
	}

	public ATableBasedDataDomain getData() {
		return data;
	}

	/**
	 * @return the ids, see {@link #ids}
	 */
	@Override
	public TypedSet getData(EDimension dim) {
		return dim.select(this.dim, this.rec);
	}
}
