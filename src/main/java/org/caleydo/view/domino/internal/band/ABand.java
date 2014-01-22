/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.INodeLocator;
import org.caleydo.view.domino.internal.band.IBandHost.SourceTarget;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ABand implements ILabeled {
	protected static final Color color = new Color(180, 212, 231, 32);

	protected static final List<SelectionType> SELECTION_TYPES = Arrays.asList(SelectionType.MOUSE_OVER,
			SelectionType.SELECTION);

	protected final MultiTypedSet shared;

	protected final TypedGroupList sData;
	protected final TypedGroupList tData;

	protected EBandMode mode = EBandMode.GROUPS;

	protected INodeLocator sLocator, tLocator;

	protected final EDimension sDim;
	protected final EDimension tDim;

	private final String identifier;

	public ABand(MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
			INodeLocator sLocator, INodeLocator tLocator, EDimension sDim,
 EDimension tDim, String identifier) {
		this.shared = shared;
		this.sData = sData;
		this.tData = tData;
		this.sLocator = sLocator;
		this.tLocator = tLocator;
		this.sDim = sDim;
		this.tDim = tDim;
		this.identifier = identifier;
	}

	/**
	 * @return the identifier, see {@link #identifier}
	 */
	public String getIdentifier() {
		return identifier;
	}

	public void initFrom(ABand band) {
		this.mode = band.mode;
	}

	/**
	 *
	 */
	public abstract boolean stubify();

	protected void updateBand(INodeLocator sLocator, INodeLocator tLocator) {
		this.sLocator = sLocator;
		this.tLocator = tLocator;
	}

	@Override
	public String getLabel() {
		return overviewRoute().getLabel();
	}

	public EDimension getDimension(SourceTarget type) {
		return type.select(sDim, tDim);
	}

	public abstract void renderMiniMap(GLGraphics g);

	public final void render(GLGraphics g, float w, float h, IBandHost host) {
		switch (mode) {
		case OVERVIEW:
			overviewRoute().renderRoute(g, host);
			break;
		case GROUPS:
			float z = g.z();
			final Collection<? extends IBandRenderAble> gR = groupRoutes();
			if (gR.isEmpty()) {
				mode = EBandMode.OVERVIEW;
				render(g, w, h, host);
				return;
			}
			for (IBandRenderAble r : gR) {
				g.incZ(0.0001f);
				r.renderRoute(g, host);
			}
			g.incZ(z - g.z());
			break;
		case DETAIL:
			z = g.z();
			final List<? extends IBandRenderAble> lR = detailRoutes();
			if (lR.isEmpty()) {
				mode = EBandMode.GROUPS;
				render(g, w, h, host);
				return;
			}
			for (IBandRenderAble r : lR) {
				g.incZ(0.0001f);
				r.renderRoute(g, host);
			}
			g.incZ(z - g.z());
			break;
		}
	}

	/**
	 * @return
	 */
	protected abstract IBandRenderAble overviewRoute();

	protected abstract List<? extends IBandRenderAble> groupRoutes();

	protected abstract List<? extends IBandRenderAble> detailRoutes();

	public final int renderPick(GLGraphics g, float w, float h, IBandHost host, PickingPool pickingPool, int start) {
		switch (mode) {
		case OVERVIEW:
			g.pushName(pickingPool.get(start++));
			overviewRoute().renderRoute(g, host);
			g.popName();
			break;
		case GROUPS:
			for (IBandRenderAble r : groupRoutes()) {
				g.pushName(pickingPool.get(start++));
				r.renderRoute(g, host);
				g.popName();
			}
			break;
		case DETAIL:
			for (IBandRenderAble r : detailRoutes()) {
				g.pushName(pickingPool.get(start++));
				r.renderRoute(g, host);
				g.popName();
			}
			break;
		}
		return start;
	}
	private boolean canHaveDetailMode() {
		return sLocator != null && GLLocation.NO_LOCATOR != sLocator && tLocator != null
				&& GLLocation.NO_LOCATOR != tLocator;
	}
	/**
	 * @param b
	 */
	public void changeLevel(boolean increase) {
		if ((mode == EBandMode.OVERVIEW && !increase))
			return;
		boolean detailsThere = canHaveDetailMode();
		if ((mode == EBandMode.GROUPS && !detailsThere && increase) || (mode == EBandMode.DETAIL && increase))
			return;
		mode = EBandMode.values()[this.mode.ordinal() + (increase ? 1 : -1)];
	}

	public abstract boolean intersects(Rectangle2D bound);


	public TypedSet getIds(SourceTarget type, int subIndex) {
		IBandRenderAble r = getRoute(subIndex);
		return r == null ? overviewRoute().asSet(type) : r.asSet(type);
	}

	public String getLabel(int subIndex) {
		IBandRenderAble r = getRoute(subIndex);
		return r == null ? "" : r.getLabel();
	}

	private IBandRenderAble getRoute(int subIndex) {
		switch (mode) {
		case OVERVIEW:
			return overviewRoute();
		case GROUPS:
			final List<? extends IBandRenderAble> g = groupRoutes();
			if (subIndex < 0 || g.size() <= subIndex)
				return null;
			return g.get(subIndex);
		case DETAIL:
			final List<? extends IBandRenderAble> l = detailRoutes();
			if (subIndex < 0 || l.size() <= subIndex)
				return null;
			return l.get(subIndex);
		}
		throw new IllegalStateException();
	}

	public IDType getIdType(SourceTarget type) {
		return overviewRoute().asSet(type).getIdType();
	}

	protected interface IBandRenderAble extends ILabeled {
		void renderRoute(GLGraphics g, IBandHost host);

		/**
		 * @param type
		 * @return
		 */
		TypedSet asSet(SourceTarget type);
	}
}
