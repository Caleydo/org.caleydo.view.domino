/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.graph;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.internal.ui.prototype.AEdge;
import org.caleydo.view.domino.internal.ui.prototype.BandEdge;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.GraphViews;
import org.caleydo.view.domino.internal.ui.prototype.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortBarrier;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.caleydo.view.domino.internal.ui.prototype.MagneticEdge;
import org.caleydo.view.domino.internal.ui.prototype.ui.PlaceholderNode;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.Pseudograph;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
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
	private final ListenableUndirectedGraph<INode, IEdge> graph = new ListenableUndirectedMultigraph<>(IEdge.class);
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
		if (!dir.isPrimaryDirection()) {
			dir = dir.opposite();
			INode t = a;
			a = b;
			b = t;
		}
		graph.addEdge(a, b, new BandEdge(dir));
	}

	public void magnetic(INode a, EDirection dir, INode b) {
		assert isCompatible(a.getIDType(dir.asDim()), b.getIDType(dir.asDim()));
		if (!dir.isPrimaryDirection()) {
			dir = dir.opposite();
			INode t = a;
			a = b;
			b = t;
		}
		graph.addEdge(a, b, new MagneticEdge(dir));
	}

	public void magnetic(INode node, Placeholder placeholder) {
		magnetic(placeholder.getNode(), placeholder.getDir(), node);
	}

	/**
	 * returns a view of the edges of a node, where it will always be the source
	 *
	 * @param vertex
	 * @return
	 */
	public Collection<IEdge> edgesOfSource(INode source) {
		return edgesOf(source, true);
	}

	public Collection<IEdge> edgesOf(INode vertex, boolean ensureSource) {
		Set<IEdge> edges = graph.edgesOf(vertex);
		if (!ensureSource)
			return edges;
		return Collections2.transform(edges, AEdge.unify(vertex));
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
		Set<IEdge> edgeSet = ImmutableSet.copyOf(graph.edgeSet());
		for (IEdge edge : edgeSet)
			if (nodes.contains(edge.getSource()) || nodes.contains(edge.getTarget())) {
				edge.transpose();
			}
		propertySupport.firePropertyChange(PROP_TRANSPOSED, null, nodes);
	}

	/**
	 * detaches a node from the graph and connects related together
	 *
	 * @param node
	 * @return
	 */
	public Collection<IEdge> detach(INode node, boolean mergeNeighbors) {
		if (!graph.containsVertex(node))
			return Collections.emptyList();
		Collection<IEdge> edges = edgesOfSource(node);
		if (edges.isEmpty())
			return Collections.emptyList();
		Map<EDirection, IEdge> index = Maps.uniqueIndex(edges, TO_DIRECTION);
		// a-X-b -> a-b
		// connect a and b if in between was our target node X with a band

		for (EDirection dir : EnumSet.of(EDirection.LEFT_OF, EDirection.BELOW)) {
			if (index.containsKey(dir) && index.containsKey(dir.opposite())) {
				INode leftNode = index.get(dir.opposite()).getTarget();
				INode rightNode = index.get(dir).getTarget();
				band(leftNode, dir, rightNode);
			}
		}
		graph.removeAllEdges(ImmutableList.copyOf(edgesOf(node, false)));
		return edges;
	}

	public void remove(INode node) {
		if (!graph.containsVertex(node))
			return;
		detach(node, true);
		graph.removeVertex(node);
	}

	public enum EPlaceHolderFlag {
		INCLUDE_TRANSPOSE, INCLUDE_BETWEEN_BANDS, INCLUDE_BETWEEN_MAGNETIC
	}
	/**
	 * find all placed where this node can be attached
	 *
	 * @param node
	 * @return
	 */
	public Set<Placeholder> findPlaceholders(INode node, EPlaceHolderFlag... flags) {
		final Set<EPlaceHolderFlag> flagsS = asSet(flags);
		final boolean includeTranspose = flagsS.contains(EPlaceHolderFlag.INCLUDE_TRANSPOSE);

		Set<Placeholder> places = new HashSet<>();
		Set<EDimension> dims = node.dimensions();
		for (INode v : graph.vertexSet()) {
			if (v == node) // not myself
				continue;
			for (EDimension dim : dims) {
				addPlaceHolders(node, places, v, dim, dim, flagsS);
				if (includeTranspose)
					addPlaceHolders(node, places, v, dim, dim.opposite(), flagsS);
			}
		}
		return ImmutableSet.copyOf(places);
	}

	private void addPlaceHolders(INode node, Set<Placeholder> places, INode v, EDimension dim, EDimension vdim,
			Set<EPlaceHolderFlag> flags) {
		// for all possible dimensions
		if (!isCompatible(v.getIDType(vdim), node.getIDType(dim))) // check the dimension types are
			return; // compatible
		boolean transposed = dim != vdim;
		Collection<IEdge> edges = edgesOfSource(v);
		for (EDirection dir : EDirection.get(vdim.opposite())) {
			IEdge edge = hasEdge(dir, edges);
			if (edge == null) {
				places.add(new Placeholder(v, dir, transposed));
			} else if ((flags.contains(EPlaceHolderFlag.INCLUDE_BETWEEN_BANDS) && (edge instanceof BandEdge))) {
				places.add(new Placeholder(v, dir, transposed));
			} else if (flags.contains(EPlaceHolderFlag.INCLUDE_BETWEEN_MAGNETIC) && edge instanceof MagneticEdge) {
				INode o = edge.getTarget();
				if (o != node && !(o instanceof PlaceholderNode)
						&& (dir == EDirection.LEFT_OF || dir == EDirection.ABOVE))
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
				INode v2 = edge.getTarget();
				graph.removeEdge(edge.getRawSource(), edge.getRawTarget());
				if (edge instanceof BandEdge)
					band(n, dir, v2);
				else
					magnetic(n, dir, v2);
			}
			magnetic(v, dir, n);
		}
		return ImmutableSet.copyOf(places);
	}

	public void removePlaceholders(Set<PlaceholderNode> placeholders) {
		for (PlaceholderNode node : placeholders) {
			remove(node);
		}
	}

	/**
	 * @param node
	 * @param dir
	 * @return
	 */
	private IEdge getEdge(INode node, EDirection dir) {
		for (IEdge edge : edgesOfSource(node))
			if (edge.getDirection() == dir)
				return edge;
		return null;
	}

	private static IEdge hasEdge(EDirection dir, Iterable<IEdge> edges) {
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
		for (IEdge edge : Iterables.filter(edgesOfSource(start), filter)) {
			INode next = edge.getTarget();
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

	public interface ListenableUndirectedGraph<V, E> extends ListenableGraph<V, E>, UndirectedGraph<V, E> {

	}

	private static class ListenableUndirectedMultigraph<V, E> extends DefaultListenableGraph<V, E> implements
			ListenableUndirectedGraph<V, E> {
		private static final long serialVersionUID = 1L;

		ListenableUndirectedMultigraph(Class<E> edgeClass) {
			super(new Pseudograph<V, E>(edgeClass));
		}
	}
}
