/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import gleem.linalg.Vec2f;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.caleydo.core.view.opengl.util.text.ETextStyle;
import org.caleydo.view.rnb.api.model.EDirection;
import org.caleydo.view.rnb.api.model.typed.IMultiTypedCollection;
import org.caleydo.view.rnb.api.model.typed.ITypedComparator;
import org.caleydo.view.rnb.api.model.typed.ITypedGroup;
import org.caleydo.view.rnb.api.model.typed.MultiTypedList;
import org.caleydo.view.rnb.api.model.typed.MultiTypedSet;
import org.caleydo.view.rnb.api.model.typed.TypedCollections;
import org.caleydo.view.rnb.api.model.typed.TypedGroupList;
import org.caleydo.view.rnb.api.model.typed.TypedGroups;
import org.caleydo.view.rnb.api.model.typed.TypedList;
import org.caleydo.view.rnb.api.model.typed.TypedListGroup;
import org.caleydo.view.rnb.api.model.typed.TypedSet;
import org.caleydo.view.rnb.api.model.typed.TypedSetGroup;
import org.caleydo.view.rnb.api.model.typed.TypedSets;
import org.caleydo.view.rnb.api.model.typed.util.RepeatingList;
import org.caleydo.view.rnb.internal.band.ShearedRect;
import org.caleydo.view.rnb.internal.data.VisualizationTypeOracle;

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

	public enum ESortingMode {
		INC, DEC, STRATIFY;

		/**
		 * @return
		 */
		public ESortingMode next() {
			switch (this) {
			case INC:
				return DEC;
			case DEC:
				return null;
			case STRATIFY:
				return INC;
			}
			throw new IllegalStateException();
		}
	}

	private final List<Pair<Node, ESortingMode>> sortCriteria = new ArrayList<>(2);
	private Node dataSelection = null;

	private MultiTypedList data;

	private boolean hasLeftBand, hasRightBand;

	public LinearBlock(EDimension dim, Node node) {
		this.dim = dim;
		this.nodes.add(node);
		this.sortCriteria.add(Pair.make(node, stratifyByDefault(node) ? ESortingMode.STRATIFY : ESortingMode.INC));
		this.dataSelection = node.getData(dim.opposite()).size() <= 1 ? null : node;
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
	 * @param hasLeftBand
	 *            setter, see {@link hasLeftBand}
	 */
	public void setHasLeftBand(boolean hasLeftBand) {
		this.hasLeftBand = hasLeftBand;
	}

	/**
	 * @param hasRightBand
	 *            setter, see {@link hasRightBand}
	 */
	public void setHasRightBand(boolean hasRightBand) {
		this.hasRightBand = hasRightBand;
	}

	/**
	 * @param node
	 * @return
	 */
	private boolean stratifyByDefault(Node node) {
		String type = node.getVisualizationType(true);
		return VisualizationTypeOracle.stratifyByDefault(type);
	}

	public boolean isStratisfied() {
		return sortCriteria.get(0).getSecond() == ESortingMode.STRATIFY;
	}

	public boolean isStratisfied(Node node) {
		return isStratisfied() && getFirstSortingCriteria() == node;
	}

	public ESortingMode getSortingMode(Node node) {
		int index = getSortPriority(node);
		if (index < 0)
			return null;
		return sortCriteria.get(index).getSecond();
	}

	/**
	 * @return the sortCriteria, see {@link #sortCriteria}
	 */
	public List<Pair<Node, ESortingMode>> getSortCriteria() {
		return Collections.unmodifiableList(sortCriteria);
	}

	public Rect getBounds() {
		assert nodes.size() > 0;
		Vec2f shift = nodes.get(0).getBlock().getLocation();
		Rect r = nodes.get(0).getDetachedRectBounds();
		for (Node elem : nodes) {
			r = Rect.union(r, elem.getDetachedRectBounds());
		}
		r.xy(shift.plus(r.xy()));
		return r;
	}

	/**
	 * @return
	 */
	public ShearedRect getShearedBounds() {
		Vec2f shift = nodes.get(0).getBlock().getLocation();
		final Rect r = getNode(true).getDetachedRectBounds().clone();
		if (size() == 1) {
			r.xy(shift.plus(r.xy()));
			return new ShearedRect(r);
		}
		Rect first = r;
		Rect last = getNode(false).getDetachedRectBounds();
		float x = first.x() + shift.x();
		float y = first.y() + shift.y();
		float x2 = last.x2() + shift.x();
		float y2 = last.y2() + shift.y();
		float shearX = dim.isHorizontal() ? 0 : last.x2() - first.x2();
		float shearY = dim.isVertical() ? 0 : last.y2() - first.y2();
		return new ShearedRect(x, y, x2, y2, shearX, shearY);
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
	public void addPlaceholdersFor(Node node, List<APlaceholder> r) {
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

	private void addPlaceHolders(List<APlaceholder> r, boolean normal, boolean transposed, Node n, EDirection dir) {
		if (normal)
			r.add(new Placeholder(n, dir, false));
		if (transposed && !normal)
			r.add(new Placeholder(n, dir, true));
		if (!dir.isPrimaryDirection())
			r.add(new FreePlaceholder(dir, n, transposed));
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
		final EDirection dir = EDirection.getPrimary(dim);
		if (index == 0) {
			nodes.get(0).setNeighbor(dir, null);
		} else if (index >= nodes.size()) {
			nodes.get(index - 1).setNeighbor(dir.opposite(), null);
		} else {
			final Node last = nodes.get(index);
			nodes.get(index - 1).setNeighbor(dir.opposite(), last);
			last.setNeighbor(dir, nodes.get(index - 1));
		}
		int i = getSortPriority(node);
		if (i >= 0)
			sortCriteria.remove(i);

		if (dataSelection == node) {
			dataSelection = nodes.size() == 1 ? nodes.get(0) : null;
		}
		if (sortCriteria.isEmpty() && !nodes.isEmpty())
			sortCriteria.add(Pair.make(nodes.get(0), ESortingMode.INC));
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
		node.setNeighbor(dir.opposite(), neighbor);
		node.setNeighbor(dir, old);
		if (old != null)
			old.setNeighbor(dir.opposite(), node);

		if (dir.isPrimaryDirection())
			this.nodes.add(index, node);
		else
			this.nodes.add(index + 1, node);

		sortCriteria.add(Pair.make(node, ESortingMode.INC));
		update();
		apply();
	}


	public void updateNeighbors() {
		Node prev = null;
		EDirection dir = EDirection.getPrimary(dim);
		for (Node node : nodes) {
			node.setNeighbor(dir, prev);
			if (prev != null)
				prev.setNeighbor(dir.opposite(), node);
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
		return ImmutableList.copyOf(Iterables.transform(sortCriteria,
				new Function<Pair<Node, ESortingMode>, ITypedComparator>() {
			@Override
					public ITypedComparator apply(Pair<Node, ESortingMode> input) {
						ITypedComparator c = input.getFirst().getComparator(dim);
						if (input.getSecond() == ESortingMode.DEC)
							c = TypedCollections.reverseOrder(c);
						return c;
			}
		}));
	}

	public void apply() {
		List<? extends ITypedGroup> g = asGroupList();

		int ngroups = g.size();
		{
			Node bak = nodes.get(0);
			bak.prepareData(dim.opposite(), ngroups);
			for (Node node : nodes.subList(1, nodes.size())) {
				node.prepareData(dim.opposite(), ngroups);
				node.updateNeighbor(EDirection.getPrimary(dim), bak, dim.opposite(), ngroups);
				bak = node;
			}
		}
		for (Node node : nodes) {
			final TypedList slice = data.slice(node.getIdType(dim.opposite()));
			node.setData(dim.opposite(), TypedGroupList.create(slice, g));
		}

	}

	private List<? extends ITypedGroup> asGroupList() {
		if (!isStratisfied())
			return Collections.singletonList(ungrouped(data.size()));
		final Node sortedBy = getFirstSortingCriteria();
		// FIXME
		final ESortingMode sortingMode = sortCriteria.get(0).getSecond();
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
	public TypedGroupList getData() {
		if (dataSelection != null) // use this as we have no missing values here
			return dataSelection.getData(dim.opposite());
		// best guess the first sorting criteria
		return getFirstSortingCriteria().getData(dim.opposite());
	}

	Node getNode(boolean first) {
		return first ? nodes.get(0) : nodes.get(nodes.size() - 1);
	}

	public INodeLocator getNodeLocator(boolean first) {
		Node n = getNode(first);
		return n.getNodeLocator(dim.opposite());
	}

	public List<Pair<Node, ESortingMode>> sortBy(List<Pair<Node, ESortingMode>> sortCriteria) {
		List<Pair<Node, ESortingMode>> act = new ArrayList<>(this.sortCriteria);
		this.sortCriteria.clear();
		this.sortCriteria.addAll(sortCriteria);
		update();
		apply();
		return act;
	}

	/**
	 * @param node
	 * @param forceStratify
	 */
	public List<Pair<Node, ESortingMode>> sortBy(Node node, ESortingMode mode) {
		List<Pair<Node, ESortingMode>> act = new ArrayList<>(this.sortCriteria);

		int index = getSortPriority(node);
		ESortingMode actMode = index < 0 ? null : sortCriteria.get(index).getSecond();
		if (actMode == mode)
			mode = mode == null ? ESortingMode.INC : mode.next();

		if (nodes.size() == 1) {
			this.sortCriteria.set(0, Pair.make(getFirstSortingCriteria(), mode));
			update();
			apply();
			return act;
		}

		if (mode == ESortingMode.STRATIFY) {
			// reset first
			sortCriteria.set(0, Pair.make(getFirstSortingCriteria(), ESortingMode.INC));
		}
		if (index >= 0)
			sortCriteria.remove(index);
		if (mode != null)
			sortCriteria.add(0, Pair.make(node, mode));
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
		int index = getSortPriority(node);
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

	public int getSortPriority(Node node) {
		for (int i = 0; i < sortCriteria.size(); ++i)
			if (sortCriteria.get(i).getFirst() == node)
				return i;
		return -1;
	}

	private Pair<Node, ESortingMode> getSorting(Node node) {
		int index = getSortPriority(node);
		return index < 0 ? null : sortCriteria.get(index);
	}

	public boolean isLimitedTo(Node node) {
		return node == dataSelection;
	}

	public int getSortPriorities() {
		return this.sortCriteria.size();
	}

	public String getStateString(Node node) {
		int index = getSortPriority(node);
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

	public void renderNodeLabels(GLGraphics g, Rect bounds) {

		// FIXME
		for (Node node : nodes) {
			if (node.has(dim)) {// handled by another
				if (dim.isHorizontal())
					render2DLabel(node, g, bounds);
				continue;
			}
			Rect b = node.getRectBounds();
			final String l = node.getLabel();
			if (dim.isVertical()) { // right
				renderHorText(l, g, bounds, b);
			} else { // top
				final boolean alone = node.getNeighbor(EDirection.WEST) == null
						&& node.getNeighbor(EDirection.EAST) == null;
				renderVertLabel(l, g, bounds, b,
						alone);
			}
		}
	}

	private static void renderHorText(final String l, GLGraphics g, Rect bounds, Rect b) {
		g.drawText(l, bounds.x2() + 10, b.y() + (b.height() - Constants.LABEL_SIZE) * 0.5f, 400, Constants.LABEL_SIZE);
		if (bounds.x2() > b.x2() + 5) {
			g.color(Color.LIGHT_GRAY).drawLine(b.x2() + 5, b.y() + b.height() * 0.5f, bounds.x2(),
					b.y() + b.height() * 0.5f);
		}
	}

	private static void renderVertLabel(final String l, GLGraphics g, Rect bounds, Rect b, boolean alone) {
		renderVerticalText(g, l, b.x(), bounds.y(), b.width(), Constants.LABEL_SIZE, alone);
		if (bounds.y() < b.y() - 5) {
			g.color(Color.LIGHT_GRAY).drawLine(b.x() + b.width() * 0.5f, b.y() - 5, b.x() + b.width() * 0.5f,
					bounds.y());
		}
	}


	private static void render2DLabel(Node node, GLGraphics g, Rect bounds) {
		Rect b = node.getRectBounds();
		boolean nWest = node.getNeighbor(EDirection.WEST) != null;
		boolean nEast = node.getNeighbor(EDirection.EAST) != null;
		boolean nSouth = node.getNeighbor(EDirection.SOUTH) != null;
		boolean nNorth = node.getNeighbor(EDirection.NORTH) != null;
		boolean nHor = nWest || nEast;
		boolean nVer = nNorth || nSouth;

		final String l = node.getLabel();
		if (!nVer || (nHor && !nNorth)) {// north no vertical at all -> north or horizontal neighbors and north is free
			final boolean alone = node.getNeighbor(EDirection.WEST) == null
					&& node.getNeighbor(EDirection.EAST) == null;
			renderVertLabel(l, g, bounds, b, alone);
		} else if (!nHor || (nVer && !nEast)) // east render right side
			renderHorText(l, g, bounds, b);
		else if (!nWest) { // west
			g.drawText(l, bounds.x() - 10 - 400, b.y() + (b.height() - Constants.LABEL_SIZE) * 0.5f, 400,
					Constants.LABEL_SIZE, VAlign.RIGHT);
			if (bounds.x() < b.x() - 5) {
				g.color(Color.LIGHT_GRAY).drawLine(b.x() - 5, b.y() + b.height() * 0.5f, bounds.x(),
						b.y() + b.height() * 0.5f);
			}
		} else if (!nSouth) { // south
			renderVerticalDownText(g, l, b.x(), bounds.y2(), b.width(), Constants.LABEL_SIZE);
			if (bounds.y2() > b.y2() + 5) {
				g.color(Color.LIGHT_GRAY).drawLine(b.x() + b.width() * 0.5f, b.y2() + 5, b.x() + b.width() * 0.5f,
						bounds.y2());
			}
		}
		// all corners are filled with neighbors ? -> no idea where to draw the label
	}

	public void renderGroupLabels(GLGraphics g, Rect bounds) {
		final float textSize = Constants.LABEL_SIZE;
		if (dim.isHorizontal()) {
			List<NodeGroup> labels = getFirstSortingCriteria().getGroupNeighbors(EDirection.WEST);
			if (labels.size() <= 1)
				return;
			if (!hasRightBand || (hasRightBand && hasLeftBand)) {
				Node last = getNode(false);
				Rect b = last.getRectBounds();
				final List<NodeGroup> locations = last.getGroupNeighbors(EDirection.WEST);

				for (int i = 0; i < labels.size(); ++i) {
					Rect bg = locations.get(i).getRectBounds();
					g.drawText(labels.get(i).getLabel(), b.x2() + 10, b.y() + bg.y() + (bg.height() - textSize)
							* 0.5f, 400, textSize);
				}
			} else {
				Node first = getNode(true);
				Rect b = first.getRectBounds();
				final List<NodeGroup> locations = first.getGroupNeighbors(EDirection.WEST);

				for (int i = 0; i < labels.size(); ++i) {
					Rect bg = locations.get(i).getRectBounds();
					g.drawText(labels.get(i).getLabel(), b.x() - 10 - 400, b.y() + bg.y()
							+ (bg.height() - textSize) * 0.5f, 400, textSize, VAlign.RIGHT);
				}
			}
		} else {
			List<NodeGroup> labels = getFirstSortingCriteria().getGroupNeighbors(EDirection.NORTH);
			if (labels.size() <= 1)
				return;
			if (!hasRightBand || (hasRightBand && hasLeftBand)) {
				Node first = getNode(true);
				Rect b = first.getRectBounds();
				final List<NodeGroup> locations = first.getGroupNeighbors(EDirection.NORTH);
				for (int i = 0; i < labels.size(); ++i) {
					Rect bg = locations.get(i).getRectBounds();
					renderVerticalText(g, labels.get(i).getLabel(), b.x() + bg.x(), b.y(), bg.width(), textSize, false);
				}
			} else {
				Node first = getNode(false);
				Rect b = first.getRectBounds();
				final List<NodeGroup> locations = first.getGroupNeighbors(EDirection.NORTH);
				for (int i = 0; i < labels.size(); ++i) {
					Rect bg = locations.get(i).getRectBounds();
					renderVerticalDownText(g, labels.get(i).getLabel(), b.x() + bg.x(), b.y2(), bg.width(), textSize);
				}
			}
		}
	}

	private Node getFirstSortingCriteria() {
		return sortCriteria.get(0).getFirst();
	}

	private static void renderVerticalText(GLGraphics g, String text, float x, float y, float w, float textSize,
			boolean alone) {
		float tw = g.text.getTextWidth(text, textSize);
		if (tw < w) {
			g.drawText(text, x, y - textSize - 10, w, textSize, VAlign.CENTER);
		} else if (alone) {
			g.drawText(text, x - 100, y - textSize - 10, w + 200, textSize, VAlign.CENTER);
		} else {
			// shift it
			float angle = 45f; // TODO find out
			g.drawRotatedText(text, x, y - textSize - 5, 400, textSize, VAlign.LEFT, ETextStyle.PLAIN, -angle);
		}
	}

	private static void renderVerticalDownText(GLGraphics g, String text, float x, float y, float w, float textSize) {
		float tw = g.text.getTextWidth(text, textSize);
		if (tw < w) {
			g.drawText(text, x, y + textSize - 10, w, textSize, VAlign.CENTER);
		} else {
			// shift it
			float angle = 45f; // TODO find out
			g.drawRotatedText(text, x + 10, y + textSize - 30, tw + 20, textSize, VAlign.RIGHT, ETextStyle.PLAIN, angle);
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
			float offset = offsets.getOffset(n.getNeighbor(dir), n);
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
