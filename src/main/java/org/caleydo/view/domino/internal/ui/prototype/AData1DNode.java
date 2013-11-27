/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AData1DNode implements INode {
	private final TablePerspective data;
	private final TypedSet rec;
	private final TypedSet dim;
	private boolean transposed = false;

	public AData1DNode(TablePerspective data) {
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

	public ATableBasedDataDomain getDataDomain() {
		return getData().getDataDomain();
	}

	@Override
	public void transpose() {
		this.transposed = !this.transposed;
	}

	@Override
	public TypedSet getData(EDimension dim) {
		return dim.isVertical() == this.transposed ? this.dim : this.rec;
	}

	@Override
	public IDType getIDType(EDimension dim) {
		return getData(dim).getIdType();
	}

	@Override
	public int getSize(EDimension dim) {
		return getData(dim).size();
	}
}
