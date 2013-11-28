/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AData2DNode extends ADataNode {
	private final ATableBasedDataDomain data;

	public AData2DNode(ATableBasedDataDomain data) {
		this(data.getDefaultTablePerspective());
	}

	/**
	 * @param defaultTablePerspective
	 */
	public AData2DNode(TablePerspective t) {
		super(TypedSet.of(t.getDimensionPerspective().getVirtualArray()), TypedSet.of(t.getRecordPerspective()
				.getVirtualArray()));
		this.data = t.getDataDomain();
	}

	/**
	 * @param clone
	 */
	public AData2DNode(AData2DNode clone) {
		super(clone);
		this.data = clone.data;
	}

	@Override
	public final String getLabel() {
		return data.getLabel();
	}

	public ATableBasedDataDomain getDataDomain() {
		return data;
	}
}
