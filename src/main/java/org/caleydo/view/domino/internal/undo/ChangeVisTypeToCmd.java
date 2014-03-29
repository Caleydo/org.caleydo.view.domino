/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import java.util.ArrayList;
import java.util.Collection;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;

/**
 * @author Samuel Gratzl
 *
 */
public class ChangeVisTypeToCmd implements ICmd {

	private final Collection<Pair<Node, String>> visTypes;

	public ChangeVisTypeToCmd(Collection<Node> nodes, String visType) {
		this.visTypes = new ArrayList<>(nodes.size());
		for (Node n : nodes)
			visTypes.add(Pair.make(n, visType));
	}

	private ChangeVisTypeToCmd(Collection<Pair<Node, String>> visTypes) {
		this.visTypes = visTypes;
	}

	@Override
	public String getLabel() {
		return "Change Vis Type";
	}

	@Override
	public ICmd run(Domino rnb) {
		Collection<Pair<Node, String>> r = new ArrayList<>(visTypes.size());
		for (Pair<Node, String> p : visTypes) {
			Node node = p.getFirst();
			String old = node.getVisualizationType();
			r.add(Pair.make(node, old));
			node.setVisualizationType(p.getSecond());
		}
		return new ChangeVisTypeToCmd(r);
	}

}
