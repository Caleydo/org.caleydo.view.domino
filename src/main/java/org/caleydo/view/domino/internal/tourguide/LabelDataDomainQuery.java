/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import java.util.Collections;
import java.util.List;

import org.caleydo.core.id.IDCategory;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.vis.lineup.model.RankTableModel;

/**
 * @author Samuel Gratzl
 *
 */
public class LabelDataDomainQuery extends ADataDomainQuery {
	private final IDCategory category;
	/**
	 * @param dataDomain
	 */
	public LabelDataDomainQuery(IDCategory category) {
		super(null);
		this.category = category;
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
		return Collections.singletonList((AScoreRow) new LabelScoreRow(category));
	}

	@Override
	public void createSpecificColumns(RankTableModel table) {

	}

	@Override
	public void removeSpecificColumns(RankTableModel table) {

	}

	@Override
	public void updateSpecificColumns(RankTableModel table) {

	}

	@Override
	public List<AScoreRow> onDataDomainUpdated() {
		return Collections.emptyList();
	}

}
