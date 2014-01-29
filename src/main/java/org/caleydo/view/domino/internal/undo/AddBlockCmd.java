package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Domino;


public class AddBlockCmd implements ICmd {
	private final Block block;

	public AddBlockCmd(Block block) {
		this.block = block;
	}

	@Override
	public String getLabel() {
		return "Add Block: " + block;
	}

	@Override
	public ICmd run(Domino domino) {
		domino.addBlock(block);
		return new RemoveBlockCmd(block);
	}
}