/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.layout;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.domino.api.model.BandRoute;
import org.caleydo.view.domino.api.ui.band.Route;
import org.caleydo.view.domino.api.ui.layout.AGraphLayout;
import org.caleydo.view.domino.api.ui.layout.IGraphEdge;
import org.caleydo.view.domino.api.ui.layout.IGraphVertex;

/**
 * @author Samuel Gratzl
 *
 */
public class DefaultGraphLayout extends AGraphLayout {

	@Override
	public GraphLayoutModel doLayout(Set<? extends IGraphVertex> vertices, Set<? extends IGraphEdge> edges) {
		float acc = 10;
		float x_shift = 0;
		float y_shift = 0;

		for (IGraphVertex vertex : vertices) {
			Vec2f loc = vertex.getLocation();
			loc.setX(Float.isNaN(loc.x()) ? acc : loc.x());
			loc.setY(Float.isNaN(loc.y()) ? acc : loc.y());
			Vec2f msize = vertex.getSize();
			vertex.setBounds(loc, msize);
			// new loc
			loc = vertex.getLocation();
			x_shift = Math.min(loc.x(), x_shift);
			y_shift = Math.min(loc.y(), y_shift);

			// FIXME
			acc += msize.x() + 10;
		}

		if (x_shift < 0 || y_shift < 0) {
			// shift all
			for (IGraphVertex vertex : vertices) {
				vertex.move(-x_shift, -y_shift);
			}
		}

		List<BandRoute> routes = new ArrayList<>();

		for (IGraphEdge edge : edges) {
			List<Vec2f> curve = new ArrayList<>();
			float radius1 = addStart(curve, edge, 10);
			float radius2 = addEnd(curve, edge, 10);
			Route r = new Route(TesselatedPolygons.spline(curve, 10));
			routes.add(new BandRoute(r, edge.getType().getColor(), edge.getIntersection(), radius1, radius2));
		}
		return new GraphLayoutModel(false, routes);
	}
}
