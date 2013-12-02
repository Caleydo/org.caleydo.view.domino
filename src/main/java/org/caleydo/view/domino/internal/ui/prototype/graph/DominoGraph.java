/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.graph;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
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
import org.caleydo.view.domino.internal.ui.prototype.ui.PlaceholderNode;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
	public static final String PROP_VERTICES = "vertices";
	public static final String PROP_EDGES = "edges";
	public static final String PROP_TRANSPOSED = "transposed";

	private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);
	private final ListenableDirectedGraph<INode, IEdge> graph = new ListenableDirectedMultigraph<>(IEdge.class);
	private final ConnectivityInspector<INode, IEdge> connectivity;

	/**
	 *
	 */
	public DominoGraph() {
		this.connectivity = new ConnectivityInspector<>(GraphViews.edgeView(graph,
				Predicates.instanceOf(MagneticEdge.class)));
		this.graph.addGraphListener(this.connectivity);
		this.graph.addGraphListener(new GraphListenerAdapter(propertySupport));

		Demo.fill(this);
	}

	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
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
	 * transpose a node and all the connected set of it
	 *
	 * @param start
	 */
	public void transpose(INode start) {
		Set<INode> nodes = connectivity.connectedSetOf(start);
		for (INode node : nodes) {
			node.transpose();
		}
		for (IEdge edge : graph.edgeSet())
			if (nodes.contains(getEdgeSource(edge)))
				edge.transpose();
		propertySupport.firePropertyChange(PROP_TRANSPOSED, null, nodes);
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

	public enum EPlaceHolderFlag {
		INCLUDE_TRANSPOSE, INCLUDE_BETWEEN_NODES
	}
	/**
	 * find all placed where this node can be attached
	 *
	 * @param node
	 * @return
	 */
	public Set<Placeholder> findPlaceholders(INode node, EPlaceHolderFlag... flags) {
		final Set<EPlaceHolderFlag> flagsS = asSet(flags);
		final boolean betweenNodes = flagsS.contains(EPlaceHolderFlag.INCLUDE_BETWEEN_NODES);
		final boolean includeTranspose = flagsS.contains(EPlaceHolderFlag.INCLUDE_TRANSPOSE);

		Set<Placeholder> places = new HashSet<>();
		Set<EDimension> dims = node.dimensions();
		for (INode v : graph.vertexSet()) {
			if (v == node) // not myself
				continue;
			for (EDimension dim : dims) {
				addPlaceHolders(node, betweenNodes, places, v, dim, dim);
				if (includeTranspose)
					addPlaceHolders(node, betweenNodes, places, v, dim, dim.opposite());
			}
		}
		return ImmutableSet.copyOf(places);
	}

	private void addPlaceHolders(INode node, boolean betweenNodes, Set<Placeholder> places, INode v,
			EDimension dim, EDimension vdim) {
		// for all possible dimensions
		if (!isCompatible(v.getIDType(vdim), node.getIDType(dim))) // check the dimension types are
			return; // compatible
		boolean transposed = dim != vdim;
		Set<IEdge> edges = graph.outgoingEdgesOf(v);
		for (EDirection dir : EDirection.get(vdim.opposite())) {
			IEdge edge = hasEdge(dir, edges);
			if (edge == null) {
				places.add(new Placeholder(v, dir, transposed));
			} else if (betweenNodes) {
				INode o = graph.getEdgeTarget(edge);
				if (o != node && !(o instanceof PlaceholderNode)
						&& (dir == EDirection.LEFT_OF || dir == EDirection.ABOVE))
					// just once split in between two nodes
					places.add(new Placeholder(v, dir, transposed));
			}
		}
	}

	private static Set<EPlaceHolderFlag> asSet(EPlaceHolderFlag... flags) {
		EnumSet<EPlaceHolderFlag> s = EnumSet.noneOf(EPlaceHolderFlag.class);
		s.addAll(Arrays.asList(flags));
		return s;
	}

	public Set<PlaceholderNode> insertPlaceholders(Set<Placeholder> placeholders, INode template) {
		Set<PlaceholderNode> places = new HashSet<>();

		for (Placeholder p : placeholders) {
			INode v = p.getNode();
			EDirection dir = p.getDir();
			PlaceholderNode n = new PlaceholderNode(template, p.isTransposed());
			places.add(n);
			addVertex(n);

			IEdge edge = getEdge(p.getNode(), p.getDir());
			if (edge != null) { // no split needed
				INode v2 = graph.getEdgeSource(edge);
				graph.removeEdge(edge);
				graph.removeEdge(v, v2);
				magnetic(v2, dir.opposite(), n);
			}
			magnetic(n, dir.opposite(), v);
		}
		return ImmutableSet.copyOf(places);
	}

	public void removePlaceholders(Set<PlaceholderNode> placeholders) {
		for (PlaceholderNode node : placeholders) {
			detach(node);
		}
	}

	/**
	 * @param node
	 * @param dir
	 * @return
	 */
	private IEdge getEdge(INode node, EDirection dir) {
		for (IEdge edge : graph.incomingEdgesOf(node))
			if (edge.getDirection() == dir)
				return edge;
		return null;
	}

	private static IEdge hasEdge(EDirection dir, Set<IEdge> edges) {
		for (IEdge edge : edges)
			if (edge.getDirection() == dir)
				return edge;
		return null;
	}

	private static boolean isCompatible(IDType a, IDType b) {
		return a.getIDCategory().isOfCategory(b);
	}

	/**
	 * return a set of node reachable in the given direction using the given start point
	 *
	 * @param dim
	 * @param start
	 * @return
	 */
	public Set<INode> walkAlong(EDimension dim, INode start, Predicate<? super IEdge> filter) {
		if (dim.isHorizontal())
			return Sets.union(walkAlong(EDirection.LEFT_OF, start, filter),
					walkAlong(EDirection.RIGHT_OF, start, filter));
		else
			return Sets.union(walkAlong(EDirection.ABOVE, start, filter), walkAlong(EDirection.BELOW, start, filter));
	}

	/**
	 * return a set of node reachable if walking along the given direction starting at the given node
	 *
	 * @param dir
	 *            walking direction
	 * @param start
	 *            start point
	 * @param filter
	 *            filter edges to stop walking
	 * @return
	 */
	public Set<INode> walkAlong(EDirection dir, INode start, Predicate<? super IEdge> filter) {
		if (!graph.containsVertex(start))
			return ImmutableSet.of();

		ImmutableSet.Builder<INode> b = ImmutableSet.builder();
		walkAlongImpl(dir, start, Predicates.and(dir, filter), b);
		return b.build();
	}

	private void walkAlongImpl(EDirection dir, INode start, Predicate<? super IEdge> filter, ImmutableSet.Builder<INode> b) {
		b.add(start);
		for (IEdge edge : Iterables.filter(graph.incomingEdgesOf(start), filter)) {
			INode next = graph.getEdgeSource(edge);
			walkAlongImpl(dir, next, filter, b);
		}
	}

	/**
	 * @param node
	 * @param eDimension
	 */
	public void sortBy(ISortableNode node, final EDimension dim) {
		// find all sorting nodes
		Comparator<ISortableNode> bySortingPriority = new Comparator<ISortableNode>() {
			@Override
			public int compare(ISortableNode o1, ISortableNode o2) {
				return o1.getSortingPriority(dim) - o2.getSortingPriority(dim);
			}
		};
		// find relevant
		Set<INode> sortingRelevant = walkAlong(dim, node, Predicates.not(Predicates.instanceOf(ISortBarrier.class)));
		// remove invalid
		Iterable<ISortableNode> sortingReallyRelevant = Iterables.filter(sortingRelevant, ISortableNode.class);

		// sort
		SortedSet<ISortableNode> sorting = ImmutableSortedSet.orderedBy(bySortingPriority)
				.addAll(sortingReallyRelevant).build();

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

	/**
	 * @author Samuel Gratzl
	 *
	 */
	private static final class GraphListenerAdapter implements GraphListener<INode, IEdge> {
		private final PropertyChangeSupport propertySupport;

		public GraphListenerAdapter(PropertyChangeSupport propertySupport) {
			this.propertySupport = propertySupport;
		}

		@Override
		public void vertexRemoved(GraphVertexChangeEvent<INode> e) {
			propertySupport.firePropertyChange(PROP_VERTICES, e.getVertex(), null);
		}

		@Override
		public void vertexAdded(GraphVertexChangeEvent<INode> e) {
			propertySupport.firePropertyChange(PROP_VERTICES, null, e.getVertex());
		}

		@Override
		public void edgeRemoved(GraphEdgeChangeEvent<INode, IEdge> e) {
			propertySupport.firePropertyChange(PROP_EDGES, e.getEdge(), null);
		}

		@Override
		public void edgeAdded(GraphEdgeChangeEvent<INode, IEdge> e) {
			propertySupport.firePropertyChange(PROP_EDGES, null, e.getEdge());
		}
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
