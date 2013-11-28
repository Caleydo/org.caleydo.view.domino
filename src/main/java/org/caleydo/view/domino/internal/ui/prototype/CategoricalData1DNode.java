/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.collection.table.CategoricalTable;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;

import com.google.common.base.Objects;
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
		this.properties = resolveCategories();
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

	public GroupList getGroups() {
		return data.getRecordPerspective().getVirtualArray().getGroupList();
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

	@SuppressWarnings("unchecked")
	private List<CategoryProperty<?>> resolveCategories() {
		final CategoricalTable<?> table = (CategoricalTable<?>) getDataDomain().getTable();

		Object spec = table.getDataClassSpecificDescription(getSingleID(), 0);
		if (spec instanceof CategoricalClassDescription<?>) {
			List<?> tmp = ((CategoricalClassDescription<?>) spec).getCategoryProperties();
			return ImmutableList.copyOf((List<CategoryProperty<?>>) tmp);
		}
		return Collections.emptyList();
	}

	@Override
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends GLElement {
		private final CategoricalData1DNode node;

		public UI(CategoricalData1DNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			GroupList groups = node.getGroups();
			final int total = node.size();
			boolean horizontal = node.isTransposed();
			List<Color> colors = toColors(groups);
			Utils.renderCategorical(g, w, h, groups, total, horizontal, colors);
			super.renderImpl(g, w, h);
		}

		private List<Color> toColors(GroupList groups) {
			List<Color> c = new ArrayList<>(groups.size());
			for(Group g : groups) {
				String label = g.getLabel();
				CategoryProperty<?> prop = findProp(label);
				c.add(prop == null ? Color.NEUTRAL_GREY : prop.getColor());
			}
			return c;
		}

		private CategoryProperty<?> findProp(String label) {
			for (CategoryProperty<?> prop : node.getProperties())
				if (prop.getCategoryName().equals(label))
					return prop;
			return null;
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
