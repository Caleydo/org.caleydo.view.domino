/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.InterpolatingFunctions;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.api.ui.band.Route;
import org.caleydo.view.domino.spi.model.IBandRenderer;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * this class represents a small fragment of a band representation including a path color and a set of represented ids
 *
 * @author Samuel Gratzl
 *
 */
public class BandRoute implements IBandRenderer {
	public static enum EBandMode {
		OVERVIEW, GROUPS, DETAIL
	}

	static final Predicate<Object> notPlaceholder = Predicates.not(Predicates.instanceOf(PlaceholderNode.class));

	private EBandMode mode = EBandMode.OVERVIEW;

	private static final Color color = new Color(180, 212, 231, 32);
	private final DataRoute overviewRoute;
	private List<DataRoute> groupRoutes;
	private List<DataRoute> detailRoutes;

	private final MultiTypedSet shared;

	private final TypedGroupList sData;
	private final TypedGroupList tData;

	public BandRoute(Route route, MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
			float sRadius, float tRadius) {
		this.shared = shared;
		this.sData = sData;
		this.tData = tData;
		{
			TypedSet sShared = shared.slice(sData.getIdType());
			TypedSet tShared = shared.slice(tData.getIdType());
			float sr = (sRadius * sShared.size()) / sData.size();
			float tr = (tRadius * tShared.size()) / tData.size();
			this.overviewRoute = new DataRoute(route, sr, tr, sShared, tShared);
		}
	}

	private final class DataRoute {
		private final Route route;
		private final float sr, tr;
		final TypedSet sShared, tShared;

		public DataRoute(Route route, float sr, float tr, TypedSet sShared, TypedSet tShared) {
			this.route = route;
			this.sr = sr;
			this.tr = tr;
			this.sShared = sShared;
			this.tShared = tShared;
		}

		void renderRoute(GLGraphics g, IBandHost host) {
			renderRoute(g, sr, tr, color);

			for (SelectionType type : selectionTypes()) {
				int sS = host.getSelected(sShared, type).size();
				int tS = host.getSelected(tShared, type).size();
				if (sS > 0 && tS > 0)
					renderRoute(g, sr * sS / sShared.size(), tr * tS / tShared.size(), type.getColor());
			}
			g.color(color.darker());
			route.setRadiusInterpolator(InterpolatingFunctions.linear(sr, tr));
			g.drawPath(route);
		}

		private List<SelectionType> selectionTypes() {
			return Arrays.asList(SelectionType.MOUSE_OVER, SelectionType.SELECTION);
		}

		private void renderRoute(GLGraphics g, float sr, float tr, Color c) {
			g.color(c.r, c.g, c.b, 0.5f);
			route.setRadiusInterpolator(InterpolatingFunctions.linear(sr, tr));
			g.fillPolygon(route);
		}
	}

	@Override
	public void render(GLGraphics g, float w, float h, IBandHost host) {
		switch (mode) {
		case OVERVIEW:
			overviewRoute.renderRoute(g, host);
			break;
		case GROUPS:
			for (DataRoute r : lazyGroupRoutes())
				r.renderRoute(g, host);
			break;
		case DETAIL:
			for (DataRoute r : lazyDetailRoutes())
				r.renderRoute(g, host);
			break;
		}

	}

	private Iterable<DataRoute> lazyGroupRoutes() {
		if (groupRoutes != null)
			return groupRoutes;
		// FIXME
		return groupRoutes;
	}

	/**
	 * @return
	 */
	private Iterable<DataRoute> lazyDetailRoutes() {
		if (detailRoutes != null)
			return detailRoutes;
		// FIXME
		return detailRoutes;
	}


	@Override
	public String getLabel() {
		return ""; // shared.getIdType().getTypeName() + ": " + StringUtils.join(shared, ",");
	}

	@Override
	public TypedSet getIds(SourceTarget type) {
		return type.select(overviewRoute.sShared, overviewRoute.tShared);
	}

	public TypedGroupList getAll(SourceTarget type) {
		return type.select(sData, tData);
	}

	@Override
	public IDType getIdType(SourceTarget type) {
		return getIds(type).getIdType();
	}

	@Override
	public void renderPick(GLGraphics g, float w, float h, IBandHost host) {
		// renderRoute(g, ratio(SourceTarget.SOURCE), ratio(SourceTarget.TARGET), color);
	}


	/**
	 * @param sourceB
	 * @param targetB
	 * @param dim
	 * @return
	 */
	private static List<Vec2f> createCurve(Rect s, Rect t) {
		if (Math.abs(s.x2() - t.x()) < 4)
			return Collections.emptyList();

		Vec2f sv = new Vec2f(s.x2()+10, s.y() + s.height() * 0.5f);
		Vec2f tv = new Vec2f(t.x() - 10, t.y() + t.height() * 0.5f);
		if (sv.y() == tv.y())
			return Arrays.asList(sv, tv);

		Vec2f shift = new Vec2f(20, 0);
		Vec2f s2 = sv.plus(shift);
		Vec2f t2 = tv.minus(shift);

		return Arrays.asList(sv, s2, t2, tv);
	}

	private static List<Vec2f> createQuestionMarkCurve(Rect s, Rect t) {
		Vec2f sa = new Vec2f(s.x2(), s.y() + s.height() * 0.5f);
		Vec2f ta = new Vec2f(t.x(), t.y() + t.height() * 0.5f);

		Vec2f shift = new Vec2f(20, 0);
		Vec2f sb = sa.plus(shift);
		Vec2f tb = ta.minus(shift);

		float yd = ta.y() - sa.y();
		Vec2f sc = sb.plus(new Vec2f(s.height() * 0.5f, s.height() * 0.5f));
		Vec2f tc = tb.minus(new Vec2f(t.height() * 0.5f, t.height() * 0.5f));

		Vec2f sd = sc.plus(new Vec2f(-10, yd * 0.45f - sc.y()));
		Vec2f td = tc.minus(new Vec2f(-10, yd * 0.45f - tc.y()));

		return Arrays.asList(sa, sb, sc, sd, td, tc, tb, ta);
	}

	/**
	 * @param curve
	 * @return
	 */
	private static List<Vec2f> rot90(List<Vec2f> curve) {
		List<Vec2f> r = new ArrayList<>(curve.size());
		for (Vec2f in : curve)
			r.add(new Vec2f(in.y(), in.x()));
		return r;
	}

	/**
	 * @param sourceB
	 * @return
	 */
	private static Rect rot90(Rect a) {
		return new Rect(a.y(), a.x(), a.height(), a.width());
	}

	public static class BandBlock {
		final EDimension dim;
		final LinearBlock block;
		final Rect bounds;

		public BandBlock(EDimension dim, LinearBlock block) {
			this.dim = dim;
			this.block = block;
			this.bounds = block.getBounds();

		}

		public float getTotal() {
			return dim.opposite().select(bounds.width(), bounds.height());
		}

		/**
		 * @return
		 */
		public TypedGroupList getData(boolean left) {
			if (left)
				return block.getFirst(notPlaceholder).getData(dim.opposite());
			else
				return block.getLast(notPlaceholder).getData(dim.opposite());
		}
	}

	public static BandRoute create(BandBlock sBlock, BandBlock tBlock) {
		BandRoute r;

		if ((r = test(sBlock, tBlock)) != null)
			return r;
		if ((r = test(tBlock, sBlock)) != null)
			return r;
		// complex case
		return null;
	}

	private static BandRoute test(BandBlock sBlock, BandBlock tBlock) {
		Rect sBounds = sBlock.bounds;
		Rect tBounds = tBlock.bounds;
		List<Vec2f> route;
		boolean sameDim = sBlock.dim == tBlock.dim;
		// simple right of
		if (sameDim && sBlock.dim == EDimension.DIMENSION && (route = test(sBounds, tBounds)) != null) {
			return createImpl(sBlock, tBlock, true, route);
		}
		if (sameDim && sBlock.dim == EDimension.RECORD && (route = test(rot90(sBounds), rot90(tBounds))) != null) {
			return createImpl(sBlock, tBlock, true, rot90(route));
		}
		return null;

	}

	private static List<Vec2f> test(Rect s, Rect t) {
		if (s.x2() < t.x() - 100) {
			return createCurve(s, t);
		} else {
			final float delta = Math.max(s.height(), t.height()) * 1.5f;
			if (t.x() < s.x2() && t.x() > s.x() && ((s.y2() < t.y() - delta) || (s.y() < t.y2() + delta))) {
				return createQuestionMarkCurve(s, t);
			}
		}
		return null;
	}

	private static BandRoute createImpl(BandBlock a, BandBlock b, boolean aRight, List<Vec2f> route) {
		TypedGroupList sData = a.getData(!aRight);
		TypedGroupList tData = b.getData(aRight);
		MultiTypedSet shared = TypedSets.intersect(sData.asSet(), tData.asSet());
		if (shared.isEmpty())
			return null;

		return new BandRoute(new Route(route), shared, sData, tData, a.getTotal() * 0.5f, b.getTotal() * 0.5f);
	}

}
