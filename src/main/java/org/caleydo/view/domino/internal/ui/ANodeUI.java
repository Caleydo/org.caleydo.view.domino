/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.ui.model.NodeUIState;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.ISortableNode;
import org.caleydo.view.domino.internal.ui.prototype.IStratisfyingableNode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ANodeUI<T extends INode> extends GLElementContainer implements PropertyChangeListener, INodeUI,
		Predicate<String>, IGLLayout2 {
	protected final T node;

	private TypedGroupList dimData;
	private TypedGroupList recData;
	private boolean rebuild = false;

	public ANodeUI(T node) {
		setLayout(this);
		this.node = node;
		this.dimData = toData(EDimension.DIMENSION, node);
		this.recData = toData(EDimension.RECORD, node);
		setLayoutData(node);
		build();
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

	@Override
	public final T asNode() {
		return node;
	}

	protected abstract String getExtensionID();

	@Override
	public void layout(int deltaTimeMs) {
		if (rebuild) {
			rebuild = false;
			build();
		}
		super.layout(deltaTimeMs);
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

		double[] colSizes = getSizes(EDimension.DIMENSION);
		double[] rowSizes = getSizes(EDimension.RECORD);

		float x = 0;
		for (int c = 0; c < cols; ++c) {
			double wc = colSizes[c];

			float y = 0;
			for (int r = 0; r < rows; ++r) {
				double hr = rowSizes[r];

				IGLLayoutElement elem = children.get(r * cols + c);
				double wi = getSize(EDimension.DIMENSION, r, c);
				double hi = getSize(EDimension.RECORD, r, c);
				double xshift = (wc - wi) * 0.5;
				double yshift = (hr - hi) * 0.5;
				elem.setBounds(x + (float) xshift, y + (float) yshift, (float) wi, (float) hi);
				y += hr + 2;
			}
			x += wc + 2;
		}

		return false;
	}

	private void build() {
		System.out.println("build " + node.getLabel());
		this.clear();
		for (TypedList dim : dimData.getGroups()) {
			for (TypedList rec : recData.getGroups()) {
				GLElementFactorySwitcher s = build(dim, rec);
				this.add(s);
			}
		}
	}

	private GLElementFactorySwitcher build(TypedList dim, TypedList rec) {
		Builder b = GLElementFactoryContext.builder();
		fill(b, dim, rec);
		b.put(EDetailLevel.class, EDetailLevel.HIGH);
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(b.build(), "domino."
				+ getExtensionID(), node.getUIState().getProximityMode());
		GLElementFactorySwitcher s = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
		return s;
	}

	@Override
	public GLElement getToolBar() {
		GLElementFactorySwitcher s = getSwitcher();
		if (s == null)
			return null;
		return s.createButtonBarBuilder().build();
	}

	private GLElementFactorySwitcher getSwitcher() {
		GLElementFactorySwitcher s = (GLElementFactorySwitcher) get(0);
		return s;
	}

	private TypedGroupList getData(EDimension dim) {
		return dim.select(dimData, recData);
	}


	protected abstract void fill(Builder b, TypedList dimData, TypedList recData);

	@Override
	public boolean apply(String input) {
		return true;
	}

	@Override
	public final GLElement asGLElement() {
		return this;
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
		return sum;
	}

	private double getSize(EDimension dim, int row, int col) {
		GLElementDimensionDesc desc = getSwitcher(row, col).getActiveDesc(dim);
		return desc.getSize(getDataSafe(row, col, dim));
	}

	private int getNumGroups(EDimension dim) {
		return getData(dim).getGroups().size();
	}

	private GLElementFactorySwitcher getSwitcher(int row, int col) {
		int cols = getNumGroups(EDimension.DIMENSION);
		int index = row * cols + col;
		return (GLElementFactorySwitcher) get(index);
	}

	private int getDataSafe(int row, int col, EDimension dim) {
		TypedList l = dim.select(dimData, recData);
		if (!l.isEmpty())
			return l.size();
		return node.getSize(dim);
	}

	@Override
	protected void init(IGLElementContext context) {
		node.addPropertyChangeListener(INode.PROP_TRANSPOSE, this);
		node.getUIState().addPropertyChangeListener(NodeUIState.PROP_PROXIMITY_MODE, this);
		super.init(context);
	}

	@Override
	protected void takeDown() {
		node.removePropertyChangeListener(INode.PROP_TRANSPOSE, this);
		node.getUIState().removePropertyChangeListener(NodeUIState.PROP_PROXIMITY_MODE, this);
		super.takeDown();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case INode.PROP_TRANSPOSE:
			rebuild();
			break;
		case NodeUIState.PROP_PROXIMITY_MODE:
			rebuild();
			break;
		}
	}

	private void rebuild() {
		this.rebuild = true;
		relayout();
	}

	public static final Function<INodeUI, INode> TO_NODE = new Function<INodeUI, INode>() {
		@Override
		public INode apply(INodeUI input) {
			return input == null ? null : input.asNode();
		}
	};

}
