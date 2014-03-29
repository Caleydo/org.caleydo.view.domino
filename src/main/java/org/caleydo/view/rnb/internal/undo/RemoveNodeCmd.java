/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import java.util.Collection;

import org.caleydo.view.rnb.api.model.EDirection;
import org.caleydo.view.rnb.internal.Block;
import org.caleydo.view.rnb.internal.Node;
import org.caleydo.view.rnb.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public class RemoveNodeCmd implements ICmd {
	private final Node node;

	public RemoveNodeCmd(Node node) {
		this.node = node;
	}

	public static ICmd multi(Collection<Node> nodes) {
		if (nodes.size() == 1)
			return new RemoveNodeCmd(nodes.iterator().next());

		ICmd[] r = new ICmd[nodes.size()];
		int i = 0;
		for (Node n : nodes)
			r[i++] = new RemoveNodeCmd(n);
		return CmdComposite.chain(r);
	}

	@Override
	public ICmd run(Domino rnb) {
		Block b = node.getBlock();
		if (b.nodeCount() == 1) // remove the whole block
			return new RemoveBlockCmd(b).run(rnb);

		Node neighbor = null;
		EDirection dir = null;
		for (EDirection d : EDirection.values()) {
			Node n = node.getNeighbor(d);
			if (n != null) {
				neighbor = n;
				dir = d;
				break;
			}
		}
		b.removeNode(node);
		rnb.cleanup(node);
		if (dir == null) {
			// throw new IllegalStateException();
			return null;
		}
		assert neighbor != null && dir != null;
		return new PlaceNodeAtCmd(node, neighbor, dir.opposite());
	}

	@Override
	public String getLabel() {
		return "Remove Node: " + node;
	}

}
