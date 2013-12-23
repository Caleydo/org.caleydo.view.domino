/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.ITypedGroup;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.ui.ANodeUI;
import org.caleydo.view.domino.internal.ui.INodeUI;
import org.caleydo.view.domino.internal.util.Utils;
import org.caleydo.view.domino.spi.model.graph.INode;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public final class CategoricalData1DNode extends AData1DNode implements IStratisfyingableNode {

	private final List<?> categories;
	private List<? extends ITypedGroup> groups;
	private boolean isStratified;

	/**
	 * @param data
	 */
	public CategoricalData1DNode(TablePerspective data, EDimension main) {
		super(data, main);
		assert DataSupportDefinitions.categoricalColumns.apply(data);
		List<CategoryProperty<?>> properties = Utils.resolveCategories(getSingleID().getId(), data.getDataDomain(),
				main.opposite());
		this.categories = toCategories(properties);
		this.groups = Utils.extractGroups(main.select(data.getDimensionPerspective(), data.getRecordPerspective()),
				getSingleID().getId(), main.opposite());
	}

	public CategoricalData1DNode(CategoricalData1DNode parent, String label, TypedSet ids) {
		super(parent, label, ids);
		this.categories = parent.categories;
		this.groups = Utils.subGroups(ids, parent.groups);
	}

	public CategoricalData1DNode(CategoricalData1DNode clone) {
		super(clone);
		this.categories = clone.categories;
		this.groups = clone.groups;
	}

	@Override
	public CategoricalData1DNode clone() {
		return new CategoricalData1DNode(this);
	}

	@Override
	public INode extract(String label, ITypedCollection dim, ITypedCollection rec) {
		return new CategoricalData1DNode(this, label, (isRightDimension(EDimension.DIMENSION) ? dim : rec).asSet());
	}

	@Override
	public boolean isStratisfyable(EDimension dim) {
		return isRightDimension(dim);
	}

	@Override
	public boolean isStratisfied(EDimension dim) {
		return isRightDimension(dim) && isStratified;
	}

	@Override
	public void setStratisfied(EDimension dim, boolean isStratified) {
		if (!isRightDimension(dim))
			return;
		propertySupport.firePropertyChange(PROP_IS_STRATISFIED, this.isStratified, this.isStratified = isStratified);
	}

	@Override
	public List<? extends ITypedGroup> getGroups(EDimension dim) {
		return groups;
	}

	/**
	 * @param properties2
	 * @return
	 */
	private final List<?> toCategories(List<CategoryProperty<?>> p) {
		ImmutableList.Builder<Object> b = ImmutableList.builder();
		for (CategoryProperty<?> prop : p)
			b.add(prop.getCategory());
		return b.build();
	}


	@Override
	public INodeUI createUI() {
		return new UI(this);
	}

	private static class UI extends ANodeUI<CategoricalData1DNode> {

		public UI(CategoricalData1DNode node) {
			super(node);
		}

		@Override
		protected String getExtensionID() {
			return "1d.categorical";
		}

		@Override
		protected void fill(Builder b, TypedList dim, TypedList rec) {
			b.put(EDimension.class, node.getDimension());
			final TypedList data = node.getDimension().select(dim, rec);
			TablePerspective t = node.asTablePerspective(data);
			b.withData(t);
		}
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		Object cat1 = getRaw(o1);
		Object cat2 = getRaw(o2);
		if (Objects.equal(cat1, cat2))
			return 0;
		if (cat1 == null)
			return 1;
		if (cat2 == null)
			return -1;
		final int i1 = categories.indexOf(cat1);
		final int i2 = categories.indexOf(cat2);
		if (i1 == i2)
			return 0;
		if (i1 < 0)
			return 1;
		if (i2 < 0)
			return -1;
		return i1 - i2;
	}

}
