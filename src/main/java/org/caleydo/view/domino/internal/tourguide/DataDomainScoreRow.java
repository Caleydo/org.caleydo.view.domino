/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.id.IDType;
import org.caleydo.view.tourguide.api.model.AVirtualArrayScoreRow;
import org.caleydo.view.tourguide.api.model.ITablePerspectiveScoreRow;

import com.google.common.base.Objects;

/**
 * a scorerow for the default table perspective
 *
 * @author Samuel Gratzl
 *
 */
public class DataDomainScoreRow extends AVirtualArrayScoreRow implements ITablePerspectiveScoreRow {
	private final ATableBasedDataDomain dataDomain;

	public DataDomainScoreRow(ATableBasedDataDomain dataDomain) {
		this.dataDomain = dataDomain;
	}

	@Override
	public String getLabel() {
		return dataDomain.getLabel();
	}

	@Override
	public IDataDomain getDataDomain() {
		return dataDomain;
	}

	@Override
	public String getPersistentID() {
		return dataDomain.getDataDomainID();
	}

	@Override
	public IDType getDimensionIdType() {
		return dataDomain.getDimensionIDType();
	}

	@Override
	public Iterable<Integer> getDimensionIDs() {
		return dataDomain.getTable().getDefaultDimensionPerspective(true).getVirtualArray();
	}

	@Override
	public VirtualArray getVirtualArray() {
		return dataDomain.getTable().getDefaultRecordPerspective(true).getVirtualArray();
	}

	@Override
	protected boolean isFiltered() {
		return false;
	}

	@Override
	protected boolean filter(Group g) {
		return false;
	}

	@Override
	public boolean is(TablePerspective tablePerspective) {
		return Objects.equal(tablePerspective.getDataDomain(), dataDomain);
	}

	@Override
	public boolean is(Perspective p) {
		return dataDomain.getTable().getDefaultRecordPerspective(true).equals(p);
	}

	@Override
	public TablePerspective asTablePerspective() {
		final Perspective r = dataDomain.getTable().getDefaultRecordPerspective(true);
		final Perspective d = dataDomain.getTable().getDefaultDimensionPerspective(true);
		return dataDomain.getTablePerspective(r.getPerspectiveID(), d.getPerspectiveID());
	}

}
