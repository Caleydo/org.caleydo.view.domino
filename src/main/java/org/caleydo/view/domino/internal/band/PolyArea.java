/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.TesselationRenderer;
import org.caleydo.view.domino.internal.band.BandLine.IBandArea;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class PolyArea implements IBandArea {
	private final List<Vec2f> top;
	private final List<Vec2f> bottom;
	private final Iterable<Vec2f> points;

	private final Shape shape;

	public PolyArea(List<Vec2f> top, List<Vec2f> bottom) {
		this.top = top;
		this.bottom = bottom;
		this.points = Iterables.concat(top, Lists.reverse(bottom));
		this.shape = createShape(points);
	}

	static Shape createShape(Iterable<Vec2f> points) {
		Polygon p = new Polygon();
		for (Vec2f point : points) {
			p.addPoint((int) point.x(), (int) point.y());
		}
		return p;
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

	@Override
	public boolean intersects(Rectangle2D bounds) {
		return shape.intersects(bounds);
	}
}