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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.INodeLocator;
import org.caleydo.view.domino.internal.band.IBandHost.SourceTarget;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * @author Samuel Gratzl
 *
 */
public class ParaBand extends ABand {
	final static float SHIFT = 30;

	private Band overview;
	private final List<IBandRenderAble> overviewRoutes = new ArrayList<>(3);

	private final Vec2f s, t;

	public ParaBand(String label, MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
			Vec2f sLoc, Vec2f tLoc, INodeLocator sLocator, INodeLocator tLocator, EDirection sDim, EDirection tDim, String identifier) {
		super(shared, sData, tData, sLocator, tLocator, sDim, tDim, identifier);
		this.s = sLoc;
		this.t = tLoc;
		{
			TypedSet sShared = shared.slice(sData.getIdType());
			TypedSet tShared = shared.slice(tData.getIdType());
			float sr = ((float) sShared.size()) / sData.size();
			float tr = ((float) tShared.size()) / tData.size();
			Vec4f sv = toVec3(sLoc, sLocator, sr, 0);
			Vec4f tv = toVec3(tLoc, tLocator, tr, 0);
			this.overview = new Band(label, sShared, tShared, sv, tv);

			overviewRoutes.add(this.overview);
			String[] split = label.split(" x ");
			if (sr < 1) {
				// add a non-mapped indicator
				TypedSet sNotMapped = sData.asSet().difference(sShared);
				sv = toVec3(sLoc, sLocator, (1 - sr), sr);
				overviewRoutes.add(new NotMapped(split[0] + " x Not Mapped", sNotMapped, TypedCollections.empty(tData
						.getIdType()), SourceTarget.SOURCE, sv));
			}
			if (tr < 1) {
				TypedSet tNotMapped = tData.asSet().difference(tShared);
				tv = toVec3(tLoc, tLocator, (1 - tr), tr);
				overviewRoutes.add(new NotMapped("Not Mapped x " + split[1], TypedCollections.empty(sData.getIdType()),
						tNotMapped, SourceTarget.TARGET, tv));
			}
		}
	}

	public boolean isHorizontal() {
		return sDim.isHorizontal();
	}

	private Vec4f toVec3(Vec2f xy, INodeLocator loc, float scale, float move) {
		GLLocation l = loc.apply(EBandMode.OVERVIEW, 0);
		if (isHorizontal())
			return new Vec4f(xy.x(), xy.y() + (float) l.getOffset() + (float) l.getSize() * move, (float) l.getSize()
					* scale, 0);
		else
			return new Vec4f(xy.x() + (float) l.getOffset() + (float) l.getSize() * move, xy.y(), (float) l.getSize()
					* scale, 0);
	}

	@Override
	public void renderMiniMap(GLGraphics g) {
		overview.renderMiniMap(g);
	}

	@Override
	public void setLocators(INodeLocator sLocator, INodeLocator tLocator) {
		super.setLocators(sLocator, tLocator);
		{
			float sr = ((float) overview.sShared.size()) / sData.size();
			float tr = ((float) overview.tShared.size()) / tData.size();
			this.overview = new Band(overview.getLabel(), overview.sShared, overview.tShared,
					toVec3(s, sLocator, sr, 0), toVec3(t, tLocator, tr, 0));
			overviewRoutes.set(0, this.overview);
			if (sr < 1) {
				NotMapped m = (NotMapped) overviewRoutes.get(1);
				// add a non-mapped indicator
				Vec4f sv = toVec3(s, sLocator, (1 - sr), sr);
				overviewRoutes.set(1, new NotMapped(m.getLabel(), m.sShared, m.tShared, SourceTarget.SOURCE, sv));
			}
			if (tr < 1) {
				int index = sr < 1 ? 2 : 1;
				NotMapped m = (NotMapped) overviewRoutes.get(index);
				Vec4f tv = toVec3(t, tLocator, (1 - tr), tr);
				overviewRoutes.set(index, new NotMapped(m.getLabel(), m.sShared, m.tShared, SourceTarget.TARGET, tv));
			}
		}
	}

	@Override
	public boolean stubify() {
		return false;
	}

	@Override
	protected IBandRenderAble overviewRoute() {
		return overview;
	}

	@Override
	protected List<? extends IBandRenderAble> overviewRoutes() {
		return overviewRoutes;
	}

	@Override
	protected List<? extends IBandRenderAble> computeGroupRoutes() {
		List<IBandRenderAble> groupRoutes = new ArrayList<>();

		List<TypedSet> sSets = new ArrayList<>();
		List<TypedSet> tSets = new ArrayList<>();

		// convert all to the subset of the shared set
		final List<TypedListGroup> sgroups = sData.getGroups();
		for (TypedListGroup sGroup : sgroups) {
			sSets.add(overview.sShared.intersect(sGroup.asSet()));
		}
		final List<TypedListGroup> tgroups = tData.getGroups();
		for (TypedListGroup tGroup : tgroups) {
			tSets.add(overview.tShared.intersect(tGroup.asSet()));
		}

		// starting points for right side groups
		int[] tinneracc = new int[tgroups.size()];
		Arrays.fill(tinneracc, 0);

		final TypedSet tEmpty = TypedCollections.empty(tData.getIdType());
		final TypedSet sEmpty = TypedCollections.empty(sData.getIdType());

		final boolean horizontal = isHorizontal();

		// for each left groups check all right groups
		for (int i = 0; i < sgroups.size(); ++i) {
			TypedListGroup sgroup = sgroups.get(i);
			TypedSet sset = sSets.get(i);
			if (sset.isEmpty())
				continue;
			GLLocation sgroupLocation = sLocator.apply(EBandMode.GROUPS, i);
			int sinneracc = 0;
			final double sFactor = sgroupLocation.getSize() / sgroup.size();
			for (int j = 0; j < tgroups.size(); ++j) {
				TypedListGroup tgroup = tgroups.get(j);
				TypedSet tset = tSets.get(j);
				if (tset.isEmpty())
					continue;

				MultiTypedSet shared = TypedSets.intersect(sset, tset);
				if (shared.isEmpty()) // nothing shared
					continue;

				GLLocation tgroupLocation = tLocator.apply(EBandMode.GROUPS, j);
				final double tFactor = tgroupLocation.getSize() / tgroup.size();
				TypedSet sShared = shared.slice(sData.getIdType());
				TypedSet tShared = shared.slice(tData.getIdType());
				double s1 = (sgroupLocation.getOffset() + sinneracc * sFactor);
				double s2 = s1 + sShared.size() * sFactor;
				double t1 = (tgroupLocation.getOffset() + tinneracc[j] * tFactor);
				double t2 = t1 + tShared.size() * tFactor;
				String label = sgroup.getLabel() + " x " + tgroup.getLabel();
				Vec4f sg, tg;
				if (horizontal) {
					sg = new Vec4f(this.s.x(), this.s.y() + (float) s1, (float) (s2 - s1), 0);
					tg = new Vec4f(this.t.x(), this.t.y() + (float) t1, (float) (t2 - t1), 0);
				} else {
					sg = new Vec4f(this.s.x() + (float) s1, this.s.y(), (float) (s2 - s1), 0);
					tg = new Vec4f(this.t.x() + (float) t1, this.t.y(), (float) (t2 - t1), 0);
				}
				groupRoutes.add(new Band(label, sShared, tShared, sg, tg));
				sinneracc += sShared.size();
				tinneracc[j] += tShared.size();
			}

			final int notMapped = sgroup.size() - sinneracc;
			if (notMapped > 0) {
				TypedSet notMappedIds = sgroup.asSet().difference(overview.sShared);
				double s1 = (sgroupLocation.getOffset() + sinneracc * sFactor);
				Vec4f s;
				if (horizontal)
					s = new Vec4f(this.s.x(), this.s.y() + (float) s1, (float) (sgroupLocation.getOffset2() - s1), 0);
				else
					s = new Vec4f(this.s.x() + (float) s1, this.s.y(), (float) (sgroupLocation.getOffset2() - s1), 0);
				groupRoutes.add(new NotMapped(sgroup.getLabel() + " x Not Mapped", notMappedIds, tEmpty, SourceTarget.SOURCE, s));
			}
		}


		for (int i = 0; i < tgroups.size(); ++i) {
			TypedListGroup tgroup = tgroups.get(i);
			final int notMapped = tgroup.size() - tinneracc[i];
			if (notMapped <= 0)
				continue;
			GLLocation tgroupLocation = tLocator.apply(EBandMode.GROUPS, i);
			final double tFactor = tgroupLocation.getSize() / tgroup.size();
			TypedSet notMappedIds = tgroup.asSet().difference(overview.tShared);
			double s1 = (tgroupLocation.getOffset() + tinneracc[i] * tFactor);
			Vec4f s;
			if (horizontal)
				s = new Vec4f(this.t.x(), this.t.y() + (float) s1, (float) (tgroupLocation.getOffset2() - s1), 0);
			else
				s = new Vec4f(this.t.x() + (float) s1, this.t.y(), (float) (tgroupLocation.getOffset2() - s1), 0);
			groupRoutes.add(new NotMapped("Not Mapped x " + tgroup.getLabel(), sEmpty, notMappedIds, SourceTarget.TARGET, s));
		}

		return groupRoutes;
	}

	@Override
	protected List<? extends IBandRenderAble> computeGroupDetailRoutes() {
		List<IBandRenderAble> detailRoutes = new ArrayList<>();

		TypedList s = shared.sliceList(sData.getIdType());
		final Multimap<Integer, Integer> slookup = computeLookup(s);
		TypedList t = shared.sliceList(tData.getIdType());
		final Multimap<Integer, Integer> tlookup = computeLookup(tData);

		Table<TypedListGroup, TypedListGroup, NavigableSet<LineAcc>> lines = ArrayTable.create(sData.getGroups(),
				tData.getGroups());

		final List<TypedListGroup> sgroups = sData.getGroups();
		final List<TypedListGroup> tgroups = tData.getGroups();
		int i = 0;
		for (int ig = 0; ig < sgroups.size(); ++ig) {
			final TypedListGroup sgroup = sgroups.get(ig);
			Map<TypedListGroup, NavigableSet<LineAcc>> row = lines.row(sgroup);
			for (int ii = 0; ii < sgroup.size(); ++ii) {
				i++;
				Integer sId = sgroup.get(ii);
				if (sId.intValue() < 0) {
					continue;
				}
				Collection<Integer> indices = slookup.get(sId);
				if (indices.isEmpty()) { // not shared
					continue;
				}
				GLLocation slocation = sLocator.apply(EBandMode.DETAIL, i);
				if (!slocation.isDefined()) { // don't know where it is
					continue;
				}
				Vec2f sloc = new Vec2f((float) slocation.getOffset(), (float) slocation.getOffset2());

				for (int sindex : indices) {
					Integer tId = t.get(sindex);
					Collection<Integer> tindices = tlookup.get(tId);
					if (tindices.isEmpty())
						continue;
					for (int tindex : tindices) {
						GLLocation tlocation = tLocator.apply(EBandMode.DETAIL, tindex);
						if (!tlocation.isDefined())
							continue;
						Vec2f tloc = new Vec2f((float) tlocation.getOffset(), (float) tlocation.getOffset2());

						TypedListGroup tgroup = tData.groupAt(tindex);

						NavigableSet<LineAcc> stlines = row.get(tgroup);
						if (stlines == null)
							row.put(tgroup, stlines = new TreeSet<LineAcc>());

						final LineAcc l = new LineAcc(sloc, tloc, sId, tId);
						LineAcc m = stlines.ceiling(l);
						if (m == null || !m.merge(sloc, tloc, sId, tId))
							stlines.add(l);
					}
				}
			}
		}

		for (int ig = 0; ig < sgroups.size(); ++ig) {
			final TypedListGroup sgroup = sgroups.get(ig);
			Map<TypedListGroup, NavigableSet<LineAcc>> row = lines.row(sgroup);
			GLLocation gLocation = sLocator.apply(EBandMode.GROUPS, ig);
			float factor = (float) gLocation.getSize() / sgroup.size();
			int j = 0;
			for (NavigableSet<LineAcc> stlines : row.values()) {
				if (stlines == null)
					continue;
				for (LineAcc line : stlines) {
					line.sGroupOffset = (float) gLocation.getOffset() + factor * j - line.s.x();
					j += line.sIds.size();
				}
			}
		}
		for (int ig = 0; ig < tgroups.size(); ++ig) {
			final TypedListGroup tgroup = tgroups.get(ig);
			Map<TypedListGroup, NavigableSet<LineAcc>> col = lines.column(tgroup);
			GLLocation gLocation = tLocator.apply(EBandMode.GROUPS, ig);
			float factor = (float) gLocation.getSize() / tgroup.size();
			int j = 0;
			for (NavigableSet<LineAcc> stlines : col.values()) {
				if (stlines == null)
					continue;
				for (LineAcc line : stlines) {
					line.tGroupOffset = (float) gLocation.getOffset() + factor * j - line.t.x();
					j += line.tIds.size();
				}
			}
		}
		{
			IDType sType = this.sData.getIdType();
			IDType tType = this.tData.getIdType();

			for (NavigableSet<LineAcc> stlines : lines.values()) {
				if (stlines == null)
					continue;
				for (LineAcc line : stlines)
					detailRoutes.add(build(line, sType, tType));
			}
		}
		return detailRoutes;
	}

	@Override
	protected List<? extends IBandRenderAble> computeDetailRoutes() {
		List<IBandRenderAble> detailRoutes = new ArrayList<>();

		TypedList s = shared.sliceList(sData.getIdType());
		final Multimap<Integer, Integer> slookup = computeLookup(s);
		TypedList t = shared.sliceList(tData.getIdType());
		final Multimap<Integer, Integer> tlookup = computeLookup(tData);

		NavigableSet<LineAcc> lines = new TreeSet<>();

		for (int i = 0; i < sData.size(); ++i) {
			Integer sId = sData.get(i);
			if (sId.intValue() < 0) {
				continue;
			}
			Collection<Integer> indices = slookup.get(sId);
			if (indices.isEmpty()) {
				continue;
			}
			GLLocation slocation = sLocator.apply(EBandMode.DETAIL, i);
			if (!slocation.isDefined()) {
				continue;
			}
			Vec2f sloc = new Vec2f((float) slocation.getOffset(), (float) slocation.getOffset2());
			for (int sindex : indices) {
				Integer tId = t.get(sindex);
				Collection<Integer> tindices = tlookup.get(tId);
				if (tindices.isEmpty())
					continue;
				for (int tindex : tindices) {
					GLLocation tlocation = tLocator.apply(EBandMode.DETAIL, tindex);
					if (!tlocation.isDefined())
						continue;
					Vec2f tloc = new Vec2f((float) tlocation.getOffset(), (float) tlocation.getOffset2());

					final LineAcc l = new LineAcc(sloc, tloc, sId, tId);
					LineAcc m = lines.ceiling(l);
					if (m == null || !m.merge(sloc, tloc, sId, tId))
						lines.add(l);
				}
			}

		}
		{
			IDType sType = this.sData.getIdType();
			IDType tType = this.tData.getIdType();

			for (LineAcc line : lines) {
				detailRoutes.add(build(line, sType, tType));
			}
		}
		return detailRoutes;
	}

	private IBandRenderAble build(LineAcc acc, IDType sType, IDType tType) {
		float sh = acc.s.y() - acc.s.x();
		float th = acc.t.y() - acc.t.x();

		final TypedSet sData = as(acc.sIds, sType);
		final TypedSet tData = as(acc.tIds, tType);
		final Vec4f ss;
		final Vec4f tt;
		if (isHorizontal()) {
			ss = new Vec4f(this.s.x(), this.s.y() + acc.s.x(), sh, acc.sGroupOffset);
			tt = new Vec4f(this.t.x(), this.t.y() + acc.t.x(), th, acc.tGroupOffset);
		} else {
			ss = new Vec4f(this.s.x() + acc.s.x(), this.s.y(), sh, acc.sGroupOffset);
			tt = new Vec4f(this.t.x() + acc.t.x(), this.t.y(), th, acc.tGroupOffset);
		}
		String label = StringUtils.join(acc.sIds, ", ") + " x " + StringUtils.join(acc.tIds, ", ");

		if (sh <= 2 && th <= 2) {
			return new Line(label, sData, tData, ss, tt);
		} else
			return new Band(label, sData, tData, ss, tt);
	}

	private static TypedSet as(Set<Integer> ids, IDType type) {
		return new TypedSet(ImmutableSet.copyOf(ids), type);
	}

	private static class LineAcc implements Comparable<LineAcc> {
		float tGroupOffset;
		float sGroupOffset;
		final Vec2f s, t;
		final Set<Integer> sIds = new HashSet<>(2);
		final Set<Integer> tIds = new HashSet<>(2);

		public LineAcc(Vec2f s, Vec2f t, Integer sIds, Integer tIds) {
			this.s = s;
			this.t = t;
			this.sIds.add(sIds);
			this.tIds.add(tIds);
		}

		public boolean merge(Vec2f s, Vec2f t, Integer sIds, Integer tIds) {
			if (c(this.s, s) != 0 || c(this.t, t) != 0)
				return false;
			this.s.setX(Math.min(this.s.x(), s.x()));
			this.s.setY(Math.max(this.s.y(), s.y()));
			this.t.setX(Math.min(this.t.x(), t.x()));
			this.t.setY(Math.max(this.t.y(), t.y()));
			this.sIds.add(sIds);
			this.tIds.add(tIds);
			return true;
		}

		@Override
		public int compareTo(LineAcc o) {
			int r = c(s, o.s);
			if (r != 0)
				return r;
			return c(t, o.t);
		}

		private static int c(Vec2f a, Vec2f b) {
			int r;
			if ((r = c(a.x(), b.x())) != 0)
				return r;
			if ((r = c(a.y(), b.y())) != 0)
				return r;
			return 0;
		}

		private static int c(float a, float b) {
			float d = a - b;
			if (d < -0.01)
				return -1;
			if (d > 0.01)
				return 1;
			return 0;
		}
	}

	private static Multimap<Integer, Integer> computeLookup(List<Integer> s) {
		Builder<Integer, Integer> b = ImmutableMultimap.builder();
		for (int i = 0; i < s.size(); ++i) {
			b.put(s.get(i), i);
		}
		return b.build();
	}

	private abstract class ARoute implements IBandRenderAble {
		private final String label;
		protected final TypedSet sShared, tShared;

		public ARoute(String label, TypedSet sData, TypedSet tData) {
			this.label = label;
			this.sShared = sData;
			this.tShared = tData;
		}

		/**
		 * @return the label, see {@link #label}
		 */
		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public TypedSet asSet(SourceTarget type) {
			return type.select(sShared, tShared);
		}

	}

	private class Band extends ARoute {
		private final List<Vec2f> points;
		private Polygon shape;

		public Band(String label, TypedSet sData, TypedSet tData, Vec4f s, Vec4f t) {
			super(label, sData, tData);

			this.points = new ArrayList<>(8);
			this.shape = new Polygon();
			if (isHorizontal()) {
				addPoint(s.x(), s.y());
				addPoint(s.x() + SHIFT, s.y());
				addPoint(t.x() - SHIFT, t.y());
				addPoint(t.x(), t.y());
				addPoint(t.x(), t.y() + t.z());
				addPoint(t.x() - SHIFT, t.y() + t.z());
				addPoint(s.x() + SHIFT, s.y() + s.z());
				addPoint(s.x(), s.y() + s.z());
			} else {
				addPoint(s.x(), s.y());
				addPoint(s.x(), s.y() + SHIFT);
				addPoint(t.x(), t.y() - SHIFT);
				addPoint(t.x(), t.y());
				addPoint(t.x() + t.z(), t.y());
				addPoint(t.x() + t.z(), t.y() - SHIFT);
				addPoint(s.x() + s.z(), s.y() + SHIFT);
				addPoint(s.x() + s.z(), s.y());
			}
		}

		private void addPoint(float x, float y) {
			points.add(new Vec2f(x, y));
			shape.addPoint((int) x, (int) y);
		}

		/**
		 *
		 */
		public void renderMiniMap(GLGraphics g) {
			final Color color = mode.getColor();
			g.color(color.r, color.g, color.b, color.a * EBandMode.alpha(1));
			g.fillPolygon(TesselatedPolygons.polygon2(points));
		}



		@Override
		public void renderRoute(GLGraphics g, IBandHost host, int nrBands) {
			Color color = mode.getColor();
			final float alpha = EBandMode.alpha(nrBands);
			g.color(color.r, color.g, color.b, color.a * alpha);
			g.fillPolygon(TesselatedPolygons.polygon2(points));

			boolean horizontal = isHorizontal();
			Vec2f s0 = points.get(0);
			Vec2f t0 = points.get(3);
			float sh = horizontal ? points.get(7).y() - s0.y() : points.get(7).x() - s0.x();
			float th = horizontal ? points.get(4).y() - t0.y() : points.get(4).x() - t0.x();

			for (SelectionType type : SELECTION_TYPES) {
				int s = host.getSelected(sShared, type).size();
				int t = host.getSelected(tShared, type).size();
				if (s > 0 && t > 0) {
					Color c = type.getColor();
					g.color(c.r, c.g, c.b, c.a * alpha);
					List<Vec2f> p = new ArrayList<>(8);
					p.addAll(points.subList(0, 4));
					float sf = s / (float) sShared.size();
					float tf = t / (float) tShared.size();

					if (horizontal) {
						p.add(new Vec2f(t0.x(), t0.y() + th * tf));
						p.add(new Vec2f(t0.x() - SHIFT, t0.y() + th * tf));
						p.add(new Vec2f(s0.x() + SHIFT, s0.y() + sh * sf));
						p.add(new Vec2f(s0.x(), s0.y() + sh * sf));
					} else {
						p.add(new Vec2f(t0.x() + th * tf, t0.y()));
						p.add(new Vec2f(t0.x() + th * tf, t0.y() - SHIFT));
						p.add(new Vec2f(s0.x() + sh * sf, s0.y() + SHIFT));
						p.add(new Vec2f(s0.x() + sh * sf, s0.y()));
					}

					g.fillPolygon(TesselatedPolygons.polygon2(p));
				}
			}
			g.color(color.r, color.g, color.b, color.a * alpha);
			g.drawPath(points, true);
		}

		@Override
		public Rect getBoundingBox() {
			Vec2f s0 = points.get(0);
			Vec2f t0 = points.get(3);
			Vec2f s1 = points.get(7);
			Vec2f t1 = points.get(4);
			float y = Math.min(s0.y(), t0.y());
			float y2 = Math.max(s1.y(), t1.y());
			return new Rect(s0.x(), y, t0.x() - s0.x(), y2 - y);
		}

		@Override
		public boolean intersects(Rectangle2D bounds) {
			return shape.intersects(bounds);
		}
	}

	private class Line extends ARoute {
		private Vec2f[] line;

		public Line(String label, TypedSet sData, TypedSet tData, Vec4f s, Vec4f t) {
			super(label, sData, tData);

			this.line= new Vec2f[4];
			if (isHorizontal()) {
				float sy = s.y() + s.z() * 0.5f;
				float ty = t.y() + t.z() * 0.5f;
				this.line[0] = new Vec2f(s.x(), sy);
				this.line[1] = new Vec2f(s.x() + SHIFT, sy + s.w());
				this.line[2] = new Vec2f(t.x() - SHIFT, ty + t.w());
				this.line[3] = new Vec2f(t.x(), ty);
			} else {
				float sx = s.x() + s.z() * 0.5f;
				float tx = t.x() + t.z() * 0.5f;
				this.line[0] = new Vec2f(sx, s.y());
				this.line[1] = new Vec2f(sx + s.w(), s.y() + SHIFT);
				this.line[2] = new Vec2f(tx + t.w(), t.y() - SHIFT);
				this.line[3] = new Vec2f(tx, t.y());
			}
		}

		@Override
		public void renderRoute(GLGraphics g, IBandHost host, int nrBands) {
			Color c = mode.getColor();
			for (SelectionType type : SELECTION_TYPES) {
				int s = host.getSelected(sShared, type).size();
				int t = host.getSelected(tShared, type).size();
				if (s > 0 && t > 0) {
					c = type.getColor();
					break;
				}
			}
			g.color(c.r, c.g, c.b, c.a * EBandMode.alpha(nrBands));
			g.drawPath(false, line);
		}

		@Override
		public Rect getBoundingBox() {
			Vec2f s  = line[0];
			Vec2f t  = line[3];
			if (isHorizontal()) {
				float y = Math.min(s.y(), t.y());
				float y2 = Math.max(s.y(), t.y());
				return new Rect(s.x(), y, t.x() - s.x(), y2 - y);
			} else {
				float x = Math.min(s.x(), t.x());
				float x2 = Math.max(s.x(), t.x());
				return new Rect(x, s.y(), x2 - x, t.y() - s.y());
			}
		}

		@Override
		public boolean intersects(Rectangle2D bounds) {
			return bounds.intersectsLine(line[0].x(), line[0].y(), line[1].x(), line[1].y()) ||
					bounds.intersectsLine(line[1].x(), line[1].y(), line[2].x(), line[2].y()) ||
					bounds.intersectsLine(line[2].x(), line[2].y(), line[3].x(), line[3].y());
		}
	}

	private class NotMapped extends ARoute {
		private final List<Vec2f> points;
		private Polygon shape;
		private SourceTarget type;

		public NotMapped(String label, TypedSet sData, TypedSet tData, SourceTarget type, Vec4f f) {
			super(label, sData, tData);
			this.type = type;

			this.points = new ArrayList<>(6);
			this.shape = new Polygon();
			final float s = Math.min(SHIFT, f.z());
			if (isHorizontal()) {
				if (type == SourceTarget.SOURCE) {
					addPoint(f.x(), f.y());
					addPoint(f.x() + SHIFT + s, f.y());
					addPoint(f.x() + SHIFT + s, f.y() + f.z() + s);
					addPoint(f.x() + SHIFT, f.y() + f.z() + s);
					addPoint(f.x() + SHIFT, f.y() + f.z());
					addPoint(f.x(), f.y() + f.z());
				} else {
					addPoint(f.x(), f.y());
					addPoint(f.x(), f.y() + f.z());
					addPoint(f.x() - SHIFT, f.y() + f.z());
					addPoint(f.x() - SHIFT, f.y() + f.z() + s);
					addPoint(f.x() - SHIFT - s, f.y() + f.z() + s);
					addPoint(f.x() - SHIFT - s, f.y());
				}
			} else {
				if (type == SourceTarget.SOURCE) {
					addPoint(f.x(), f.y());
					addPoint(f.x(), f.y() + SHIFT + s);
					addPoint(f.x() + f.z() + s, f.y() + SHIFT + s);
					addPoint(f.x() + f.z() + s, f.y() + SHIFT);
					addPoint(f.x() + f.z(), f.y() + SHIFT);
					addPoint(f.x() + f.z(), f.y());
				} else {
					addPoint(f.x(), f.y());
					addPoint(f.x() + f.z(), f.y());
					addPoint(f.x() + f.z(), f.y() - SHIFT);
					addPoint(f.x() + f.z() + s, f.y() - SHIFT);
					addPoint(f.x() + f.z() + s, f.y() - SHIFT - s);
					addPoint(f.x(), f.y() - SHIFT - s);
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
		public void renderRoute(GLGraphics g, IBandHost host, int nrBands) {
			Color color = mode.getColor();
			final float alpha = EBandMode.alpha(nrBands);
			g.color(color.r, color.g, color.b, color.a * alpha);
			g.fillPolygon(TesselatedPolygons.polygon2(points));

			boolean horizontal = isHorizontal();
			Vec2f f = points.get(0);
			Vec2f f2 = points.get(type.select(5, 1));
			float fz = horizontal ? f2.y() - f.y() : f2.x() - f.x();
			final float s = Math.min(SHIFT, fz);

			for (SelectionType type : SELECTION_TYPES) {
				final TypedSet d = this.type.select(sShared, tShared);
				int se = host.getSelected(d, type).size();
				if (se > 0) {
					Color c = type.getColor();
					g.color(c.r, c.g, c.b, c.a * alpha);
					List<Vec2f> p = new ArrayList<>(6);
					float sf = se / (float) d.size();

					p.add(points.get(0));
					if (horizontal) {
						if (this.type == SourceTarget.SOURCE) {
							p.add(new Vec2f(f.x() + SHIFT + s*sf, f.y()));
							p.add(new Vec2f(f.x() + SHIFT + s*sf, f.y() + fz*sf + s*sf));
							p.add(new Vec2f(f.x() + SHIFT, f.y() + fz*sf + s*sf));
							p.add(new Vec2f(f.x() + SHIFT, f.y() + fz*sf));
							p.add(new Vec2f(f.x(), f.y() + fz*sf));
						} else {
							p.add(new Vec2f(f.x(), f.y() + fz*sf));
							p.add(new Vec2f(f.x() - SHIFT, f.y() + fz*sf));
							p.add(new Vec2f(f.x() - SHIFT, f.y() + fz*sf + s*sf));
							p.add(new Vec2f(f.x() - SHIFT - s*sf, f.y() + fz*sf + s*sf));
							p.add(new Vec2f(f.x() - SHIFT - s*sf, f.y()));
						}
					} else {
						if (this.type == SourceTarget.SOURCE) {
							p.add(new Vec2f(f.x(), f.y() + SHIFT + s*sf));
							p.add(new Vec2f(f.x() + fz*sf + s*sf, f.y() + SHIFT + s*sf));
							p.add(new Vec2f(f.x() + fz*sf + s*sf, f.y() + SHIFT));
							p.add(new Vec2f(f.x() + fz*sf, f.y() + SHIFT));
							p.add(new Vec2f(f.x() + fz*sf, f.y()));
						} else {
							p.add(new Vec2f(f.x() + fz*sf, f.y()));
							p.add(new Vec2f(f.x() + fz*sf, f.y() - SHIFT));
							p.add(new Vec2f(f.x() + fz*sf + s*sf, f.y() - SHIFT));
							p.add(new Vec2f(f.x() + fz*sf + s*sf, f.y() - SHIFT - s*sf));
							p.add(new Vec2f(f.x(), f.y() - SHIFT - s*sf));
						}
					}
					g.fillPolygon(TesselatedPolygons.polygon2(p));
				}
			}
			g.color(color.r, color.g, color.b, color.a * alpha);
			g.drawPath(points, true);
		}

		@Override
		public boolean intersects(Rectangle2D bounds) {
			return shape.intersects(bounds);
		}
	}
}
