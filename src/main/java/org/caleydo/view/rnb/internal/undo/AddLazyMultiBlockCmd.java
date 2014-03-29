package org.caleydo.view.rnb.internal.undo;

import gleem.linalg.Vec2f;

import java.util.Set;

import org.caleydo.view.rnb.api.model.EDirection;
import org.caleydo.view.rnb.internal.Block;
import org.caleydo.view.rnb.internal.Domino;
import org.caleydo.view.rnb.internal.Node;
import org.caleydo.view.rnb.internal.NodeGroup;


public class AddLazyMultiBlockCmd implements ICmd {
	private final Node node;
	private final Vec2f loc;
	private final NodeGroup act;
	private final Set<NodeGroup> items;

	public AddLazyMultiBlockCmd(Node node, Vec2f loc, NodeGroup act, Set<NodeGroup> items) {
		this.node = node;
		this.loc = loc;
		this.act = act;
		this.items = items;
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
		rebuild(b, node, act, items, null);
		return new RemoveBlockCmd(b);
	}

	private void rebuild(Block b, Node asNode, NodeGroup act, Set<NodeGroup> items, EDirection commingFrom) {
		items.remove(act);
		for (EDirection dir : EDirection.values()) {
			if (dir == commingFrom)
				continue;
			NodeGroup next = act.findNeigbhor(dir, items);
			if (next == null)
				continue;
			Node nextNode = next.toNode();
			b.addNode(asNode, dir, nextNode);
			rebuild(b, nextNode, next, items, dir);
		}
	}
}