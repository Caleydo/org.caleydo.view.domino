/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.ITesselatedPolygon;
import org.caleydo.core.view.opengl.util.spline.TesselationRenderer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class PolyArea implements ITesselatedPolygon {
	private final List<Vec2f> top;
	private final List<Vec2f> bottom;
	private final Iterable<Vec2f> points;

	public PolyArea(List<Vec2f> top, List<Vec2f> bottom) {
		this.top = top;
		this.bottom = bottom;
		this.points = Iterables.concat(top, Lists.reverse(bottom));
	}
	@Override
	public void draw(GLGraphics g) {
		g.drawPath(points, true);
	}
	@Override
	public void fill(GLGraphics g, TesselationRenderer renderer) {
		renderer.render2(g, points);
	}

	@Override
	public int size() {
		return top.size() + bottom.size();
	}
}