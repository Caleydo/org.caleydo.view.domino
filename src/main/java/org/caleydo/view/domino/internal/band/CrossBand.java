/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
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
	private final Disc overview;

	private Vec2f sLoc, tLoc;

	public CrossBand(String label, MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
			INodeLocator sLocator, INodeLocator tLocator, Vec2f s, Vec2f t, EDirection sDir, EDirection tDir,
			String identifier) {
		super(shared, sData, tData, sLocator, tLocator, sDir, tDir, identifier);

		this.sLoc = s;
		this.tLoc = t;

		{
			TypedSet sShared = shared.slice(sData.getIdType());
			TypedSet tShared = shared.slice(tData.getIdType());
			float sr = ((float) sShared.size()) / sData.size();
			float tr = ((float) tShared.size()) / tData.size();
			float w = tr * (float) tLocator.apply(EBandMode.OVERVIEW, 0).getSize();
			float h = sr * (float) sLocator.apply(EBandMode.OVERVIEW, 0).getSize();
			Rect bounds = new Rect(tLoc.x(), sLoc.y(), w, h);
			this.overview = new Disc(label, bounds, sShared, tShared, sLoc.x(), tLoc.y());
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
	protected void renderRoutes(GLGraphics g, IBandHost host, Collection<? extends IBandRenderAble> routes) {
		renderAdapter(g);
		super.renderRoutes(g, host, routes);
	}

	private void renderAdapter(GLGraphics g) {
		g.color(EBandMode.OVERVIEW.getColor());
		final float hs = (float) sLocator.apply(EBandMode.OVERVIEW, 0).getSize();
		final float wt = (float) tLocator.apply(EBandMode.OVERVIEW, 0).getSize();

		{
			float x;
			float w;
			final int sGroups = sData.getGroups().size();
			if (sDim == EDirection.WEST) {
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
					GLLocation gi = sLocator.apply(EBandMode.GROUPS, i);
					g.fillRect(x, sLoc.y() + (float) gi.getOffset(), w, (float) gi.getSize());
				}
			}
		}
		{
			float y;
			float h;
			final int tGroups = tData.getGroups().size();
			if (tDim == EDirection.NORTH) {
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
					GLLocation gi = tLocator.apply(EBandMode.GROUPS, i);
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
		if (sDim == EDirection.WEST)
			smallRect(g, xStart, bounds.y(), bounds.x() - xStart, bounds.height());
		else {
			smallRect(g, bounds.x2(), bounds.y(), xStart - bounds.x2(), bounds.height());
		}
		if (tDim == EDirection.NORTH)
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
			sSets.add(overview.sIds.intersect(sGroup.asSet()));
		final List<TypedListGroup> tgroups = tData.getGroups();
		for (TypedListGroup tGroup : tgroups)
			tSets.add(overview.tIds.intersect(tGroup.asSet()));

		// for each left groups check all right groups
		for (int i = 0; i < sgroups.size(); ++i) {
			TypedListGroup sgroup = sgroups.get(i);
			TypedSet sset = sSets.get(i);
			if (sset.isEmpty())
				continue;
			GLLocation sgroupLocation = sLocator.apply(EBandMode.GROUPS, i);
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

				GLLocation tgroupLocation = tLocator.apply(EBandMode.GROUPS, j);

				double x = tLoc.x() + tgroupLocation.getOffset();

				TypedSet sShared = shared.slice(sData.getIdType());
				TypedSet tShared = shared.slice(tData.getIdType());
				String label = sgroup.getLabel() + " x " + tgroup.getLabel();

				final double tFactor = tgroupLocation.getSize() / tgroup.size();
				double h = sShared.size() * sFactor;
				double w = tShared.size() * tFactor;

				Rect bounds = new Rect((float) x, (float) y, (float) w, (float) h);
				groupRoutes.add(new MosaicRect(label, bounds, sShared, tShared));
			}
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
			GLLocation slocation = sLocator.apply(EBandMode.DETAIL, i);
			if (!slocation.isDefined()) {
				continue;
			}
			for (int sindex : indices) {
				Integer tId = t.get(sindex);
				Collection<Integer> tindices = tlookup.get(tId);
				if (tindices.isEmpty())
					continue;
				for (int tindex : tindices) {
					GLLocation tlocation = tLocator.apply(EBandMode.DETAIL, tindex);
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
		flushPoints(detailRoutes, points);
		return detailRoutes;
	}

	@Override
	protected List<? extends IBandRenderAble> computeGroupDetailRoutes() {
		return detailRoutes();
	}

	private void flushPoints(List<IBandRenderAble> detailRoutes, Set<PointB> lines) {
		if (lines.isEmpty())
			return;
		final IDType s = this.overview.sIds.getIdType();
		final IDType t = this.overview.tIds.getIdType();
		for (PointB line : lines)
			detailRoutes.add(line.create(s, t));
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

		public MosaicRect create(IDType s, IDType t) {
			String label = StringUtils.join(sIds, ", ") + " x " + StringUtils.join(tIds, ", ");
			double x = tLoc.x() + tloc.getOffset();
			double y = sLoc.y() + sloc.getOffset();
			double w = tloc.getSize();
			double h = sloc.getSize();
			Rect bounds = new Rect((float) x, (float) y, (float) w, (float) h);
			return new MosaicRect(label, bounds, new TypedSet(sIds, s), new TypedSet(tIds, t));
		}
	}

	private static Multimap<Integer, Integer> computeLookup(List<Integer> s) {
		Builder<Integer, Integer> b = ImmutableMultimap.builder();
		for (int i = 0; i < s.size(); ++i) {
			b.put(s.get(i), i);
		}
		return b.build();
	}

	private class Disc implements IBandRenderAble {
		private final String label;
		private final Rect bounds;
		private final TypedSet sIds;
		private final TypedSet tIds;
		private final float xStart, yStart;

		public Disc(String label, Rect bounds, TypedSet sIds, TypedSet tIds, float xStart, float yStart) {
			this.label = label;
			this.bounds = bounds;
			this.sIds = sIds;
			this.tIds = tIds;
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
				if (sDim == EDirection.WEST) {
					if (tDim == EDirection.NORTH) {
						g.fillRect(xStart, b.y(), b.x() - xStart, b.height() * hFactor);
						g.fillRect(b.x(), yStart, b.width() * wFactor, b.y() - yStart + b.height()
								* hFactor);
					} else {
						g.fillRect(xStart, b.y() + b.height() * (1 - hFactor), b.x() - xStart, b.height() * hFactor);
						g.fillRect(b.x(), b.y() + b.height() * (1 - hFactor), b.width() * wFactor,
								yStart - b.y2() + b.height() * hFactor);
					}
				} else {
					if (tDim == EDirection.NORTH) {
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
				if (sDim == EDirection.WEST) {
					if (tDim == EDirection.NORTH) {
						g.drawPath(true, v(xStart, b.y()), b.xy(), v(b.x(), yStart), v(b.x2(), yStart), b.x2y2(),
								v(xStart, b.y2()));
					} else {
						g.drawPath(true, v(xStart, b.y()), b.x2y(), v(b.x2(), yStart), v(b.x(), yStart), b.xy2(),
								v(xStart, b.y2()));
					}
				} else {
					if (tDim == EDirection.NORTH) {
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
			final Rect b = bounds;
			if (b.asRectangle2D().intersects(bound))
				return true;
			if (sDim == EDirection.WEST) {
				if (new Rect(xStart, b.y(), b.x() - xStart, b.height()).asRectangle2D().intersects(bound))
					return true;
			} else {
				if (new Rect(b.x2(), b.y(), xStart - b.x2(), b.height()).asRectangle2D().intersects(bound))
					return true;
			}

			if (tDim == EDirection.NORTH) {
				if (new Rect(b.x(), yStart, b.width(), b.y() - yStart).asRectangle2D().intersects(bound))
					return true;
			} else {
				if (new Rect(b.x(), b.y2(), b.width(), yStart - b.y2()).asRectangle2D().intersects(bound))
					return true;
			}
			return false;
		}

		@Override
		public Rect getBoundingBox() {
			return bounds;
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public void renderRoute(GLGraphics g, IBandHost host, int nrItems) {
			final Color color = EBandMode.OVERVIEW.getColor();
			g.color(color);
			renderBox(g, true, 1.f, 1.f);


			if (g.isPickingPass())
				return;
			for (SelectionType type : SELECTION_TYPES) {
				int sS = host.getSelected(sIds, type).size();
				int tS = host.getSelected(tIds, type).size();
				if (sS > 0 && tS > 0) {
					final Color c = type.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
					renderBox(g, true, (tS / (float) tIds.size()), (sS / (float) sIds.size()));
				}
			}

			g.color(color.darker());
			renderBox(g, false, 1.f, 1.f);
		}

		@Override
		public TypedSet asSet(SourceTarget type) {
			return type.select(sIds, tIds);
		}
	}

	static Vec2f v(float x, float y) {
		return new Vec2f(x, y);
	}

	private class MosaicRect implements IBandRenderAble {
		private final String label;
		private final Rect bounds;
		private final TypedSet sIds;
		private final TypedSet tIds;

		public MosaicRect(String label, Rect bounds, TypedSet sIds, TypedSet tIds) {
			this.label = label;
			this.bounds = bounds;
			this.sIds = sIds;
			this.tIds = tIds;
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public void renderRoute(GLGraphics g, IBandHost host, int nrItems) {
			if (bounds.width() <= 2 && bounds.height() <= 2) {
				renderPoint(g, host);
				return;
			}
			Color color = mode.getColor();
			g.color(color);
			g.fillRect(bounds);

			if (g.isPickingPass())
				return;
			for (SelectionType type : SELECTION_TYPES) {
				int sS = host.getSelected(sIds, type).size();
				int tS = host.getSelected(tIds, type).size();
				if (sS > 0 && tS > 0) {
					final Color c = type.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
					g.fillRect(bounds.x(), bounds.y(), bounds.width() * (tS / (float) tIds.size()), bounds.height()
							* (sS / (float) sIds.size()));

					if (type == SelectionType.MOUSE_OVER)
						renderConnector(g, bounds);
				}
			}
			if (mode == EBandMode.GROUPS) {
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
					int sS = host.getSelected(sIds, type).size();
					int tS = host.getSelected(tIds, type).size();
					if (sS > 0 && tS > 0) {
						c = type.getColor();
						if (type == SelectionType.MOUSE_OVER)
							hovered = true;
						break;
					}
				}
			}
			g.color(c.r, c.g, c.b, 1.f);
			g.pointSize(2);
			g.drawPoint(bounds.x() + bounds.width() * 0.5f, bounds.y() + bounds.height() * 0.5f);
			g.pointSize(1);

			if (hovered)
				renderConnector(g, bounds);
		}

		@Override
		public TypedSet asSet(SourceTarget type) {
			return type.select(sIds, tIds);
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
