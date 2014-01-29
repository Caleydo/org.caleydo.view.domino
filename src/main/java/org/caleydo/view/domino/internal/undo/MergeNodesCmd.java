/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedGroups;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ESetOperation;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.data.IDataValues;
import org.caleydo.view.domino.internal.data.StratificationDataValue;

/**
 * @author Samuel Gratzl
 *
 */
public class MergeNodesCmd implements ICmd {
	private final Node node;
	private final ESetOperation op;
	private final Node with;

	public MergeNodesCmd(Node node, ESetOperation op, Node with) {
		this.node = node;
		this.op = op;
		this.with = with;
	}

	@Override
	public String getLabel() {
		return "Merge";
	}

	@Override
	public ICmd run(Domino domino) {
		ICmd readd = null;
		if (domino.containsNode(with))
			readd = new RemoveNodeCmd(with).run(domino);

		EDimension dim = node.getSingleGroupingDimension();
		TypedGroupSet a = node.getUnderlyingData(dim);
		TypedGroupList b = with.getData(dim);
		TypedGroupSet r;
		IDataValues data = node.getDataValues();
		switch (op) {
		case INTERSECTION:
			r = TypedGroups.intersect(a, b);
			break;
		case UNION:
			r = TypedGroups.union(a, b);
			// we need a new data as we have the super set
			data = new StratificationDataValue(getLabel(), r, dim);
			break;
		case DIFFERENCE:
			r = TypedGroups.difference(a, b);
			break;
		default:
			throw new IllegalStateException();
		}
		node.setDataValues(data);
		node.setUnderlyingData(dim, r);
		return new MergeUndoCmd(readd, dim, a, data);
	}

	private class MergeUndoCmd implements ICmd {
		private final ICmd readd;
		private final EDimension dim;
		private final TypedGroupSet ori;
		private final IDataValues data;

		public MergeUndoCmd(ICmd readd, EDimension dim, TypedGroupSet ori, IDataValues data) {
			this.readd = readd;
			this.dim = dim;
			this.ori = ori;
			this.data = data;
		}

		@Override
		public ICmd run(Domino domino) {
			node.setDataValues(data);
			node.setUnderlyingData(dim, ori);
			if (readd != null)
				readd.run(domino);
			return MergeNodesCmd.this;
		}

		@Override
		public String getLabel() {
			return "Undo Merge";
		}

	}

}
