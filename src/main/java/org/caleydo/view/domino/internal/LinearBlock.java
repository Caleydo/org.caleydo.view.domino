/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.awt.geom.Rectangle2D;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.typed.IMultiTypedCollection;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.ITypedGroup;
import org.caleydo.view.domino.api.model.typed.MultiTypedList;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroups;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.api.model.typed.util.RepeatingList;
import org.caleydo.view.domino.internal.data.VisualizationTypeOracle;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class LinearBlock extends AbstractCollection<Node> {
	private EDimension dim;
	private final List<Node> nodes = new ArrayList<>();

	private List<Node> sortCriteria = new ArrayList<>(2);
	private Node dataSelection = null;

	private boolean stratified = false;
	private MultiTypedList data;

	private final Map<String, Float> scaleFactors = new HashMap<>();

	public LinearBlock(EDimension dim, Node node) {
		this.dim = dim;
		this.nodes.add(node);
		this.sortCriteria.add(node);
		this.dataSelection = node;
		this.stratified = stratifyByDefault(node);
		update();
		apply();
		updateNeighbors();
	}

	/**
	 *
	 */
	public void transposedMe() {
		this.dim = dim.opposite();
	}

	/**
	 * @param node
	 * @return
	 */
	private boolean stratifyByDefault(Node node) {
		String type = node.getVisualizationType();
		return VisualizationTypeOracle.stratifyByDefault(type);
	}

	public boolean isStratisfied() {
		return stratified;
	}

	public Rectangle2D getBounds() {
		Rectangle2D r = null;
		for (Node elem : nodes) {
			if (r == null) {
				r = elem.getDetachedRectBounds().asRectangle2D();
			} else
				Rectangle2D.union(r, elem.getDetachedRectBounds().asRectangle2D(), r);
		}
		return r;
	}

	public IDType getIdType() {
		return nodes.get(0).getIdType(dim.opposite());
	}

	public Node get(int index) {
		return nodes.get(index);
	}

	/**
	 * @param node
	 * @param r
	 */
	public void addPlaceholdersFor(Node node, List<Placeholder> r) {
		boolean normal = isCompatible(node.getIdType(dim.opposite()), getIdType());
		boolean transposed = isCompatible(node.getIdType(dim), getIdType());
		if (!normal && !transposed)
			return;
		Node n = nodes.get(0);
		final EDirection dir = EDirection.getPrimary(dim);
		if (n != node) {
			addPlaceHolders(r, normal, transposed, n, dir);
		} else if (nodes.size() > 1) {
			addPlaceHolders(r, normal, transposed, nodes.get(1), dir);
		}
		n = nodes.get(nodes.size()-1);
		if (n != node) {
			addPlaceHolders(r, normal, transposed, n, dir.opposite());
		} else if (nodes.size() > 1) {
			addPlaceHolders(r, normal, transposed, nodes.get(nodes.size() - 2), dir.opposite());
		}
	}

	private void addPlaceHolders(List<Placeholder> r, boolean normal, boolean transposed, Node n, EDirection dir) {
		if (normal)
			r.add(new Placeholder(n, dir, false));
		if (transposed && !normal)
			r.add(new Placeholder(n, dir, true));
	}

	private static boolean isCompatible(IDType a, IDType b) {
		return a.getIDCategory().isOfCategory(b);
	}
	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public Iterator<Node> iterator() {
		return nodes.iterator();
	}

	@Override
	public boolean add(Node node) {
		this.add(this.nodes.get(this.nodes.size() - 1), EDirection.getPrimary(dim).opposite(), node);
		return true;
	}

	/**
	 * @param node
	 */
	public int remove(Node node) {
		int index = nodes.indexOf(node);
		this.nodes.remove(index);
		if (nodes.isEmpty())
			return 0;
		// update neighbors
		if (index == 0) {
			nodes.get(0).setNeighbor(EDirection.getPrimary(dim), null);
		} else if (index >= nodes.size()) {
			nodes.get(index - 1).setNeighbor(EDirection.getPrimary(dim).opposite(), null);
		} else {
			nodes.get(index - 1).setNeighbor(EDirection.getPrimary(dim).opposite(), nodes.get(index));
		}

		sortCriteria.remove(node);
		if (dataSelection == node) {
			dataSelection = nodes.size() == 1 ? nodes.get(0) : null;
		}
		if (sortCriteria.isEmpty() && !nodes.isEmpty())
			sortCriteria.add(nodes.get(0));
		update();
		apply();
		return index;
	}


	/**
	 * @param neighbor
	 * @param dir
	 * @param node
	 * @param detached
	 */
	public void add(Node neighbor, EDirection dir, Node node) {
		int index = nodes.indexOf(neighbor);
		assert index >= 0;

		Node old = neighbor.getNeighbor(dir);
		neighbor.setNeighbor(dir, node);
		node.setNeighbor(dir, old);

		if (dir.isPrimaryDirection())
			this.nodes.add(index, node);
		else
			this.nodes.add(index + 1, node);

		sortCriteria.add(node);
		update();
		apply();
	}


	public void updateNeighbors() {
		Node prev = null;
		EDirection dir = EDirection.getPrimary(dim);
		for (Node node : nodes) {
			node.setNeighbor(dir, prev);
			prev = node;
		}
		nodes.get(nodes.size() - 1).setNeighbor(dir.opposite(), null);
	}

	public void doLayout(Map<GLElement, ? extends IGLLayoutElement> lookup) {

	}

	/**
	 * @param neighbor
	 * @return
	 */
	public boolean contains(Node node) {
		return nodes.contains(node);
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
		List<ITypedComparator> c = asComparators(dim.opposite());
		this.data = TypedSets.sort(data, c.toArray(new ITypedComparator[0]));
	}

	public void update() {
		if (nodes.isEmpty())
			return;
		MultiTypedSet union;

		if (dataSelection == null) {
			union = unionAll();
		} else {
			union = intersectSome();
		}
		resortImpl(union);
	}

	/**
	 * @return
	 */
	private MultiTypedSet intersectSome() {
		TypedSet dataSelection = this.dataSelection.getUnderlyingData(dim.opposite());
		Collection<TypedSet> all = Collections2.transform(nodes, new Function<Node, TypedSet>() {
			@Override
			public TypedSet apply(Node input) {
				return input.getUnderlyingData(dim.opposite());
			}
		});
		MultiTypedSet intersect = TypedSets.intersect(dataSelection);
		return intersect.expand(all);
	}

	private MultiTypedSet unionAll() {
		Collection<TypedSet> sets = Collections2.transform(nodes, new Function<Node, TypedSet>() {
			@Override
			public TypedSet apply(Node input) {
				return input.getUnderlyingData(dim.opposite());
			}
		});
		return TypedSets.unionDeep(sets.toArray(new TypedSet[0]));
	}

	private List<ITypedComparator> asComparators(final EDimension dim) {
		return ImmutableList.copyOf(Iterables.transform(sortCriteria, new Function<Node, ITypedComparator>() {
			@Override
			public ITypedComparator apply(Node input) {
				return input.getComparator(dim);
			}
		}));
	}

	public void apply() {
		List<? extends ITypedGroup> g = asGroupList();

		for (Node node : nodes) {
			final TypedList slice = data.slice(node.getIdType(dim.opposite()));
			node.setData(dim.opposite(), TypedGroupList.create(slice, g));
		}
		{
			Node bak = nodes.get(0);
			for (Node node : nodes.subList(1, nodes.size())) {
				node.updateNeighbor(EDirection.getPrimary(dim), bak);
			}
		}
	}

	private List<? extends ITypedGroup> asGroupList() {
		if (!isStratisfied())
			return Collections.singletonList(ungrouped(data.size()));
		final Node sortedBy = sortCriteria.get(0);
		final Node selected = dataSelection;

		List<TypedSetGroup> groups = sortedBy.getUnderlyingData(dim.opposite()).getGroups();
		if (selected == sortedBy) // 1:1 mapping
			return groups;
		List<ITypedGroup> g = new ArrayList<>(groups.size() + 1);
		if (selected == null) {
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
					g.add(new TypedListGroup(RepeatingList.repeat(TypedCollections.INVALID_ID, sum - bak), group
							.getIdType(), group.getLabel(), group.getColor()));
				}
			}
			if (sum < data.size())
				g.add(unmapped(data.size() - sum));
		} else {
			// we have data selection but a different grouping
			int sum = 0;
			TypedList gdata = data.slice(groups.get(0).getIdType());
			for (ITypedGroup group : groups) {
				int bak = sum;
				while (sum < gdata.size() && group.contains(gdata.get(sum)))
					sum++;
				if ((bak + groups.size()) == sum) { // no extra elems
					g.add(group);
				} else { // have repeating or less elements
					g.add(new TypedListGroup(RepeatingList.repeat(TypedCollections.INVALID_ID, sum - bak), group
							.getIdType(), group.getLabel(), group.getColor()));
				}
			}
			if (sum < data.size())
				g.add(unmapped(data.size() - sum));
		}
		return g;
	}

	private static ITypedGroup ungrouped(int size) {
		return TypedGroups.createUngroupedGroup(TypedCollections.INVALID_IDTYPE, size);
	}

	private static ITypedGroup unmapped(int size) {
		return TypedGroups.createUnmappedGroup(TypedCollections.INVALID_IDTYPE, size);
	}


	/**
	 * @param b
	 * @return
	 */
	public TypedGroupList getData(boolean first) {
		return getNode(first).getData(dim.opposite());
	}

	Node getNode(boolean first) {
		return first ? nodes.get(0) : nodes.get(nodes.size() - 1);
	}

	public INodeLocator getNodeLocator(boolean first) {
		Node n = getNode(first);
		return n.getNodeLocator(dim.opposite());
	}

	public Pair<List<Node>, Boolean> sortBy(List<Node> sortCriteria, boolean stratified) {
		Pair<List<Node>, Boolean> act = Pair.make((List<Node>) new ArrayList<>(sortCriteria), stratified);
		this.stratified = stratified;
		this.sortCriteria.clear();
		this.sortCriteria.addAll(sortCriteria);
		return act;
	}

	/**
	 * @param node
	 * @param forceStratify
	 */
	public Pair<List<Node>, Boolean> sortBy(Node node, boolean forceStratify) {
		Pair<List<Node>, Boolean> act = Pair.make((List<Node>) new ArrayList<>(sortCriteria), stratified);
		if (nodes.size() == 1) {
			this.stratified = !this.stratified;
			update();
			apply();
			return act;
		}
		int index = sortCriteria.indexOf(node);
		if (index == 0 && stratified) {
			stratified = false;
		} else if (index == 0 && forceStratify)
			stratified = true;
		else if (index == 0)
			sortCriteria.remove(index);
		if (index != 0) {
			sortCriteria.add(0, node);
			this.stratified = true;
		} else if (sortCriteria.isEmpty()) {
			for (Node n : nodes)
				if (n != node) {
					sortCriteria.add(n);
					stratified = true;
					break;
				}
		}

		update();
		apply();
		return act;
	}

	public Node limitDataTo(Node node) {
		if (nodes.size() == 1) {
			return dataSelection;
		}
		Node bak = dataSelection;
		// add if it was not part of
		if (dataSelection == node) {
			dataSelection = null;
		} else
			dataSelection = node;
		update();
		apply();
		return bak;
	}

	/**
	 * @param node
	 * @return
	 */
	public Color getStateColor(Node node) {
		int index = sortCriteria.indexOf(node);
		boolean limited = dataSelection == node;
		boolean stratified = index == 0 && isStratisfied();
		boolean sorted = index != -1;
		Color c = Color.GRAY;
		if (stratified) {
			c = Color.RED;
		} else if (sorted)
			c = Color.MAGENTA;
		if (limited)
			c = c.darker();
		return c;
	}

	public String getStateString(Node node) {
		int index = sortCriteria.indexOf(node);
		boolean limited = dataSelection == node;
		boolean stratified = index == 0 && isStratisfied();
		StringBuilder b = new StringBuilder();
		if (index >= 0)
			b.append(index + 1);
		if (stratified)
			b.append("*");
		if (limited)
			b.append("F");
		return b.toString();
	}

	public void renderLabels(GLGraphics g) {
		final float textSize = 14;
		// FIXME
		for (Node node : nodes) {
			if (node.has(dim)) // handled by another
				continue;
			Rect b = node.getRectBounds();
			if (dim.isVertical()) {
				g.drawText(node.getLabel(), b.x2() + 10, b.y() + (b.height() - textSize) * 0.5f, 400, textSize);
			} else {
				renderVerticalText(g, node.getLabel(), b.x(), b.y(), b.width(), textSize);
			}
		}
		if (dim.isHorizontal()) {
			Node last = getNode(false);
			Rect b = last.getRectBounds();
			for (NodeGroup group : sortCriteria.get(0).getGroupNeighbors(EDirection.EAST)) {
				Rect bg = group.getRectBounds();
				g.drawText(group.getLabel(), b.x2() + 10, b.y() + bg.y() + (bg.height() - textSize) * 0.5f, 400,
						textSize);
			}
		} else {
			Node first = getNode(true);
			Rect b = first.getRectBounds();
			for (NodeGroup group : sortCriteria.get(0).getGroupNeighbors(EDirection.NORTH)) {
				Rect bg = group.getRectBounds();
				renderVerticalText(g, group.getLabel(), b.x() + bg.x(), b.y(), bg.width(), textSize);
			}
		}
	}

	private static void renderVerticalText(GLGraphics g, String text, float x, float y, float w, float textSize) {
		float tw = g.text.getTextWidth(text, textSize);
		if (tw < w) {
			g.drawText(text, x, y - textSize - 10, w, textSize, VAlign.CENTER);
		} else {
			// shift it
			float angle = 45f; // TODO find out
			g.save().move(x, y - textSize - 5).gl.glRotatef(-angle, 0, 0, 1);
			g.drawText(text, 0, 0, 400, textSize);
			g.restore();
		}
	}

	/**
	 * @param startPoint
	 * @param offsets
	 */
	public void alignAlong(Node startPoint, OffsetShifts offsets) {
		if (nodes.size() <= 1)
			return;
		int start = nodes.indexOf(startPoint);
		Rect bounds = startPoint.getDetachedRectBounds();
		EDirection dir = EDirection.getPrimary(dim).opposite();
		for (int i = start - 1; i >= 0; --i) {
			Node n = nodes.get(i);
			Rect n_bounds = n.getDetachedRectBounds().clone();
			float offset = offsets.getOffset(n.getNeighbor(dir), n);
			if (dim.isDimension()) {
				float shift = -(bounds.height() - n_bounds.height()) * 0.5f;
				n.setDetachedBounds(bounds.x() - n_bounds.width() - offset, bounds.y() - shift,
						n_bounds.width(),
						n_bounds.height());
			} else {
				float shift = -(bounds.width() - n_bounds.width()) * 0.5f;
				n.setDetachedBounds(bounds.x() - shift, bounds.y() - n_bounds.height() - offset,
 n_bounds.width(),
						n_bounds.height());
			}
			bounds = n.getDetachedRectBounds();
		}
		bounds = startPoint.getDetachedRectBounds();
		dir = dir.opposite();
		for (int i = start + 1; i < nodes.size(); ++i) {
			Node n = nodes.get(i);
			Rect n_bounds = n.getDetachedRectBounds().clone();
			float offset = Math.max(offsets.getOffset(n.getNeighbor(dir), n), 1);
			if (dim.isDimension()) {
				float shift = (bounds.height() - n_bounds.height()) * 0.5f;
				n.setDetachedBounds(bounds.x2() + offset, bounds.y() + shift, n_bounds.width(),
 n_bounds.height());
			} else {
				float shift = (bounds.width() - n_bounds.width()) * 0.5f;
				n.setDetachedBounds(bounds.x() + shift, bounds.y2() + offset, n_bounds.width(),
						n_bounds.height());
			}
			bounds = n.getDetachedRectBounds();
		}
	}

}
