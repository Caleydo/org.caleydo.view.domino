/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import gleem.linalg.Vec2f;

import java.util.Set;

import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public class MoveBlockCmd implements ICmd {
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
	public ICmd run(Domino domino) {
		domino.moveBlocks(blocks, shift);
		return new MoveBlockCmd(blocks, shift.times(-1));
	}

}
