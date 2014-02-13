/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public class ExplodeSlicesCmd implements ICmd {
	private Block block;
	private EDimension dim;

	public ExplodeSlicesCmd(Block block, EDimension dim) {
		this.block = block;
		this.dim = dim;
	}

	@Override
	public ICmd run(Domino domino) {
		List<Block> blocks = domino.explode(block, dim);
		return new CombineSlicesCmd(blocks, dim);
	}

	@Override
	public String getLabel() {
		return "Explode Block";
	}

}
