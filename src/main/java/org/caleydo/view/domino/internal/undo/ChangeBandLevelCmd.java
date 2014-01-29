/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public class ChangeBandLevelCmd implements ICmd {

	private final String bandIdentifier;
	private final boolean increase;

	/**
	 * @param identifier
	 * @param b
	 */
	public ChangeBandLevelCmd(String identifier, boolean increase) {
		this.bandIdentifier = identifier;
		this.increase = increase;

	}

	@Override
	public String getLabel() {
		return "Change Band Details";
	}

	@Override
	public ICmd run(Domino domino) {
		domino.getBands().changeLevel(bandIdentifier, increase);
		return new ChangeBandLevelCmd(bandIdentifier, !increase);
	}

}
