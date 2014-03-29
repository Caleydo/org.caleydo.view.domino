package org.caleydo.view.rnb.internal.undo;

import java.util.Collection;

import org.caleydo.view.rnb.internal.Block;
import org.caleydo.view.rnb.internal.Domino;


public class AddBlockCmd implements ICmd {
	private final Block block;

	public AddBlockCmd(Block block) {
		this.block = block;
	}

	public static ICmd multi(Collection<Block> nodes) {
		if (nodes.size() == 1)
			return new AddBlockCmd(nodes.iterator().next());

		ICmd[] r = new ICmd[nodes.size()];
		int i = 0;
		for (Block n : nodes)
			r[i++] = new AddBlockCmd(n);
		return CmdComposite.chain(r);
	}

	@Override
	public String getLabel() {
		return "Add Block: " + block;
	}

	@Override
	public ICmd run(Domino rnb) {
		rnb.addBlock(block);
		return new RemoveBlockCmd(block);
	}
}