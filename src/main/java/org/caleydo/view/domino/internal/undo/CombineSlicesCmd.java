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
public class CombineSlicesCmd implements ICmd {
	private List<Block> blocks;
	private EDimension dim;

	public CombineSlicesCmd(List<Block> blocks, EDimension dim) {
		this.blocks = blocks;
		this.dim = dim;
	}

	@Override
	public ICmd run(Domino domino) {
		Block block = domino.combine(blocks, dim);
		return new ExplodeSlicesCmd(block, dim);
	}

	@Override
	public String getLabel() {
		return "Combine Block";
	}

}
