/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import java.util.Arrays;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.InterpolatingFunctions;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.ui.band.Route;
import org.caleydo.view.domino.spi.model.IBandRenderer;

/**
 * this class represents a small fragment of a band representation including a path color and a set of represented ids
 *
 * @author Samuel Gratzl
 *
 */
public class BandRoute implements IBandRenderer {
	private final Route route;
	private final Color color;

	private final MultiTypedSet shared;

	private final TypedSet sData, sShared;
	private final float sRadius;

	private final TypedSet tData, tShared;
	private final float tRadius;

	public BandRoute(Route route, Color color, MultiTypedSet shared, TypedSet sData, TypedSet tData,
			float sRadius, float tRadius) {
		this.route = route;
		this.color = color;
		this.shared = shared;
		this.sData = sData;
		this.tData = tData;
		this.sRadius = sRadius;
		this.tRadius = tRadius;
		this.sShared = shared.slice(sData.getIdType());
		this.tShared = shared.slice(tData.getIdType());
	}

	@Override
	public void render(GLGraphics g, float w, float h, IBandHost host) {
		float sR = ratio(SourceTarget.SOURCE);
		float tR = ratio(SourceTarget.TARGET);
		renderRoute(g, sR, tR, color);

		for (SelectionType type : Arrays.asList(SelectionType.MOUSE_OVER, SelectionType.SELECTION)) {
			int sS = host.getSelected(sShared, type).size();
			int tS = host.getSelected(tShared, type).size();
			if (sS > 0 && tS > 0)
				renderRoute(g, sR * sS / sShared.size(), tR * tS / tShared.size(), type.getColor());
		}

		g.color(color.darker());
		route.setRadiusInterpolator(InterpolatingFunctions.linear(sRadius * sR, tRadius * tR));
		g.drawPath(route);
	}

	private void renderRoute(GLGraphics g, float sf, float tf, Color c) {
		g.color(c.r, c.g, c.b, 0.5f);
		route.setRadiusInterpolator(InterpolatingFunctions.linear(sRadius * sf, tRadius * tf));
		g.fillPolygon(route);
	}

	@Override
	public String getLabel() {
		return ""; // shared.getIdType().getTypeName() + ": " + StringUtils.join(shared, ",");
	}

	@Override
	public TypedSet getIds(SourceTarget type) {
		return type.select(sShared, tShared);
	}

	public TypedSet getAll(SourceTarget type) {
		return type.select(sData, tData);
	}

	@Override
	public IDType getIdType(SourceTarget type) {
		return getIds(type).getIdType();
	}

	@Override
	public void renderPick(GLGraphics g, float w, float h, IBandHost host) {
		renderRoute(g, ratio(SourceTarget.SOURCE), ratio(SourceTarget.TARGET), color);
	}

	/**
	 * @param source
	 * @return
	 */
	private float ratio(SourceTarget type) {
		if (type == SourceTarget.SOURCE)
			return ((float) sShared.size()) / sData.size();
		else
			return ((float) tShared.size()) / tData.size();
	}
}
