/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.ITesselatedPolygon;
import org.caleydo.core.view.opengl.util.spline.TesselationRenderer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class StubifiedArea implements ITesselatedPolygon {
	private final List<Vec2f> top;
	private final List<Vec2f> bottom;
	private final int split;
	private final float[] bottomPercentages;
	private final float[] topPercentages;
	private final float stubAlpha;

	public StubifiedArea(List<Vec2f> top, List<Vec2f> bottom, int split, float[] topPercentages,
			float[] bottomPercentages, float stubAlpha) {
		this.top = top;
		this.bottom = bottom;
		this.split = split;
		this.topPercentages = topPercentages;
		this.bottomPercentages = bottomPercentages;
		this.stubAlpha = stubAlpha;
	}

	@Override
	public void draw(GLGraphics g) {
		if (stubAlpha >= 1) {
			int n = top.size();
			Iterable<Vec2f> left = Iterables.concat(top.subList(0, split), Lists.reverse(bottom.subList(0, split)));
			Iterable<Vec2f> right = Iterables.concat(top.subList(n - split, n),
					Lists.reverse(bottom.subList(n - split, n)));
			g.drawPath(left, true);
			g.drawPath(right, true);
		}
		// Color c = g.getColor();
		// topPercentages[split]
		//
		// g.drawPath(points, true);
		// a.draw(g);
		// b.draw(g);
	}

	@Override
	public void fill(GLGraphics g, TesselationRenderer renderer) {
		if (stubAlpha >= 1) {
			int n = top.size();
			Iterable<Vec2f> left = Iterables.concat(top.subList(0, split), Lists.reverse(bottom.subList(0, split)));
			Iterable<Vec2f> right = Iterables.concat(top.subList(n - split, n),
					Lists.reverse(bottom.subList(n - split, n)));
			renderer.render2(g, left);
			renderer.render2(g, right);
		}
	}

	@Override
	public int size() {
		return top.size() + bottom.size();
	}
}