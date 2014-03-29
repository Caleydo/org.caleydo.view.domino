/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import java.util.Collection;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;

/**
 * @author Samuel Gratzl
 *
 */
public class LimitToNodeCmd implements ICmd {
	private final Node node;
	private final EDimension dim;

	public LimitToNodeCmd(Node node, EDimension dim) {
		this.node = node;
		this.dim = dim;
	}

	public static ICmd multi(Collection<Node> nodes, EDimension dim) {
		if (nodes.size() == 1)
			return new LimitToNodeCmd(nodes.iterator().next(), dim);

		ICmd[] r = new ICmd[nodes.size()];
		int i = 0;
		for (Node n : nodes)
			r[i++] = new LimitToNodeCmd(n, dim);
		return CmdComposite.chain(r);
	}

	@Override
	public String getLabel() {
		return "Limit To Node";
	}

	@Override
	public ICmd run(Domino rnb) {
		Block b = node.getBlock();
		Node old = b.limitTo(node, dim);
		if (old == null)
			old = node;
		return new LimitToNodeCmd(old, dim);
	}
}
