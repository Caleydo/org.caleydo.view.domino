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
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorBrewer;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.domino.api.model.ITypedComparator;
import org.caleydo.view.domino.api.model.TypedCollections;
import org.caleydo.view.domino.api.model.TypedList;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationNode extends ANode implements ISortableNode, ITypedComparator {
	private final Perspective data;
	private final TypedSet ids;
	private final EDimension mainDim;
	private EDimension dim;
	private int sortingPriority = NO_SORTING;
	private final Integer referenceId;

	public StratificationNode(Perspective data, EDimension dim) {
		this(data, dim, null);
	}

	public StratificationNode(Perspective data, EDimension dim, Integer referenceId) {
		this.data = data;
		this.referenceId = referenceId;
		this.ids = TypedSet.of(data.getVirtualArray());
		this.dim = this.mainDim = dim;
	}

	public StratificationNode(StratificationNode clone) {
		super(clone);
		this.data = clone.data;
		this.mainDim = clone.mainDim;
		this.referenceId = clone.referenceId;
		this.ids = clone.ids;
		this.dim = clone.dim;
		this.sortingPriority = NO_SORTING;
	}

	public ATableBasedDataDomain getDataDomain() {
		return (ATableBasedDataDomain) data.getDataDomain();
	}

	public List<Color> getGroupColors() {
		if (referenceId == null) {
			return ColorBrewer.Set2.getColors(getGroups().size());
		}
		// lookup the colors from the properties
		List<CategoryProperty<?>> categories = CategoricalData1DNode.resolveCategories(referenceId, getDataDomain(),
				mainDim.opposite());
		GroupList groups = getGroups();
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
	private CategoryProperty<?> findProp(String label, List<CategoryProperty<?>> categories) {
		for (CategoryProperty<?> prop : categories) {
			if (prop.getCategoryName().equals(label))
				return prop;
		}
		return null;
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

	@Override
	public final String getLabel() {
		return data.getLabel();
	}
	/**
	 * @return the data, see {@link #data}
	 */
	public Perspective getData() {
		return data;
	}

	public int size() {
		return data.getVirtualArray().size();
	}

	public GroupList getGroups() {
		return getData().getVirtualArray().getGroupList();
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
	public GLElement createUI() {
		return new UI(this);
	}

	private static class UI extends GLElement {
		private final StratificationNode node;

		public UI(StratificationNode node) {
			this.node = node;
			setLayoutData(node);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			GroupList groups = node.getGroups();
			final int total = node.size();
			boolean horizontal = node.dim.isHorizontal();
			List<Color> colors = node.getGroupColors();
			Utils.renderCategorical(g, w, h, groups, total, horizontal, colors);
			super.renderImpl(g, w, h);
		}
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
	public final TypedList getTypedList(EDimension dim) {
		return isRightDimension(dim) ? TypedList.of(data.getVirtualArray()) : TypedCollections.INVALID_LIST;
	}

	@Override
	public ITypedComparator getComparator(EDimension dim) {
		return isRightDimension(dim) ? this : TypedCollections.NATURAL_ORDER;
	}

	@Override
	public int compare(Integer o1, Integer o2) {
		Group g1 = toGroup(o1);
		Group g2 = toGroup(o2);
		if (g1 == g2)
			return 0;
		if (g1 == null)
			return 1;
		if (g2 == null)
			return -1;
		return g1.getGroupIndex().compareTo(g2.getGroupIndex());
	}

	@Override
	public IDType getIdType() {
		return data.getIdType();
	}

	/**
	 * @param o1
	 * @return
	 */
	private Group toGroup(Integer index) {
		int i = data.getVirtualArray().indexOf(index);
		if (i < 0)
			return null;
		Group group = getGroups().getGroupOfVAIndex(i);
		return group;
	}
}
