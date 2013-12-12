/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.domino.api.model.typed.IMultiTypedCollection;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.MultiTypedList;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * @author Samuel Gratzl
 *
 */
public class LinearBlock implements Iterable<NodeLayoutElement> {
	private Rect bounds = new Rect();
	private final EDimension dim;
	private final List<NodeLayoutElement> nodes = new ArrayList<>();

	private MultiTypedList data;

	public LinearBlock(EDimension dim) {
		this.dim = dim;
	}

	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	public void remove(NodeLayoutElement node) {
		if (this.nodes.remove(node))
			dirty();
	}

	/**
	 * @param nodee
	 * @return
	 */
	public boolean contains(NodeLayoutElement node) {
		return nodes.contains(node);
	}

	/**
	 *
	 */
	private void dirty() {
		// TODO Auto-generated method stub

	}

	public void add(NodeLayoutElement node) {
		if (this.nodes.add(node))
			dirty();

	}

	@Override
	public Iterator<NodeLayoutElement> iterator() {
		return Iterators.unmodifiableIterator(nodes.iterator());
	}

	public void setLocation(Vec2f xy) {
		bounds.xy(xy);
	}

	public void updateBounds(EDimension inDir) {
		float w = 0;
		float h = 0;
		for (NodeLayoutElement elem : nodes) {
			Vec2f size = elem.getSize();
			if (dim.isHorizontal()) {
				w += size.x();
				h = Math.max(h, size.y());
			} else {
				w = Math.max(w, size.x());
				h += size.y();
			}
		}
		if (inDir.isHorizontal())
			bounds.width(w);
		else
			bounds.height(h);
	}

	public void applyBounds(EDimension inDir) {
		float v = inDir.select(bounds.width(), bounds.height());
		for (NodeLayoutElement elem : nodes) {
			elem.setSize(inDir, v);
		}
	}

	public void resort() {
		if (this.data == null)
			update();
		else
			resortImpl(this.data);
	}

	/**
	 * @param data2
	 */
	private void resortImpl(IMultiTypedCollection data) {
		List<ITypedComparator> c = findComparators(Iterables.transform(nodes, ANodeUI.TO_NODE), dim);

		this.data = TypedSets.sort(data, c.toArray(new ITypedComparator[0]));
	}

	public void update() {
		if (nodes.isEmpty())
			return;
		Collection<TypedSet> sets = Collections2.transform(nodes, new Function<INodeUI, TypedSet>() {
			@Override
			public TypedSet apply(INodeUI input) {
				return input.asNode().getData(dim);
			}
		});
		MultiTypedSet union = TypedSets.unionDeep(sets.toArray(new TypedSet[0]));
		resortImpl(union);
	}

	private final List<ITypedComparator> findComparators(Iterable<INode> nodes, final EDimension dim) {
		List<ISortableNode> s = new ArrayList<>();
		for (ISortableNode node : Iterables.filter(nodes, ISortableNode.class)) {
			final int p = node.getSortingPriority(dim);
			if (p == ISortableNode.NO_SORTING)
				continue;
			s.add(node);
		}
		Comparator<ISortableNode> bySortingPriority = new Comparator<ISortableNode>() {
			@Override
			public int compare(ISortableNode o1, ISortableNode o2) {
				return o1.getSortingPriority(dim) - o2.getSortingPriority(dim);
			}
		};
		Collections.sort(s, bySortingPriority);
		return ImmutableList.copyOf(Iterables.transform(s, new Function<ISortableNode, ITypedComparator>() {
			@Override
			public ITypedComparator apply(ISortableNode input) {
				return input.getComparator(dim);
			}
		}));
	}

	public void apply() {
		for (NodeLayoutElement node : nodes) {
			node.setData(dim, data.slice(node.asNode().getIDType(dim)));
		}
	}

	/**
	 * @param transform
	 */
	public void addAll(Collection<NodeLayoutElement> elems) {
		this.nodes.addAll(elems);
		dirty();
	}

	/**
	 * @param shared
	 * @return
	 */
	public List<NodeLayoutElement> before(NodeLayoutElement shared) {
		return this.nodes.subList(0, nodes.indexOf(shared));
	}

	/**
	 * @param shared
	 * @return
	 */
	public List<NodeLayoutElement> after(NodeLayoutElement shared) {
		return this.nodes.subList(nodes.indexOf(shared) + 1, this.nodes.size());
	}
}
