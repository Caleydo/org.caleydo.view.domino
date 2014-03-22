/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.Separator;

/**
 * @author Samuel Gratzl
 *
 */
public class TransposeSeparatorCmd implements ICmd {
	private final Separator separator;

	public TransposeSeparatorCmd(Separator separator) {
		this.separator = separator;
	}

	@Override
	public String getLabel() {
		return "Transpose";
	}

	@Override
	public ICmd run(Domino domino) {
		separator.transpose();
		domino.getBands().relayout();
		return this;
	}

}
