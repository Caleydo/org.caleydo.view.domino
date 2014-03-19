/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.opengl.GL2ES1;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.util.gleem.ColoredVec2f;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.Constants;
import org.caleydo.view.domino.internal.INodeLocator;
import org.caleydo.view.domino.internal.MiniMapCanvas.IHasMiniMap;
import org.caleydo.view.domino.internal.band.IBandHost.SourceTarget;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ABand implements ILabeled, IHasMiniMap {
	final static float SHIFT = 15;
	protected static final List<SelectionType> SELECTION_TYPES = Arrays.asList(SelectionType.SELECTION,
			SelectionType.MOUSE_OVER);

	protected final MultiTypedSet shared;

	protected final String sLabel, tLabel;
	protected final TypedGroupList sData;
	protected final TypedGroupList tData;

	protected EBandMode mode = EBandMode.GROUPS;

	private INodeLocator sLocator, tLocator;

	protected final EDirection sDir;
	protected final EDirection tDir;

	private final BandIdentifier id;

	private List<? extends IBandRenderAble> groupRoutes;
	private List<? extends IBandRenderAble> groupDetailRoutes;
	private List<? extends IBandRenderAble> detailRoutes;

	private int pickingOffset;

	private final IIDTypeMapper<Integer, String> s2Label, t2Label;

	public ABand(MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
 INodeLocator sLocator,
			INodeLocator tLocator, EDirection sDim, EDirection tDim, BandIdentifier id, Pair<String, String> labels) {
		this.shared = shared;
		this.sData = sData;
		this.tData = tData;
		this.sLocator = sLocator;
		this.tLocator = tLocator;
		this.sDir = sDim;
		this.tDir = tDim;
		this.id = id;
		this.sLabel = labels.getFirst();
		this.tLabel = labels.getSecond();

		s2Label = id2Label(sData.getIdType());
		if (sData.getIdType().equals(tData.getIdType()))
			t2Label = s2Label;
		else
			t2Label = id2Label(tData.getIdType());
	}

	private static IIDTypeMapper<Integer, String> id2Label(final IDType idType) {
		return IDMappingManagerRegistry.get().getIDMappingManager(idType)
				.getIDTypeMapper(idType, idType.getIDCategory().getHumanReadableIDType());
	}

	/**
	 * @param pickingOffset
	 *            setter, see {@link pickingOffset}
	 */
	public void setPickingOffset(int pickingOffset) {
		this.pickingOffset = pickingOffset;
	}

	/**
	 * @return the pickingOffset, see {@link #pickingOffset}
	 */
	public int getPickingOffset() {
		return pickingOffset;
	}

	protected static String toIntersectionLabel(String a, int asize, String b, int bsize, TypedSet ashared,
			TypedSet bshared) {
		StringBuilder s = new StringBuilder(String.format("%s \u2229 %s\n", a, b));
		s.append(String.format("|%d| \u2229 |%d| = ", asize, bsize));
		if (ashared == bshared)
			s.append(String.format("|%d| (%.2f%%, %.2f%%)", ashared.size(), 100 * ashared.size() / (float) asize, 100
					* bshared.size()
					/ (float) bsize));
		else
			s.append(String.format("|%d| (%.2f%%) |%d| (%.2f%%)", ashared.size(), 100 * ashared.size() / (float) asize,
					bshared.size(), 100 * bshared.size()
					/ (float) bsize));
		return s.toString();
	}

	protected String toItemLabel(TypedSet idsA, TypedSet idsB) {
		if (idsA.equals(idsB)) {
			return toLabels(idsA, s2Label);
		}
		return toLabels(idsA, s2Label) + " / " + toLabels(idsB, t2Label);
	}

	private String toLabels(TypedSet idsA, IIDTypeMapper<Integer, String> toLabel) {
		if (idsA.size() < 3)
			return StringUtils.join(toLabel.apply(idsA), ", ");
		else
			return String.format("|%d| (%s,...)", StringUtils.join(toLabel.apply(idsA.asList().subList(0, 3)), ", "));
	}

	protected static String toNotMappedLabel(String a, int asize, String b, int bsize, TypedSet notMapped) {
		return String.format("%s \u2216 %s\n|%d| \u2216 |%d| = |%d| (%.2f%%)", a, b, asize, bsize, notMapped, 100
				* notMapped.size() / (float) asize);
	}

	protected GLLocation locS(EBandMode mode, int id) {
		return sLocator.apply(mode, id, !sDir.isPrimaryDirection());
	}

	protected GLLocation loc(SourceTarget st, EBandMode mode, int id) {
		if (st == SourceTarget.SOURCE)
			return locS(mode, id);
		return locT(mode, id);
	}

	protected GLLocation locT(EBandMode mode, int id) {
		return tLocator.apply(mode, id, !tDir.isPrimaryDirection());
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
	 * @return the id, see {@link #id}
	 */
	public BandIdentifier getId() {
		return id;
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
		return type.select(sDir, tDir).asDim();
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

	public boolean intersects(Rectangle2D bounds) {
		for (IBandRenderAble r : overviewRoutes())
			if (r.intersects(bounds))
				return true;
		return false;
		// return overviewRoute().intersects(bounds);
	}

	@Override
	public Rect getBoundingBox() {
		return overviewRoute().getBoundingBox();
	}

	public void render(GLGraphics g, float w, float h, IBandHost host) {
		switch (mode) {
		case OVERVIEW:
			renderRoutes(g, host, overviewRoutes(), true);
			break;
		case GROUPS:
			final Collection<? extends IBandRenderAble> gR = groupRoutes();
			if (gR.isEmpty()) { // auto switch to the previous one
				mode = EBandMode.OVERVIEW;
				render(g, w, h, host);
				return;
			}
			renderRoutes(g, host, gR, true);
			break;
		case GROUPED_DETAIL:
			boolean smooth = g.gl.glIsEnabled(GL2ES1.GL_POINT_SMOOTH);
			g.gl.glEnable(GL2ES1.GL_POINT_SMOOTH);
			g.pointSize(Constants.SCATTER_POINT_SIZE);
			final Collection<? extends IBandRenderAble> gdR = groupDetailRoutes();
			if (this instanceof ParaBand) // FIXME hack
				renderRoutes(g, host, groupRoutes(), false);
			renderRoutes(g, host, gdR, true);
			if (!smooth)
				g.gl.glDisable(GL2ES1.GL_POINT_SMOOTH);
			g.pointSize(1);
			break;
		case DETAIL:
			smooth = g.gl.glIsEnabled(GL2ES1.GL_POINT_SMOOTH);
			g.gl.glEnable(GL2ES1.GL_POINT_SMOOTH);
			g.pointSize(Constants.SCATTER_POINT_SIZE);
			final List<? extends IBandRenderAble> lR = detailRoutes();
			if (lR.isEmpty()) { // auto switch to the previous one
				mode = EBandMode.GROUPS;
				render(g, w, h, host);
				return;
			}
			renderRoutes(g, host, lR, true);
			if (!smooth)
				g.gl.glDisable(GL2ES1.GL_POINT_SMOOTH);
			g.pointSize(1);
			break;
		}
	}

	protected void renderRoutes(GLGraphics g, IBandHost host, final Collection<? extends IBandRenderAble> routes,
			boolean withSelection) {
		float z = g.z();
		final int size = routes.size();
		for (IBandRenderAble r : routes) {
			g.incZ(0.0001f);
			r.renderRoute(g, host, size, withSelection);
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
		final int size = bands.size();
		for (IBandRenderAble r : bands) {
			g.pushName(pickingPool.get(start++));
			r.renderRoute(g, host, size, false);
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
			if (!increase) {
				if (detailsThere)
					mode = EBandMode.DETAIL;
				else if (hasGroups)
					mode = EBandMode.GROUPED_DETAIL;
			} else if (hasGroups)
				mode = EBandMode.GROUPS;
			else if (detailsThere)
				mode = EBandMode.DETAIL;
			break;
		case GROUPS:
			if (!increase)
				mode = EBandMode.OVERVIEW;
			else if (detailsThere)
				mode = EBandMode.GROUPED_DETAIL;
			else
				mode = EBandMode.OVERVIEW;
			break;
		case GROUPED_DETAIL:
			if (!increase)
				mode = EBandMode.GROUPS;
			else
				mode = EBandMode.DETAIL;
			break;
		case DETAIL:
			if (increase)
				mode = EBandMode.OVERVIEW;
			else if (hasGroups)
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

	public String getTooltip(int subIndex) {
		IBandRenderAble r = getRoute(subIndex);
		return r == null ? "" : r.getLabel();
	}

	public String getLabel(int subIndex) {
		IBandRenderAble r = getRoute(subIndex);
		if (r == null)
			return "";
		String l = r.getLabel();
		int i = l.indexOf('\n');
		if (i < 0)
			return l;
		return l.substring(0, i);
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
		void renderRoute(GLGraphics g, IBandHost host, int nrBands, boolean withSelection);

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
		return type.select(sDir, tDir);
	}

	@Override
	public Vec2f getLocation() {
		return new Vec2f(0, 0);
	}

	@Override
	public EVisibility getVisibility() {
		return EVisibility.VISIBLE;
	}

	protected abstract class ARelation implements IBandRenderAble {
		private final String label;
		protected final TypedSet sShared, tShared;
		protected final EBandMode mode;

		public ARelation(String label, TypedSet sData, TypedSet tData, EBandMode mode) {
			this.label = label;
			this.sShared = sData;
			this.tShared = tData;
			this.mode = mode;
		}

		/**
		 * @return the label, see {@link #label}
		 */
		@Override
		public final String getLabel() {
			return label;
		}

		@Override
		public final TypedSet asSet(SourceTarget type) {
			return type.select(sShared, tShared);
		}

	}

	protected class NotMapped extends ARelation {
		private final List<Vec2f> points;
		private final Polygon shape;
		private final SourceTarget type;
		private final float fz;
		private final EDirection dir;

		public NotMapped(String label, TypedSet sData, TypedSet tData, SourceTarget type, Vec4f s, Vec4f t,
				EDirection dir, EBandMode mode) {
			super(label, sData, tData, mode);
			this.type = type;
			this.dir = dir;

			this.points = new ArrayList<>(5);
			this.shape = new Polygon();
			this.fz = type.select(s, t).z();
			float a = 0.3f;
			if (dir.isHorizontal()) {
				float sh = dir == EDirection.WEST ? +SHIFT : -SHIFT;
				if (type == SourceTarget.SOURCE) {
					addPoint(s.x(), s.y());
					addPoint(s.x() + sh, s.y());
					addPoint(s.x() * (1.f - a) + t.x() * a, (s.y() + s.z() * 0.5f) * (1 - a) + (t.y() + t.z()) * a);
					addPoint(s.x() + sh, s.y() + s.z());
					addPoint(s.x(), s.y() + s.z());
				} else {
					addPoint(t.x(), t.y() + t.z());
					addPoint(t.x() - sh, t.y() + t.z());
					addPoint(t.x() * (1.f - a) + s.x() * a, (t.y() + t.z() * 0.5f) * (1 - a) + (s.y() + s.z()) * a);
					addPoint(t.x() - sh, t.y());
					addPoint(t.x(), t.y());
				}
			} else {
				float sh = dir == EDirection.NORTH ? -SHIFT : +SHIFT;
				if (type == SourceTarget.SOURCE) {
					addPoint(s.x(), s.y());
					addPoint(s.x(), s.y() + sh);
					addPoint((s.x() + s.z() * 0.5f) * (1 - a) + (t.x() + t.z()) * a, s.y() * (1.f - a) + t.y() * a);
					addPoint(s.x() + s.z(), s.y() + sh);
					addPoint(s.x() + s.z(), s.y());
				} else {
					addPoint(t.x() + t.z(), t.y());
					addPoint(t.x() + t.z(), t.y() - sh);
					addPoint((t.x() + t.z() * 0.5f) * (1 - a) + (s.x() + s.z()) * a, t.y() * (1.f - a) + s.y() * a);
					addPoint(t.x(), t.y() - sh);
					addPoint(t.x(), t.y());
				}
			}
		}

		@Override
		public Rect getBoundingBox() {
			return new Rect(shape.getBounds2D());
		}

		private void addPoint(float x, float y) {
			points.add(new Vec2f(x, y));
			shape.addPoint((int) x, (int) y);
		}

		@Override
		public void renderRoute(GLGraphics g, IBandHost host, int nrBands, boolean withSelection) {
			Color color = mode.getColor();
			final float alpha = EBandMode.alpha(nrBands);
			g.color(color.r, color.g, color.b, color.a * alpha);
			final Collection<Vec2f> colored = colored(points, color, alpha);
			g.fillPolygon(TesselatedPolygons.polygon2(colored));

			for (SelectionType type : SELECTION_TYPES) {
				final TypedSet d = this.type.select(sShared, tShared);
				int se = host.getSelected(d, type).size();
				if (se > 0) {
					Color c = type.getColor();
					List<Vec2f> p = new ArrayList<>(6);
					float sf = se / (float) d.size();

					float shiftX = dir.isHorizontal() ? 0 : -fz * (1 - sf);
					float shiftY = dir.isVertical() ? 0 : -fz * (1 - sf);

					if (this.type == SourceTarget.SOURCE) {
						p.add(points.get(0));
						p.add(points.get(1));
						p.add(points.get(2));
						p.add(shifted(points.get(3), shiftX, shiftY));
						p.add(shifted(points.get(4), shiftX, shiftY));
					} else {
						p.add(shifted(points.get(0), shiftX, shiftY));
						p.add(shifted(points.get(1), shiftX, shiftY));
						p.add(points.get(2));
						p.add(points.get(3));
						p.add(points.get(4));
					}
					g.fillPolygon(TesselatedPolygons.polygon2(colored(p, c, alpha)));
				}
			}
			g.drawPath(colored, true);
		}

		private Vec2f shifted(Vec2f v, float dx, float dy) {
			return new Vec2f(v.x() + dx, v.y() + dy);
		}

		private Collection<Vec2f> colored(List<Vec2f> points, Color color, float alpha) {
			assert points.size() == 5;
			alpha *= color.a;
			Vec2f[] r = new Vec2f[5];
			r[0] = new ColoredVec2f(points.get(0), new Color(color.r, color.g, color.b, alpha));
			r[1] = new ColoredVec2f(points.get(1), new Color(color.r, color.g, color.b, alpha * .5f));
			r[2] = new ColoredVec2f(points.get(2), new Color(color.r, color.g, color.b, 0));
			r[3] = new ColoredVec2f(points.get(3), new Color(color.r, color.g, color.b, alpha * .5f));
			r[4] = new ColoredVec2f(points.get(4), new Color(color.r, color.g, color.b, alpha));
			return Arrays.asList(r);
		}

		@Override
		public boolean intersects(Rectangle2D bounds) {
			return shape.intersects(bounds);
		}
	}
}
