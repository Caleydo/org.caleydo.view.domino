/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
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
	private final TypedSet rec;
	private final TypedSet dim;
	private boolean transposed = false;
	private int sortingPriority = NO_SORTING;

	public AData1DNode(TablePerspective data) {
		this.data = data;
		this.rec = TypedSet.of(data.getRecordPerspective().getVirtualArray());
		this.dim = TypedSet.of(data.getDimensionPerspective().getVirtualArray());
		assert dim.size() == 1;
	}

	/**
	 * @param clone
	 */
	public AData1DNode(AData1DNode clone) {
		super(clone);
		this.data = clone.data;
		this.rec = clone.rec;
		this.dim = clone.dim;
		this.transposed = clone.transposed;
		this.sortingPriority = NO_SORTING;
	}

	public final Integer getSingleID() {
		return this.dim.iterator().next();
	}

	@Override
	public final IDType getIdType() {
		return this.dim.getIdType();
	}

	public int size() {
		return this.rec.size();
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
		this.transposed = !this.transposed;
	}

	/**
	 * @return the transposed, see {@link #transposed}
	 */
	protected boolean isTransposed() {
		return transposed;
	}

	@Override
	public TypedSet getData(EDimension dim) {
		return isHorizontal(dim) ? TypedCollections.INVALID_SET : this.rec;
	}

	protected boolean isHorizontal(EDimension dim) {
		return dim.isVertical() == this.transposed;
	}

	@Override
	public final boolean isSortable(EDimension dim) {
		return isHorizontal(dim);
	}

	@Override
	public final int getSortingPriority(EDimension dim) {
		return isHorizontal(dim) ? sortingPriority : NO_SORTING;
	}

	@Override
	public final void setSortingPriority(EDimension dim, int sortingPriority) {
		if (!isHorizontal(dim))
			return;
		propertySupport.firePropertyChange(SORTING_PRIORITY, this.sortingPriority,
				this.sortingPriority = sortingPriority);
	}

	@Override
	public final ITypedComparator getComparator(EDimension dim) {
		return isHorizontal(dim) ? this : TypedCollections.NATURAL_ORDER;
	}

	@Override
	public final TypedList getTypedList(EDimension dim) {
		return TypedList.of((isHorizontal(dim) ? data.getDimensionPerspective() : data.getRecordPerspective())
				.getVirtualArray());
	}
}
