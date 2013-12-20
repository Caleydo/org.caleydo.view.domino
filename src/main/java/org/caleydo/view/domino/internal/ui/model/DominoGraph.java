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

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.id.IDCategory;
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
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoGraph implements Function<Integer, INode> {
	private final List<IDominoGraphListener> listeners = new ArrayList<>(2);
	private final ListenableUndirectedGraph<INode, IEdge> graph = new ListenableUndirectedMultigraph<>(IEdge.class);
	private final ConnectivityInspector<INode, IEdge> connectivity;

	public final static IDCategory GRAPH_CATEGORY = IDCategory.registerInternalCategory("domino");
	public final static IDType NODE_IDTYPE = IDType.registerInternalType("dominoNode", GRAPH_CATEGORY,
			EDataType.INTEGER);

	/**
	 *
	 */
	public DominoGraph() {
		this.connectivity = new ConnectivityInspector<>(GraphViews.edgeView(graph,
				Predicates.instanceOf(MagneticEdge.class)));
		this.graph.addGraphListener(this.connectivity);
	}

	public INode getByID(int id) {
		for (INode node : vertexSet())
			if (node.getID() == id)
				return node;
		return null;
	}

	@Override
	public INode apply(Integer input) {
		return input == null ? null : getByID(input.intValue());
	}

	public void addGraphListener(IDominoGraphListener l) {
		this.listeners.add(l);
	}

	public void removeGraphListener(IDominoGraphListener l) {
		this.listeners.remove(l);
	}

	/**
	 * @param n
	 */
	public void addVertex(INode n) {
		graph.addVertex(n);
		vertexAdded(n);
	}

	private void magnetic(INode a, EDirection dir, INode b) {
		addEdge(a, b, new MagneticEdge(dir));
	}

	private void beam(INode a, EDirection dir, INode b) {
		addEdge(a, b, new BeamEdge(dir));
	}

	private void band(INode a, EDimension dir, INode b) {
		band(a, dir, b, dir);
	}

	private void band(INode a, EDimension dirA, INode b, EDimension dirB) {
		addEdge(a, b, new BandEdge(dirA, dirB));
	}

	private void edgeLike(IEdge edge, INode from, INode to) {
		INode other = edge.getOpposite(from);
		EDirection dir = edge.getDirection(from);
		if (edge instanceof BandEdge)
			band(other, edge.getDirection(other).asDim(), to, dir.asDim());
		if (edge instanceof MagneticEdge)
			magnetic(to, dir, other);
		else if (edge instanceof BeamEdge)
			beam(to, dir, other);
	}

	private void addEdge(INode a, INode b, final IEdge edge) {
		EDirection ad = edge.getDirection(a);
		EDirection bd = edge.getDirection(b);
		assert isCompatible(a.getIDType(ad.asDim().opposite()), b.getIDType(bd.asDim().opposite()));
		// if (!dirA.isPrimaryDirection()) {
		// INode t = a;
		// a = b;
		// b = t;
		// edge.swapDirection(a);
		// }
		// if (!dirA.isPrimaryDirection()) {
		// INode t = a;
		// a = b;
		// b = t;
		// edge.swapDirection(a);
		// }
		graph.addEdge(a, b, edge);
		if (!(a instanceof PlaceholderNode) && !(b instanceof PlaceholderNode)) {
			updateProximity(a, edge);
			updateProximity(b, edge);
		}
	}

	private void updateProximity(INode node, IEdge edge) {
		final NodeUIState uiState = node.getUIState();
		EProximityMode m;
		if (edge != null) { // added
			m = EProximityMode.min(uiState.getProximityMode(), edge.asMode());
		} else { // removed
			m = EProximityMode.min(edgesOf(node));
		}
		uiState.setProximityMode(m);
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

		mergeMagnetic(node, Collections2.filter(edges, Edges.SAME_STRATIFICATION));
		copyBands(node, Iterables.filter(edges, BandEdge.class));
		graph.removeAllEdges(edges);
		return edges;
	}

	/**
	 * @param node
	 * @param filter
	 */
	private void copyBands(INode node, Iterable<BandEdge> edges) {
		for (BandEdge edge : edges) {
			EDirection sdir = edge.getDirection(node);
			INode neighbor = getNeighbor(sdir.opposite(), node, Edges.SAME_STRATIFICATION);
			if (neighbor == null)
				continue;
			INode t = edge.getOpposite(node);
			EDimension tdir = edge.getDimension(t);
			band(neighbor, sdir.asDim(), t, tdir);
		}
	}

	private void mergeMagnetic(INode node, Collection<IEdge> edges) {
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
				band(top, EDimension.RECORD, bottom);
			else
				edgeLike(above, top, bottom);
		}
		if (leftOf != null && rightOf != null) {
			INode top = leftOf.getOpposite(node);
			INode bottom = rightOf.getOpposite(node);
			if (hasVertical)
				band(top, EDimension.DIMENSION, bottom);
			else
				edgeLike(leftOf, top, bottom);
		}
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
		if (!(node instanceof PlaceholderNode)) {
			for (IEdge edge : edges) {
				updateProximity(edge.getOpposite(node), null);
			}
		}
		graph.removeVertex(node);
		for (IDominoGraphListener l : listeners)
			l.vertexRemoved(node, edges);
	}

	public enum EPlaceHolderFlag {
		INCLUDE_TRANSPOSE, INCLUDE_BETWEEN_BAND, INCLUDE_BETWEEN_MAGNETIC, INCLUDE_BETWEEN_BEAM
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
			} else if ((flags.contains(EPlaceHolderFlag.INCLUDE_BETWEEN_BAND) && Edges.BANDS.apply(edge))) {
				places.add(new Placeholder(v, dir, transposed));
			} else if ((flags.contains(EPlaceHolderFlag.INCLUDE_BETWEEN_BEAM) && Edges.SAME_STRATIFICATION.apply(edge))) {
				places.add(new Placeholder(v, dir, transposed));
			} else if (flags.contains(EPlaceHolderFlag.INCLUDE_BETWEEN_MAGNETIC) && Edges.SAME_SORTING.apply(edge)) {
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
					graph.removeEdge(edge);
					edgeLike(edge, v, n);
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
	private void vertexAdded(INode n) {
		if (!(n instanceof PlaceholderNode)) {
			for (EDimension dim : n.dimensions())
				createBandEdges(n, dim);
		}
		Collection<IEdge> edges = edgesOf(n);
		for (IDominoGraphListener l : listeners)
			l.vertexAdded(n, edges);
	}

	/**
	 * @param n
	 */
	private void createBandEdges(INode start, EDimension dim) {
		Set<INode> done = new HashSet<>();
		done.addAll(walkAlong(dim.opposite(), start, Edges.SAME_STRATIFICATION));

		final IDType idType = start.getIDType(dim);
		for (INode node : vertexSet()) {
			if (done.contains(node))
				continue;
			for (EDimension dim2 : node.dimensions()) {
				if (!isCompatible(node.getIDType(dim2), idType))
					continue;
				List<INode> neighors = walkAlong(dim2.opposite(), node, Edges.SAME_STRATIFICATION);
				band(neighors.get(0), dim2.opposite(), start, dim.opposite());
				done.addAll(neighors);
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
		return ImmutableList.<INode> builder().addAll(Lists.reverse(before)).addAll(after.subList(1, after.size()))
				.build();
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
			if (next == start)
				continue;
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
		List<INode> sortingRelevant = walkAlong(dim.opposite(), node,
				Predicates.not(Predicates.instanceOf(ISortBarrier.class)));
		// remove invalid
		Iterable<ISortableNode> sortingReallyRelevant = Iterables.filter(sortingRelevant, ISortableNode.class);

		// sort
		SortedSet<ISortableNode> sorting = ImmutableSortedSet.orderedBy(bySortingPriority)
				.addAll(sortingReallyRelevant).build();

		int priority = node.getSortingPriority(dim);
		if (priority == ISortableNode.TOP_PRIORITY) { // deselect
			node.setSortingPriority(dim, ISortableNode.NO_SORTING);
			for (ISortableNode n : sorting) {
				if (n != node && n != null && n.getSortingPriority(dim) != ISortableNode.NO_SORTING)
					n.setSortingPriority(dim, n.getSortingPriority(dim) - 1);
			}
		} else if (priority == ISortableNode.NO_SORTING) {
			node.setSortingPriority(dim, ISortableNode.TOP_PRIORITY);
			for (ISortableNode n : sorting) {
				final int npriority = n.getSortingPriority(dim);
				if (n != node && npriority != ISortableNode.NO_SORTING) {
					n.setSortingPriority(dim, npriority + 1);
					if (npriority > ISortableNode.MINIMUM_PRIORITY)
						n.setSortingPriority(dim, ISortableNode.NO_SORTING);
				}
			}
		} else { // increase sorting
			node.setSortingPriority(dim, ISortableNode.TOP_PRIORITY);
			for (ISortableNode n : sorting) {
				final int npriority = n.getSortingPriority(dim);
				if (n != node && npriority < priority) {
					n.setSortingPriority(dim, nextPriority(npriority));
				}
			}
		}
		for (IDominoGraphListener l : listeners)
			l.vertexSortingChanged(node, dim);
	}

	/**
	 * @param npriority
	 * @return
	 */
	private static int nextPriority(int p) {
		if (p >= ISortableNode.MINIMUM_PRIORITY)
			return ISortableNode.NO_SORTING;
		return p + 1;
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

	/**
	 * @return
	 */
	public static SelectionManager newNodeSelectionManager() {
		return new SelectionManager(DominoGraph.NODE_IDTYPE);
	}
}
