/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import gleem.linalg.Vec2f;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.Separator;

/**
 * @author Samuel Gratzl
 *
 */
public class MoveSeparatorCmd implements IMergeAbleCmd {
	private final Separator separator;
	private final Vec2f shift;

	public MoveSeparatorCmd(Separator separator, Vec2f shift) {
		this.separator = separator;
		this.shift = shift;
	}

	@Override
	public String getLabel() {
		return "Move Separator";
	}

	@Override
	public ICmd run(Domino domino) {
		separator.shiftLocation(shift);
		domino.getBands().relayout();
		return new MoveSeparatorCmd(separator, shift.times(-1));
	}

	@Override
	public boolean merge(ICmd cmd) {
		if (cmd instanceof MoveSeparatorCmd) {
			final MoveSeparatorCmd m = (MoveSeparatorCmd) cmd;
			if (m.separator.equals(separator)) {
				shift.add(m.shift);
				return true;
			}
		}
		return false;
	}

}
