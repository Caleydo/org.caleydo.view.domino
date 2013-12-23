/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class AData1DNode extends ANode implements IStratisfyingableNode, ITypedComparator {
	protected final EDimension main;
	protected final DataDomainDataProvider data;
	protected final TypedSet ids;
	protected final TypedID id;
	private boolean transposed = false;
	private int sortingPriority = NO_SORTING;

	private boolean isStratified;


	public AData1DNode(TablePerspective data, EDimension main) {
		super(data.getLabel());
		Perspective p = main.select(data.getDimensionPerspective(), data.getRecordPerspective());
		Perspective o = main.opposite().select(data.getDimensionPerspective(), data.getRecordPerspective());

		this.data = new DataDomainDataProvider(data);
		this.ids = TypedSet.of(p.getVirtualArray());
		assert o.getVirtualArray().size() == 1;
		this.id = new TypedID(o.getVirtualArray().get(0),o.getIdType());
		this.main = main;
	}

	public AData1DNode(AData1DNode parent, String label, TypedSet ids) {
		super(parent, label);
		this.main = parent.main;
		this.data = parent.data;
		this.ids = ids;
		this.id = parent.id;
		this.transposed = parent.transposed;
		this.sortingPriority = parent.sortingPriority;
		this.isStratified = parent.isStratified;

	}
	/**
	 * @param clone
	 */
	public AData1DNode(AData1DNode clone) {
		super(clone);
		this.main = clone.main;
		this.data = clone.data;
		this.ids = clone.ids;
		this.id = clone.id;
		this.transposed = clone.transposed;
		this.sortingPriority = NO_SORTING;
	}

	@Override
	public boolean isStratisfyable(EDimension dim) {
		return isRightDimension(dim) && getGroups(dim).size() > 1;
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

	public final TypedID getSingleID() {
		return this.id;
	}

	@Override
	public final IDType getIdType() {
		return this.ids.getIdType();
	}

	public int size() {
		return this.ids.size();
	}

	/**
	 * @param data
	 * @return
	 */
	protected final TablePerspective asTablePerspective(TypedList data) {
		TypedID singleID = getSingleID();
		TypedList single = TypedCollections.singletonList(singleID);
		if (this.isRightDimension(EDimension.DIMENSION)) {
			TypedList t = single;
			single = data;
			data = t;
		}
		return this.data.asTablePerspective(single, data);
	}

	protected final TablePerspective asTablePerspective(TypedGroupList data) {
		TypedID singleID = getSingleID();
		TypedGroupList single = TypedGroupList.createUngrouped(TypedCollections.singletonList(singleID));
		if (this.isRightDimension(EDimension.DIMENSION)) {
			TypedGroupList t = single;
			single = data;
			data = t;
		}
		return this.data.asTablePerspective(single, data);
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

	protected final boolean isRightDimension(EDimension dim) {
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
		propertySupport.firePropertyChange(PROP_SORTING_PRIORITY, this.sortingPriority,
				this.sortingPriority = sortingPriority);
	}

	@Override
	public final ITypedComparator getComparator(EDimension dim) {
		return isRightDimension(dim) ? this : TypedCollections.NATURAL_ORDER;
	}

	public final Color getColor(Integer id) {
		Integer single = getSingleID().getId();
		return data.getColor(main.select(id, single), main.select(single, id));
	}

	public final Object getRaw(Integer id) {
		Integer single = getSingleID().getId();
		return data.getRaw(main.select(id, single), main.select(single, id));
	}

	public final float getNormalized(Integer id) {
		Integer single = getSingleID().getId();
		return data.getNormalized(main.select(id, single), main.select(single, id));
	}
}
