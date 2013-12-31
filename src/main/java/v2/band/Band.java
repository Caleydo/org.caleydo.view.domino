/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

import java.util.Arrays;
import java.util.List;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.spi.model.IBandRenderer;

/**
 * @author Samuel Gratzl
 *
 */
public class Band implements IBandRenderer {
	public static enum EBandMode {
		OVERVIEW, GROUPS, DETAIL
	}

	private static final Color color = new Color(180, 212, 231, 32);

	private final BandLine band;
	private final MultiTypedSet shared;

	private final TypedGroupList sData;
	private final TypedGroupList tData;

	private EBandMode mode = EBandMode.OVERVIEW;

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
			this.overviewRoute = new DataRoute(sr, tr, sShared, tShared);
		}
	}

	@Override
	public TypedSet getIds(SourceTarget type) {
		return type.select(overviewRoute.sShared, overviewRoute.tShared);
	}

	@Override
	public IDType getIdType(SourceTarget type) {
		return getIds(type).getIdType();
	}

	private final class DataRoute {
		private final float sr, tr;
		private final PolyArea base;
		final TypedSet sShared, tShared;

		public DataRoute(float sr, float tr, TypedSet sShared, TypedSet tShared) {
			this.sr = sr;
			this.tr = tr;
			this.sShared = sShared;
			this.tShared = tShared;
			this.base = band.computeArea(0, sr, 0, tr);
		}

		void renderRoute(GLGraphics g, IBandHost host) {
			g.color(color.r, color.g, color.b, 0.5f);
			g.fillPolygon(base);

			for (SelectionType type : selectionTypes()) {
				int sS = host.getSelected(sShared, type).size();
				int tS = host.getSelected(tShared, type).size();
				if (sS > 0 && tS > 0) {
					final Color c = type.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
					g.fillPolygon(band.computeArea(0, sr * sS / sShared.size(), 0, tr * tS / tShared.size()));
				}
			}
			g.color(color.darker());
			g.drawPath(base);
		}

		private List<SelectionType> selectionTypes() {
			return Arrays.asList(SelectionType.MOUSE_OVER, SelectionType.SELECTION);
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

	@Override
	public String getLabel() {
		return "";
	}

	@Override
	public void renderPick(GLGraphics g, float w, float h, IBandHost host) {
		// TODO Auto-generated method stub

	}

	private Iterable<DataRoute> lazyGroupRoutes() {
		if (groupRoutes != null)
			return groupRoutes;
		// FIXME
		// groupRoutes = new ArrayList<>();
		// for(TypedListGroup sGroup : sData.getGroups()) {
		// TypedSet s = overviewRoute.sShared.intersect(sGroup.asSet());
		// if (s.isEmpty())
		// continue;
		// for(Typed)
		// }
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

}
