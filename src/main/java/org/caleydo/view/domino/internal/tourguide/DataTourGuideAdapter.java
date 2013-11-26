/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import java.net.URL;
import java.util.Collection;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.domino.internal.DominoView;
import org.caleydo.view.domino.internal.DominoViewPart;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.vis.ITourGuideView;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideAdapter;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideAdapterFactory;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideDataMode;
import org.caleydo.view.tourguide.spi.score.IScore;
import org.caleydo.vis.lineup.model.RankTableModel;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Samuel Gratzl
 *
 */
public class DataTourGuideAdapter implements ITourGuideAdapter {

	/**
	 *
	 */
	private static final String SECONDARY_ID = "domino.datasets";
	private DominoView domino;
	private final DataTourGuideDataMode mode = new DataTourGuideDataMode();

	private ITourGuideView vis;

	@Override
	public String getLabel() {
		return "Domino Data";
	}

	@Override
	public void addDefaultColumns(RankTableModel table) {
		mode.addDefaultColumns(table);
	}

	@Override
	public URL getIcon() {
		return Resources.icon();
	}

	@Override
	public void setup(ITourGuideView vis, GLElementContainer lineUp) {
		this.vis = vis;
		lineUp.remove(0); // remove data domain query selection

		for (ADataDomainQuery query : vis.getQueries()) {
			query.setActive(true);
		}
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPreviewing(AScoreRow row) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVisible(AScoreRow row) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(AScoreRow old, AScoreRow new_, Collection<IScore> visibleScores, IScore sortedByScore) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preDisplay() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canShowPreviews() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onRowClick(RankTableModel table, PickingMode pickingMode, AScoreRow row, boolean isSelected,
			IGLElementContext context) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPartName() {
		return "Domino";
	}

	@Override
	public String getSecondaryID() {
		return SECONDARY_ID;
	}

	@Override
	public ITourGuideDataMode asMode() {
		return this.mode;
	}

	@Override
	public boolean filterBoundView(ADataDomainQuery query) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isRepresenting(IWorkbenchPart part, boolean isBoundTo) {
		if (part instanceof DominoViewPart)
			return !isBoundTo || ((DominoViewPart) part).getView() == domino;
		return false;
	}

	@Override
	public void bindTo(IViewPart part) {
		if (part instanceof DominoViewPart)
			this.domino = ((DominoViewPart)part).getView();
		else
			this.domino = null;
	}

	@Override
	public boolean ignoreActive(IViewPart part) {
		return false;
	}

	@Override
	public boolean isBound2View() {
		return this.domino != null;
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
