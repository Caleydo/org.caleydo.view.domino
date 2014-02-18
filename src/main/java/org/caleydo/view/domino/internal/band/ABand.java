/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.INodeLocator;
import org.caleydo.view.domino.internal.MiniMapCanvas.IHasMiniMap;
import org.caleydo.view.domino.internal.band.IBandHost.SourceTarget;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ABand implements ILabeled, IHasMiniMap {
	protected static final List<SelectionType> SELECTION_TYPES = Arrays.asList(SelectionType.SELECTION,
			SelectionType.MOUSE_OVER);

	protected final MultiTypedSet shared;

	protected final TypedGroupList sData;
	protected final TypedGroupList tData;

	protected EBandMode mode = EBandMode.GROUPS;

	protected INodeLocator sLocator, tLocator;

	protected final EDirection sDim;
	protected final EDirection tDim;

	private final String identifier;

	private List<? extends IBandRenderAble> groupRoutes;
	private List<? extends IBandRenderAble> groupDetailRoutes;
	private List<? extends IBandRenderAble> detailRoutes;

	public ABand(MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
 INodeLocator sLocator,
			INodeLocator tLocator, EDirection sDim, EDirection tDim, String identifier) {
		this.shared = shared;
		this.sData = sData;
		this.tData = tData;
		this.sLocator = sLocator;
		this.tLocator = tLocator;
		this.sDim = sDim;
		this.tDim = tDim;
		this.identifier = identifier;
	}

	public INodeLocator getLocator(SourceTarget type) {
		return type.select(sLocator, tLocator);
	}

	public void setLocators(INodeLocator sLocator, INodeLocator tLocator) {
		this.sLocator = sLocator;
		this.tLocator = tLocator;
		groupRoutes = null;
		groupDetailRoutes = null;
		detailRoutes = null;
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

	@Override
	public String getLabel() {
		return overviewRoute().getLabel();
	}

	public EDimension getDimension(SourceTarget type) {
		return type.select(sDim, tDim).asDim();
	}

	public Pair<TypedSet, TypedSet> intersectingIds(Rectangle2D bounds) {
		IBandRenderAble r = overviewRoute();
		if (!r.intersects(bounds))
			return Pair.make(TypedCollections.empty(getIdType(SourceTarget.SOURCE)),
					TypedCollections.empty(getIdType(SourceTarget.TARGET)));

		Collection<? extends IBandRenderAble> l;
		switch(mode) {
		case OVERVIEW:
			l = overviewRoutes();
			if (l.size() == 1)
				return Pair.make(r.asSet(SourceTarget.SOURCE), r.asSet(SourceTarget.TARGET));
			break;
		case GROUPS:
			l = groupRoutes();
			break;
		case GROUPED_DETAIL:
			l = groupDetailRoutes();
			break;
		case DETAIL:
			l = detailRoutes();
			break;
		default:
			throw new IllegalStateException();
		}

		Set<Integer> rs = new HashSet<>();
		Set<Integer> rt = new HashSet<>();
		for(IBandRenderAble ri : l) {
			if (ri.intersects(bounds)) {
				rs.addAll(ri.asSet(SourceTarget.SOURCE));
				rt.addAll(ri.asSet(SourceTarget.TARGET));
			}
		}
		return Pair.make(new TypedSet(rs, getIdType(SourceTarget.SOURCE)), new TypedSet(rs,
				getIdType(SourceTarget.TARGET)));
	}

	public final boolean intersects(Rectangle2D bounds) {
		return overviewRoute().intersects(bounds);
	}

	@Override
	public Rect getBoundingBox() {
		return overviewRoute().getBoundingBox();
	}

	public final void render(GLGraphics g, float w, float h, IBandHost host) {
		switch (mode) {
		case OVERVIEW:
			renderRoutes(g, host, overviewRoutes());
			break;
		case GROUPS:
			final Collection<? extends IBandRenderAble> gR = groupRoutes();
			if (gR.isEmpty()) { // auto switch to the previous one
				mode = EBandMode.OVERVIEW;
				render(g, w, h, host);
				return;
			}
			renderRoutes(g, host, gR);
			break;
		case GROUPED_DETAIL:
			final Collection<? extends IBandRenderAble> gdR = groupDetailRoutes();
			renderRoutes(g, host, gdR);
			break;
		case DETAIL:
			final List<? extends IBandRenderAble> lR = detailRoutes();
			if (lR.isEmpty()) { // auto switch to the previous one
				mode = EBandMode.GROUPS;
				render(g, w, h, host);
				return;
			}
			renderRoutes(g, host, lR);
			break;
		}
	}

	protected void renderRoutes(GLGraphics g, IBandHost host, final Collection<? extends IBandRenderAble> routes) {
		float z = g.z();
		for (IBandRenderAble r : routes) {
			g.incZ(0.0001f);
			r.renderRoute(g, host, routes.size());
		}
		g.incZ(z - g.z());
	}

	/**
	 * @return
	 */
	protected abstract IBandRenderAble overviewRoute();

	protected List<? extends IBandRenderAble> overviewRoutes() {
		return Collections.singletonList(overviewRoute());
	}

	protected final List<? extends IBandRenderAble> groupRoutes() {
		if (this.groupRoutes != null)
			return groupRoutes;
		return groupRoutes = computeGroupRoutes();
	}

	protected abstract List<? extends IBandRenderAble> computeGroupRoutes();

	protected abstract List<? extends IBandRenderAble> computeDetailRoutes();

	protected abstract List<? extends IBandRenderAble> computeGroupDetailRoutes();

	protected final List<? extends IBandRenderAble> detailRoutes() {
		if (this.detailRoutes != null)
			return detailRoutes;
		return detailRoutes = computeDetailRoutes();
	}

	protected final List<? extends IBandRenderAble> groupDetailRoutes() {
		if (this.groupDetailRoutes != null)
			return groupDetailRoutes;
		return groupDetailRoutes = computeGroupDetailRoutes();
	}

	public final int renderPick(GLGraphics g, float w, float h, IBandHost host, PickingPool pickingPool, int start) {
		switch (mode) {
		case OVERVIEW:
			start = renderRoutePick(g, host, pickingPool, start, overviewRoutes());
			break;
		case GROUPS:
			start = renderRoutePick(g, host, pickingPool, start, groupRoutes());
			break;
		case GROUPED_DETAIL:
			start = renderRoutePick(g, host, pickingPool, start, groupDetailRoutes());
			break;
		case DETAIL:
			start = renderRoutePick(g, host, pickingPool, start, detailRoutes());
			break;
		}
		return start;
	}

	private int renderRoutePick(GLGraphics g, IBandHost host, PickingPool pickingPool, int start,
			final List<? extends IBandRenderAble> bands) {
		for (IBandRenderAble r : bands) {
			g.pushName(pickingPool.get(start++));
			r.renderRoute(g, host, bands.size());
			g.popName();
		}
		return start;
	}
	private boolean canHaveDetailMode() {
		return sLocator.hasLocator(EBandMode.DETAIL) && tLocator.hasLocator(EBandMode.DETAIL);
	}
	/**
	 * @param b
	 */
	public void changeLevel(boolean increase) {
		boolean detailsThere = canHaveDetailMode();
		boolean hasGroups = sData.getGroups().size() > 1 || tData.getGroups().size() > 1;
		switch (mode) {
		case OVERVIEW:
			if (!increase)
				return;
			if (hasGroups)
				mode = EBandMode.GROUPS;
			else if (detailsThere)
				mode = EBandMode.DETAIL;
			break;
		case GROUPS:
			if (!increase)
				mode = EBandMode.OVERVIEW;
			else if (detailsThere)
				mode = EBandMode.GROUPED_DETAIL;
			break;
		case GROUPED_DETAIL:
			if (!increase)
				mode = EBandMode.GROUPS;
			else
				mode = EBandMode.DETAIL;
			break;
		case DETAIL:
			if (increase)
				return;
			if (hasGroups)
				mode = EBandMode.GROUPED_DETAIL;
			else
				mode = EBandMode.OVERVIEW;
			break;
		}
	}

	public void setLevel(EBandMode mode) {
		this.mode = mode;
	}


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
			final List<? extends IBandRenderAble> o = overviewRoutes();
			if (subIndex < 0 || o.size() <= subIndex)
				return overviewRoute();
			return o.get(subIndex);
		case GROUPS:
			final List<? extends IBandRenderAble> g = groupRoutes();
			if (subIndex < 0 || g.size() <= subIndex)
				return null;
			return g.get(subIndex);
		case GROUPED_DETAIL:
			final List<? extends IBandRenderAble> d = groupDetailRoutes();
			if (subIndex < 0 || d.size() <= subIndex)
				return null;
			return d.get(subIndex);
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
		void renderRoute(GLGraphics g, IBandHost host, int nrBands);

		/**
		 * @return
		 */
		Rect getBoundingBox();

		/**
		 * @param bounds
		 * @return
		 */
		boolean intersects(Rectangle2D bounds);

		/**
		 * @param type
		 * @return
		 */
		TypedSet asSet(SourceTarget type);
	}

	/**
	 * @param source
	 * @return
	 */
	public EDirection getAttachingDirection(SourceTarget type) {
		return type.select(sDim, tDim);
	}

	@Override
	public Vec2f getLocation() {
		return new Vec2f(0, 0);
	}

	@Override
	public EVisibility getVisibility() {
		return EVisibility.VISIBLE;
	}
}
