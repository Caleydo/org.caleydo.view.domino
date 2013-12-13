/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.InOutTransitions.IInTransition;
import org.caleydo.core.view.opengl.layout2.animation.InOutTransitions.IOutTransition;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.AGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IHasGLLayoutData;
import org.caleydo.view.domino.internal.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.internal.event.ShowPlaceHoldersEvent;
import org.caleydo.view.domino.internal.ui.model.DominoGraph;
import org.caleydo.view.domino.internal.ui.model.Edges;
import org.caleydo.view.domino.internal.ui.model.IDominoGraphListener;
import org.caleydo.view.domino.internal.ui.model.IEdge;
import org.caleydo.view.domino.internal.ui.model.Placeholder;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class DominoNodeLayer extends AnimatedGLElementContainer implements IDominoGraphListener, IGLLayout2,
		Function<INode, ANodeElement> {

	private final DominoGraph graph;
	private Deque<IChange> changes = new ArrayDeque<>();

	/**
	 * @param graph
	 * @param graphElement
	 */
	public DominoNodeLayer(DominoGraph graph) {
		setLayout(this);
		this.graph = graph;
		this.graph.addGraphListener(this);
		for (INode node : graph.vertexSet()) {
			vertexAdded(node, null);
		}
		setDefaultInTransition(new IInTransition() {
			@Override
			public Vec4f in(Vec4f to, float w, float h, float alpha) {
				if (alpha >= 1)
					return to.copy();
				return new Vec4f(0, 0, 0, 0);
			}
		});
		setDefaultOutTransition(new IOutTransition() {
			@Override
			public Vec4f out(Vec4f from, float w, float h, float alpha) {
				return new Vec4f(0, 0, 0, 0);
			}
		});
	}

	@Override
	public void vertexAdded(INode node, Collection<IEdge> edges) {
		ANodeElement new_;
		if (node instanceof PlaceholderNode)
			new_ = new PlaceholderNodeElement((PlaceholderNode) node);
		else
			new_ = new NodeElement(node);
		this.add(new_);

		Collection<IChange> changes = updateData(node, new_);
		this.changes.add(new Added(new_));
		this.changes.addAll(changes);
	}

	private Collection<IChange> updateData(INode node, ANodeElement new_) {
		if (node instanceof PlaceholderNode)
			return Collections.emptyList();
		// check data changes
		List<IChange> r = new ArrayList<>(2);
		for (EDimension dim : EDimension.values()) {
			List<INode> nodes = graph.walkAlong(dim, node, Edges.SAME_SORTING);
			if (nodes.size() <= 1)
				continue;
			IChange cr = updateDataImpl(dim, node, new_, nodes);
			if (cr != null)
				r.add(cr);
		}
		return r;
	}

	private IChange updateDataImpl(EDimension dim, INode node, ANodeElement new_, Collection<INode> nodes) {
		ImmutableList<ANodeElement> anodes = ImmutableList.copyOf(Collections2.transform(nodes, this));
		// we have to adapt the values
		EDimension toAdapt = dim.opposite();
		BitSet changes = LinearBlock.updateData(toAdapt, anodes, new_);
		Resized return_ = null;
		for (int i = changes.nextSetBit(0); i != -1; i = changes.nextSetBit(i + 1)) {
			final ANodeElement nodei = anodes.get(i);
			final Resized r = new Resized(nodei, toAdapt, toAdapt.select(nodei.getPreferredSize()));
			if (new_ == nodei)
				return_ = r;
			else
				this.changes.add(r);
		}
		return return_;
	}

	@Override
	public void vertexRemoved(INode node, Collection<IEdge> edges) {
		ANodeElement elem = apply(node);
		this.remove(elem);
		Vec2f size = elem.getSize();
		edges = Collections2.filter(edges, Edges.SAME_SORTING);
		for(EDimension dim : EDimension.values()) {
			Pair<ANodeElement, ANodeElement> leftRight = extract(edges, node, dim);
			ANodeElement l = leftRight.getFirst();
			ANodeElement r = leftRight.getSecond();
			if (l == null && r == null)
				continue;
			this.changes.add(new Removed(dim, dim.select(size), l, r));

			if (!(node instanceof PlaceholderNode)) {
				List<INode> ls = graph.walkAlong(EDirection.getPrimary(dim), l == null ? null : l.asNode(),
						Edges.SAME_SORTING);
				List<INode> rs = graph.walkAlong(EDirection.getPrimary(dim).opposite(), r == null ? null : r.asNode(),
						Edges.SAME_SORTING);
				if ((ls.size() + rs.size()) < 1)
					continue;
				updateDataImpl(dim, node, elem, ImmutableList.<INode> builder().addAll(ls).addAll(rs).build());
			}
		}
	}

	@Override
	public void vertexSortingChanged(ISortableNode vertex) {

	}

	private Pair<ANodeElement, ANodeElement> extract(Collection<IEdge> edges, INode vertex, EDimension dim) {
		EDirection primaryDir = EDirection.getPrimary(dim);
		INode l = null;
		INode r = null;
		for(IEdge edge : edges) {
			if(edge.getDirection(vertex) == primaryDir)
				l = edge.getOpposite(vertex);
			else if (edge.getDirection(vertex) == primaryDir.opposite())
				r = edge.getOpposite(vertex);
		}
		return Pair.make(apply(l), apply(r));
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		if (changes.isEmpty())
			return false;
		Deque<IChange> change = new ArrayDeque<>(changes);
		Map<GLElement, ? extends IGLLayoutElement> lookup = Maps.uniqueIndex(children, AGLLayoutElement.TO_GL_ELEMENT);
		Map<INode, ? extends IGLLayoutElement> lookup2 = Maps.uniqueIndex(children,
				GLLayoutDatas.toLayoutData(INode.class, null));
		changes.clear();
		while (!change.isEmpty()) {
			IChange next = change.pollFirst();
			if (next instanceof Resized) {
				Resized s = (Resized) next;
				IGLLayoutElement node = lookup.get(s.node);
				if (node == null)
					continue;
				final INode nnode = s.node.node;
				final EDimension dim = s.dim;
				float v = dim.select(node.getWidth(), node.getHeight());
				float v_new = s.new_;
				float v_delta = v_new - v;
				if (v_delta == 0)
					continue;
				move(node, v_delta, dim, children);
				node.setSize(dim.select(v_new, node.getWidth()), dim.select(node.getHeight(), v_new));
				final Vec2f loc = node.getLocation();
				float shift = 0;
				INode leftN = graph.getNeighbor(EDirection.getPrimary(dim), nnode, Edges.SAME_SORTING);
				INode rightN = graph.getNeighbor(EDirection.getPrimary(dim).opposite(), nnode, Edges.SAME_SORTING);
				if ((leftN == null) == (rightN == null))
					shift = v_delta * 0.5f;
				else if (rightN != null)
					shift = v_delta;
				node.setLocation(loc.x() - dim.select(shift, 0), loc.y() - dim.select(0, shift));
			}
			if (next instanceof Added) {
				Added r = (Added) next;
				IGLLayoutElement node = lookup.get(r.node);
				if (node == null)
					continue;
				Vec2f size = node.getSetSize();
				final INode nnode = r.node.node;
				INode leftN = graph.getNeighbor(EDirection.LEFT_OF, nnode, Edges.SAME_SORTING);
				INode rightN = graph.getNeighbor(EDirection.RIGHT_OF, nnode, Edges.SAME_SORTING);
				INode aboveN = graph.getNeighbor(EDirection.ABOVE, nnode, Edges.SAME_SORTING);
				INode belowN = graph.getNeighbor(EDirection.BELOW, nnode, Edges.SAME_SORTING);

				move(node, size.x(), EDimension.DIMENSION, children);
				move(node, size.y(), EDimension.RECORD, children);

				if (leftN != null) {
					Rect left = lookup2.get(leftN).getRectBounds();
					node.setLocation(left.x2(), left.y() + (left.height() - size.y()) * 0.5f);
				} else if (rightN != null) {
					Rect right = lookup2.get(rightN).getRectBounds();
					node.setLocation(right.x() - size.x(), right.y() + (right.height() - size.y()) * 0.5f);
				} else if (aboveN != null) {
					Rect left = lookup2.get(aboveN).getRectBounds();
					node.setLocation(left.x() + (left.width() - size.x()) * 0.5f, left.y2());
				} else if (belowN != null) {
					Rect right = lookup2.get(belowN).getRectBounds();
					node.setLocation(right.x() + (right.width() - size.x()) * 0.5f, right.y() - size.y());
				}
			}
			if (next instanceof Removed) {
				Removed r = (Removed) next;
				IGLLayoutElement left = lookup.get(r.left);
				IGLLayoutElement right = lookup.get(r.right);
				move(-r.size, r.dim, left, right, children);
			}
		}
		return false;
	}

	private void move(IGLLayoutElement node, float v_delta,
			EDimension dim, List<? extends IGLLayoutElement> children) {
		EDirection prim = EDirection.getPrimary(dim);
		Collection<? extends IGLLayoutElement> leftOf = allReachable(node, prim, children, false);
		Collection<? extends IGLLayoutElement> rightOf = allReachable(node, prim.opposite(), children, false);
		move(v_delta, dim, leftOf, rightOf);
	}

	private void move(float v_delta, EDimension dim,
			IGLLayoutElement leftOf, IGLLayoutElement rightOf, List<? extends IGLLayoutElement> children) {
		EDirection prim = EDirection.getPrimary(dim);
		Collection<? extends IGLLayoutElement> leftOfs = allReachable(leftOf, prim, children, true);
		Collection<? extends IGLLayoutElement> rightOfs = allReachable(rightOf, prim.opposite(), children, true);
		move(v_delta, dim, leftOfs, rightOfs);
	}

	private void move(float v_delta, EDimension dim,
			Collection<? extends IGLLayoutElement> leftOf, Collection<? extends IGLLayoutElement> rightOf) {
		final float total = leftOf.size() + rightOf.size();
		float l_delta = v_delta * (1.f - leftOf.size() / total);
		float r_delta = v_delta * (1.f - rightOf.size() / total);
		shift(leftOf, -l_delta, dim);
		shift(rightOf, r_delta, dim);
	}

	private <T extends IHasGLLayoutData> Collection<T> allReachable(IHasGLLayoutData node, EDirection dir,
			List<T> children, final boolean include) {
		if (node == null)
			return Collections.emptyList();

		final INode start = node.getLayoutDataAs(INode.class, null);
		final Set<INode> reachable = graph.allReachable(dir, start, Edges.SAME_SORTING, include);
		return Collections2.filter(children, new Predicate<T>() {
			@Override
			public boolean apply(T input) {
				final INode n = input.getLayoutDataAs(INode.class, null);
				if (n == start)
					return include;
				return reachable.contains(n);
			}
		});
	}

	private static void shift(Collection<? extends IGLLayoutElement> elems, float v, EDimension dim) {
		if (v == 0)
			return;
		float v_x = dim.select(v, 0);
		float v_y = dim.select(0, v);
		for (IGLLayoutElement elem : elems) {
			Vec2f l = elem.getLocation();
			elem.setLocation(l.x() + v_x, l.y() + v_y);
		}
	}

	@ListenTo(sendToMe = true)
	private void onShowPlaceHoldersEvent(ShowPlaceHoldersEvent event) {
		Set<Placeholder> placeholders = graph.findPlaceholders(event.getNode());
		graph.insertPlaceholders(placeholders, event.getNode());
	}

	@ListenTo(sendToMe = true)
	private void onHidePlaceHoldersEvent(HidePlaceHoldersEvent event) {
		graph.removePlaceholders(ImmutableSet.copyOf(Iterables.filter(graph.vertexSet(), PlaceholderNode.class)));
	}


	/**
	 * @return
	 */
	public Iterable<ANodeElement> getNodes() {
		return Iterables.filter(this, ANodeElement.class);
	}

	/**
	 * @param n
	 * @return
	 */
	@Override
	public ANodeElement apply(INode n) {
		if (n == null)
			return null;
		for (ANodeElement elem : getNodes())
			if (elem.asNode() == n)
				return elem;
		return null;
	}

	public void resized(INode n, EDimension dim, float new_) {
		ANodeElement r = apply(n);
		if (r == null)
			return;
		changes.add(new Resized(r, dim, new_));
		relayout();
	}

	public void resized(INode n, float w, float h) {
		ANodeElement r = apply(n);
		if (r == null)
			return;
		changes.add(new Resized(r, EDimension.DIMENSION, w));
		changes.add(new Resized(r, EDimension.RECORD, h));
		relayout();
	}

	private interface IChange {

	}

	private class Resized implements IChange {
		public final ANodeElement node;
		public final EDimension dim;
		public final float new_;

		public Resized(ANodeElement node, EDimension dim, float new_) {
			this.node = node;
			this.dim = dim;
			this.new_ = new_;
		}

	}

	private class Added implements IChange {
		public final ANodeElement node;

		public Added(ANodeElement node) {
			this.node = node;
		}

	}

	private class Removed implements IChange {
		public final EDimension dim;
		public final float size;
		public final ANodeElement left, right;

		public Removed(EDimension dim, float size, ANodeElement left, ANodeElement right) {
			this.dim = dim;
			this.size = size;
			this.left = left;
			this.right = right;
		}
	}

}
