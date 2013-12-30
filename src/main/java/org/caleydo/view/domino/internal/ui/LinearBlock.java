/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.domino.api.model.graph.ISortableNode;
import org.caleydo.view.domino.api.model.graph.IStratisfyingableNode;
import org.caleydo.view.domino.api.model.typed.IMultiTypedCollection;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.ITypedGroup;
import org.caleydo.view.domino.api.model.typed.MultiTypedList;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.RepeatingList;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author Samuel Gratzl
 *
 */
public class LinearBlock implements Iterable<INodeElement> {
	private final EDimension dim;
	private final List<? extends INodeElement> nodes;

	private List<? extends ITypedGroup> groups;
	private MultiTypedList data;

	public LinearBlock(EDimension dim, List<? extends INodeElement> nodes) {
		this.dim = dim;
		this.nodes = nodes;
	}

	public INodeElement getFirst(Predicate<? super INodeElement> filter) {
		for (INodeElement elem : nodes)
			if (filter.apply(elem))
				return elem;
		return null;
	}

	public INodeElement getLast(Predicate<? super INodeElement> filter) {
		for (INodeElement elem : Lists.reverse(nodes))
			if (filter.apply(elem))
				return elem;
		return null;
	}

	public Rect getBounds() {
		Rectangle2D r = null;
		for (INodeElement elem : nodes) {
			if (r == null) {
				r = elem.getRectangleBounds();
			} else
				Rectangle2D.union(r, elem.getRectangleBounds(), r);
		}
		if (r == null)
			return null;
		return new Rect((float) r.getX(), (float) r.getY(), (float) r.getWidth(), (float) r.getHeight());
	}

	public boolean isStratisfied() {
		return groups != null && groups.size() > 1;
	}

	@Override
	public Iterator<INodeElement> iterator() {
		return Iterators.filter(nodes.iterator(), INodeElement.class);
	}

	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
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
		List<ISortableNode> s = orderBy(Iterables.transform(nodes, new Function<INodeElement, INode>() {
			@Override
			public INode apply(INodeElement input) {
				return input.asNode();
			}
		}), dim);
		List<ITypedComparator> c = asComparators(dim, s);
		this.data = TypedSets.sort(data, c.toArray(new ITypedComparator[0]));
		IStratisfyingableNode groupBy = (!s.isEmpty() && s.get(0) instanceof IStratisfyingableNode) ? (IStratisfyingableNode) s
				.get(0) : null;
		if (groupBy != null && groupBy.isStratisfied(dim))
			this.groups = groupBy.getGroups(dim);
		else
			this.groups = null;
	}

	public void update() {
		if (nodes.isEmpty())
			return;
		Collection<TypedSet> sets = Collections2.transform(nodes, new Function<INodeElement, TypedSet>() {
			@Override
			public TypedSet apply(INodeElement input) {
				return input.asNode().getData(dim);
			}
		});
		MultiTypedSet union = TypedSets.unionDeep(sets.toArray(new TypedSet[0]));
		resortImpl(union);
	}

	private List<ITypedComparator> asComparators(final EDimension dim, List<ISortableNode> s) {
		return ImmutableList.copyOf(Iterables.transform(s, new Function<ISortableNode, ITypedComparator>() {
			@Override
			public ITypedComparator apply(ISortableNode input) {
				return input.getComparator(dim);
			}
		}));
	}

	private List<ISortableNode> orderBy(Iterable<INode> nodes, final EDimension dim) {
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
		return s;
	}

	public BitSet apply() {
		BitSet b = new BitSet();
		int i = 0;
		List<ITypedGroup> g = asGroupList();

		for (INodeElement node : nodes) {
			final TypedList slice = data.slice(node.asNode().getIDType(dim));
			b.set(i++, node.setData(dim, TypedGroupList.create(slice, g)));
		}
		return b;
	}

	private List<ITypedGroup> asGroupList() {
		if (groups == null)
			return Collections.singletonList(ungrouped(data.size()));
		List<ITypedGroup> g = new ArrayList<>(groups.size() + 1);
		int sum = 0;
		TypedList gdata = data.slice(groups.get(0).getIdType());
		for (ITypedGroup group : groups) {
			int bak = sum;
			sum += group.size();
			while (sum < gdata.size() && group.contains(gdata.get(sum)))
				sum++;
			if ((bak + groups.size()) == sum) { // no extra elems
				g.add(group);
			} else { // have repeating elements
				g.add(new TypedListGroup(new RepeatingList<>(TypedCollections.INVALID_ID, sum - bak),
						group.getIdType(), group.getLabel(), group.getColor()));
			}
		}
		if (sum < data.size())
			g.add(unmapped(data.size() - sum));
		return g;
	}

	private static ITypedGroup ungrouped(int size) {
		return TypedGroupList.createUngroupedGroup(TypedCollections.INVALID_IDTYPE, size);
	}

	private static ITypedGroup unmapped(int size) {
		return TypedGroupList.createUnmappedGroup(TypedCollections.INVALID_IDTYPE, size);
	}
}
