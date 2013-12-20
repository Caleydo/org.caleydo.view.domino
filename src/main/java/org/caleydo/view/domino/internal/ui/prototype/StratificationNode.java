/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.Function2;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroup;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.ui.ANodeUI;
import org.caleydo.view.domino.internal.ui.INodeUI;
import org.caleydo.view.domino.internal.ui.model.ANode;

import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationNode extends ANode implements IStratisfyingableNode, ITypedComparator,
		Function2<Integer, Integer, Color> {
	private final List<TypedGroup> groups;
	private final TypedSet ids;
	private final EDimension mainDim;
	private EDimension dim;
	private int sortingPriority = NO_SORTING;
	private boolean isStratified;

	public StratificationNode(Perspective data, EDimension dim) {
		this(data, dim, null);
	}

	public StratificationNode(Perspective data, EDimension dim, Integer referenceId) {
		super(data.getLabel());
		this.ids = TypedSet.of(data.getVirtualArray());
		this.groups = Utils.extractGroups(data, referenceId, dim);
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
	public List<TypedGroup> getGroups(EDimension dim) {
		return groups;
	}

	@Override
	public int getSortingPriority(EDimension dim) {
		return isRightDimension(dim) ? sortingPriority : NO_SORTING;
	}

	@Override
	public void setSortingPriority(EDimension dim, int sortingPriority) {
		if (!isRightDimension(dim))
			return;
		propertySupport.firePropertyChange(PROP_SORTING_PRIORITY, this.sortingPriority,
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
		protected void fill(Builder b, TypedGroupList dim, TypedGroupList rec) {
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
