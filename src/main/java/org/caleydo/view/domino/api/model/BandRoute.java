/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.InterpolatingFunctions;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
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

	private final TypedSet ids;

	private final float radius1;
	private final float radius2;

	public BandRoute(Route route, Color color, TypedSet ids, float radius1, float radius2) {
		this.route = route;
		this.color = color;
		this.ids = ids;
		this.radius1 = radius1;
		this.radius2 = radius2;
	}

	@Override
	public void render(GLGraphics g, float w, float h, IBandHost host) {
		renderRoute(g, 1.0f, color);
		float factor = 1.f / ids.size();
		for (SelectionType type : Arrays.asList(SelectionType.MOUSE_OVER, SelectionType.SELECTION)) {
			int s = host.getSelected(ids, type);
			if (s > 0)
				renderRoute(g, s * factor, type.getColor());
		}

		g.color(color.darker());
		route.setRadiusInterpolator(InterpolatingFunctions.linear(radius1, radius2));
		g.drawPath(route);
	}

	private void renderRoute(GLGraphics g, float f, Color c) {
		g.color(c.r, c.g, c.b, 0.5f);
		route.setRadiusInterpolator(InterpolatingFunctions.linear(radius1 * f, radius2 * f));
		g.fillPolygon(route);
	}

	@Override
	public String getLabel() {
		return ids.getIdType().getTypeName() + ": " + StringUtils.join(ids, ",");
	}

	/**
	 * @return the ids, see {@link #ids}
	 */
	@Override
	public TypedSet getIds() {
		return ids;
	}

	@Override
	public IDType getIdType() {
		return ids.getIdType();
	}

	@Override
	public void renderPick(GLGraphics g, float w, float h, IBandHost host) {
		renderRoute(g, 1, color);
	}
}
