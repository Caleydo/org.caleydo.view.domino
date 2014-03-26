/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import java.util.Collection;

import org.caleydo.view.rnb.internal.Block;
import org.caleydo.view.rnb.internal.RnB;

/**
 * @author Samuel Gratzl
 *
 */
public class RemoveBlockCmd implements ICmd {
	private final Block block;

	public RemoveBlockCmd(Block block) {
		this.block = block;
	}

	public static ICmd multi(Collection<Block> nodes) {
		if (nodes.size() == 1)
			return new RemoveBlockCmd(nodes.iterator().next());

		ICmd[] r = new ICmd[nodes.size()];
		int i = 0;
		for (Block n : nodes)
			r[i++] = new RemoveBlockCmd(n);
		return CmdComposite.chain(r);
	}

	@Override
	public String getLabel() {
		return "Remove Block: " + block;
	}

	@Override
	public ICmd run(RnB domino) {
		domino.removeBlock(block);
		return new AddBlockCmd(block);
	}

}
