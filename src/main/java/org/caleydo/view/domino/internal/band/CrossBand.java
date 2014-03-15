/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import static org.caleydo.view.domino.internal.band.IBandHost.SourceTarget.SOURCE;
import static org.caleydo.view.domino.internal.band.IBandHost.SourceTarget.TARGET;
import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.Constants;
import org.caleydo.view.domino.internal.INodeLocator;
import org.caleydo.view.domino.internal.band.IBandHost.SourceTarget;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * @author Samuel Gratzl
 *
 */
public class CrossBand extends ABand {
	private Disc overview;
	private final List<IBandRenderAble> overviewRoutes = new ArrayList<>(3);

	private Vec2f sLoc, tLoc;

	public CrossBand(String label, MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
			INodeLocator sLocator, INodeLocator tLocator, Vec2f s, Vec2f t, EDirection sDir, EDirection tDir,
			String identifier) {
		super(shared, sData, tData, sLocator, tLocator, sDir, tDir, identifier + sDir.asDim().select("CH", "CV"));

		assert sDir.asDim().isHorizontal();
		this.sLoc = s;
		this.tLoc = t;

		{
			TypedSet sShared = shared.slice(sData.getIdType());
			TypedSet tShared = shared.slice(tData.getIdType());
			float sr = ((float) sShared.size()) / sData.size();
			float tr = ((float) tShared.size()) / tData.size();
			boolean s_top = this.tDir == EDirection.SOUTH;
			boolean t_left = this.sDir == EDirection.EAST;
			final float wtotal = (float) locT(EBandMode.OVERVIEW, 0).getSize();
			final float htotal = (float) locS(EBandMode.OVERVIEW, 0).getSize();
			Rect bounds = new Rect(tLoc.x() + (t_left ? ((1 - tr) * wtotal) : 0), sLoc.y()
					+ (!s_top ? 0 : (1 - sr) * htotal), tr * wtotal, sr * htotal);
			this.overview = new Disc(label, bounds, sShared, tShared, sLoc.x(), tLoc.y());

			overviewRoutes.add(this.overview);

			String[] split = label.split(" x ");
			if (sr < 1) {
				// add a non-mapped indicator
				TypedSet sNotMapped = sData.asSet().difference(sShared);
				Pair<Vec4f, Vec4f> r = notMappedConnectors(SOURCE, sr);
				overviewRoutes.add(new NotMapped(split[0] + " x Not Mapped", sNotMapped, TypedCollections.empty(tData
						.getIdType()), SOURCE, r.getFirst(), r.getSecond(), sDir, EBandMode.OVERVIEW));
			}
			if (tr < 1) {
				TypedSet tNotMapped = tData.asSet().difference(tShared);
				Pair<Vec4f, Vec4f> r = notMappedConnectors(TARGET, tr);
				overviewRoutes.add(new NotMapped("Not Mapped x " + split[1], TypedCollections.empty(sData.getIdType()),
						tNotMapped, TARGET, r.getFirst(), r.getSecond(), tDir, EBandMode.OVERVIEW));
			}
		}
	}

	/**
	 * @param source
	 * @param ratio
	 * @return
	 */
	private Pair<Vec4f, Vec4f> notMappedConnectors(SourceTarget source, float ratio) {
		final float wtotal = (float) locT(EBandMode.OVERVIEW, 0).getSize();
		final float htotal = (float) locS(EBandMode.OVERVIEW, 0).getSize();
		if (source == SourceTarget.SOURCE) {
			return notMappedConnectors(source, (1 - ratio) * htotal, (ratio * htotal), wtotal);
		} else {
			return notMappedConnectors(source, (1 - ratio) * wtotal, (ratio * wtotal), htotal);
		}
	}

	private Pair<Vec4f, Vec4f> notMappedConnectors(SourceTarget source, float offset, float size, float otherTotal) {
		boolean s_top = this.tDir == EDirection.SOUTH;
		boolean t_left = this.sDir == EDirection.EAST;

		Vec4f sv, tv;
		if (source == SourceTarget.SOURCE) {
			final float s_y = sLoc.y() + (s_top ? 0 : size);
			if (this.sDir == EDirection.WEST) {
				sv = new Vec4f(tLoc.x(), s_y, offset, 0);
				tv = new Vec4f(tLoc.x() + otherTotal, s_y, 0, 0);
			} else {
				sv = new Vec4f(tLoc.x() + otherTotal, s_y, offset, 0);
				tv = new Vec4f(tLoc.x(), s_y, 0, 0);
			}
		} else {
			final float t_x = tLoc.x() + (t_left ? 0 : size);
			if (this.tDir == EDirection.SOUTH) {
				sv = new Vec4f(t_x, sLoc.y(), 0, 0);
				tv = new Vec4f(t_x, sLoc.y() + otherTotal, offset, 0);
			} else {
				sv = new Vec4f(t_x, sLoc.y() + otherTotal, 0, 0);
				tv = new Vec4f(t_x, sLoc.y(), offset, 0);
			}
		}
		return Pair.make(sv, tv);
	}

	@Override
	public void setLocators(INodeLocator sLocator, INodeLocator tLocator) {
		super.setLocators(sLocator, tLocator);
		{
			float sr = ((float) overview.sShared.size()) / sData.size();
			float tr = ((float) overview.tShared.size()) / tData.size();
			final float wtotal = (float) locT(EBandMode.OVERVIEW, 0).getSize();
			final float htotal = (float) locS(EBandMode.OVERVIEW, 0).getSize();
			boolean s_top = this.tDir == EDirection.SOUTH;
			boolean t_left = this.sDir == EDirection.EAST;
			Rect bounds = new Rect(tLoc.x() + (t_left ? ((1 - tr) * wtotal) : 0), sLoc.y()
					+ (!s_top ? 0 : (1 - sr) * htotal), tr * wtotal, sr * htotal);
			this.overview = new Disc(overview.getLabel(), bounds, overview.sShared, overview.tShared, sLoc.x(),
					tLoc.y());
			overviewRoutes.set(0, this.overview);
			if (sr < 1) {
				NotMapped m = (NotMapped) overviewRoutes.get(1);
				// add a non-mapped indicator
				Pair<Vec4f, Vec4f> r = notMappedConnectors(SourceTarget.SOURCE, sr);
				overviewRoutes
.add(new NotMapped(m.getLabel(), m.sShared, m.tShared, SourceTarget.SOURCE, r.getFirst(),
						r.getSecond(), sDir, EBandMode.OVERVIEW));
			}
			if (tr < 1) {
				int index = sr < 1 ? 2 : 1;
				NotMapped m = (NotMapped) overviewRoutes.get(index);
				Pair<Vec4f, Vec4f> r = notMappedConnectors(SourceTarget.TARGET, tr);
				overviewRoutes
.add(new NotMapped(m.getLabel(), m.sShared, m.tShared, SourceTarget.TARGET, r.getFirst(),
						r.getSecond(), tDir, EBandMode.OVERVIEW));
			}
		}
	}

	@Override
	public boolean stubify() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void renderMiniMap(GLGraphics g) {
		overview.renderMiniMap(g);
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
	public void render(GLGraphics g, float w, float h, IBandHost host) {
		renderAdapter(g);
		super.render(g, w, h, host);
	}

	@Override
	public boolean intersects(Rectangle2D bounds) {
		if (super.intersects(bounds))
			return true;
		// TODO check adapter
		final float hs = (float) locS(EBandMode.OVERVIEW, 0).getSize();
		final float wt = (float) locT(EBandMode.OVERVIEW, 0).getSize();

		{
			float x;
			float w;
			if (sDir == EDirection.WEST) {
				x = sLoc.x();
				w = tLoc.x() - sLoc.x();
			} else {
				x = tLoc.x() + wt;
				w = sLoc.x() - tLoc.x() - wt;
			}
			if (bounds.intersects(x, sLoc.y(), w, hs))
				return true;
		}
		{
			float y;
			float h;
			if (tDir == EDirection.NORTH) {
				y = tLoc.y();
				h = sLoc.y() - tLoc.y();
			} else {
				y = sLoc.y() + hs;
				h = tLoc.y() - sLoc.y() - hs;
			}
			if (bounds.intersects(tLoc.x(), y, wt, h))
				return true;
		}

		return false;
		// return overviewRoute().intersects(bounds);
	}

	private void renderAdapter(GLGraphics g) {
		g.color(EBandMode.OVERVIEW.getColor());
		final float hs = (float) locS(EBandMode.OVERVIEW, 0).getSize();
		final float wt = (float) locT(EBandMode.OVERVIEW, 0).getSize();

		{
			float x;
			float w;
			final int sGroups = sData.getGroups().size();
			if (sDir == EDirection.WEST) {
				x = sLoc.x();
				w = tLoc.x() - sLoc.x();
			} else {
				x = tLoc.x() + wt;
				w = sLoc.x() - tLoc.x() - wt;
			}
			if (mode == EBandMode.DETAIL || sGroups <= 1)
				g.fillRect(x, sLoc.y(), w, hs);
			else {
				for (int i = 0; i < sGroups; ++i) {
					GLLocation gi = locS(EBandMode.GROUPS, i);
					g.fillRect(x, sLoc.y() + (float) gi.getOffset(), w, (float) gi.getSize());
				}
			}
		}
		{
			float y;
			float h;
			final int tGroups = tData.getGroups().size();
			if (tDir == EDirection.NORTH) {
				y = tLoc.y();
				h = sLoc.y() - tLoc.y();
			} else {
				y = sLoc.y() + hs;
				h = tLoc.y() - sLoc.y() - hs;
			}
			if (mode == EBandMode.DETAIL || tGroups <= 1)
				g.fillRect(tLoc.x(), y, wt, h);
			else {
				for (int i = 0; i < tGroups; ++i) {
					GLLocation gi = locT(EBandMode.GROUPS, i);
					g.fillRect(tLoc.x() + (float) gi.getOffset(), y, (float) gi.getSize(), h);
				}
			}
		}
	}

	void renderConnector(GLGraphics g, Rect bounds) {
		final Color c = SelectionType.MOUSE_OVER.getColor();
		g.color(c.r, c.g, c.b, 0.5f);
		float xStart = this.sLoc.x();
		float yStart = this.tLoc.y();
		if (sDir == EDirection.WEST)
			smallRect(g, xStart, bounds.y(), bounds.x() - xStart, bounds.height());
		else {
			smallRect(g, bounds.x2(), bounds.y(), xStart - bounds.x2(), bounds.height());
		}
		if (tDir == EDirection.NORTH)
			smallRect(g, bounds.x(), yStart, bounds.width(), bounds.y() - yStart);
		else {
			smallRect(g, bounds.x(), bounds.y2(), bounds.width(), yStart - bounds.y2());
		}

	}

	private static void smallRect(GLGraphics g, float x, float y, float w, float h) {
		if (w <= 2)
			g.lineWidth(2).drawLine(x, y, x, y + h).lineWidth(1);
		else if (h <= 2)
			g.lineWidth(2).drawLine(x, y, x + w, y).lineWidth(1);
		else
			g.fillRect(x, y, w, h);
	}

	@Override
	protected List<? extends IBandRenderAble> computeGroupRoutes() {
		List<IBandRenderAble> groupRoutes = new ArrayList<>();
		List<TypedSet> sSets = new ArrayList<>();
		List<TypedSet> tSets = new ArrayList<>();
		// convert all to the subset of the shared set
		final List<TypedListGroup> sgroups = sData.getGroups();
		for (TypedListGroup sGroup : sgroups)
			sSets.add(overview.sShared.intersect(sGroup.asSet()));
		final List<TypedListGroup> tgroups = tData.getGroups();
		for (TypedListGroup tGroup : tgroups)
			tSets.add(overview.tShared.intersect(tGroup.asSet()));

		// starting points for right side groups
		int[] tinneracc = new int[tgroups.size()];
		Arrays.fill(tinneracc, 0);

		final TypedSet tEmpty = TypedCollections.empty(tData.getIdType());
		final TypedSet sEmpty = TypedCollections.empty(sData.getIdType());
		final float wtotal = (float) locT(EBandMode.OVERVIEW, 0).getSize();
		final float htotal = (float) locS(EBandMode.OVERVIEW, 0).getSize();

		// for each left groups check all right groups
		for (int i = 0; i < sgroups.size(); ++i) {
			TypedListGroup sgroup = sgroups.get(i);
			TypedSet sset = sSets.get(i);
			if (sset.isEmpty())
				continue;
			GLLocation sgroupLocation = locS(EBandMode.GROUPS, i);
			int sinneracc = 0;
			final double sFactor = sgroupLocation.getSize() / sgroup.size();
			double y = sLoc.y() + sgroupLocation.getOffset();

			for (int j = 0; j < tgroups.size(); ++j) {
				TypedListGroup tgroup = tgroups.get(j);
				TypedSet tset = tSets.get(j);
				if (tset.isEmpty())
					continue;

				MultiTypedSet shared = TypedSets.intersect(sset, tset);
				if (shared.isEmpty()) // nothing shared
					continue;

				GLLocation tgroupLocation = locT(EBandMode.GROUPS, j);

				double x = tLoc.x() + tgroupLocation.getOffset();

				TypedSet sShared = shared.slice(sData.getIdType());
				TypedSet tShared = shared.slice(tData.getIdType());
				String label = sgroup.getLabel() + " x " + tgroup.getLabel();

				final double tFactor = tgroupLocation.getSize() / tgroup.size();
				double h = sShared.size() * sFactor;
				double w = tShared.size() * tFactor;

				Rect bounds = new Rect((float) (x + tinneracc[j] * tFactor), (float) (y + sinneracc * sFactor),
						(float) w, (float) h);
				groupRoutes.add(new MosaicRect(label, bounds, sShared, tShared, EBandMode.GROUPS));

				sinneracc += sShared.size();
				tinneracc[j] += tShared.size();
			}

			final int notMapped = sgroup.size() - sinneracc;
			if (notMapped > 0) {
				TypedSet notMappedIds = sgroup.asSet().difference(overview.sShared);
				float s1 = (float) (y + sinneracc * sFactor);
				Pair<Vec4f, Vec4f> r = notMappedConnectors(SourceTarget.SOURCE, s1, (float) (sgroupLocation.getSize())
						- s1, wtotal);
				groupRoutes.add(new NotMapped(sgroup.getLabel() + " x Not Mapped", notMappedIds, tEmpty,
						SourceTarget.SOURCE, r.getFirst(), r.getSecond(), sDir, EBandMode.GROUPS));
			}
		}

		for (int i = 0; i < tgroups.size(); ++i) {
			TypedListGroup tgroup = tgroups.get(i);
			final int notMapped = tgroup.size() - tinneracc[i];
			if (notMapped <= 0)
				continue;
			GLLocation tgroupLocation = locT(EBandMode.GROUPS, i);
			final double tFactor = tgroupLocation.getSize() / tgroup.size();
			TypedSet notMappedIds = tgroup.asSet().difference(overview.tShared);
			double x = tLoc.x() + tgroupLocation.getOffset();
			float s1 = (float) (x + tinneracc[i] * tFactor);
			Pair<Vec4f, Vec4f> r = notMappedConnectors(SourceTarget.TARGET, s1,
					(float) (tgroupLocation.getSize()) - s1, htotal);
			groupRoutes.add(new NotMapped("Not Mapped x " + tgroup.getLabel(), sEmpty, notMappedIds,
					SourceTarget.SOURCE, r.getFirst(), r.getSecond(), tDir, EBandMode.GROUPS));
		}

		return groupRoutes;
	}

	@Override
	protected List<? extends IBandRenderAble> computeDetailRoutes() {
		List<IBandRenderAble> detailRoutes = new ArrayList<>();
		TypedList s = shared.sliceList(sData.getIdType());
		final Multimap<Integer, Integer> slookup = computeLookup(s);
		TypedList t = shared.sliceList(tData.getIdType());
		final Multimap<Integer, Integer> tlookup = computeLookup(tData);

		Set<PointB> points = new HashSet<>();

		for (int i = 0; i < sData.size(); ++i) {
			Integer sId = sData.get(i);
			if (sId.intValue() < 0) {
				continue;
			}
			Collection<Integer> indices = slookup.get(sId);
			if (indices.isEmpty()) {
				continue;
			}
			GLLocation slocation = locS(EBandMode.DETAIL, i);
			if (!slocation.isDefined()) {
				continue;
			}
			for (int sindex : indices) {
				Integer tId = t.get(sindex);
				Collection<Integer> tindices = tlookup.get(tId);
				if (tindices.isEmpty())
					continue;
				for (int tindex : tindices) {
					GLLocation tlocation = locT(EBandMode.DETAIL, tindex);
					if (!tlocation.isDefined())
						continue;
					boolean merged = false;
					for (PointB point : points) {
						if (point.merge(slocation, tlocation, sId, tId)) {
							merged = true;
							break;
						}
					}
					if (!merged) {
						points.add(new PointB(slocation, tlocation, sId, tId));
					}
				}
			}

		}
		flushPoints(detailRoutes, points, EBandMode.DETAIL);
		return detailRoutes;
	}

	@Override
	protected List<? extends IBandRenderAble> computeGroupDetailRoutes() {
		return detailRoutes();
	}

	private void flushPoints(List<IBandRenderAble> detailRoutes, Set<PointB> lines, EBandMode mode) {
		if (lines.isEmpty())
			return;
		final IDType s = this.overview.sShared.getIdType();
		final IDType t = this.overview.tShared.getIdType();
		for (PointB line : lines)
			detailRoutes.add(line.create(s, t, mode));
		lines.clear();
	}

	class PointB {
		protected GLLocation sloc;
		protected GLLocation tloc;
		private final Set<Integer> sIds = new HashSet<>(2);
		private final Set<Integer> tIds = new HashSet<>(2);

		public PointB(GLLocation s, GLLocation t, Integer sId, Integer tId) {
			this.sloc = s;
			this.tloc = t;
			this.sIds.add(sId);
			this.tIds.add(tId);
		}

		public boolean merge(GLLocation sloc, GLLocation tloc, Integer sId, Integer tId) {
			if (!combineAble(this.sloc, sloc) || !combineAble(this.tloc, tloc))
				return false;

			this.sloc = mergeImpl(this.sloc, sloc);
			this.tloc = mergeImpl(this.tloc, tloc);
			this.sIds.add(sId);
			this.tIds.add(tId);
			return true;
		}

		private boolean combineAble(GLLocation a, GLLocation b) {
			// two lines can be combined if
			double aStart = a.getOffset();
			double aEnd = a.getOffset2();
			double bStart = b.getOffset();
			double bEnd = b.getOffset2();
			if (c(aStart, bStart) && c(aEnd, bEnd)) // same
				return true;
			return false;
		}

		/**
		 * @param sloc2
		 * @param sloc3
		 * @return
		 */
		private GLLocation mergeImpl(GLLocation a, GLLocation b) {
			double start = Math.min(a.getOffset(), b.getOffset());
			double end = Math.max(a.getOffset2(), b.getOffset2());
			return new GLLocation(start, end - start);
		}

		private boolean c(double a, double b) {
			return Math.abs(a - b) < 0.01;
		}

		public MosaicRect create(IDType s, IDType t, EBandMode mode) {
			String label = StringUtils.join(sIds, ", ") + " x " + StringUtils.join(tIds, ", ");
			double x = tLoc.x() + tloc.getOffset();
			double y = sLoc.y() + sloc.getOffset();
			double w = tloc.getSize();
			double h = sloc.getSize();
			Rect bounds = new Rect((float) x, (float) y, (float) w, (float) h);
			return new MosaicRect(label, bounds, new TypedSet(sIds, s), new TypedSet(tIds, t), mode);
		}
	}

	private static Multimap<Integer, Integer> computeLookup(List<Integer> s) {
		Builder<Integer, Integer> b = ImmutableMultimap.builder();
		for (int i = 0; i < s.size(); ++i) {
			b.put(s.get(i), i);
		}
		return b.build();
	}

	private class Disc extends MosaicRect {
		private final float xStart, yStart;

		public Disc(String label, Rect bounds, TypedSet sIds, TypedSet tIds, float xStart, float yStart) {
			super(label, bounds, sIds, tIds, EBandMode.OVERVIEW);
			this.xStart = xStart;
			this.yStart = yStart;
		}

		/**
		 * @param g
		 */
		public void renderMiniMap(GLGraphics g) {
			final Color color = EBandMode.OVERVIEW.getColor();
			g.color(color.r, color.g, color.b, color.a);
			renderBox(g, true, 1.f, 1.f);
		}

		private void renderBox(GLGraphics g, boolean fill, float wFactor, float hFactor) {
			final Rect b = bounds;
			if (fill) {
				if (sDir == EDirection.WEST) {
					if (tDir == EDirection.NORTH) {
						g.fillRect(xStart, b.y(), b.x() - xStart, b.height() * hFactor);
						g.fillRect(b.x(), yStart, b.width() * wFactor, b.y() - yStart + b.height()
								* hFactor);
					} else {
						g.fillRect(xStart, b.y() + b.height() * (1 - hFactor), b.x() - xStart, b.height() * hFactor);
						g.fillRect(b.x(), b.y() + b.height() * (1 - hFactor), b.width() * wFactor,
								yStart - b.y2() + b.height() * hFactor);
					}
				} else {
					if (tDir == EDirection.NORTH) {
						g.fillRect(b.x2(), b.y(), xStart - b.x2(), b.height() * hFactor);
						g.fillRect(b.x() + (b.width() * (1 - wFactor)), yStart, b.width() * wFactor,
								b.y() - yStart + b.height() * hFactor);
					} else {
						g.fillRect(b.x2(), b.y() + b.height() * (1 - hFactor), xStart - b.x2(), b.height() * hFactor);
						g.fillRect(b.x() + (b.width() * (1 - wFactor)), b.y() + b.height() * (1 - hFactor), b.width()
								* wFactor, yStart - b.y2() + b.height()
								* hFactor);
					}
				}
			} else {
				if (sDir == EDirection.WEST) {
					if (tDir == EDirection.NORTH) {
						g.drawPath(true, v(xStart, b.y()), b.xy(), v(b.x(), yStart), v(b.x2(), yStart), b.x2y2(),
								v(xStart, b.y2()));
					} else {
						g.drawPath(true, v(xStart, b.y()), b.x2y(), v(b.x2(), yStart), v(b.x(), yStart), b.xy2(),
								v(xStart, b.y2()));
					}
				} else {
					if (tDir == EDirection.NORTH) {
						g.drawPath(true, v(b.x(), yStart), v(b.x2(), yStart), b.x2y(), v(xStart, b.y()),
								v(xStart, b.y2()), b.xy2());
					} else {
						g.drawPath(true, b.xy(), v(xStart, b.y()), v(xStart, b.y2()), b.x2y2(), v(b.x2(), yStart),
								v(b.x(), yStart));
					}
				}
			}
		}

		/**
		 * @param bound
		 * @return
		 */
		@Override
		public boolean intersects(Rectangle2D bound) {
			if (super.intersects(bound))
				return true;
			final Rect b = bounds;
			// FIXME wrong
			// if (sDim == EDirection.WEST) {
			// if (new Rect(xStart, b.y(), b.x() - xStart, b.height()).asRectangle2D().intersects(bound))
			// return true;
			// } else {
			// if (new Rect(b.x2(), b.y(), xStart - b.x2(), b.height()).asRectangle2D().intersects(bound))
			// return true;
			// }
			//
			// if (tDim == EDirection.NORTH) {
			// if (new Rect(b.x(), yStart, b.width(), b.y() - yStart).asRectangle2D().intersects(bound))
			// return true;
			// } else {
			// if (new Rect(b.x(), b.y2(), b.width(), yStart - b.y2()).asRectangle2D().intersects(bound))
			// return true;
			// }
			return false;
		}

		@Override
		public void renderRoute(GLGraphics g, IBandHost host, int nrItems, boolean withSelection) {
			super.renderRoute(g, host, nrItems, withSelection);
			// final Color color = EBandMode.OVERVIEW.getColor();
			// g.color(color);
			// renderBox(g, true, 1.f, 1.f);
			//
			//
			// if (g.isPickingPass())
			// return;
			// for (SelectionType type : SELECTION_TYPES) {
			// int sS = host.getSelected(sShared, type).size();
			// int tS = host.getSelected(tShared, type).size();
			// if (sS > 0 && tS > 0) {
			// final Color c = type.getColor();
			// g.color(c.r, c.g, c.b, 0.5f);
			// renderBox(g, true, (tS / (float) tShared.size()), (sS / (float) sShared.size()));
			// }
			// }
			//
			// g.color(color.darker());
			// renderBox(g, false, 1.f, 1.f);
		}
	}

	static Vec2f v(float x, float y) {
		return new Vec2f(x, y);
	}

	private class MosaicRect extends ARelation {
		protected final Rect bounds;

		public MosaicRect(String label, Rect bounds, TypedSet sIds, TypedSet tIds, EBandMode mode) {
			super(label, sIds, tIds, mode);
			this.bounds = bounds;
		}

		@Override
		public void renderRoute(GLGraphics g, IBandHost host, int nrItems, boolean withSelection) {
			if (mode.compareTo(EBandMode.GROUPED_DETAIL) >= 0
					|| (bounds.width() <= Constants.SCATTER_POINT_SIZE && bounds.height() <= Constants.SCATTER_POINT_SIZE)) {
				renderPoint(g, host);
				return;
			}
			Color color = mode.getColor();
			g.color(color);
			g.fillRect(bounds);

			if (g.isPickingPass())
				return;
			for (SelectionType type : SELECTION_TYPES) {
				int sS = host.getSelected(sShared, type).size();
				int tS = host.getSelected(tShared, type).size();
				if (sS > 0 && tS > 0) {
					final Color c = type.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
					Rect b2 = new Rect(bounds.x(), bounds.y(), bounds.width() * (tS / (float) tShared.size()),
							bounds.height() * (sS / (float) sShared.size()));
					g.fillRect(b2);

					if (type == SelectionType.MOUSE_OVER)
						renderConnector(g, b2);
				}
			}
			if (mode.ordinal() <= EBandMode.GROUPS.ordinal()) {
				g.color(color.darker());
				g.drawRect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
			}
		}

		/**
		 * @param g
		 * @param host
		 */
		private void renderPoint(GLGraphics g, IBandHost host) {
			Color c = mode.getColor();

			boolean hovered = false;

			if (!g.isPickingPass()) {
				for (SelectionType type : Lists.reverse(SELECTION_TYPES)) {
					int sS = host.getSelected(sShared, type).size();
					int tS = host.getSelected(tShared, type).size();
					if (sS > 0 && tS > 0) {
						c = type.getColor();
						if (type == SelectionType.MOUSE_OVER)
							hovered = true;
						break;
					}
				}
			}
			g.color(c.r, c.g, c.b, 1.f);
			g.drawPoint(bounds.x() + bounds.width() * 0.5f, bounds.y() + bounds.height() * 0.5f);
			if (hovered)
				renderConnector(g, bounds);
		}

		@Override
		public boolean intersects(Rectangle2D bounds) {
			return this.bounds.asRectangle2D().intersects(bounds);
		}

		@Override
		public Rect getBoundingBox() {
			return bounds;
		}
	}
}
