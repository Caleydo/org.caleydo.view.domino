/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import java.util.Collection;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.LinearBlock.ESortingMode;

/**
 * @author Samuel Gratzl
 *
 */
public class SortByNodesCmd implements ICmd {
	private final Node node;
	private final EDimension dim;
	private final boolean stratify;

	public SortByNodesCmd(Node node, EDimension dim, boolean stratify) {
		this.node = node;
		this.dim = dim;
		this.stratify = stratify;
	}

	public static ICmd multi(Collection<Node> nodes, EDimension dim, boolean stratify) {
		if (nodes.size() == 1)
			return new SortByNodesCmd(nodes.iterator().next(), dim, stratify);

		ICmd[] r = new ICmd[nodes.size()];
		int i = 0;
		for (Node n : nodes)
			r[i++] = new SortByNodesCmd(n, dim, stratify);
		return CmdComposite.chain(r);
	}

	@Override
	public String getLabel() {
		return "Sort Node";
	}

	@Override
	public ICmd run(Domino rnb) {
		Block b = node.getBlock();
		List<Pair<Node, ESortingMode>> r;
		if (stratify)
			r = b.stratifyBy(node, dim);
		else
			r = b.sortBy(node, dim);
		if (r == null)
			return null;
		return new RestoreSortingCmd(r);
	}

	private class RestoreSortingCmd implements ICmd {
		private final List<Pair<Node, ESortingMode>> sortCriteria;

		public RestoreSortingCmd(List<Pair<Node, ESortingMode>> sortCriteria) {
			this.sortCriteria = sortCriteria;
		}

		@Override
		public String getLabel() {
			return "Sort Node";
		}

		@Override
		public ICmd run(Domino rnb) {
			Block b = node.getBlock();
			List<Pair<Node, ESortingMode>> r = b.restoreSorting(node, dim, sortCriteria);
			if (r == null)
				return null;
			return new RestoreSortingCmd(r);
		}

	}

}
