/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.ITesselatedPolygon;
import org.caleydo.core.view.opengl.util.spline.TesselationRenderer;

public class PolyAreas implements ITesselatedPolygon {
	private final PolyArea a, b;

	public PolyAreas(PolyArea a, PolyArea b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public void draw(GLGraphics g) {
		a.draw(g);
		b.draw(g);
	}
	@Override
	public void fill(GLGraphics g, TesselationRenderer renderer) {
		a.fill(g, renderer);
		b.fill(g, renderer);
	}

	@Override
	public int size() {
		return a.size() + b.size();
	}
}