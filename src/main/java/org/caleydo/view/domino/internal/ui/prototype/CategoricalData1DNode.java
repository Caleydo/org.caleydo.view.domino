/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.internal.ui.prototype.ui.ANodeUI;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public final class CategoricalData1DNode extends AData1DNode {

	private final List<CategoryProperty<?>> properties;
	private final List<?> categories;

	/**
	 * @param data
	 */
	public CategoricalData1DNode(TablePerspective data, EDimension main) {
		super(data, main);
		assert DataSupportDefinitions.categoricalColumns.apply(data);
		this.properties = Utils.resolveCategories(getSingleID().getId(), data.getDataDomain(), main.opposite());
		this.categories = toCategories(properties);
	}

	public CategoricalData1DNode(CategoricalData1DNode clone) {
		super(clone);
		this.properties = clone.properties;
		this.categories = clone.categories;
	}

	@Override
	public CategoricalData1DNode clone() {
		return new CategoricalData1DNode(this);
	}

	/**
	 * @return the categories, see {@link #categories}
	 */
	public List<?> getCategories() {
		return categories;
	}

	/**
	 * @return the properties, see {@link #properties}
	 */
	public List<CategoryProperty<?>> getProperties() {
		return properties;
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
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends ANodeUI<CategoricalData1DNode> {

		public UI(CategoricalData1DNode node) {
			super(node);
		}

		@Override
		protected List<GLElementSupplier> createVis() {
			Builder b = GLElementFactoryContext.builder();
			// b.withData(node.getData());
			return GLElementFactories.getExtensions(b.build(), "domino.1d.categorical", Predicates.alwaysTrue());
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
		return categories.indexOf(cat1) - categories.indexOf(cat2);
	}

}
