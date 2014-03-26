/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.tourguide;

import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.vis.lineup.model.RankTableModel;

/**
 * @author Samuel Gratzl
 *
 */
public class DataDomainQuery extends ADataDomainQuery {
	/**
	 * @param dataDomain
	 */
	public DataDomainQuery(ATableBasedDataDomain dataDomain) {
		super(dataDomain);
	}

	@Override
	public boolean apply(AScoreRow input) {
		return true;
	}

	@Override
	protected boolean hasFilterImpl() {
		return false;
	}

	@Override
	protected List<AScoreRow> getAll() {
		return Collections.singletonList((AScoreRow) new DataDomainScoreRow((ATableBasedDataDomain) getDataDomain()));
	}

	@Override
	public void createSpecificColumns(RankTableModel table) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeSpecificColumns(RankTableModel table) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSpecificColumns(RankTableModel table) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AScoreRow> onDataDomainUpdated() {
		return Collections.emptyList();
	}

}
