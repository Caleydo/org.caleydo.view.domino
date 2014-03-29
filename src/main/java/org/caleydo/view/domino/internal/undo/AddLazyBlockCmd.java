package org.caleydo.view.domino.internal.undo;

import gleem.linalg.Vec2f;

import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;


public class AddLazyBlockCmd implements ICmd {
	private final Node node;
	private final Vec2f loc;

	public AddLazyBlockCmd(Node node, Vec2f loc) {
		this.node = node;
		this.loc = loc;
	}

	@Override
	public String getLabel() {
		return "Add Block: " + node.getLabel();
	}

	@Override
	public ICmd run(Domino rnb) {
		Block b = new Block(node);
		b.setLocation(loc.x(), loc.y());
		rnb.addBlock(b);
		return new RemoveBlockCmd(b);
	}
}