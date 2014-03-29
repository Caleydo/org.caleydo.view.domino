/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import java.util.Set;

import org.caleydo.view.rnb.internal.Block;
import org.caleydo.view.rnb.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public class TransposeBlocksCmd implements ICmd {
	private final Set<Block> blocks;

	public TransposeBlocksCmd(Set<Block> blocks) {
		this.blocks = blocks;
	}

	@Override
	public String getLabel() {
		return "Transpose";
	}

	@Override
	public ICmd run(Domino rnb) {
		for (Block b : blocks)
			b.transpose();
		return this;
	}

}
