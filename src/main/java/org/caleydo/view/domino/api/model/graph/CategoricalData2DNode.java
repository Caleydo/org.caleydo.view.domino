/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import java.util.List;

import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.collection.table.CategoricalTable;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.ui.ANodeUI;
import org.caleydo.view.domino.internal.ui.INodeUI;

/**
 * @author Samuel Gratzl
 *
 */
public class CategoricalData2DNode extends AData2DNode {

	private final List<CategoryProperty<?>> categories;

	/**
	 * @param data
	 */
	public CategoricalData2DNode(ATableBasedDataDomain data) {
		super(data);
		assert DataSupportDefinitions.categoricalTables.apply(data);
		this.categories = getCategories(data);
	}

	public CategoricalData2DNode(CategoricalData2DNode parent, TypedSet dim, TypedSet rec, String label) {
		super(parent, dim, rec, label);
		this.categories = parent.categories;
	}

	public CategoricalData2DNode(CategoricalData2DNode clone) {
		super(clone);
		this.categories = clone.categories;
	}

	@Override
	public CategoricalData2DNode clone() {
		return new CategoricalData2DNode(this);
	}

	@Override
	public INodeUI createUI() {
		return new UI(this);
	}

	@SuppressWarnings("unchecked")
	private static List<CategoryProperty<?>> getCategories(ATableBasedDataDomain dataDomain) {
		final CategoricalTable<?> table = (CategoricalTable<?>) dataDomain.getTable();

		CategoricalClassDescription<?> cats = table.getCategoryDescriptions();
		List<?> tmp = cats.getCategoryProperties();
		return (List<CategoryProperty<?>>) tmp;
	}

	private static class UI extends ANodeUI<CategoricalData2DNode> {

		public UI(CategoricalData2DNode node) {
			super(node);
		}

		@Override
		protected String getExtensionID() {
			return "1d.categorical";
		}

		@Override
		protected void fill(Builder b, TypedList dim, TypedList rec) {
			TablePerspective t = node.asTablePerspective(dim, rec);
			b.withData(t);
		}
	}
}
