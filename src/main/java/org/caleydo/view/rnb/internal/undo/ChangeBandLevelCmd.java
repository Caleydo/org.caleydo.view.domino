/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.internal.Domino;
import org.caleydo.view.rnb.internal.band.ABandIdentifier;

/**
 * @author Samuel Gratzl
 *
 */
public class ChangeBandLevelCmd implements ICmd {

	private final ABandIdentifier bandIdentifier;
	private final boolean increase;

	/**
	 * @param identifier
	 * @param b
	 */
	public ChangeBandLevelCmd(ABandIdentifier identifier, boolean increase) {
		this.bandIdentifier = identifier;
		this.increase = increase;

	}

	@Override
	public String getLabel() {
		return "Change Band Details";
	}

	@Override
	public ICmd run(Domino rnb) {
		rnb.getBands().changeLevel(bandIdentifier, increase);
		return new ChangeBandLevelCmd(bandIdentifier, !increase);
	}

}
