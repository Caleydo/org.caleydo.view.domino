/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.TesselationRenderer;
import org.caleydo.view.domino.internal.band.BandLine.IBandArea;

public class PolyAreaLine implements IBandArea {
	private final List<Vec2f> line;

	private final Shape shape;

	public PolyAreaLine(List<Vec2f> line) {
		this.line = line;
		this.shape = createShape(line);
	}

	private static Shape createShape(List<Vec2f> line) {
		Path2D p = new Path2D.Float();
		Vec2f a = line.get(0);
		p.moveTo(a.x(), a.y());
		for (Vec2f o : line.subList(1, line.size()))
			p.lineTo(o.x(), o.y());
		return p;
	}

	@Override
	public void draw(GLGraphics g) {
		g.drawPath(line, false);
	}
	@Override
	public void fill(GLGraphics g, TesselationRenderer renderer) {
		draw(g);
	}

	@Override
	public int size() {
		return line.size();
	}

	@Override
	public boolean intersects(Rectangle2D bounds) {
		return shape.intersects(bounds);
	}
}