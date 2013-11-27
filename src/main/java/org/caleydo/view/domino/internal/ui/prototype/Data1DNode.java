/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class Data1DNode implements INode {
	private final TablePerspective data;
	private final TypedSet rec;
	private final TypedSet dim;

	public Data1DNode(TablePerspective data) {
		this.data = data;
		this.rec = TypedSet.of(data.getRecordPerspective().getVirtualArray());
		this.dim = TypedSet.of(data.getDimensionPerspective().getVirtualArray());
	}

	/**
	 * @return the data, see {@link #data}
	 */
	public TablePerspective getData() {
		return data;
	}

	@Override
	public TypedSet getData(EDimension dim) {
		switch(dim) {
		case DIMENSION:
			return this.dim;
		case RECORD:
			return this.rec;
		}
		throw new IllegalStateException();
	}
}
