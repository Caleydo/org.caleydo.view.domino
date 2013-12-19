/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorBrewer;
import org.caleydo.core.util.function.Function2;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroup;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.ui.ANodeUI;
import org.caleydo.view.domino.internal.ui.INodeUI;
import org.caleydo.view.domino.internal.ui.model.ANode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationNode extends ANode implements ISortableNode, ITypedComparator,
		Function2<Integer, Integer, Color> {
	private final List<TypedGroup> groups;
	private final TypedSet ids;
	private final EDimension mainDim;
	private EDimension dim;
	private int sortingPriority = NO_SORTING;

	public StratificationNode(Perspective data, EDimension dim) {
		this(data, dim, null);
	}

	public StratificationNode(Perspective data, EDimension dim, Integer referenceId) {
		super(data.getLabel());
		this.ids = TypedSet.of(data.getVirtualArray());
		this.groups = extractGroups(data, referenceId, dim);
		this.dim = this.mainDim = dim;
	}

	public StratificationNode(StratificationNode clone) {
		super(clone);
		this.mainDim = clone.mainDim;
		this.groups = clone.groups;
		this.ids = clone.ids;
		this.dim = clone.dim;
		this.sortingPriority = NO_SORTING;
	}

	/**
	 * @param referenceId
	 * @param virtualArray
	 * @return
	 */
	private static List<TypedGroup> extractGroups(Perspective p, Integer referenceId, EDimension mainDim) {
		VirtualArray va = p.getVirtualArray();
		GroupList groups = va.getGroupList();
		List<Color> colors = getGroupColors(referenceId, (ATableBasedDataDomain)p.getDataDomain(), groups, mainDim);
		List<TypedGroup> r = new ArrayList<>();
		for (int i = 0; i < groups.size(); ++i) {
			r.add(new TypedGroup(ImmutableSet.copyOf(va.getIDsOfGroup(i)), va.getIdType(), colors.get(i), groups.get(i)
					.getLabel()));
		}
		return ImmutableList.copyOf(r);
	}

	private static List<Color> getGroupColors(Integer referenceId, ATableBasedDataDomain dataDomain, GroupList groups,
			EDimension mainDim) {
		if (referenceId == null) {
			return ColorBrewer.Set2.getColors(groups.size());
		}
		// lookup the colors from the properties
		List<CategoryProperty<?>> categories = Utils.resolveCategories(referenceId, dataDomain,
				mainDim.opposite());
		List<Color> colors = new ArrayList<>(groups.size());
		for (Group group : groups) {
			String label = group.getLabel();
			CategoryProperty<?> prop = findProp(label, categories);
			colors.add(prop == null ? Color.NEUTRAL_GREY : prop.getColor());
		}
		return colors;
	}

	/**
	 * @param label
	 * @return
	 */
	private static CategoryProperty<?> findProp(String label, List<CategoryProperty<?>> categories) {
		for (CategoryProperty<?> prop : categories) {
			if (prop.getCategoryName().equals(label))
				return prop;
		}
		return null;
	}

	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDimension() {
		return dim;
	}

	@Override
	public StratificationNode clone() {
		return new StratificationNode(this);
	}

	@Override
	public void transpose() {
		this.dim = dim.opposite();
		propertySupport.firePropertyChange(PROP_TRANSPOSE, !this.dim.isVertical(), this.dim.isVertical());
	}

	public int size() {
		return ids.size();
	}

	/**
	 * @return the ids, see {@link #ids}
	 */
	@Override
	public TypedSet getData(EDimension dim) {
		return isRightDimension(dim) ? ids : TypedCollections.INVALID_SET;
	}

	private boolean isRightDimension(EDimension dim) {
		return dim == this.dim;
	}

	@Override
	public INodeUI createUI() {
		return new UI(this);
	}

	@Override
	public boolean isSortable(EDimension dim) {
		return isRightDimension(dim);
	}

	@Override
	public int getSortingPriority(EDimension dim) {
		return isRightDimension(dim) ? sortingPriority : NO_SORTING;
	}

	@Override
	public void setSortingPriority(EDimension dim, int sortingPriority) {
		if (!isRightDimension(dim))
			return;
		propertySupport.firePropertyChange(SORTING_PRIORITY, this.sortingPriority,
				this.sortingPriority = sortingPriority);
	}

	@Override
	public ITypedComparator getComparator(EDimension dim) {
		return isRightDimension(dim) ? this : TypedCollections.NATURAL_ORDER;
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		int g1 = toGroupIndex(o1);
		int g2 = toGroupIndex(o2);
		if (g1 == g2)
			return 0;
		if (g1 < 0)
			return 1;
		if (g2 < 0)
			return -1;
		return g1 - g2;
	}

	@Override
	public IDType getIdType() {
		return ids.getIdType();
	}

	private int toGroupIndex(Integer id) {
		if (!ids.contains(id))
			return -1;
		int i = 0;
		for (TypedGroup g : groups) {
			if (g.contains(id))
				return i;
			i++;
		}
		return -1;
	}


	@Override
	public Color apply(Integer record, Integer dimension) {
		Integer id = getDimension().select(dimension, record);
		int groupIndex = toGroupIndex(id);
		if (groupIndex < 0)
			return Color.NOT_A_NUMBER_COLOR;
		return groups.get(groupIndex).getColor();
	}

	private static class UI extends ANodeUI<StratificationNode> {
		public UI(StratificationNode node) {
			super(node);
		}

		@Override
		protected String getExtensionID() {
			return "stratification";
		}

		@Override
		protected void fill(Builder b, TypedList dim, TypedList rec) {
			final EDimension dimension = node.getDimension();
			b.put(EDimension.class, dimension);
			final TypedList data = dimension.select(dim, rec);
			final List<Integer> op = ImmutableList.of(0);
			String prim = dimension.select("dimensions", "records");
			String sec = dimension.select("records", "dimensions");
			b.put("heatmap." + prim, data);
			b.put("heatmap." + prim + ".idType", node.getIdType());
			b.put("heatmap." + sec, op);
			b.put("heatmap." + sec + ".idType", TypedCollections.INVALID_IDTYPE);
			b.set("heatmap." + "forceTextures");
			b.put(Function2.class, node);
		}
	}
}
