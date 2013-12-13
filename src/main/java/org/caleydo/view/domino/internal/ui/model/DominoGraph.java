/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.internal.ui.PlaceholderNode;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.GraphViews;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.traverse.DepthFirstIterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoGraph {
	private final List<IDominoGraphListener> listeners = new ArrayList<>(2);
	private final ListenableUndirectedGraph<INode, IEdge> graph = new ListenableUndirectedMultigraph<>(IEdge.class);
	private final ConnectivityInspector<INode, IEdge> connectivity;

	/**
	 *
	 */
	public DominoGraph() {
		this.connectivity = new ConnectivityInspector<>(GraphViews.edgeView(graph,
				Predicates.instanceOf(MagneticEdge.class)));
		this.graph.addGraphListener(this.connectivity);

		// Demo.fill(this);
	}

	public void addGraphListener(IDominoGraphListener l) {
		this.listeners.add(l);
	}

	public void removeGraphListener(IDominoGraphListener l) {
		this.listeners.remove(l);
	}

	private void magnetic(INode a, EDirection dir, INode b) {
		addEdge(a, dir, b, new MagneticEdge(dir));
	}

	private void beam(INode a, EDirection dir, INode b) {
		addEdge(a, dir, b, new BeamEdge(dir));
	}

	private void band(INode a, EDirection dir, INode b) {
		addEdge(a, dir, b, new BandEdge(dir.asDim(), dir.asDim()));
	}

	private void edgeLike(IEdge edge, INode from, INode to) {
		INode other = edge.getOpposite(from);
		EDirection dir = edge.getDirection(from);
		if (edge instanceof MagneticEdge)
			magnetic(to, dir, other);
		else if (edge instanceof BeamEdge)
			beam(to, dir, other);
	}

	private void addEdge(INode a, EDirection dir, INode b, final IEdge edge) {
		assert isCompatible(a.getIDType(dir.asDim().opposite()), b.getIDType(dir.asDim().opposite()));
		if (!dir.isPrimaryDirection()) {
			INode t = a;
			a = b;
			b = t;
			edge.swapDirection(a);
		}
		graph.addEdge(a, b, edge);
	}

	public Set<IEdge> edgesOf(INode vertex) {
		return graph.edgesOf(vertex);
	}

	public boolean contains(INode v) {
		return graph.containsVertex(v);
	}

	public Set<IEdge> edgeSet() {
		return Collections.unmodifiableSet(graph.edgeSet());
	}

	public Set<INode> vertexSet() {
		return Collections.unmodifiableSet(graph.vertexSet());
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

	// /**
	// * transpose a node and all the connected set of it
	// *
	// * @param start
	// */
	// public void transpose(INode start) {
	// Set<INode> nodes = connectivity.connectedSetOf(start);
	// for (INode node : nodes) {
	// node.transpose();
	// }
	// Set<IEdge> edgeSet = ImmutableSet.copyOf(graph.edgeSet());
	// for (IEdge edge : edgeSet)
	// if (nodes.contains(edge.getSource()) || nodes.contains(edge.getTarget())) {
	// edge.transpose();
	// }
	// propertySupport.firePropertyChange(PROP_TRANSPOSED, null, nodes);
	// }

	/**
	 * detaches a node from the graph and connects related together
	 *
	 * @param node
	 * @return
	 */
	private Collection<IEdge> detach(INode node) {
		if (!graph.containsVertex(node))
			return Collections.emptyList();
		Collection<IEdge> edges = ImmutableList.copyOf(edgesOf(node));
		if (edges.isEmpty())
			return Collections.emptyList();

		Map<EDirection, IEdge> index = Maps.uniqueIndex(edges, to_direction(node));
		// a-X-b -> a-b
		// connect a and b if in between was our target node X with a band

		IEdge above = index.get(EDirection.ABOVE);
		IEdge below = index.get(EDirection.BELOW);
		IEdge leftOf = index.get(EDirection.LEFT_OF);
		IEdge rightOf = index.get(EDirection.RIGHT_OF);
		boolean hasVertical = above != null || below != null;
		boolean hasHorizontal = leftOf != null || rightOf != null;

		if (above != null && below != null) {
			INode top = above.getOpposite(node);
			INode bottom = below.getOpposite(node);
			if (hasHorizontal)
				band(top, EDirection.ABOVE, bottom);
			else
				magnetic(top, EDirection.ABOVE, bottom);
		}
		if (leftOf != null && rightOf != null) {
			INode top = leftOf.getOpposite(node);
			INode bottom = rightOf.getOpposite(node);
			if (hasVertical)
				band(top, EDirection.LEFT_OF, bottom);
			else
				magnetic(top, EDirection.LEFT_OF, bottom);
		}
		graph.removeAllEdges(edges);
		return edges;
	}

	/**
	 * @param node
	 * @return
	 */
	private Function<? super IEdge, EDirection> to_direction(final INode node) {
		return new Function<IEdge, EDirection>() {
			@Override
			public EDirection apply(IEdge input) {
				return input == null ? null : input.getDirection(node);
			}
		};
	}

	public void remove(INode node) {
		if (!graph.containsVertex(node))
			return;
		Collection<IEdge> edges = detach(node);
		graph.removeVertex(node);
		for (IDominoGraphListener l : listeners)
			l.vertexRemoved(node, edges);
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
		Collection<IEdge> edges = edgesOf(v);
		for (EDirection dir : EDirection.get(vdim.opposite())) {
			IEdge edge = hasEdge(v, dir, edges);
			if (edge == null) {
				places.add(new Placeholder(v, dir, transposed));
			} else if ((flags.contains(EPlaceHolderFlag.INCLUDE_BETWEEN_BANDS) && (edge instanceof BandEdge))) {
				places.add(new Placeholder(v, dir, transposed));
			} else if (flags.contains(EPlaceHolderFlag.INCLUDE_BETWEEN_MAGNETIC) && edge instanceof MagneticEdge) {
				INode o = edge.getOpposite(v);
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
			PlaceholderNode n = new PlaceholderNode(template, p.isTransposed());
			places.add(n);
			graph.addVertex(n);
			INode v = p.getNode();
			if (v != null) {// non free node
				EDirection dir = p.getDir();
				IEdge edge = getEdge(v, dir); // right of
				if (edge != null) { // split needed
					INode v2 = edge.getOpposite(v);
					graph.removeEdge(edge);
					if (edge instanceof BandEdge)
						band(n, dir, v2);
					else
						magnetic(n, dir, v2);
				}
				magnetic(v, dir, n);
			}
			vertexAdded(n);
		}
		return ImmutableSet.copyOf(places);
	}

	/**
	 * @param n
	 */
	public void addVertex(INode n) {
		graph.addVertex(n);
		vertexAdded(n);
	}

	/**
	 * @param n
	 */
	private void vertexAdded(INode n) {
		createBandEdges(n);
		Collection<IEdge> edges = edgesOf(n);
		for (IDominoGraphListener l : listeners)
			l.vertexAdded(n, edges);
	}

	/**
	 * @param n
	 */
	private void createBandEdges(INode n) {
		Set<INode> toCheck = new HashSet<>(vertexSet());
		toCheck.removeAll(walkAlong(EDimension.DIMENSION, n, Edges.SAME_STRATIFICATION));
		toCheck.removeAll(walkAlong(EDimension.RECORD, n, Edges.SAME_STRATIFICATION));

		for (EDimension dim : n.dimensions()) {
			final IDType idType = n.getIDType(dim);
			for (INode other : toCheck) {
				if (isCompatible(idType, other.getIDType(dim))) {
					// band n dim - dim other
				}
				if (isCompatible(idType, other.getIDType(dim.opposite()))) {
					// band n dim - opp other
				}
			}
		}
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
		for (IEdge edge : edgesOf(node))
			if (edge.getDirection(node) == dir)
				return edge;
		return null;
	}

	private static IEdge hasEdge(INode v, EDirection dir, Iterable<IEdge> edges) {
		for (IEdge edge : edges)
			if (edge.getDirection(v) == dir)
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
	public List<INode> walkAlong(EDimension dim, INode start, Predicate<? super IEdge> filter) {
		List<INode> before = walkAlong(dim.select(EDirection.LEFT_OF, EDirection.ABOVE), start, filter);
		List<INode> after = walkAlong(dim.select(EDirection.RIGHT_OF, EDirection.BELOW), start, filter);
		// as we have the start twice
		return ImmutableList.<INode> builder().addAll(before).addAll(after.subList(1, after.size())).build();
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
	public List<INode> walkAlong(EDirection dir, INode start, Predicate<? super IEdge> filter) {
		if (!graph.containsVertex(start))
			return ImmutableList.of();

		ImmutableList.Builder<INode> b = ImmutableList.builder();
		walkAlongImpl(dir, start, filter, b);
		return b.build();
	}

	private void walkAlongImpl(EDirection dir, INode start, Predicate<? super IEdge> filter,
			ImmutableCollection.Builder<INode> b) {

		b.add(start);
		for (IEdge edge : Iterables.filter(edgesOf(start), filter)) {
			if (edge.getDirection(start) != dir)
				continue;
			INode next = edge.getOpposite(start);
			walkAlongImpl(dir, next, filter, b);
		}
	}

	public INode getNeighbor(EDirection dir, INode start, Predicate<? super IEdge> filter) {
		for (IEdge edge : Iterables.filter(edgesOf(start), filter)) {
			if (edge.getDirection(start) == dir)
				return edge.getOpposite(start);
		}
		return null;
	}

	/**
	 * returns all reachable nodes of a given start node in a given direction
	 *
	 * @param dir
	 * @param start
	 * @param filter
	 * @return
	 */
	public Set<INode> allReachable(EDirection dir, INode start, Predicate<? super IEdge> filter) {
		return allReachable(dir, start, filter, false);
	}

	public Set<INode> allReachable(EDirection dir, INode start, Predicate<? super IEdge> filter, boolean include) {
		if (!graph.containsVertex(start))
			return ImmutableSet.of();
		ImmutableSet.Builder<INode> b = ImmutableSet.builder();
		allReachableImpl(dir, start, filter, b, include);

		return b.build();
	}

	public Set<INode> allReachableInclude(EDirection dir, INode start, Predicate<? super IEdge> filter) {
		return allReachable(dir, start, filter, true);
	}

	private void allReachableImpl(EDirection dir, INode start, Predicate<? super IEdge> filter,
			ImmutableSet.Builder<INode> b, boolean include) {

		Set<IEdge> badEdges = new HashSet<>(edgesOf(start));
		for(Iterator<IEdge> it = badEdges.iterator(); it.hasNext(); ) {
			final EDirection sdir = it.next().getDirection(start);
			if ((!include && sdir == dir) || (include && sdir != dir)) // remove the good edge
				it.remove();
		}
		// idea: filter the graph such that all other edges from the start node except the target dir are removed
		// and iterate over the rest
		UndirectedGraph<INode, IEdge> filtered = GraphViews.graphView(graph, Predicates.in(connectedSetOf(start)),
				Predicates.and(filter, Predicates.not(Predicates.in(badEdges))));

		DepthFirstIterator<INode, IEdge> it = new DepthFirstIterator<INode, IEdge>(filtered, start);
		b.addAll(it);
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
		List<INode> sortingRelevant = walkAlong(dim, node, Predicates.not(Predicates.instanceOf(ISortBarrier.class)));
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

	public interface ListenableUndirectedGraph<V, E> extends ListenableGraph<V, E>, UndirectedGraph<V, E> {

	}

	private static class ListenableUndirectedMultigraph<V, E> extends DefaultListenableGraph<V, E> implements
			ListenableUndirectedGraph<V, E> {
		private static final long serialVersionUID = 1L;

		ListenableUndirectedMultigraph(Class<E> edgeClass) {
			super(new Pseudograph<V, E>(edgeClass));
		}
	}

	public boolean moveToAlone(INode node) {
		remove(node);
		addVertex(node);
		return true;
	}
	/**
	 * @param node
	 * @param node2
	 */
	public boolean move(INode node, PlaceholderNode to) {
		if (!contains(to))
			return false;
		if (!match(node, to, true))
			return false;
		remove(node);

		if (needTranspose(node, to))
			node.transpose();

		Collection<IEdge> edges = ImmutableSet.copyOf(edgesOf(to));
		remove(to);
		graph.addVertex(node);

		for (IEdge edge : edges) {
			edgeLike(edge, to, node);
		}

		vertexAdded(node);
		return true;
	}





	/**
	 * @param node
	 * @param to
	 * @return
	 */
	private static boolean match(INode a, INode b, boolean checkTranspose) {
		IDType a_d = a.getIDType(EDimension.DIMENSION);
		IDType a_r = a.getIDType(EDimension.RECORD);
		IDType b_d = b.getIDType(EDimension.DIMENSION);
		IDType b_r = b.getIDType(EDimension.RECORD);
		if (a_d == b_d && a_r == b_r)
			return true;
		if (checkTranspose && (a_d == b_r && a_r == b_d))
			return true;
		return false;
	}

	/**
	 * @param node
	 * @param to
	 * @return
	 */
	private static boolean needTranspose(INode a, INode b) {
		assert match(a, b, true);
		return a.getIDType(EDimension.DIMENSION) != b.getIDType(EDimension.DIMENSION);
	}

	/**
	 * @return
	 */
	public boolean hasPlaceholders() {
		return Iterables.any(vertexSet(), Predicates.instanceOf(PlaceholderNode.class));
	}
}
