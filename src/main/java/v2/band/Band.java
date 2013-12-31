/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSets;
import org.caleydo.view.domino.spi.model.IBandRenderer.IBandHost;
import org.caleydo.view.domino.spi.model.IBandRenderer.SourceTarget;

/**
 * @author Samuel Gratzl
 *
 */
public class Band {
	public static enum EBandMode {
		OVERVIEW, GROUPS, DETAIL
	}

	private static final Color color = new Color(180, 212, 231, 32);

	private final BandLine band;
	private final MultiTypedSet shared;

	private final TypedGroupList sData;
	private final TypedGroupList tData;

	private EBandMode mode = EBandMode.GROUPS;

	private final DataRoute overviewRoute;
	private List<DataRoute> groupRoutes;
	private List<DataRoute> detailRoutes;

	public Band(BandLine band, MultiTypedSet shared, TypedGroupList sData, TypedGroupList tData) {
		this.band = band;
		this.shared = shared;
		this.sData = sData;
		this.tData = tData;
		{
			TypedSet sShared = shared.slice(sData.getIdType());
			TypedSet tShared = shared.slice(tData.getIdType());
			float sr = ((float) sShared.size()) / sData.size();
			float tr = ((float) tShared.size()) / tData.size();
			this.overviewRoute = new DataRoute(0, sr, 0, tr, sShared, tShared);
		}
	}

	public TypedSet getIds(SourceTarget type, int subIndex) {
		DataRoute r = getRoute(subIndex);
		return type.select(r.sShared, r.tShared);
	}

	private DataRoute getRoute(int subIndex) {
		switch (mode) {
		case OVERVIEW:
			return overviewRoute;
		case GROUPS:
			return lazyGroupRoutes().get(subIndex);
		case DETAIL:
			return lazyDetailRoutes().get(subIndex);
		}
		throw new IllegalStateException();
	}

	public IDType getIdType(SourceTarget type, int subIndex) {
		return getIds(type, subIndex).getIdType();
	}

	private final class DataRoute {
		private final float s1, s2, t1, t2;
		private final PolyArea base;
		final TypedSet sShared, tShared;

		public DataRoute(float s1, float s2, float t1, float t2, TypedSet sShared, TypedSet tShared) {
			this.s1 = s1;
			this.t1 = t1;
			this.s2 = s2;
			this.t2 = t2;
			this.sShared = sShared;
			this.tShared = tShared;
			this.base = band.computeArea(s1, s2, t1, t2);
		}

		void renderRoute(GLGraphics g, IBandHost host) {
			g.color(color.r, color.g, color.b, 0.5f);
			g.fillPolygon(base);
			if (g.isPickingPass())
				return;
			for (SelectionType type : selectionTypes()) {
				int sS = host.getSelected(sShared, type).size();
				int tS = host.getSelected(tShared, type).size();
				if (sS > 0 && tS > 0) {
					final Color c = type.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
					g.fillPolygon(band.computeArea(s1, (s2 - s1) * sS / sShared.size(), t1,
							(t2 - t1) * tS / tShared.size()));
				}
			}
			g.color(color.darker());
			g.drawPath(base);
		}

		private List<SelectionType> selectionTypes() {
			return Arrays.asList(SelectionType.MOUSE_OVER, SelectionType.SELECTION);
		}
	}

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

	public String getLabel() {
		return "";
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
			for (DataRoute r : lazyDetailRoutes()) {
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

				groupRoutes.add(new DataRoute(s1, s2, t1, t2, sShared, tShared));
				sinneracc += sShared.size();
				tinneracc[j] += tShared.size();
			}
		}
		return groupRoutes;
	}

	/**
	 * @return
	 */
	private List<DataRoute> lazyDetailRoutes() {
		if (detailRoutes != null)
			return detailRoutes;
		// FIXME
		return detailRoutes;
	}

}
