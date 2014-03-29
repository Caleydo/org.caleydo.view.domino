/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import gleem.linalg.Vec2f;

import org.caleydo.core.id.IDCategory;
import org.caleydo.view.rnb.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public class MoveRulerCmd implements IMergeAbleCmd {
	private final IDCategory category;
	private final Vec2f shift;

	public MoveRulerCmd(IDCategory category, Vec2f shift) {
		this.category = category;
		this.shift = shift;
	}

	@Override
	public String getLabel() {
		return "Move Ruler";
	}

	@Override
	public ICmd run(Domino rnb) {
		rnb.moveRuler(category, shift);
		return new MoveRulerCmd(category, shift.times(-1));
	}

	@Override
	public boolean merge(ICmd cmd) {
		if (cmd instanceof MoveRulerCmd) {
			final MoveRulerCmd m = (MoveRulerCmd) cmd;
			if (m.category.equals(category)) {
				shift.add(m.shift);
				return true;
			}
		}
		return false;
	}

}
