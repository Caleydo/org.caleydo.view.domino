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
import com.google.common.collect.Multimap;
/**
 * @author Samuel Gratzl
 *
 */
public class CrossBand extends ABand {
	private final Disc overview;
	private List<MosaicRect> groupRoutes;
	private List<MosaicRect> detailRoutes;

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
			this.overview = new Disc(label, bounds, sShared, tShared);
		}
	}

	@Override
	public void setLocators(INodeLocator sLocator, INodeLocator tLocator) {
		this.sLocator = sLocator;
		this.tLocator = tLocator;
		if (this.detailRoutes != null)
			detailRoutes = null;
		if (this.groupRoutes != null)
			groupRoutes = null;

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
	protected List<? extends IBandRenderAble> groupRoutes() {
		if (groupRoutes != null)
			return groupRoutes;
		groupRoutes = new ArrayList<>();
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
				groupRoutes.add(new MosaicRect(label, bounds, sShared, tShared, true));
			}
		}
		return groupRoutes;
	}

	@Override
	protected List<? extends IBandRenderAble> detailRoutes() {
		if (detailRoutes != null)
			return detailRoutes;
		detailRoutes = new ArrayList<>();
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
		flushPoints(points);
		return detailRoutes;
	}

	private void flushPoints(Set<PointB> lines) {
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
			return new MosaicRect(label, bounds, new TypedSet(sIds, s), new TypedSet(tIds, t), false);
		}
	}

	private static Multimap<Integer, Integer> computeLookup(List<Integer> s) {
		Builder<Integer, Integer> b = ImmutableMultimap.builder();
		for (int i = 0; i < s.size(); ++i) {
			b.put(s.get(i), i);
		}
		return b.build();
	}

	private static class Disc implements IBandRenderAble {
		private final String label;
		private final Rect bounds;
		private final TypedSet sIds;
		private final TypedSet tIds;

		public Disc(String label, Rect bounds, TypedSet sIds, TypedSet tIds) {
			this.label = label;
			this.bounds = bounds;
			this.sIds = sIds;
			this.tIds = tIds;
		}

		/**
		 * @param g
		 */
		public void renderMiniMap(GLGraphics g) {
			g.color(color.r, color.g, color.b, color.a);
			g.fillRect(bounds);
		}

		/**
		 * @param bound
		 * @return
		 */
		@Override
		public boolean intersects(Rectangle2D bound) {
			return bounds.asRectangle2D().intersects(bound);
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public void renderRoute(GLGraphics g, IBandHost host, int nrItems) {
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
				}
			}

			g.color(color.darker());
			g.drawRect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
		}

		@Override
		public TypedSet asSet(SourceTarget type) {
			return type.select(sIds, tIds);
		}
	}

	private static class MosaicRect implements IBandRenderAble {
		private final String label;
		private final Rect bounds;
		private final TypedSet sIds;
		private final TypedSet tIds;
		private final boolean outlines;

		public MosaicRect(String label, Rect bounds, TypedSet sIds, TypedSet tIds, boolean outlines) {
			this.label = label;
			this.bounds = bounds;
			this.sIds = sIds;
			this.tIds = tIds;
			this.outlines = outlines;
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
				}
			}
			if (outlines) {
				g.color(color.darker());
				g.drawRect(bounds.x(), bounds.y(), bounds.width(), bounds.height());
			}
		}

		/**
		 * @param g
		 * @param host
		 */
		private void renderPoint(GLGraphics g, IBandHost host) {
			Color c = color;

			if (!g.isPickingPass()) {
				for (SelectionType type : SELECTION_TYPES) {
					int sS = host.getSelected(sIds, type).size();
					int tS = host.getSelected(tIds, type).size();
					if (sS > 0 && tS > 0) {
						c = type.getColor();
						break;
					}
				}
			}
			g.color(c.r, c.g, c.b, 1.f);
			g.pointSize(2);
			g.drawPoint(bounds.x() + bounds.width() * 0.5f, bounds.y() + bounds.height() * 0.5f);
			g.pointSize(1);
		}

		@Override
		public TypedSet asSet(SourceTarget type) {
			return type.select(sIds, tIds);
		}

		@Override
		public boolean intersects(Rectangle2D bounds) {
			return this.bounds.asRectangle2D().intersects(bounds);
		}
	}
}
