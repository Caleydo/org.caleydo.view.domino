/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.tourguide;

import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.view.rnb.internal.tourguide.ui.EntityTypeSelector;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.ASingleIDDataDomainQuery;
import org.caleydo.view.tourguide.api.model.InhomogenousDataDomainQuery;
import org.caleydo.view.tourguide.api.model.StratificationDataDomainQuery;
import org.caleydo.view.tourguide.api.vis.ITourGuideView;
import org.caleydo.view.tourguide.api.vis.TourGuideUtils;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideAdapter;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideAdapterFactory;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideDataMode;

/**
 * @author Samuel Gratzl
 *
 */
public class StratifiationTourGuideAdapter extends ATourGuideAdapter implements ICallback<IDCategory> {
	private static final String SECONDARY_ID = "rnb.stratifications";
	private final StratificationTourGuideDataMode mode = new StratificationTourGuideDataMode();
	private IDCategory activeCategory;

	@Override
	public String getLabel() {
		return "RnB Partitioned Blocks";
	}

	public static void show() {
		TourGuideUtils.showTourGuide(SECONDARY_ID);
	}

	@Override
	public void setup(ITourGuideView vis, GLElementContainer lineUp) {
		super.setup(vis, lineUp);
		EntityTypeSelector selector = new EntityTypeSelector(this);
		activeCategory = selector.getActive();
		lineUp.add(0, selector);

		vis.updateBound2ViewState();
	}

	@Override
	public void on(IDCategory data) {
		this.activeCategory = data;
		if (vis != null)
			vis.updateBound2ViewState();
	}

	@Override
	public boolean filterBoundView(ADataDomainQuery query) {
		if (activeCategory == null)
			return true;
		IDType idType = null;
		if (query instanceof ASingleIDDataDomainQuery) {
			idType = ((ASingleIDDataDomainQuery) query).getStratificationIDType();
		} else if (query instanceof StratificationDataDomainQuery) {
			idType = ((StratificationDataDomainQuery) query).getIDType();
		} else if (query instanceof PathwaySetDataDomainQuery)
			idType = ((PathwaySetDataDomainQuery) query).getIDType();
		else if (query instanceof InhomogenousDataDomainQuery) {
			idType = ((InhomogenousDataDomainQuery) query).getDataDomain().getRecordIDType();
		}
		return idType == null ? false : activeCategory.isOfCategory(idType);
	}

	@Override
	public ITourGuideDataMode asMode() {
		return this.mode;
	}

	@Override
	public String getSecondaryID() {
		return SECONDARY_ID;
	}

	public static final class Factory implements ITourGuideAdapterFactory {
		@Override
		public ITourGuideAdapter create() {
			return new StratifiationTourGuideAdapter();
		}

		@Override
		public String getSecondaryID() {
			return SECONDARY_ID;
		}
	}


}
