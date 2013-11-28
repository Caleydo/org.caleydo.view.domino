/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.domino.api.model.BandRoute;
import org.caleydo.view.domino.api.model.TypedCollections;
import org.caleydo.view.domino.api.ui.band.Route;
import org.caleydo.view.domino.internal.ui.DominoBandLayer;
import org.caleydo.view.domino.internal.ui.DominoBandLayer.IBandRootProvider;
import org.caleydo.view.domino.internal.ui.DominoLayoutInfo;
import org.caleydo.view.domino.internal.ui.prototype.BandEdge;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.Graph;
import org.caleydo.view.domino.internal.ui.prototype.Graph.ListenableDirectedGraph;
import org.caleydo.view.domino.internal.ui.prototype.GraphViews;
import org.caleydo.view.domino.internal.ui.prototype.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortBarrier;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.caleydo.view.domino.spi.model.IBandRenderer;
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
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class GraphElement extends GLElementContainer implements IGLLayout2, IPickingListener, IBandRootProvider {

	private final ListenableDirectedGraph<INode, IEdge> graph;
	private final ConnectivityInspector<INode, IEdge> connectivity;

	private List<IBandRenderer> routes = new ArrayList<>();
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

		DominoBandLayer band = new DominoBandLayer(this);
		this.add(band);

		this.nodes = new GLElementContainer(this);
		this.fillNodes(nodes);
		this.add(nodes);
	}

	@Override
	public List<? extends IBandRenderer> getBandRoutes() {
		return routes;
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
		float x = 0;
		float y = 0;
		for (Set<INode> block : sets) {
			Vec2f r = layout(block.iterator().next(), lookup, x, y);
			x += r.x();
		}

		createBandRoutes(lookup);

		return false;
	}

	/**
	 *
	 */
	private void createBandRoutes(Function<INode, NodeLayoutElement> lookup) {
		routes.clear();
		for (IEdge edge : graph.edgeSet()) {
			if (edge.getDirection() == EDirection.LEFT_OF || edge.getDirection() == EDirection.ABOVE)
				continue; // as directed will come again
			NodeLayoutElement source = lookup.apply(graph.getEdgeSource(edge));
			NodeLayoutElement target = lookup.apply(graph.getEdgeTarget(edge));

			Rect sourceB = source.elem.getRectBounds();
			Rect targetB = target.elem.getRectBounds();
			EDimension dim = edge.getDirection().asDim();

			if ((dim.isHorizontal() && sourceB.x2() == targetB.x())
					|| (dim.isVertical() && sourceB.y2() == targetB.y()))
				continue;

			float r_s = dim.opposite().select(sourceB.width(), sourceB.height()) * 0.5f;
			float r_t = dim.opposite().select(targetB.width(), targetB.height()) * 0.5f;
			List<Vec2f> curve;
			if (dim.isHorizontal()) {
				curve = Arrays.asList(new Vec2f(sourceB.x2(), sourceB.y() + sourceB.height() * 0.5f),
						new Vec2f(targetB.x(), targetB.y() + targetB.height() * 0.5f));
			} else {
				curve = Arrays.asList(new Vec2f(sourceB.x() + sourceB.width() * 0.5f, sourceB.y2()),
						new Vec2f(targetB.x() + targetB.width() * 0.5f, targetB.y()));
			}
			Color color = edge instanceof BandEdge ? Color.LIGHT_GRAY : Color.LIGHT_BLUE;
			routes.add(new BandRoute(new Route(curve), color, TypedCollections.INVALID_SET, r_s, r_t));
		}
	}

	private Vec2f layout(INode root, Function<INode, NodeLayoutElement> lookup, float x_offset, float y_offset) {
		Map<INode, Point> grid = Maps.newIdentityHashMap();
		placeNode(root, 0, 0, grid);
		Rectangle r = boundingBoxOf(grid.values());

		// shift grid such that grid starts at 0,0
		grid = shiftGrid(grid, -r.x, -r.y);
		r.x = r.y = 0;

		NodeLayoutElement[][] arr = transformGrid(grid, r.width, r.height, lookup);
		float[] cols = new float[r.width];
		float[] rows = new float[r.height];

		for(int i = 0; i < arr.length; ++i) {
			NodeLayoutElement[] line = arr[i];
			for(int j = 0; j < line.length; ++j) {
				NodeLayoutElement v= line[j];
				if (v == null)
					continue;

				Vec2f size = v.getSize();
				float wShift = 0;
				float hShift = 0;
				if (j > 0 && line[j - 1] != null && line[j - 1].getSize().y() < size.y())
					wShift += 10;
				if (j < line.length - 1 && line[j + 1] != null && line[j + 1].getSize().y() < size.y())
					wShift += 10;
				if (i > 0 && arr[i - 1][j] != null && arr[i - 1][j].getSize().x() < size.x())
					hShift += 10;
				if (i < arr.length - 1 && arr[i + 1][j] != null && arr[i + 1][j].getSize().x() < size.x())
					hShift += 10;
				rows[i] = Math.max(rows[i], size.y() + hShift);
				cols[j] = Math.max(cols[j], size.x() + wShift);
			}
		}

		// shift where

		// postsum for faster access: 1,2,3 will be 1,3,6 -> delta = x[i]-i==0?:0:-x[i-1]
		cols = postsum(cols);
		rows = postsum(rows);

		for (Entry<INode, Point> entry : grid.entrySet()) {
			NodeLayoutElement node = lookup.apply(entry.getKey());
			Point p = entry.getValue();
			Vec2f size = node.getSize();
			float xi = (p.x == 0 ? 0 : cols[p.x - 1]);
			float yi = (p.y == 0 ? 0 : rows[p.y - 1]);
			float wi = cols[p.x] - xi;
			float hi = rows[p.y] - yi;
			xi += x_offset + (wi - size.x()) * 0.5f; // shift and center
			yi += y_offset + (hi - size.y()) * 0.5f; // shift and center
			node.elem.setBounds(xi, yi, size.x(), size.y());
		}
		return new Vec2f(cols[cols.length - 1], rows[rows.length - 1]);
	}

	/**
	 * @param cols
	 * @return
	 */
	private float[] postsum(float[] cols) {
		for (int i = 1; i < cols.length; ++i) {
			cols[i] += cols[i - 1];
		}
		return cols;
	}

	/**
	 * @param grid
	 * @param lookup
	 * @return
	 */
	private NodeLayoutElement[][] transformGrid(Map<INode, Point> grid, int cols, int rows,
			Function<INode, NodeLayoutElement> lookup) {
		NodeLayoutElement[][] r = new NodeLayoutElement[rows][cols];
		for (Entry<INode, Point> entry : grid.entrySet()) {
			Point p = entry.getValue();
			r[p.y][p.x] = lookup.apply(entry.getKey());
		}
		return r;
	}

	private static Map<INode, Point> shiftGrid(Map<INode, Point> grid, int x_shift, int y_shift) {
		if (x_shift == 0 && y_shift == 0)
			return grid;
		for (Entry<INode, Point> entry : grid.entrySet()) {
			Point p = entry.getValue();
			p.x += x_shift;
			p.y += y_shift;
		}
		return grid;
	}

	/**
	 * @param grid
	 * @return
	 */
	private static Rectangle boundingBoxOf(Iterable<Point> points) {
		int x = 0;
		int y = 0;
		int x_max = 0;
		int y_max = 0;
		for (Point p : points) {
			if (p.x < x)
				x = p.x;
			else if (p.x > x_max)
				x_max = p.x;
			if (p.y < y)
				y = p.y;
			else if (p.y > y_max)
				y_max = p.y;
		}
		return new Rectangle(x, y, x_max - x + 1, y_max - y + 1);
	}

	private void placeNode(INode node, int x, int y, Map<INode, Point> grid) {
		if (grid.containsKey(node))
			return;
		grid.put(node, new Point(x, y));
		for (IEdge edge : graph.outgoingEdgesOf(node)) {
			INode target = graph.getEdgeTarget(edge);
			EDirection dir = edge.getDirection();
			int f = (edge instanceof BandEdge) ? 2 : 1;
			placeNode(target, x + f * dir.asInt(EDimension.DIMENSION), y + f * dir.asInt(EDimension.RECORD), grid);
		}
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

	private static class NodeLayoutElement {
		private final IGLLayoutElement elem;
		private final DominoLayoutInfo info;

		public NodeLayoutElement(IGLLayoutElement elem) {
			this.elem = elem;
			this.info = elem.getLayoutDataAs(DominoLayoutInfo.class,
					GLLayoutDatas.<DominoLayoutInfo> throwInvalidException());
		}

		/**
		 * @return
		 */
		public INode asNode() {
			return elem.getLayoutDataAs(INode.class,
					GLLayoutDatas.<INode> throwInvalidException());
		}

		public IGLLayoutElement asElem() {
			return elem;
		}

		public Vec2f getSize() {
			return info.getSize();
		}
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
