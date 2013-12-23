/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.collection.EDataType;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin.ISelectionMixinCallback;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.AGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IHasGLLayoutData;
import org.caleydo.view.domino.api.model.graph.DominoGraph;
import org.caleydo.view.domino.api.model.graph.DominoGraph.EPlaceHolderFlag;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.graph.Edges;
import org.caleydo.view.domino.api.model.graph.ISortableNode;
import org.caleydo.view.domino.api.model.graph.NodeUIState;
import org.caleydo.view.domino.api.model.graph.Placeholder;
import org.caleydo.view.domino.internal.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.internal.event.ShowPlaceHoldersEvent;
import org.caleydo.view.domino.spi.model.graph.IDominoGraphListener;
import org.caleydo.view.domino.spi.model.graph.IEdge;
import org.caleydo.view.domino.spi.model.graph.INode;

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
public class DominoNodeLayer extends GLElementContainer implements IDominoGraphListener, IGLLayout2,
		Function<INode, INodeElement>, ISelectionMixinCallback {

	private final DominoGraph graph;
	private final Deque<IChange> changes = new ArrayDeque<>();
	private final Map<INode, NodeData> metaData = new IdentityHashMap<>();

	public final static IDCategory GRAPH_GROUP_CATEGORY = IDCategory.registerInternalCategory("dominoGroup");
	public final static IDType NODE_GROUP_IDTYPE = IDType.registerInternalType("dominoGroupNode",
 GRAPH_GROUP_CATEGORY,
			EDataType.INTEGER);

	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

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
		// setDefaultInTransition(new IInTransition() {
		// @Override
		// public Vec4f in(Vec4f to, float w, float h, float alpha) {
		// if (alpha >= 1)
		// return to.copy();
		// return new Vec4f(0, 0, 0, 0);
		// }
		// });
		// setDefaultOutTransition(new IOutTransition() {
		// @Override
		// public Vec4f out(Vec4f from, float w, float h, float alpha) {
		// return new Vec4f(0, 0, 0, 0);
		// }
		// });

		selections.add(DominoGraph.newNodeSelectionManager());
		selections.add(newNodeGroupSelectionManager());
	}

	/**
	 * @return
	 */
	public static SelectionManager newNodeGroupSelectionManager() {
		return new SelectionManager(NODE_GROUP_IDTYPE);
	}


	public boolean isSelected(INode node, SelectionType type) {
		return selections.get(0).checkStatus(type, node.getID());
	}

	public NodeGroupElement getGroupNodeById(int id) {
		for (NodeElement elem : Iterables.filter(this, NodeElement.class)) {
			NodeGroupElement g = elem.getByID(id);
			if (g != null)
				return g;
		}
		return null;
	}

	/**
	 * @param mouseOver
	 * @param b
	 */
	public void select(INode node, SelectionType type, boolean enable, boolean clear) {
		SelectionManager m = selections.get(0);
		if (clear)
			m.clearSelection(type);
		if (node != null) {
			if (enable)
				m.addToType(type, node.getID());
			else
				m.removeFromType(type, node.getID());
		}
		selections.fireSelectionDelta(m);
		onSelectionUpdate(m);
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vertexAdded(INode node, Collection<IEdge> edges) {
		INodeElement new_;
		if (node instanceof PlaceholderNode) {
			new_ = new PlaceholderNodeElement((PlaceholderNode) node);
			this.add((GLElement) new_);
			this.changes.add(new Added(new_));
		} else {
			new_ = new NodeElement(node);
			initListeners((NodeElement) new_);

			this.add((GLElement) new_);
			this.changes.add(new UpdateData((NodeElement) new_));
			this.changes.add(new Added(new_));
		}
	}

	private class NodeData implements PropertyChangeListener {
		private final INodeElement node;
		private LinearBlock blockDim;
		private LinearBlock blockRec;

		public NodeData(INodeElement node) {
			this.node = node;
		}

		public void setBlock(EDimension dim, LinearBlock block) {
			if (dim.isHorizontal())
				blockDim = block;
			else
				blockRec = block;
		}

		public LinearBlock getBlock(EDimension dim) {
			return dim.select(blockDim, blockRec);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			switch (evt.getPropertyName()) {
			case NodeUIState.PROP_ZOOM:
				Vec2f old = (Vec2f) evt.getOldValue();
				Vec2f new_ = (Vec2f) evt.getNewValue();
				for (EDimension dim : EDimension.values()) {
					if (dim.select(old) != dim.select(new_)) {
						resize(node, dim, (float) node.getSize(dim));
					}
				}
				relayout();
			}
		}
	}

	void resize(INodeElement r, EDimension dim, float value) {
		List<INode> neighbors = graph.walkAlong(dim.opposite(), r.asNode(), Edges.SAME_SORTING);
		for (INode node : neighbors) {
			INodeElement rn = apply(node);
			if (rn == null)
				continue;
			changes.add(new Resized(rn, dim, value));
		}

		relayout();
	}
	/**
	 * @param new_
	 */
	private void initListeners(final NodeElement node) {
		NodeData data = getMetaData(node);
		NodeUIState state = node.asNode().getUIState();
		state.addPropertyChangeListener(data);
	}

	/**
	 * @param asNode
	 * @return
	 */
	private NodeData getMetaData(INodeElement node) {
		INode n = node.asNode();
		if (!metaData.containsKey(n)) {
			metaData.put(n, new NodeData(node));
		}
		return metaData.get(n);
	}

	/**
	 * @param elem
	 */
	private void removeListener(NodeElement node) {
		NodeData data = getMetaData(node);
		NodeUIState state = node.asNode().getUIState();
		state.removePropertyChangeListener(data);
	}

	private Collection<IChange> updateData(Collection<IChange> changes, NodeElement new_) {
		// check data changes
		List<IChange> r = new ArrayList<>(2);
		for (EDimension dim : EDimension.values()) {
			List<INode> nodes = filterPlaceholder(graph.walkAlong(dim, new_.asNode(), Edges.SAME_SORTING));
			if (nodes.size() <= 1)
				continue;
			IChange cr = updateDataImpl(changes, dim, new_, nodes);
			if (cr != null)
				r.add(cr);
		}
		return r;
	}

	/**
	 * @param walkAlong
	 * @return
	 */
	private List<INode> filterPlaceholder(List<INode> walkAlong) {
		if (walkAlong.size() <= 1)
			return walkAlong;
		int i = walkAlong.get(0) instanceof PlaceholderNode ? 1 : 0;
		int j = walkAlong.size() + (walkAlong.get(walkAlong.size() - 1) instanceof PlaceholderNode ? -1 : 0);
		return walkAlong.subList(i, j);
	}

	private IChange updateDataImpl(Collection<IChange> changes, EDimension dim, INodeElement new_,
			Collection<INode> nodes) {
		ImmutableList<INodeElement> anodes = ImmutableList.copyOf(Collections2.transform(nodes, this));
		// we have to adapt the values
		EDimension toAdapt = dim.opposite();
		Resized return_ = null;
		LinearBlock b = new LinearBlock(toAdapt, anodes);
		for (INodeElement elem : anodes) {
			getMetaData(elem).setBlock(dim, b);
		}
		b.update();
		BitSet changed = b.apply();

		for (int i = changed.nextSetBit(0); i != -1; i = changed.nextSetBit(i + 1)) {
			final INodeElement nodei = anodes.get(i);
			final Resized r = new Resized(nodei, toAdapt, (float) nodei.getSize(toAdapt));
			if (new_ == nodei)
				return_ = r;
			else
				this.changes.add(r);
		}
		return return_;
	}

	@Override
	public void vertexRemoved(INode node, Collection<IEdge> edges) {
		INodeElement elem = apply(node);
		if (elem == null)
			return;
		this.remove((GLElement) elem);

		select(node, SelectionType.SELECTION, false, false);

		if (elem instanceof NodeElement)
			removeListener(((NodeElement) elem));
		Vec2f size = elem.getSize();
		edges = Collections2.filter(edges, Edges.SAME_SORTING);
		for(EDimension dim : EDimension.values()) {
			Pair<INodeElement, INodeElement> leftRight = extract(edges, node, dim);
			INodeElement l = leftRight.getFirst();
			INodeElement r = leftRight.getSecond();
			if (l == null && r == null)
				continue;
			this.changes.add(new Removed(dim, dim.select(size), l, r));
			if (elem instanceof NodeElement)
				this.changes.add(new UpdateRemovedData(dim, (NodeElement) elem, l, r));
		}
	}

	@Override
	public void vertexSortingChanged(ISortableNode vertex, EDimension dim, boolean stratisfy) {
		NodeData data = getMetaData(apply(vertex));
		LinearBlock block = data.getBlock(dim.opposite()); // since we sort vertically but have a horizontal block
		if (block != null) {
			block.resort();
			block.apply();
		}
	}

	private Pair<INodeElement, INodeElement> extract(Collection<IEdge> edges, INode vertex, EDimension dim) {
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

		final Map<GLElement, ? extends IGLLayoutElement> lookup = Maps.uniqueIndex(children,
				AGLLayoutElement.TO_GL_ELEMENT);
		final Map<INode, ? extends IGLLayoutElement> lookup2 = Maps.uniqueIndex(children,
				GLLayoutDatas.toLayoutData(INode.class, null));
		changes.clear();
		final int offset = 4;
		while (!change.isEmpty()) {
			IChange next = change.pollFirst();
			if (next instanceof Resized) {
				Resized s = (Resized) next;
				cmdResize(s, change, lookup, children);
				// change.addAll(resizeAll(node, v_new, dim, children, shift));
			} else if (next instanceof UpdateData) {
				UpdateData r = (UpdateData) next;
				cmdUpdateData(r);
			} else if (next instanceof Added) {
				Added r = (Added) next;
				cmdAdded(r, children, lookup, lookup2, offset);
			} else if (next instanceof Removed) {
				Removed r = (Removed) next;
				cmdRemove(r, children, lookup);
			} else if (next instanceof UpdateRemovedData) {
				UpdateRemovedData r = (UpdateRemovedData) next;
				cmdRemoveUpdateData(r);
			}
		}
		return false;
	}

	private void cmdRemoveUpdateData(UpdateRemovedData r) {
		List<INode> ls = filterPlaceholder(graph.walkAlong(EDirection.getPrimary(r.dim),
				r.left == null ? null : r.left.asNode(), Edges.SAME_SORTING));
		List<INode> rs = filterPlaceholder(graph.walkAlong(EDirection.getPrimary(r.dim).opposite(),
				r.right == null ? null : r.right.asNode(), Edges.SAME_SORTING));
		if ((ls.size() + rs.size()) < 1)
			return;
		updateDataImpl(changes, r.dim, r.elem, ImmutableList.<INode> builder().addAll(ls).addAll(rs).build());
	}

	private void cmdRemove(Removed r, List<? extends IGLLayoutElement> children,
			Map<GLElement, ? extends IGLLayoutElement> lookup) {
		IGLLayoutElement left = lookup.get(r.left);
		IGLLayoutElement right = lookup.get(r.right);
		move(-r.size, r.dim, left, right, children);
	}

	private void cmdAdded(Added r, List<? extends IGLLayoutElement> children,
			Map<GLElement, ? extends IGLLayoutElement> lookup, Map<INode, ? extends IGLLayoutElement> lookup2,
			final int offset) {
		IGLLayoutElement node = lookup.get(r.node);
		if (node == null)
			return;
		Vec2f size = node.getSetSize();
		final INode nnode = r.node.asNode();
		INode leftN = graph.getNeighbor(EDirection.LEFT_OF, nnode, Edges.SAME_SORTING);
		INode rightN = graph.getNeighbor(EDirection.RIGHT_OF, nnode, Edges.SAME_SORTING);
		INode aboveN = graph.getNeighbor(EDirection.ABOVE, nnode, Edges.SAME_SORTING);
		INode belowN = graph.getNeighbor(EDirection.BELOW, nnode, Edges.SAME_SORTING);

		if (leftN != null || rightN != null) {
			size.setY(lookup2.get(leftN == null ? rightN : leftN).getHeight());
		}
		if (belowN != null || aboveN != null) {
			size.setX(lookup2.get(aboveN == null ? belowN : aboveN).getWidth());
		}

		move(node, size.x(), EDimension.DIMENSION, children);
		move(node, size.y(), EDimension.RECORD, children);

		node.setSize(size.x(), size.y());

		if (leftN != null) {
			Rect left = lookup2.get(leftN).getRectBounds();
			node.setLocation(left.x2() + offset, left.y());
		} else if (rightN != null) {
			Rect right = lookup2.get(rightN).getRectBounds();
			node.setLocation(right.x() - size.x() - offset, right.y());
		}

		if (aboveN != null) {
			Rect left = lookup2.get(aboveN).getRectBounds();
			node.setLocation(left.x(), left.y2() + offset);
		} else if (belowN != null) {
			Rect right = lookup2.get(belowN).getRectBounds();
			node.setLocation(right.x(), right.y() - size.y() - offset);
		}
	}

	private void cmdUpdateData(UpdateData r) {
		changes.addAll(updateData(changes, r.node));
	}

	/**
	 * @param s
	 * @param change
	 * @param children
	 * @param lookup
	 */
	private void cmdResize(Resized s, Deque<IChange> change, Map<GLElement, ? extends IGLLayoutElement> lookup,
			List<? extends IGLLayoutElement> children) {
		IGLLayoutElement node = lookup.get(s.node);
		if (node == null)
			return;
		final INode nnode = s.node.asNode();
		final EDimension dim = s.dim;
		float v = dim.select(node.getWidth(), node.getHeight());
		float v_new = s.new_;
		float v_delta = v_new - v;
		if (v_delta == 0)
			return;
		move(node, v_delta, dim, children);

		node.setSize(dim.select(v_new, node.getWidth()), dim.select(node.getHeight(), v_new));

		final Vec2f loc = node.getLocation();
		float shift = 0;
		List<INode> neighbors = graph.walkAlong(dim.opposite(), nnode, Edges.SAME_SORTING);
		boolean anyLeft = false;
		for (INode neighor : neighbors) {
			if (graph.getNeighbor(EDirection.getPrimary(dim), neighor, Edges.SAME_SORTING) != null) {
				anyLeft = true;
				break;
			}
		}
		boolean anyRight = false;
		for (INode neighor : neighbors) {
			if (graph.getNeighbor(EDirection.getPrimary(dim).opposite(), neighor, Edges.SAME_SORTING) != null) {
				anyRight = true;
				break;
			}
		}
		if (anyLeft == anyRight)
			shift = v_delta * 0.5f;
		else if (anyRight)
			shift = v_delta;
		node.setLocation(loc.x() - dim.select(shift, 0), loc.y() - dim.select(0, shift));
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
		Set<Placeholder> placeholders = graph.findPlaceholders(event.getNode(), EPlaceHolderFlag.INCLUDE_BETWEEN_BAND,
				EPlaceHolderFlag.INCLUDE_TRANSPOSE);
		graph.insertPlaceholders(placeholders, event.getNode());
	}

	@ListenTo(sendToMe = true)
	private void onHidePlaceHoldersEvent(HidePlaceHoldersEvent event) {
		graph.removePlaceholders(ImmutableSet.copyOf(Iterables.filter(graph.vertexSet(), PlaceholderNode.class)));
	}

	public Rect getBounds(Iterable<INode> nodes) {
		Rectangle2D r = null;
		for (INode n : nodes) {
			INodeElement elem = apply(n);
			if (elem == null)
				continue;
			if (r == null) {
				r = elem.getRectangleBounds();
			} else
				Rectangle2D.union(r, elem.getRectangleBounds(), r);
		}
		if (r == null)
			return null;
		return new Rect((float) r.getX(), (float) r.getY(), (float) r.getWidth(), (float) r.getHeight());
	}


	/**
	 * @return
	 */
	public Iterable<INodeElement> getNodes() {
		return Iterables.filter(this, INodeElement.class);
	}

	/**
	 * @param n
	 * @return
	 */
	@Override
	public INodeElement apply(INode n) {
		if (n == null)
			return null;
		for (INodeElement elem : getNodes())
			if (elem.asNode() == n)
				return elem;
		return null;
	}

	private interface IChange {

	}

	private class Resized implements IChange {
		public final INodeElement node;
		public final EDimension dim;
		public final float new_;

		public Resized(INodeElement node, EDimension dim, float new_) {
			this.node = node;
			this.dim = dim;
			this.new_ = new_;
		}

	}

	private class Added implements IChange {
		public final INodeElement node;

		public Added(INodeElement node) {
			this.node = node;
		}

	}

	private class UpdateData implements IChange {
		public final NodeElement node;

		public UpdateData(NodeElement node) {
			this.node = node;
		}

	}

	private class Removed implements IChange {
		public final EDimension dim;
		public final float size;
		public final INodeElement left, right;

		public Removed(EDimension dim, float size, INodeElement left, INodeElement right) {
			this.dim = dim;
			this.size = size;
			this.left = left;
			this.right = right;
		}
	}

	private class UpdateRemovedData implements IChange {
		public final EDimension dim;
		public final NodeElement elem;
		public final INodeElement left, right;

		public UpdateRemovedData(EDimension dim, NodeElement elem, INodeElement left, INodeElement right) {
			this.dim = dim;
			this.elem = elem;
			this.left = left;
			this.right = right;
		}

	}
}
