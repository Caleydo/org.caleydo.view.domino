/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.List;

import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.collection.table.CategoricalTable;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class CategoricalData2DNode extends AData2DNode {

	/**
	 * @param data
	 */
	public CategoricalData2DNode(ATableBasedDataDomain data) {
		super(data);
		assert DataSupportDefinitions.categoricalTables.apply(data);
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	@SuppressWarnings("unchecked")
	public List<CategoryProperty<?>> getCategories() {
		final CategoricalTable<?> table = (CategoricalTable<?>) getDataDomain().getTable();

		CategoricalClassDescription<?> cats = table.getCategoryDescriptions();
		List<?> tmp = cats.getCategoryProperties();
		return (List<CategoryProperty<?>>) tmp;
	}

	private static class UI extends GLElementDecorator {
		private final CategoricalData2DNode node;

		public UI(CategoricalData2DNode node) {
			this.node = node;
			setLayoutData(node);
			GLElementFactoryContext context = GLElementFactoryContext.builder()
					.withData(node.getDataDomain().getDefaultTablePerspective()).build();
			ImmutableList<GLElementSupplier> children = GLElementFactories.getExtensions(context,
					"domino.2d.categorical", Predicates.alwaysTrue());
			GLElementFactorySwitcher s = new GLElementFactorySwitcher(children, ELazyiness.DESTROY);
			setContent(s);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			// g.color(node.getDataDomain().getColor()).fillRect(0, 0, w, h);
			super.renderImpl(g, w, h);
		}
	}
}
