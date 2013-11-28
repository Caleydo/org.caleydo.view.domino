/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.graph;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoGraph {
	private final ListenableDirectedGraph<INode, IEdge> graph;
	private final ConnectivityInspector<INode, IEdge> connectivity;

	/**
	 *
	 */
	public DominoGraph() {
		this.graph = Graph.build();
		this.connectivity = new ConnectivityInspector<>(graph);
		this.graph.addGraphListener(this.connectivity);

	}

	public Set<IEdge> incomingEdgesOf(INode vertex) {
		return graph.incomingEdgesOf(vertex);
	}

	public Set<IEdge> outgoingEdgesOf(INode vertex) {
		return graph.outgoingEdgesOf(vertex);
	}

	public boolean containsEdge(IEdge e) {
		return graph.containsEdge(e);
	}

	public boolean containsVertex(INode v) {
		return graph.containsVertex(v);
	}

	public Set<IEdge> edgeSet() {
		return graph.edgeSet();
	}

	public Set<INode> vertexSet() {
		return graph.vertexSet();
	}

	public INode getEdgeSource(IEdge e) {
		return graph.getEdgeSource(e);
	}

	public INode getEdgeTarget(IEdge e) {
		return graph.getEdgeTarget(e);
	}

	public Set<INode> connectedSetOf(INode vertex) {
		return connectivity.connectedSetOf(vertex);
	}

	public List<Set<INode>> connectedSets() {
		return connectivity.connectedSets();
	}

	public boolean pathExists(INode sourceVertex, INode targetVertex) {
		return connectivity.pathExists(sourceVertex, targetVertex);
	}

	public void detach(INode node) {
		if (!graph.containsVertex(node))
			return;
		graph.removeAllEdges(graph.outgoingEdgesOf(node));
		graph.removeAllEdges(graph.incomingEdgesOf(node));
	}

	/**
	 * find all placed where this node can be attached
	 *
	 * @param node
	 * @return
	 */
	public Set<Placeholder> findPlaceholders(INode node) {
		Set<Placeholder> places = new HashSet<>();

		Set<EDimension> dims = node.dimensions();
		for (INode v : graph.vertexSet()) {
			if (v == node) // not myself
				continue;
			for (EDimension dim : dims)
				// for all possible dimensions
				if (isCompatible(node.getIDType(dim), node.getIDType(dim))) { // check the dimension types are
																				// compatible
					Set<IEdge> edges = graph.incomingEdgesOf(v);
					for (EDirection dir : EDirection.get(dim))
						// check left and right
						if (!hasEdge(dir, edges))
							places.add(new Placeholder(v, dir));
				}
		}
		return ImmutableSet.copyOf(places);
	}

	private static boolean hasEdge(EDirection dir, Set<IEdge> edges) {
		for (IEdge edge : edges)
			if (edge.getDirection() == dir)
				return true;
		return false;
	}

	private static boolean isCompatible(IDType a, IDType b) {
		return a.getIDCategory().isOfCategory(b);
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
}
