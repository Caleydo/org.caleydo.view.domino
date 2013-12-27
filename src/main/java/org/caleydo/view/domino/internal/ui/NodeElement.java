/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.DominoGraph;
import org.caleydo.view.domino.api.model.graph.ISortableNode;
import org.caleydo.view.domino.api.model.graph.IStratisfyingableNode;
import org.caleydo.view.domino.api.model.graph.NodeUIState;
import org.caleydo.view.domino.api.model.typed.ConcatedList;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.ui.NodeGroupElement.ENodeUIState;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeElement extends GLElementContainer implements IGLLayout2, IPickingListener, PropertyChangeListener,
		INodeElement {
	protected final INode node;
	private TypedGroupList dimData;
	private TypedGroupList recData;

	public NodeElement(INode node) {
		setLayout(this);
		this.node = node;
		this.node.addPropertyChangeListener(INode.PROP_TRANSPOSE, this);
		this.dimData = toData(EDimension.DIMENSION, node);
		this.recData = toData(EDimension.RECORD, node);

		Vec2f pos = node.getLayoutDataAs(Vec2f.class, null);
		if (pos != null)
			setLocation(pos.x(), pos.y());
		setPicker(GLRenderers.fillRect(Color.LIGHT_RED));

		setVisibility(EVisibility.PICKABLE);
		onPick(this);
		guessZoomSettings();
		rebuild();

		setSize((float) getSize(EDimension.DIMENSION), (float) getSize(EDimension.RECORD));
	}

	public NodeGroupElement getByID(int id) {
		for (NodeGroupElement elem : Iterables.filter(this, NodeGroupElement.class)) {
			if (elem.getID() == id)
				return elem;
		}
		return null;
	}

	@Override
	public INode asNode() {
		return node;
	}

	private static TypedList toList(EDimension dim, INode node) {
		if (node.hasDimension(dim) && node instanceof ISortableNode && ((ISortableNode) node).isSortable(dim))
			return TypedSets.sort(node.getData(dim), ((ISortableNode) node).getComparator(dim));
		else
			return node.getData(dim).asList();
	}

	private static TypedGroupList toData(EDimension dim, INode node) {
		if (node instanceof IStratisfyingableNode && ((IStratisfyingableNode) node).isStratisfyable(dim))
			return TypedGroupList.create(toList(dim, node), ((IStratisfyingableNode) node).getGroups(dim));
		return TypedGroupList.createUngrouped(toList(dim, node));
	}

	/**
	 *
	 */
	private void guessZoomSettings() {

	}

	@Override
	public boolean setData(EDimension dim, TypedGroupList data) {
		final TypedGroupList oldList = getData(dim);
		if (Objects.equals(oldList, data))
			return false;
		int old = oldList.size();
		if (dim.isHorizontal())
			dimData = data;
		else
			recData = data;
		rebuild();
		return old != data.size();
	}

	private void rebuild() {
		int i = 0;
		for (TypedListGroup dimGroup : dimData.getGroups()) {
			for (TypedListGroup recGroup : recData.getGroups()) {
				NodeGroupElement elem = getOrCreate(i++);
				elem.setData(dimGroup, recGroup);
			}
		}
		if (i < size())
			this.asList().subList(i, size()).clear();
		// update default state
		((NodeGroupElement) get(0)).setDefault(dimData.getGroups().size() == 1 && recData.getGroups().size() == 1);

		relayout();
	}

	private NodeGroupElement getOrCreate(int i) {
		if (i == size()) {
			this.add(new NodeGroupElement(node));
		}
		return (NodeGroupElement) get(i);
	}

	private TypedGroupList getData(EDimension dim) {
		return dim.select(dimData, recData);
	}

	private double[] getSizes(EDimension dim) {
		final int cols = getNumGroups(EDimension.DIMENSION);
		final int rows = getNumGroups(EDimension.RECORD);
		// complex case
		double[] maxCol = new double[dim.select(cols, rows)];
		for (int c = 0; c < cols; ++c) {
			for (int r = 0; r < rows; ++r) {
				int b = dim.select(c, r);
				double wi = getSize(dim, r, c);
				maxCol[b] = Math.max(maxCol[b], wi);
			}
		}
		return maxCol;
	}

	@Override
	public double getSize(EDimension dim) {
		double[] sizes = getSizes(dim);
		int sum = (sizes.length - 1) * 2;
		for (double size : sizes)
			sum += size;
		// sum = sum <= 0 ? 20 : sum;
		sum += dim.select(node.getUIState().getSizeChange());
		return sum;
	}

	private double getSize(EDimension dim, int row, int col) {
		return getNodeElement(row, col).getSize(dim);
	}

	private int getNumGroups(EDimension dim) {
		return getData(dim).getGroups().size();
	}

	private NodeGroupElement getNodeElement(int row, int col) {
		int cols = getNumGroups(EDimension.DIMENSION);
		int index = row * cols + col;
		return (NodeGroupElement) get(index);
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		// children are organized row based
		final int cols = getNumGroups(EDimension.DIMENSION);
		final int rows = getNumGroups(EDimension.RECORD);

		// simple case
		if (cols == 1 && rows == 1) {
			return GLLayouts.LAYERS.doLayout(children, w, h, parent, deltaTimeMs);
		}

		double[] colSizes = scale(getSizes(EDimension.DIMENSION), w, EDimension.DIMENSION);
		double[] rowSizes = scale(getSizes(EDimension.RECORD), h, EDimension.RECORD);

		float x = 0;
		for (int c = 0; c < cols; ++c) {
			double wc = colSizes[c];

			float y = 0;
			for (int r = 0; r < rows; ++r) {
				double hr = rowSizes[r];

				IGLLayoutElement elem = children.get(r * cols + c);
				double xshift = 0;
				double yshift = 0;
				elem.setBounds(x + (float) xshift, y + (float) yshift, (float) wc, (float) hr);
				y += hr + 2;
			}
			x += wc + 2;
		}

		return false;
	}

	/**
	 * @param sizes
	 * @param groups
	 * @param w
	 * @return
	 */
	private double[] scale(double[] sizes, float total, EDimension dim) {
		final List<TypedListGroup> groups = getData(dim).getGroups();
		final float shift = dim.select(node.getUIState().getSizeChange());
		int elems = 0;
		double rem = total - sizes.length + 1;
		for(int i = 0; i < sizes.length; ++i) {
			rem -= sizes[i];
			elems += groups.get(i).size();
		}
		if(rem <= 0)
			return sizes;
		if (elems == 0 && sizes.length == 1) {
			sizes[0] += rem;
			return sizes;
		}
		double s = rem / elems;
		for(int i = 0; i < sizes.length; ++i)
			sizes[i] += s * groups.get(i).size();
		return sizes;
	}

	@Override
	public void pick(Pick pick) {
		IMouseEvent event = ((IMouseEvent) pick);
		switch (pick.getPickingMode()) {
		case MOUSE_WHEEL:
			node.getUIState().zoom(event);
			break;
		default:
			break;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case INode.PROP_TRANSPOSE:
			NodeUIState s = node.getUIState();
			Vec2f z = s.getSizeChange();
			s.setSizeChange(z.y(), z.x());
			break;
		default:
			repaint();
		}
	}

	protected final DominoGraph findGraph() {
		return findParent(GraphElement.class).getGraph();
	}

	@Override
	public <T> T getLayoutDataAs(Class<T> clazz, Supplier<? extends T> default_) {
		if (clazz.isInstance(node))
			return clazz.cast(node);
		return super.getLayoutDataAs(clazz, default_);
	}

	/**
	 * @param move
	 */
	public void setState(ENodeUIState move) {
		for (NodeGroupElement elem : Iterables.filter(this, NodeGroupElement.class)) {
			elem.setState(move);
		}
	}

	public void removeGroup(TypedListGroup dimData, TypedListGroup recData) {
		final int dims = getNumGroups(EDimension.DIMENSION);
		final int recs = getNumGroups(EDimension.RECORD);

		if (dims == 1 && recs == 1)
			return; // last group

		if (dims > 1 && recs > 1) // can't remove only a cross just slices
			return;
		final EDimension toRemove = EDimension.get(dims > 1);
		ITypedCollection remaining = remove(getData(toRemove), toRemove.select(dimData, recData));

		final INode extracted = node.extract(node.getLabel(), toRemove.select(remaining, this.dimData),
				toRemove.select(this.recData, remaining));
		extracted.setLayoutData(getLocation());
		findGraph().replace(node, extracted);
	}

	/**
	 * @param data
	 * @param select
	 * @return
	 */
	private ITypedCollection remove(TypedGroupList data, TypedListGroup select) {
		List<TypedListGroup> groups = new ArrayList<>(data.getGroups());
		groups.remove(select);
		return new TypedList(new ConcatedList<>(groups), data.getIdType());
	}

	/**
	 * @return
	 */
	public boolean has2DimGroups() {
		return getNumGroups(EDimension.DIMENSION) > 1 && getNumGroups(EDimension.RECORD) > 1;
	}
}
