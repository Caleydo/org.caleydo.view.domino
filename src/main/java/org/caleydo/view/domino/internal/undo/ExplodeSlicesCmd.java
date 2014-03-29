/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import java.util.Collection;
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

	public static ICmd multi(Collection<Block> nodes, EDimension dim) {
		if (nodes.size() == 1)
			return new ExplodeSlicesCmd(nodes.iterator().next(), dim);

		ICmd[] r = new ICmd[nodes.size()];
		int i = 0;
		for (Block n : nodes)
			r[i++] = new ExplodeSlicesCmd(n, dim);
		return CmdComposite.chain(r);
	}

	@Override
	public ICmd run(Domino rnb) {
		List<Block> blocks = rnb.explode(block, dim);
		return CmdComposite.chain(RemoveBlockCmd.multi(blocks), new AddBlockCmd(block));
	}

	@Override
	public String getLabel() {
		return "Explode Block";
	}

}
