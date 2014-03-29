/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.Ruler;

/**
 * @author Samuel Gratzl
 *
 */
public class TransposeRulerCmd implements ICmd {
	private final Ruler ruler;

	public TransposeRulerCmd(Ruler ruler) {
		this.ruler = ruler;
	}

	@Override
	public String getLabel() {
		return "Transpose";
	}

	@Override
	public ICmd run(Domino domino) {
		ruler.transpose();
		domino.getBands().relayout();
		return this;
	}

}
