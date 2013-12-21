/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.api.model.typed.IMultiTypedCollection;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.ITypedGroup;
import org.caleydo.view.domino.api.model.typed.MultiTypedList;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.caleydo.view.domino.internal.ui.prototype.IStratisfyingableNode;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * @author Samuel Gratzl
 *
 */
public class LinearBlock implements Iterable<INodeUI> {
	private final EDimension dim;
	private List<? extends ITypedGroup> groups;
	private MultiTypedList data;
	private final List<? extends INodeUI> nodes;

	public LinearBlock(EDimension dim, List<? extends INodeUI> nodes) {
		this.dim = dim;
		this.nodes = nodes;
	}

	@Override
	public Iterator<INodeUI> iterator() {
		return Iterators.filter(nodes.iterator(), INodeUI.class);
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
		List<ISortableNode> s = orderBy(Iterables.transform(nodes, ANodeUI.TO_NODE), dim);
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
		Collection<TypedSet> sets = Collections2.transform(nodes, new Function<INodeUI, TypedSet>() {
			@Override
			public TypedSet apply(INodeUI input) {
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
		List<ITypedGroup> g = asGroupList(data.size());

		for (INodeUI node : nodes) {
			final TypedList slice = data.slice(node.asNode().getIDType(dim));
			b.set(i++, node.setData(dim, TypedGroupList.create(slice, g)));
		}
		return b;
	}

	private List<ITypedGroup> asGroupList(final int size) {
		if (groups == null)
			return Collections.singletonList(ungrouped(size));
		int sum = 0;
		for (ITypedGroup group : groups)
			sum += group.size();
		List<ITypedGroup> g = new ArrayList<>(groups.size()+1);
		g.addAll(groups);
		if (sum < size)
			g.add(ungrouped(size - sum));
		return g;
	}

	private static ITypedGroup ungrouped(int size) {
		return TypedGroupList.createUngrouped(TypedCollections.INVALID_IDTYPE, size);
	}
}
