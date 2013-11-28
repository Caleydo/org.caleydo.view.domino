/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.id.IDType;
import org.caleydo.view.domino.api.model.ITypedComparator;
import org.caleydo.view.domino.api.model.TypedCollections;
import org.caleydo.view.domino.api.model.TypedList;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AData1DNode extends ANode implements ISortableNode, ITypedComparator {
	protected final TablePerspective data;
	private final EDimension main;
	private final TypedSet ids;
	private final IDType idType;
	private final Integer id;
	private boolean transposed = false;
	private int sortingPriority = NO_SORTING;

	public AData1DNode(TablePerspective data, EDimension main) {
		this.data = data;
		this.main = main;
		Perspective p = getPerspective();
		Perspective o = main.opposite().select(data.getDimensionPerspective(), data.getRecordPerspective());
		this.idType = o.getIdType();
		this.ids = TypedSet.of(p.getVirtualArray());
		assert o.getVirtualArray().size() == 1;
		this.id = o.getVirtualArray().get(0);
	}

	private Perspective getPerspective() {
		return main.select(data.getDimensionPerspective(), data.getRecordPerspective());
	}

	/**
	 * @param clone
	 */
	public AData1DNode(AData1DNode clone) {
		super(clone);
		this.data = clone.data;
		this.main = clone.main;
		this.ids = clone.ids;
		this.id = clone.id;
		this.idType = clone.idType;
		this.transposed = clone.transposed;
		this.sortingPriority = NO_SORTING;
	}

	public final Integer getSingleID() {
		return this.id;
	}

	@Override
	public final IDType getIdType() {
		return this.idType;
	}

	public int size() {
		return this.ids.size();
	}

	public TypedList getTypedList() {
		return TypedList.of(getPerspective().getVirtualArray());
	}

	/**
	 * @param o1
	 * @return
	 */
	protected Object getRaw(Integer recordID) {
		return getDataDomain().getTable().getRaw(getSingleID(), recordID);
	}

	@Override
	public final String getLabel() {
		return data.getLabel();
	}

	/**
	 * @return the data, see {@link #data}
	 */
	public final TablePerspective getData() {
		return data;
	}

	public final ATableBasedDataDomain getDataDomain() {
		return getData().getDataDomain();
	}

	@Override
	public void transpose() {
		propertySupport.firePropertyChange(PROP_TRANSPOSE, this.transposed, this.transposed = !this.transposed);
	}

	/**
	 * @return the transposed, see {@link #transposed}
	 */
	protected boolean isTransposed() {
		return transposed;
	}

	@Override
	public TypedSet getData(EDimension dim) {
		return isRightDimension(dim) ? this.ids : TypedCollections.INVALID_SET;
	}

	private boolean isRightDimension(EDimension dim) {
		return dim == getDimension();
	}

	protected EDimension getDimension() {
		return transposed ? main.opposite() : main;
	}

	@Override
	public final boolean isSortable(EDimension dim) {
		return isRightDimension(dim);
	}

	@Override
	public final int getSortingPriority(EDimension dim) {
		return isRightDimension(dim) ? sortingPriority : NO_SORTING;
	}

	@Override
	public final void setSortingPriority(EDimension dim, int sortingPriority) {
		if (!isRightDimension(dim))
			return;
		propertySupport.firePropertyChange(SORTING_PRIORITY, this.sortingPriority,
				this.sortingPriority = sortingPriority);
	}

	@Override
	public final ITypedComparator getComparator(EDimension dim) {
		return isRightDimension(dim) ? this : TypedCollections.NATURAL_ORDER;
	}

	@Override
	public final TypedList getTypedList(EDimension dim) {
		return isRightDimension(dim) ? getTypedList() : TypedCollections.INVALID_LIST;
	}
}
