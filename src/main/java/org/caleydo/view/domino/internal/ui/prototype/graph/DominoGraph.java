/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.graph;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.internal.ui.prototype.BandEdge;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.GraphViews;
import org.caleydo.view.domino.internal.ui.prototype.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortBarrier;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.caleydo.view.domino.internal.ui.prototype.MagneticEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.VertexSetListener;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoGraph {
	private static final Function<IEdge, EDirection> TO_DIRECTION = new Function<IEdge, EDirection>() {
		@Override
		public EDirection apply(IEdge input) {
			return input == null ? null : input.getDirection();
		}
	};
	private final ListenableDirectedGraph<INode, IEdge> graph = new ListenableDirectedMultigraph<>(IEdge.class);
	private final ConnectivityInspector<INode, IEdge> connectivity;

	/**
	 *
	 */
	public DominoGraph() {
		this.connectivity = new ConnectivityInspector<>(graph);
		this.graph.addGraphListener(this.connectivity);

		Demo.fill(this);
	}

	public void addVertexSetListener(VertexSetListener<INode> l) {
		graph.addVertexSetListener(l);
	}

	public void removeVertexSetListener(VertexSetListener<INode> l) {
		graph.removeVertexSetListener(l);
	}

	public void addGraphListener(GraphListener<INode, IEdge> l) {
		graph.addGraphListener(l);
	}

	public void removeGraphListener(GraphListener<INode, IEdge> l) {
		graph.removeGraphListener(l);
	}

	public boolean addVertex(INode v) {
		return graph.addVertex(v);
	}

	public void band(INode a, EDirection dir, INode b) {
		assert isCompatible(a.getIDType(dir.asDim()), b.getIDType(dir.asDim()));
		graph.addEdge(a, b, new BandEdge(dir));
		graph.addEdge(b, a, new BandEdge(dir.opposite()));
	}

	public void magnetic(INode a, EDirection dir, INode b) {
		assert isCompatible(a.getIDType(dir.asDim()), b.getIDType(dir.asDim()));
		graph.addEdge(a, b, new MagneticEdge(dir));
		graph.addEdge(b, a, new MagneticEdge(dir.opposite()));
	}

	public void magnetic(INode node, Placeholder placeholder) {
		magnetic(placeholder.getNode(), placeholder.getDir(), node);
	}

	public Set<IEdge> incomingEdgesOf(INode vertex) {
		return graph.incomingEdgesOf(vertex);
	}

	public Set<IEdge> outgoingEdgesOf(INode vertex) {
		return graph.outgoingEdgesOf(vertex);
	}

	public boolean contains(IEdge e) {
		return graph.containsEdge(e);
	}

	public boolean contains(INode v) {
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

	/**
	 * detaches a node from the graph and connects related together
	 *
	 * @param node
	 */
	public void detach(INode node) {
		if (!graph.containsVertex(node))
			return;
		Set<IEdge> edges = graph.outgoingEdgesOf(node);
		if (edges.isEmpty())
			return;
		Map<EDirection, IEdge> index = Maps.uniqueIndex(edges, TO_DIRECTION);
		// a-X-b -> a-b
		// connect a and b if in between was our target node X with a band

		for (EDirection dir : EnumSet.of(EDirection.LEFT_OF, EDirection.BELOW)) {
			if (index.containsKey(dir) && index.containsKey(dir.opposite())) {
				INode leftNode = getEdgeTarget(index.get(dir.opposite()));
				INode rightNode = getEdgeTarget(index.get(dir));
				band(leftNode, dir, rightNode);
			}
		}
		graph.removeAllEdges(ImmutableSet.copyOf(graph.outgoingEdgesOf(node)));
		graph.removeAllEdges(ImmutableSet.copyOf(graph.incomingEdgesOf(node)));
	}

	public void remove(INode node) {
		if (!graph.containsVertex(node))
			return;
		detach(node);
		graph.removeVertex(node);
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

	public interface ListenableDirectedGraph<V, E> extends ListenableGraph<V, E>, DirectedGraph<V, E> {

	}

	private static class ListenableDirectedMultigraph<V, E> extends DefaultListenableGraph<V, E> implements
			ListenableDirectedGraph<V, E> {
		private static final long serialVersionUID = 1L;

		ListenableDirectedMultigraph(Class<E> edgeClass) {
			super(new DirectedMultigraph<V, E>(edgeClass));
		}
	}
}
