/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import java.util.Set;

import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.Node;
import org.caleydo.view.rnb.internal.NodeGroup;

/**
 * @author Samuel Gratzl
 *
 */
public class RemoveSliceCmd implements ICmd {
	private Node node;
	private Set<NodeGroup> selection;

	public RemoveSliceCmd(Node node, Set<NodeGroup> selection) {
		this.node = node;
		this.selection = selection;
	}

	@Override
	public ICmd run(RnB domino) {
		node.removeSlice(selection);
		return null; // FIXME undo can't undo
	}

	@Override
	public String getLabel() {
		return "Remove Slice";
	}

}
