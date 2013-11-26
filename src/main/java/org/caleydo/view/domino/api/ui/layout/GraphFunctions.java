/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.ui.layout;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

/**
 * utility graph functions
 *
 * @author Samuel Gratzl
 *
 */
public class GraphFunctions {
	/**
	 * has a {@link EEdgeType#PARENT_CHILD} edge and is the parent (source)
	 */
	public static final Predicate<IGraphVertex> IS_PARENT = new Predicate<IGraphVertex>() {
		@Override
		public boolean apply(IGraphVertex input) {
			if (input == null)
				return false;
			for (IGraphEdge edge : input.getEdges())
				if (edge.getType() == EEdgeType.PARENT_CHILD && edge.getSource() == input)
					return true;
			return false;
		}
	};

	/**
	 * return the children of this parent {@link IGraphVertex}
	 *
	 * @param parent
	 * @return
	 */
	public static final ImmutableCollection<IGraphVertex> getChildren(IGraphVertex parent) {
		ImmutableList.Builder<IGraphVertex> children = ImmutableList.builder();
		for (IGraphEdge edge : parent.getEdges()) {
			if (edge.getType() == EEdgeType.PARENT_CHILD && edge.getSource() == parent)
				children.add(edge.getTarget());
		}
		return children.build();
	}

	/**
	 * resolve the first parent of this vertex
	 * 
	 * @param child
	 * @return
	 */
	public static final IGraphVertex getParent(IGraphVertex child) {
		for (IGraphEdge edge : child.getEdges()) {
			if (edge.getType() == EEdgeType.PARENT_CHILD && edge.getTarget() == child)
				return edge.getSource();
		}
		return null;
	}

}
