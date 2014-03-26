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
public class RemoveRulerCmd implements ICmd {
	private final Ruler ruler;

	public RemoveRulerCmd(Ruler ruler) {
		this.ruler = ruler;
	}

	@Override
	public String getLabel() {
		return "Remove Ruler: " + ruler;
	}

	@Override
	public ICmd run(RnB rnb) {
		rnb.removeRuler(ruler);
		return new AddRulerCmd(ruler);
	}

}
