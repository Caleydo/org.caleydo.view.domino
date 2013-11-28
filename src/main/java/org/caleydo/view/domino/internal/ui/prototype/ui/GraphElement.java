/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.domino.internal.ui.DominoBandLayer;
import org.caleydo.view.domino.internal.ui.prototype.Graph;
import org.caleydo.view.domino.internal.ui.prototype.Graph.ListenableDirectedGraph;
import org.caleydo.view.domino.internal.ui.prototype.GraphViews;
import org.caleydo.view.domino.internal.ui.prototype.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortBarrier;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.traverse.DepthFirstIterator;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * @author Samuel Gratzl
 *
 */
public class GraphElement extends GLElementContainer implements IGLLayout2, IPickingListener {

	private final ListenableDirectedGraph<INode, IEdge> graph;
	private final ConnectivityInspector<INode, IEdge> connectivity;

	private final Routes routes = new Routes();
	private final GLElementContainer nodes;

	/**
	 *
	 */
	public GraphElement() {
		setLayout(GLLayouts.LAYERS);
		this.graph = Graph.build();
		this.connectivity = new ConnectivityInspector<>(graph);
		this.graph.addGraphListener(this.connectivity);

		this.add(new PickableGLElement().onPick(this).setzDelta(-0.1f));

		DominoBandLayer band = new DominoBandLayer(routes);
		this.add(band);

		this.nodes = new GLElementContainer(this);
		this.fillNodes(nodes);
		this.add(nodes);
	}




	@Override
	public void pick(Pick pick) {
		if (pick.getPickingMode() == PickingMode.MOUSE_WHEEL) {
			for (NodeElement elem : Iterables.filter(nodes, NodeElement.class)) {
				elem.pick(pick);
			}
		}
	}

	/**
	 *
	 */
	private void fillNodes(GLElementContainer container) {
		for (INode node : this.graph.vertexSet()) {
			container.add(new NodeElement(node));
		}
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		Function<INode, NodeLayoutElement> lookup = Functions.forMap(asLookup(children));
		List<Set<INode>> sets = this.connectivity.connectedSets();

		List<LayoutBlock> blocks = new ArrayList<>();
		for (Set<INode> block : sets) {
			blocks.add(LayoutBlock.create(block.iterator().next(), graph, lookup));
		}


		float x = 0;
		for (LayoutBlock block : blocks) {
			Vec2f size = block.getSize();
			block.shift(x, 0);
			block.run();
			x += size.x();
		}

		routes.update(graph, lookup);
		return false;
	}




	/**
	 * @param children
	 * @return
	 */
	private Map<INode, NodeLayoutElement> asLookup(List<? extends IGLLayoutElement> children) {
		ImmutableMap.Builder<INode, NodeLayoutElement> b = ImmutableMap.builder();
		for (IGLLayoutElement elem : children)
			b.put(elem.getLayoutDataAs(INode.class, GLLayoutDatas.<INode> throwInvalidException()),
					new NodeLayoutElement(elem));
		return b.build();
	}



	/**
	 * @param node
	 * @param eDimension
	 */
	public void sortBy(ISortableNode node, final EDimension dim) {
		// project to just the relevant edges
		Predicate<IEdge> isSortingEdge = new Predicate<IEdge>() {
			@Override
			public boolean apply(IEdge input) {
				return input != null && input.getDirection().asDim() == dim && !(input instanceof ISortBarrier);
			}
		};
		DirectedGraph<INode, IEdge> subView = GraphViews.edgeView(graph, isSortingEdge);
		// find all sorting nodes
		Comparator<ISortableNode> bySortingPriority = new Comparator<ISortableNode>() {
			@Override
			public int compare(ISortableNode o1, ISortableNode o2) {
				return o1.getSortingPriority(dim) - o2.getSortingPriority(dim);
			}
		};
		SortedSet<ISortableNode> sorting = ImmutableSortedSet.orderedBy(bySortingPriority)
				.addAll(Iterators.filter(new DepthFirstIterator<>(subView, node), ISortableNode.class)).build();

		int priority = node.getSortingPriority(dim);
		if (priority == ISortableNode.TOP_PRIORITY) { // deselect
			node.setSortingPriority(dim, ISortableNode.NO_SORTING);
			for (ISortableNode n : sorting) {
				if (n != null && n.getSortingPriority(dim) != ISortableNode.NO_SORTING)
					n.setSortingPriority(dim, n.getSortingPriority(dim) - 1);
			}
		} else if (priority == ISortableNode.NO_SORTING) {
			node.setSortingPriority(dim, ISortableNode.TOP_PRIORITY);
			for (ISortableNode n : sorting) {
				if (n != null && n.getSortingPriority(dim) != ISortableNode.NO_SORTING) {
					n.setSortingPriority(dim, n.getSortingPriority(dim) + 1);
					if (n.getSortingPriority(dim) > ISortableNode.MINIMUM_PRIORITY)
						n.setSortingPriority(dim, ISortableNode.NO_SORTING);
				}
			}
		} else { // increase sorting
			node.setSortingPriority(dim, ISortableNode.TOP_PRIORITY);
			for (ISortableNode n : sorting) {
				if (n != null && n.getSortingPriority(dim) < priority) {
					n.setSortingPriority(dim, n.getSortingPriority(dim) + 1);
				}
			}
		}

		Predicate<ISortableNode> isPartOfSorting = new Predicate<ISortableNode>() {
			@Override
			public boolean apply(ISortableNode input) {
				return input != null && input.getSortingPriority(dim) != ISortableNode.NO_SORTING;
			}
		};
		SortedSet<ISortableNode> sortCriteria = ImmutableSortedSet.orderedBy(bySortingPriority)
				.addAll(Iterables.filter(sorting, isPartOfSorting)).build();
		// TODO create a comparator or something like that, which determines the order
		// TODO now we must apply the new sorting to all nodes
	}

	public static void main(String[] args) {
		GLSandBox.main(args, new GraphElement());
	}
}
