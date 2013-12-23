/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Random;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorBrewer;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.InOutTransitions.IInTransition;
import org.caleydo.core.view.opengl.layout2.animation.InOutTransitions.IOutTransition;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.AGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IHasGLLayoutData;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EDirection;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Samuel Gratzl
 *
 */
public class TestLayout extends AnimatedGLElementContainer implements IGLLayout2, IPickingListener {

	private Deque<IChange> changes = new ArrayDeque<>();
	private Random random;
	private int pickingId;
	/**
	 *
	 */
	public TestLayout() {
		setLayout(this);
		this.random = new Random(5);
		setDefaultInTransition(new IInTransition() {
			@Override
			public Vec4f in(Vec4f to, float w, float h, float alpha) {
				if (alpha >= 1)
					return to;
				return new Vec4f(to.x(),to.y(),0,0);
			}
		});
		setDefaultOutTransition(new IOutTransition() {
			@Override
			public Vec4f out(Vec4f from, float w, float h, float alpha) {
				return new Vec4f(from.x(), from.y(), 0, 0);
			}
		});
	}

	@Override
	protected void init(IGLElementContext context) {
		pickingId = context.registerPickingListener(this);
		super.init(context);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.pushName(pickingId).fillRect(0, 0, w, h).popName();
		super.renderPickImpl(g, w, h);
	}

	@Override
	protected boolean hasPickAbles() {
		return true;
	}


	@Override
	public void pick(Pick pick) {
		switch(pick.getPickingMode()) {
		case CLICKED:
			Elem elem = new Elem();
			elem.setLayoutData(random.nextInt(100));
			Vec2f xy = toRelative(pick.getPickedPoint());
			elem.setBounds(xy.x(), xy.y(), 100, 100);
			changes.add(new Added(elem));
			this.add(elem);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		if (changes.isEmpty())
			return false;
		Deque<IChange> change = new ArrayDeque<>(changes);
		Map<GLElement, ? extends IGLLayoutElement> lookup = Maps.uniqueIndex(children, AGLLayoutElement.TO_GL_ELEMENT);
		changes.clear();
		while (!change.isEmpty()) {
			IChange next = change.pollFirst();
			if (next instanceof Resized) {
				Resized s = (Resized) next;
				IGLLayoutElement node = lookup.get(s.node);
				float scale = s.scale;
				if (scale == 1)
					continue;
				final EDimension dim = s.dim;
				float v = dim.select(node.getWidth(), node.getHeight());
				float v_new = v * scale;
				float v_delta = v_new - v;
				Pair<? extends IGLLayoutElement, ? extends IGLLayoutElement> neighors = move(node, v_delta, dim,
						children);
				node.setSize(dim.select(v_new, node.getWidth()), dim.select(node.getHeight(), v_new));
				final Vec2f loc = node.getLocation();
				float shift = 0;
				if ((neighors.getFirst() == null) == (neighors.getSecond() == null))
					shift = v_delta * 0.5f;
				else if (neighors.getSecond() != null)
					shift = v_delta;
				node.setLocation(loc.x() - dim.select(shift, 0), loc.y() - dim.select(0, shift));
			}
			if (next instanceof Added ) {
				Added r = (Added) next;
				IGLLayoutElement node = lookup.get(r.node);
				Vec2f size = node.getSetSize();
				Pair<? extends IGLLayoutElement, ? extends IGLLayoutElement> dNeighbors = move(node, size.x(),
						EDimension.DIMENSION,
						children);
				Pair<? extends IGLLayoutElement, ? extends IGLLayoutElement> rNeighbors = move(node, size.y(),
						EDimension.RECORD, children);
				if (dNeighbors.getFirst() != null) {
					Rect left = dNeighbors.getFirst().getRectBounds();
					node.setLocation(left.x2(), left.y());
				} else if (dNeighbors.getSecond() != null) {
					Rect right = dNeighbors.getSecond().getRectBounds();
					node.setLocation(right.x() - size.x(), right.y());
				} else if (rNeighbors.getFirst() != null) {
					Rect left = rNeighbors.getFirst().getRectBounds();
					node.setLocation(left.x(), left.y2());
				} else if (rNeighbors.getSecond() != null) {
					Rect right = rNeighbors.getSecond().getRectBounds();
					node.setLocation(right.x(), right.y() - size.y());
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



	private Pair<? extends IGLLayoutElement, ? extends IGLLayoutElement> move(IGLLayoutElement node,
			float v_delta, EDimension dim, List<? extends IGLLayoutElement> children) {
		EDirection prim = EDirection.getPrimary(dim);
		Collection<? extends IGLLayoutElement> leftOf = allReachable(node, prim, children,false);
		Collection<? extends IGLLayoutElement> rightOf = allReachable(node, prim.opposite(), children,false);
		return move(v_delta, dim, leftOf, rightOf);
	}

	private Pair<? extends IGLLayoutElement, ? extends IGLLayoutElement> move(float v_delta, EDimension dim,
			IGLLayoutElement leftOf, IGLLayoutElement rightOf,List<? extends IGLLayoutElement> children) {
		EDirection prim = EDirection.getPrimary(dim);
		Collection<? extends IGLLayoutElement> leftOfs = allReachable(leftOf, prim, children,true);
		Collection<? extends IGLLayoutElement> rightOfs = allReachable(rightOf, prim.opposite(), children,true);
		return move(v_delta, dim, leftOfs, rightOfs);
	}


	private Pair<? extends IGLLayoutElement, ? extends IGLLayoutElement> move(float v_delta, EDimension dim,
			Collection<? extends IGLLayoutElement> leftOf, Collection<? extends IGLLayoutElement> rightOf) {
		final float total = leftOf.size() + rightOf.size();
		float l_delta = v_delta * (1.f - leftOf.size() / total);
		float r_delta = v_delta * (1.f - rightOf.size() / total);
		shift(leftOf, -l_delta, dim);
		shift(rightOf, r_delta, dim);
		return Pair.make(leftOf.isEmpty() ? null : leftOf.iterator().next(), rightOf.isEmpty() ? null : rightOf
				.iterator().next());
	}

	private <T extends IHasGLLayoutData> Collection<T> allReachable(IHasGLLayoutData node, EDirection dir,
			List<T> children,boolean include) {
		if (dir.isVertical() || node == null)
			return Collections.emptyList();
		int act = node.getLayoutDataAs(Integer.class, 0);
		NavigableMap<Integer, T> lookup = ImmutableSortedMap.copyOf(Maps.uniqueIndex(children,
				GLLayoutDatas.toLayoutData(Integer.class, Suppliers.ofInstance(0))));
		if (dir == EDirection.LEFT_OF) {
			return lookup.headMap(act, include).descendingMap().values();
		} else {
			return lookup.tailMap(act, include).values();
		}
	}

	private <T extends IHasGLLayoutData> Pair<T, T> neighbors(IHasGLLayoutData node, EDimension dir, List<T> children) {
		if (dir.isVertical() || node == null)
			return Pair.make(null, null);
		int act = node.getLayoutDataAs(Integer.class, 0);
		NavigableMap<Integer, T> lookup = ImmutableSortedMap.copyOf(Maps.uniqueIndex(children,
				GLLayoutDatas.toLayoutData(Integer.class, Suppliers.ofInstance(0))));
		Entry<Integer, T> p = lookup.floorEntry(act - 1);
		Entry<Integer, T> a = lookup.ceilingEntry(act + 1);
		return Pair.make(p == null ? null : p.getValue(), a == null ? null : a.getValue());
	}


	private static void shift(Collection<? extends IGLLayoutElement> elems, float v, EDimension dim) {
		float v_x = dim.select(v, 0);
		float v_y = dim.select(0, v);
		for (IGLLayoutElement elem : elems) {
			Vec2f l = elem.getLocation();
			elem.setLocation(l.x() + v_x, l.y() + v_y);
		}
	}

	/**
	 * @param elem
	 */
	public void removeNode(Elem elem) {
		List<GLElement> children = Lists.newArrayList(this.iterator());
		for(EDimension dim : EDimension.values()) {
			Pair<GLElement, GLElement> neighbors = neighbors(elem, dim, children);
			if (neighbors.getFirst() == null && neighbors.getSecond() == null)
				continue;
			changes.add(new Removed(dim, dim.select(elem.getSize()), (Elem) neighbors.getFirst(), (Elem) neighbors
					.getSecond()));
		}
		remove(elem);
	}

	private interface IChange {

	}

	private class Resized implements IChange {
		private Elem node;
		private EDimension dim;
		public float scale;

		public Resized(Elem node, EDimension dim, float scale) {
			this.node = node;
			this.dim = dim;
			this.scale = scale;
		}

	}

	private class Added implements IChange {
		private Elem node;

		public Added(Elem node) {
			this.node = node;
		}

	}

	private class Removed implements IChange {
		private EDimension dim;
		private float size;
		private Elem left, right;
		public Removed(EDimension dim, float size, Elem left, Elem right) {
			this.dim = dim;
			this.size = size;
			this.left = left;
			this.right = right;
		}
	}

	private class Elem extends PickableGLElement {
		private final Color color = ColorBrewer.Set3.get(5).get(random.nextInt(5));
		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(color).fillRect(0, 0, w, h);
		}

		@Override
		protected void onDoubleClicked(Pick pick) {
			removeNode(this);
		}

		@Override
		protected void onMouseWheel(Pick pick) {
			final IMouseEvent m = (IMouseEvent) pick;
			float s = m.getWheelRotation() > 0 ? 1.2f : 1 / 1.2f;
			changes.add(new Resized(this, EDimension.DIMENSION, s));
			relayoutParent();
		}
	}

	public static void main(String[] args) {
		GLSandBox.main(args, new TestLayout());
	}

}
