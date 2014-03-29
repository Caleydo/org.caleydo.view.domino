/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import gleem.linalg.Vec2f;

import org.caleydo.view.rnb.internal.Domino;
import org.caleydo.view.rnb.internal.ui.AItem;

/**
 * @author Samuel Gratzl
 *
 */
public class MoveItemCmd implements IMergeAbleCmd {
	private final AItem item;
	private final Vec2f shift;

	public MoveItemCmd(AItem item, Vec2f shift) {
		this.item = item;
		this.shift = shift;
	}

	@Override
	public String getLabel() {
		return "Move " + item.getClass().getSimpleName();
	}

	@Override
	public ICmd run(Domino rnb) {
		item.shiftLocation(shift);
		rnb.getBands().relayout();
		return new MoveItemCmd(item, shift.times(-1));
	}

	@Override
	public boolean merge(ICmd cmd) {
		if (cmd instanceof MoveItemCmd) {
			final MoveItemCmd m = (MoveItemCmd) cmd;
			if (m.item.equals(item)) {
				shift.add(m.shift);
				return true;
			}
		}
		return false;
	}

}
