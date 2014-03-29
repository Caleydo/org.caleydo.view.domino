/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.AItem;

/**
 * @author Samuel Gratzl
 *
 */
public class TransposeItemCmd implements ICmd {
	private final AItem item;

	public TransposeItemCmd(AItem item) {
		this.item = item;
	}

	@Override
	public String getLabel() {
		return "Transpose";
	}

	@Override
	public ICmd run(Domino rnb) {
		item.transpose();
		rnb.getBands().relayout();
		return this;
	}

}
