/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import java.util.Collection;

import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;

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
	public ICmd run(Domino domino) {
		Block b = node.getBlock();
		if (b.size() == 1) // remove the whole block
			return new RemoveBlockCmd(b).run(domino);

		domino.removeNode(node);
		return null; // FIXME undo can't undo
	}

	@Override
	public String getLabel() {
		return "Remove Node: " + node;
	}

}
