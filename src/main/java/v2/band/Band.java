/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

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
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation.ILocator;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.util.spline.ITesselatedPolygon;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.spi.model.IBandRenderer.IBandHost;
import org.caleydo.view.domino.spi.model.IBandRenderer.SourceTarget;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

/**
 * @author Samuel Gratzl
 *
 */
public class Band implements ILabeled {
	public static enum EBandMode {
		OVERVIEW, GROUPS, DETAIL
	}

	private static final Color color = new Color(180, 212, 231, 32);

	private BandLine band;
	private final MultiTypedSet shared;

	private final TypedGroupList sData;
	private final TypedGroupList tData;

	private EBandMode mode = EBandMode.GROUPS;

	private final DataRoute overviewRoute;
	private List<DataRoute> groupRoutes;
	private List<ADataRoute> detailRoutes;

	private ILocator sLocator, tLocator;

	private final EDimension sDim;
	private final EDimension tDim;

	public Band(BandLine band, String label, MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData,
			ILocator sLocator, ILocator tLocator, EDimension sDim, EDimension tDim) {
		this.band = band;
		this.shared = shared;
		this.sData = sData;
		this.tData = tData;
		this.sLocator = sLocator;
		this.tLocator = tLocator;
		this.sDim = sDim;
		this.tDim = tDim;

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
	public void stubify() {
		updateBand(band.asStubified(), sLocator, tLocator);
	}

	public void updateBand(BandLine band, ILocator sLocator, ILocator tLocator) {
		this.band = band;
		this.sLocator = sLocator;
		this.tLocator = tLocator;
		overviewRoute.updateBand();
		if (groupRoutes != null)
			for (DataRoute r : groupRoutes)
				r.updateBand();
		if (detailRoutes != null)
			for (ADataRoute r : detailRoutes)
				r.updateBand();
	}

	public TypedSet getIds(SourceTarget type, int subIndex) {
		ADataRoute r = getRoute(subIndex);
		return r == null ? overviewRoute.asSet(type) : r.asSet(type);
	}

	public EDimension getDimension(SourceTarget type) {
		return type.select(sDim, tDim);
	}

	public String getLabel(int subIndex) {
		ADataRoute r = getRoute(subIndex);
		return r == null ? "" : r.getLabel();
	}

	private ADataRoute getRoute(int subIndex) {
		switch (mode) {
		case OVERVIEW:
			return overviewRoute;
		case GROUPS:
			final List<DataRoute> g = lazyGroupRoutes();
			if (subIndex < 0 || g.size() <= subIndex)
				return null;
			return g.get(subIndex);
		case DETAIL:
			final List<ADataRoute> l = lazyDetailRoutes();
			if (subIndex < 0 || l.size() <= subIndex)
				return null;
			return l.get(subIndex);
		}
		throw new IllegalStateException();
	}

	public IDType getIdType(SourceTarget type, int subIndex) {
		return getIds(type, subIndex).getIdType();
	}

	private abstract class ADataRoute implements ILabeled {
		private final String rlabel;
		protected final float s1, s2, t1, t2;
		protected ITesselatedPolygon base;

		public ADataRoute(String label, float s1, float s2, float t1, float t2) {
			this.rlabel = label;
			this.s1 = s1;
			this.t1 = t1;
			this.s2 = s2;
			this.t2 = t2;
			this.base = band.computeArea(s1, s2, t1, t2);
		}

		/**
		 * @param type
		 * @return
		 */
		public abstract TypedSet asSet(SourceTarget type);

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

		protected List<SelectionType> selectionTypes() {
			return Arrays.asList(SelectionType.MOUSE_OVER, SelectionType.SELECTION);
		}

		void renderRoute(GLGraphics g, IBandHost host) {
			g.color(color.r, color.g, color.b, 0.5f);
			g.fillPolygon(base);
			if (g.isPickingPass())
				return;

			renderSelection(g, host);

			g.color(color.darker());
			g.drawPath(base);
		}

		protected abstract void renderSelection(GLGraphics g, IBandHost host);

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
			for (SelectionType type : selectionTypes()) {
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
			for (SelectionType type : selectionTypes()) {
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
			for (SelectionType type : selectionTypes()) {
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

	public void render(GLGraphics g, float w, float h, IBandHost host) {
		switch (mode) {
		case OVERVIEW:
			overviewRoute.renderRoute(g, host);
			break;
		case GROUPS:
			float z = g.z();
			final List<DataRoute> gR = lazyGroupRoutes();
			if (gR.isEmpty()) {
				mode = EBandMode.OVERVIEW;
				render(g, w, h, host);
				return;
			}
			for (DataRoute r : gR) {
				g.incZ(0.0001f);
				r.renderRoute(g, host);
			}
			g.incZ(z - g.z());
			break;
		case DETAIL:
			z = g.z();
			final List<ADataRoute> lR = lazyDetailRoutes();
			if (lR.isEmpty()) {
				mode = EBandMode.GROUPS;
				render(g, w, h, host);
				return;
			}
			for (ADataRoute r : lR) {
				g.incZ(0.0001f);
				r.renderRoute(g, host);
			}
			g.incZ(z - g.z());
			break;
		}
	}

	@Override
	public String getLabel() {
		return overviewRoute.getLabel();
	}

	public int renderPick(GLGraphics g, float w, float h, IBandHost host, PickingPool pickingPool,int start) {
		switch (mode) {
		case OVERVIEW:
			g.pushName(pickingPool.get(start++));
			overviewRoute.renderRoute(g, host);
			g.popName();
			break;
		case GROUPS:
			for (DataRoute r : lazyGroupRoutes()) {
				g.pushName(pickingPool.get(start++));
				r.renderRoute(g, host);
				g.popName();
			}
			break;
		case DETAIL:
			for (ADataRoute r : lazyDetailRoutes()) {
				g.pushName(pickingPool.get(start++));
				r.renderRoute(g, host);
				g.popName();
			}
			break;
		}
		return start;
	}

	private List<DataRoute> lazyGroupRoutes() {
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

		int sacc = 0;
		float stotal = sData.size();
		float ttotal = tData.size();

		// starting points for right side groups
		float[] tinneracc = new float[tgroups.size()];
		{
			float tacc = 0;
			for (int j = 0; j < tgroups.size(); ++j) {
				TypedListGroup tgroup = tgroups.get(j);
				tacc += tgroup.size();
				tinneracc[j] = tacc;
			}
		}
		// for each left groups check all right groups
		for (int i = 0; i < sgroups.size(); ++i) {
			TypedListGroup sgroup = sgroups.get(i);
			sacc += sgroup.size();
			TypedSet sset = sSets.get(i);
			if (sset.isEmpty())
				continue;
			float sinneracc = sacc;
			for (int j = 0; j < tgroups.size(); ++j) {
				TypedListGroup tgroup = tgroups.get(j);
				TypedSet tset = tSets.get(j);
				if (tset.isEmpty())
					continue;

				MultiTypedSet shared = TypedSets.intersect(sset, tset);
				if (shared.isEmpty()) // nothing shared
					continue;

				TypedSet sShared = shared.slice(sData.getIdType());
				TypedSet tShared = shared.slice(tData.getIdType());
				float s1 = (sinneracc - sgroup.size()) / stotal;
				float s2 = s1 + (sShared.size() / stotal);
				float t1 = (tinneracc[j] - tgroup.size()) / ttotal;
				float t2 = t1 + (sShared.size() / ttotal);
				String label = sgroup.getLabel() + " x " + tgroup.getLabel();
				groupRoutes.add(new DataRoute(label, s1, s2, t1, t2, sShared, tShared));
				sinneracc += sShared.size();
				tinneracc[j] += tShared.size();
			}
		}
		return groupRoutes;
	}

	/**
	 * @return
	 */
	private List<ADataRoute> lazyDetailRoutes() {
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
			GLLocation slocation = sLocator.apply(i);
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
					GLLocation tlocation = tLocator.apply(tindex);
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
	private void flushLines(Set<Line> lines) {
		if (lines.isEmpty())
			return;
		final IDType s = this.overviewRoute.sShared.getIdType();
		final IDType t = this.overviewRoute.tShared.getIdType();
		float sMax = band.getDistance(true);
		float tMax = band.getDistance(false);
		for (Line line : lines)
			detailRoutes.add(line.create(sMax, tMax, s, t));
		lines.clear();
	}

	private class Line {
		private GLLocation sloc;
		private GLLocation tloc;
		private final List<Integer> sIds = new ArrayList<>(2);
		private final List<Integer> tIds = new ArrayList<>(2);

		public Line(GLLocation sloc, GLLocation tloc, Integer sId, Integer tId) {
			this.sloc = sloc;
			this.tloc = tloc;
			this.sIds.add(sId);
			this.tIds.add(tId);
		}

		public boolean merge(GLLocation sloc, GLLocation tloc, Integer sId, Integer tId) {
			if (!combineAble(this.sloc, sloc) || !combineAble(this.tloc, tloc))
				return false;

			this.sIds.add(sId);
			this.tIds.add(tId);

			this.sloc = merge(this.sloc, sloc);
			this.tloc = new GLLocation(this.tloc.getOffset(), tloc.getOffset2() - this.tloc.getOffset());
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
			if (bStart > aStart && bStart <= aEnd) // b starts in a
				return true;
			if (aStart > bStart && aStart <= bEnd) // a starts in b
				return true;
			return false;
		}

		/**
		 * @param sloc2
		 * @param sloc3
		 * @return
		 */
		private GLLocation merge(GLLocation a, GLLocation b) {
			double start = Math.min(a.getOffset(), b.getOffset());
			double end = Math.max(a.getOffset2(), b.getOffset2());
			return new GLLocation(start, end - start);
		}

		private boolean c(double a, double b) {
			return Math.abs(a - b) < 0.001;
		}

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
	private Multimap<Integer, Integer> computeLookup(List<Integer> s) {
		Builder<Integer, Integer> b = ImmutableMultimap.builder();
		for (int i = 0; i < s.size(); ++i) {
			b.put(s.get(i), i);
		}
		return b.build();
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

	/**
	 * @param bound
	 * @return
	 */
	public boolean intersects(Rectangle2D bound) {
		return band.intersects(bound);
	}

}
