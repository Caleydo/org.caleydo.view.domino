/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.tourguide;

import java.net.URL;
import java.util.Collection;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IMouseLayer;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.rnb.internal.Resources;
import org.caleydo.view.rnb.internal.plugin.RnBView;
import org.caleydo.view.rnb.internal.plugin.RnBViewPart;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.vis.ITourGuideView;
import org.caleydo.view.tourguide.spi.adapter.ITourGuideAdapter;
import org.caleydo.view.tourguide.spi.score.IScore;
import org.caleydo.vis.lineup.model.RankTableModel;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ATourGuideAdapter implements ITourGuideAdapter {
	protected RnBView domino;
	protected ITourGuideView vis;
	private IDragGLSource dragSource;

	@Override
	public void addDefaultColumns(RankTableModel table) {
		asMode().addDefaultColumns(table);
	}

	@Override
	public final URL getIcon() {
		return Resources.ICON;
	}

	@Override
	public void setup(ITourGuideView vis, GLElementContainer lineUp) {
		this.vis = vis;
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
		if (!isSelected)
			return;
		IMouseLayer m = context.getMouseLayer();
		switch (pickingMode) {
		case MOUSE_OVER:
			if (dragSource != null) {
				m.removeDragSource(dragSource);
				dragSource = null;
			}
			m.addDragSource(dragSource = new DragRowSource(row));
			break;
		case MOUSE_OUT:
			if (dragSource != null) {
				m.removeDragSource(dragSource);
			}
			dragSource = null;
			break;
		default:
			break;
		}
	}

	@Override
	public final String getPartName() {
		return getLabel();
	}

	@Override
	public boolean filterBoundView(ADataDomainQuery query) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public final boolean isRepresenting(IWorkbenchPart part, boolean isBoundTo) {
		if (part instanceof RnBViewPart)
			return !isBoundTo || ((RnBViewPart) part).getView() == domino;
		return false;
	}

	@Override
	public final void bindTo(IViewPart part) {
		if (part instanceof RnBViewPart)
			this.domino = ((RnBViewPart)part).getView();
		else
			this.domino = null;
		if (vis != null)
			vis.updateBound2ViewState();
	}

	@Override
	public final boolean ignoreActive(IViewPart part) {
		return false;
	}

	@Override
	public final boolean isBound2View() {
		return this.domino != null;
	}
}
