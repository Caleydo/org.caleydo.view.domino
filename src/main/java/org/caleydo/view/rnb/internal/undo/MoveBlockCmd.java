/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import gleem.linalg.Vec2f;

import java.util.Set;

import org.caleydo.view.rnb.internal.Block;
import org.caleydo.view.rnb.internal.RnB;

/**
 * @author Samuel Gratzl
 *
 */
public class MoveBlockCmd implements IMergeAbleCmd {
	private final Set<Block> blocks;
	private final Vec2f shift;

	public MoveBlockCmd(Set<Block> blocks, Vec2f shift) {
		this.blocks = blocks;
		this.shift = shift;
	}

	@Override
	public String getLabel() {
		return "Move Block";
	}

	@Override
	public ICmd run(RnB rnb) {
		rnb.moveBlocks(blocks, shift);
		return new MoveBlockCmd(blocks, shift.times(-1));
	}

	@Override
	public boolean merge(ICmd cmd) {
		if (cmd instanceof MoveBlockCmd) {
			final MoveBlockCmd m = (MoveBlockCmd) cmd;
			if (m.blocks.equals(blocks)) {
				shift.add(m.shift);
				return true;
			}
		}
		return false;
	}

}
