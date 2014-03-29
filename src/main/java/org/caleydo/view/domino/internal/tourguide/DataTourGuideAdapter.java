/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.vis.ITourGuideView;
import org.caleydo.view.tourguide.api.vis.TourGuideUtils;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideAdapter;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideAdapterFactory;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideDataMode;

/**
 * @author Samuel Gratzl
 *
 */
public class DataTourGuideAdapter extends ATourGuideAdapter {
	private static final String SECONDARY_ID = "domino.datasets";
	private final DataTourGuideDataMode mode = new DataTourGuideDataMode();

	@Override
	public String getLabel() {
		return "Domino Numerical/Matrix Blocks";
	}

	public static void show() {
		TourGuideUtils.showTourGuide(SECONDARY_ID);
	}


	@Override
	public void setup(ITourGuideView vis, GLElementContainer lineUp) {
		super.setup(vis, lineUp);
		lineUp.remove(0); // remove data domain query selection

		for (ADataDomainQuery query : vis.getQueries()) {
			query.setActive(true);
		}
	}

	@Override
	public String getSecondaryID() {
		return SECONDARY_ID;
	}

	@Override
	public ITourGuideDataMode asMode() {
		return this.mode;
	}

	public static final class Factory implements ITourGuideAdapterFactory {
		@Override
		public ITourGuideAdapter create() {
			return new DataTourGuideAdapter();
		}

		@Override
		public String getSecondaryID() {
			return SECONDARY_ID;
		}
	}

}
