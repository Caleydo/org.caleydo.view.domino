/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.view.domino.api.model.graph.ISortableNode;
import org.caleydo.view.domino.api.model.graph.IStratisfyingableNode;
import org.caleydo.view.domino.api.model.graph.NodeUIState;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ANodeUI<T extends INode> extends GLElementDecorator implements PropertyChangeListener, INodeUI,
		Predicate<String> {
	protected final T node;

	private TypedList dimData;
	private TypedList recData;
	private boolean rebuild = false;

	public ANodeUI(T node) {
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

	private void build() {
		System.out.println("build " + node.getLabel());
		Builder b = GLElementFactoryContext.builder();
		fill(b, dimData, recData);
		b.put(EDetailLevel.class, EDetailLevel.HIGH);
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(b.build(), "domino."
				+ getExtensionID(), null, node.getUIState().getProximityMode());
		GLElementFactorySwitcher s = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
		setContent(s);
	}

	@Override
	public GLElement getToolBar() {
		GLElementFactorySwitcher s = getSwitcher();
		if (s == null)
			return null;
		return s.createButtonBarBuilder().build();
	}

	private GLElementFactorySwitcher getSwitcher() {
		GLElementFactorySwitcher s = (GLElementFactorySwitcher) getContent();
		return s;
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
	public boolean setData(EDimension dim, TypedList data) {
		final TypedList oldList = dim.select(dimData, recData);
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

	@Override
	public double getSize(EDimension dim) {
		GLElementDimensionDesc desc = getSwitcher().getActiveDesc(dim);
		return desc.size(getDataSafe(dim));
	}

	private int getDataSafe(EDimension dim) {
		TypedList l = dim.select(dimData, recData);
		if (!l.isEmpty())
			return l.size();
		return Math.max(node.getSize(dim), 1);
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
