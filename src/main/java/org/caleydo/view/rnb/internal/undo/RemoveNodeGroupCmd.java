/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.rnb.api.model.typed.TypedGroupSet;
import org.caleydo.view.rnb.api.model.typed.TypedListGroup;
import org.caleydo.view.rnb.internal.LinearBlock;
import org.caleydo.view.rnb.internal.Node;
import org.caleydo.view.rnb.internal.NodeGroup;
import org.caleydo.view.rnb.internal.RnB;

/**
 * @author Samuel Gratzl
 *
 */
public class RemoveNodeGroupCmd implements ICmd {
	private final NodeGroup group;
	private final Node node;

	public RemoveNodeGroupCmd(NodeGroup group) {
		this.group = group;
		this.node = group.getNode();
	}

	@Override
	public String getLabel() {
		return "Remove Node Group " + group.getLabel();
	}

	@Override
	public ICmd run(RnB rnb) {
		EDimension dim = node.getSingleGroupingDimension();
		TypedGroupSet bak = node.getUnderlyingData(dim);
		final List<TypedListGroup> gropus = node.getData(dim).getGroups();
		int index = gropus.indexOf(group.getData(dim));

		node.setUnderlyingData(dim, LinearBlock.clean(gropus, index));

		// group.prepareRemoveal();
		return new UndoRemoveGroupCmd(dim, bak);
	}

	private class UndoRemoveGroupCmd implements ICmd {
		private final EDimension dim;
		private final TypedGroupSet ori;

		public UndoRemoveGroupCmd(EDimension dim, TypedGroupSet bak) {
			this.dim = dim;
			this.ori = bak;
		}

		@Override
		public ICmd run(RnB rnb) {
			node.setUnderlyingData(dim, ori);
			return RemoveNodeGroupCmd.this;
		}

		@Override
		public String getLabel() {
			return "Undo Merge Groups";
		}

	}

}
