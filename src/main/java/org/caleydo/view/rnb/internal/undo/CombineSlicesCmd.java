/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.rnb.internal.Block;
import org.caleydo.view.rnb.internal.Domino;

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
	public ICmd run(Domino rnb) {
		Block block = rnb.combine(blocks, dim);
		return CmdComposite.chain(new RemoveBlockCmd(block), AddBlockCmd.multi(blocks));
	}

	@Override
	public String getLabel() {
		return "Combine Block";
	}

}
