/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.NodeGroup;

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
	public ICmd run(Domino domino) {
		EDimension dim = node.getSingleGroupingDimension();
		TypedGroupSet bak = node.getUnderlyingData(dim);
		List<TypedListGroup> d = new ArrayList<>(node.getData(dim).getGroups());
		final TypedListGroup toRemove = group.getData(dim);
		d.remove(toRemove);

		TypedGroupList l = new TypedGroupList(d);
		node.setUnderlyingData(dim, l.asSet());

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
		public ICmd run(Domino domino) {
			node.setUnderlyingData(dim, ori);
			return RemoveNodeGroupCmd.this;
		}

		@Override
		public String getLabel() {
			return "Undo Merge Groups";
		}

	}

}
