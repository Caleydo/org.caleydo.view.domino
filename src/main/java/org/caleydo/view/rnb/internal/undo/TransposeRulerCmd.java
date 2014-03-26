/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.ui.Ruler;

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
	public ICmd run(RnB rnb) {
		ruler.transpose();
		rnb.getBands().relayout();
		return this;
	}

}
