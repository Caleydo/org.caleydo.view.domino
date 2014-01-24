/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.internal.INodeLocator;
import org.caleydo.view.domino.internal.band.BandLine.IBandArea;
import org.caleydo.view.domino.internal.band.IBandHost.SourceTarget;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

/**
 * @author Samuel Gratzl
 *
 */
public class Band extends ABand {

	private BandLine band;

	private final DataRoute overviewRoute;
	private List<DataRoute> groupRoutes;
	private List<ADataRoute> detailRoutes;

	public Band(BandLine band, String label, MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
			INodeLocator sLocator, INodeLocator tLocator, EDimension sDim,
 EDimension tDim, String identifier) {
		super(shared, sData, tData, sLocator, tLocator, sDim, tDim, identifier);
		this.band = band;

		{
			TypedSet sShared = shared.slice(sData.getIdType());
			TypedSet tShared = shared.slice(tData.getIdType());
			float sr = ((float) sShared.size()) / sData.size();
			float tr = ((float) tShared.size()) / tData.size();
			this.overviewRoute = new DataRoute(label, 0, sr, 0, tr, sShared, tShared);
		}
	}

	/**
	 *
	 */
	@Override
	public boolean stubify() {
		updateBand(band.asStubified(), sLocator, tLocator);
		return true;
	}

	public void updateBand(BandLine band, INodeLocator sLocator, INodeLocator tLocator) {
		this.band = band;
		super.updateBand(sLocator, tLocator);
		overviewRoute.updateBand();
		if (groupRoutes != null)
			for (DataRoute r : groupRoutes)
				r.updateBand();
		if (detailRoutes != null)
			for (ADataRoute r : detailRoutes)
				r.updateBand();
	}

	private abstract class ADataRoute implements IBandRenderAble {
		private final String rlabel;
		protected final float s1, s2, t1, t2;
		protected IBandArea base;

		public ADataRoute(String label, float s1, float s2, float t1, float t2) {
			this.rlabel = label;
			this.s1 = s1;
			this.t1 = t1;
			this.s2 = s2;
			this.t2 = t2;
			this.base = band.computeArea(s1, s2, t1, t2);
		}

		public void updateBand() {
			this.base = band.computeArea(s1, s2, t1, t2);
		}

		/**
		 * @return the label, see {@link #rlabel}
		 */
		@Override
		public String getLabel() {
			return rlabel;
		}


		@Override
		public void renderRoute(GLGraphics g, IBandHost host) {
			g.color(color.r, color.g, color.b, 0.5f);
			g.fillPolygon(base);
			if (g.isPickingPass())
				return;

			renderSelection(g, host);

			g.color(color.darker());
			g.drawPath(base);
		}

		void renderMiniMap(GLGraphics g) {
			g.color(color.r, color.g, color.b, 0.5f);
			g.fillPolygon(base);
		}

		protected abstract void renderSelection(GLGraphics g, IBandHost host);

		@Override
		public boolean intersects(Rectangle2D bounds) {
			return base.intersects(bounds);
		}
	}

	private final class DataRoute extends ADataRoute {
		final TypedSet sShared, tShared;

		public DataRoute(String label, float s1, float s2, float t1, float t2, TypedSet sShared, TypedSet tShared) {
			super(label, s1, s2, t1, t2);
			this.sShared = sShared;
			this.tShared = tShared;
		}

		@Override
		public TypedSet asSet(SourceTarget type) {
			return type.select(sShared, tShared);
		}

		@Override
		protected void renderSelection(GLGraphics g, IBandHost host) {
			for (SelectionType type : SELECTION_TYPES) {
				int sS = host.getSelected(sShared, type).size();
				int tS = host.getSelected(tShared, type).size();
				if (sS > 0 && tS > 0) {
					final Color c = type.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
					if (sS == sShared.size() && tS == tShared.size())
						g.fillPolygon(base);
					else {
						final float s2_s = s1 + (s2 - s1) * sS / sShared.size();
						final float t2_s = t1 + (t2 - t1) * tS / tShared.size();
						g.fillPolygon(band.computeArea(s1, s2_s, t1, t2_s));
					}
				}
			}
		}
	}

	private final class DataListRoute extends ADataRoute {
		final TypedList sshared;
		final TypedList tshared;

		public DataListRoute(String label, float s1, float s2, float t1, float t2, TypedList shared, TypedList tshared) {
			super(label, s1, s2, t1, t2);
			this.sshared = shared;
			this.tshared = tshared;
		}

		@Override
		public TypedSet asSet(SourceTarget type) {
			return type.select(sshared, tshared).asSet();
		}

		@Override
		protected void renderSelection(GLGraphics g, IBandHost host) {
			for (SelectionType type : SELECTION_TYPES) {
				BitSet sS = host.isSelected(sshared, type);
				if (!sS.isEmpty()) {
					final Color c = type.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
					if (sS.cardinality() == sshared.size())
						g.fillPolygon(base);
					else {
						final float sd = (s2 - s1) / sshared.size();
						final float td = (t2 - t1) / sshared.size();
						int last = -1;
						int size = 0;
						for (int i = sS.nextSetBit(0); i != -1; i = sS.nextSetBit(i + 1)) {
							if ((last + 1) != i && size > 0) {
								int start = last - size + 1;
								int end = last + 1;
								g.fillPolygon(band.computeArea(s1 + start * sd, s1 + end * sd, t1 + start * td, t1
										+ end * td));
								size = 0;
							}
							last = i;
							size++;
						}
						{
							int start = last - size + 1;
							int end = last + 1;
							g.fillPolygon(band.computeArea(s1 + start * sd, s1 + end * sd, t1 + start * td, t1 + end
									* td));
						}
					}
				}
			}
		}
	}

	private final class DataSingleRoute extends ADataRoute {
		final TypedID sShared, tShared;

		public DataSingleRoute(String label, float s1, float s2, float t1, float t2, TypedID sShared, TypedID tShared) {
			super(label, s1, s2, t1, t2);
			this.sShared = sShared;
			this.tShared = tShared;
		}

		@Override
		public TypedSet asSet(SourceTarget type) {
			TypedID id = type.select(sShared, tShared);
			return TypedCollections.singleton(id);
		}

		@Override
		protected void renderSelection(GLGraphics g, IBandHost host) {
			for (SelectionType type : SELECTION_TYPES) {
				boolean sS = host.isSelected(sShared, type);
				boolean tS = host.isSelected(tShared, type);
				if (sS && tS) {
					final Color c = type.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
					g.fillPolygon(base);
				}
			}
		}
	}

	/**
	 * @param g
	 */
	@Override
	public void renderMiniMap(GLGraphics g) {
		overviewRoute.renderMiniMap(g);
	}

	@Override
	protected DataRoute overviewRoute() {
		return overviewRoute;
	}

	@Override
	protected List<DataRoute> groupRoutes() {
		if (groupRoutes != null)
			return groupRoutes;
		groupRoutes = new ArrayList<>();
		List<TypedSet> sSets = new ArrayList<>();
		List<TypedSet> tSets = new ArrayList<>();

		// convert all to the subset of the shared set
		final List<TypedListGroup> sgroups = sData.getGroups();
		for (TypedListGroup sGroup : sgroups)
			sSets.add(overviewRoute.sShared.intersect(sGroup.asSet()));
		final List<TypedListGroup> tgroups = tData.getGroups();
		for (TypedListGroup tGroup : tgroups)
			tSets.add(overviewRoute.tShared.intersect(tGroup.asSet()));

		// starting points for right side groups
		int[] tinneracc = new int[tgroups.size()];
		Arrays.fill(tinneracc, 0);

		final double sOverallFactor = 1.f / sLocator.apply(EBandMode.OVERVIEW, 0).getSize();
		final double tOverallFactor = 1.f / tLocator.apply(EBandMode.OVERVIEW, 0).getSize();

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
				double s1 = (sgroupLocation.getOffset() + sinneracc * sFactor) * sOverallFactor;
				double s2 = s1 + sShared.size() * sFactor * sOverallFactor;
				double t1 = (tgroupLocation.getOffset() + tinneracc[j] * tFactor) * tOverallFactor;
				double t2 = t1 + tShared.size() * tFactor * tOverallFactor;
				String label = sgroup.getLabel() + " x " + tgroup.getLabel();
				groupRoutes.add(new DataRoute(label, (float) s1, (float) s2, (float) t1, (float) t2, sShared, tShared));
				sinneracc += sShared.size();
				tinneracc[j] += tShared.size();
			}
		}
		return groupRoutes;
	}

	/**
	 * @return
	 */
	@Override
	protected List<ADataRoute> detailRoutes() {
		if (detailRoutes != null)
			return detailRoutes;
		detailRoutes = new ArrayList<>();
		// FIXME using locators and data create routes from 1:1

		TypedList s = shared.sliceList(sData.getIdType());
		final Multimap<Integer, Integer> slookup = computeLookup(s);
		TypedList t = shared.sliceList(tData.getIdType());
		final Multimap<Integer, Integer> tlookup = computeLookup(tData);

		Set<Line> lines = new HashSet<>();

		for (int i = 0; i < sData.size(); ++i) {
			Integer sId = sData.get(i);
			if (sId.intValue() < 0) {
				flushLines(lines);
				continue;
			}
			Collection<Integer> indices = slookup.get(sId);
			if (indices.isEmpty()) {
				flushLines(lines);
				continue;
			}
			GLLocation slocation = sLocator.apply(EBandMode.DETAIL, i);
			if (!slocation.isDefined()) {
				flushLines(lines);
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
					for (Line line : lines) {
						if (line.merge(slocation, tlocation, sId, tId)) {
							merged = true;
							break;
						}
					}
					if (!merged) {
						lines.add(new Line(slocation, tlocation, sId, tId));
					}
				}
			}

		}
		flushLines(lines);
		return detailRoutes;
	}

	/**
	 * @param lines
	 */
	private void flushLines(Set<? extends ALine> lines) {
		if (lines.isEmpty())
			return;
		final IDType s = this.overviewRoute.sShared.getIdType();
		final IDType t = this.overviewRoute.tShared.getIdType();
		float sMax = (float) sLocator.apply(EBandMode.OVERVIEW, 0).getSize();
		float tMax = (float) tLocator.apply(EBandMode.OVERVIEW, 0).getSize();
		for (ALine line : lines)
			detailRoutes.add(line.create(sMax, tMax, s, t));
		lines.clear();
	}

	private abstract class ALine {
		protected GLLocation sloc;
		protected GLLocation tloc;

		public ALine(GLLocation sloc, GLLocation tloc) {
			this.sloc = sloc;
			this.tloc = tloc;
		}

		public boolean merge(GLLocation sloc, GLLocation tloc) {
			if (!combineAble(this.sloc, sloc) || !combineAble(this.tloc, tloc))
				return false;

			this.sloc = mergeImpl(this.sloc, sloc);
			this.tloc = mergeImpl(this.tloc, tloc);
			return true;
		}

		/**
		 * @param sloc2
		 * @param sloc3
		 * @return
		 */
		private boolean combineAble(GLLocation a, GLLocation b) {
			// two lines can be combined if
			double aStart = a.getOffset();
			double aEnd = a.getOffset2();
			double bStart = b.getOffset();
			double bEnd = b.getOffset2();
			if (c(aEnd, bStart) || c(aStart, bEnd)) // concat each other
				return true;
			if (c(aStart, bStart) && c(aEnd, bEnd)) // same
				return true;
			if (bStart >= aStart && bStart <= aEnd) // b starts in a
				return true;
			if (aStart >= bStart && aStart <= bEnd) // a starts in b
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

		public abstract ADataRoute create(float sMax, float tMax, IDType s, IDType t);
	}

	private class Line extends ALine {
		private final List<Integer> sIds = new ArrayList<>(2);
		private final List<Integer> tIds = new ArrayList<>(2);

		public Line(GLLocation sloc, GLLocation tloc, Integer sId, Integer tId) {
			super(sloc, tloc);
			this.sIds.add(sId);
			this.tIds.add(tId);
		}

		public boolean merge(GLLocation sloc, GLLocation tloc, Integer sId, Integer tId) {
			if (!super.merge(sloc, tloc))
				return false;
			this.sIds.add(sId);
			this.tIds.add(tId);
			return true;
		}

		@Override
		public ADataRoute create(float sMax, float tMax, IDType s, IDType t) {
			String label = StringUtils.join(sIds, ", ") + " x " + StringUtils.join(tIds, ", ");
			float s1 = (float) sloc.getOffset() / sMax;
			float s2 = (float) sloc.getOffset2() / sMax;
			float t1 = (float) tloc.getOffset() / tMax;
			float t2 = (float) tloc.getOffset2() / tMax;
			if (sIds.size() == 1)
				return new DataSingleRoute(label, s1, s2, t1, t2, new TypedID(sIds.get(0), s), new TypedID(tIds.get(0),
						t));
			return new DataListRoute(label, s1, s2, t1, t2, new TypedList(sIds, s), new TypedList(tIds, t));
		}

	}
	/**
	 * @param s
	 * @return
	 */
	private static Multimap<Integer, Integer> computeLookup(List<Integer> s) {
		Builder<Integer, Integer> b = ImmutableMultimap.builder();
		for (int i = 0; i < s.size(); ++i) {
			b.put(s.get(i), i);
		}
		return b.build();
	}

	/**
	 * @param bound
	 * @return
	 */
	@Override
	public boolean intersects(Rectangle2D bound) {
		return band.intersects(bound);
	}

}
