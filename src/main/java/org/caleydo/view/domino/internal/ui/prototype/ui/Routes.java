/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.domino.api.model.BandRoute;
import org.caleydo.view.domino.api.model.TypedCollections;
import org.caleydo.view.domino.api.ui.band.Route;
import org.caleydo.view.domino.internal.ui.DominoBandLayer.IBandRoutesProvider;
import org.caleydo.view.domino.internal.ui.prototype.BandEdge;
import org.caleydo.view.domino.internal.ui.prototype.EDirection;
import org.caleydo.view.domino.internal.ui.prototype.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.internal.ui.prototype.graph.DominoGraph;
import org.caleydo.view.domino.spi.model.IBandRenderer;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class Routes implements IBandRoutesProvider {
	private List<IBandRenderer> routes = new ArrayList<>();
	@Override
	public List<? extends IBandRenderer> getBandRoutes() {
		return routes;
	}

	/**
	 * @param graph
	 * @param lookup
	 */
	public void update(DominoGraph graph, Function<INode, NodeLayoutElement> lookup) {
		routes.clear();
		for (IEdge edge : graph.edgeSet()) {
			if (edge.getDirection() == EDirection.RIGHT_OF || edge.getDirection() == EDirection.BELOW)
				continue; // as directed will come again
			NodeLayoutElement source = lookup.apply(graph.getEdgeSource(edge));
			NodeLayoutElement target = lookup.apply(graph.getEdgeTarget(edge));

			Rect sourceB = source.getRectBounds();
			Rect targetB = target.getRectBounds();
			EDimension dim = edge.getDirection().asDim();

			if ((dim.isHorizontal() && sourceB.x2() == targetB.x())
					|| (dim.isVertical() && sourceB.y2() == targetB.y()))
				continue;

			float r_s = dim.opposite().select(sourceB.width(), sourceB.height()) * 0.5f;
			float r_t = dim.opposite().select(targetB.width(), targetB.height()) * 0.5f;
			List<Vec2f> curve;
			if (dim.isHorizontal()) {
				curve = Arrays.asList(new Vec2f(sourceB.x2(), sourceB.y() + sourceB.height() * 0.5f),
						new Vec2f(targetB.x(), targetB.y() + targetB.height() * 0.5f));
			} else {
				curve = Arrays.asList(new Vec2f(sourceB.x() + sourceB.width() * 0.5f, sourceB.y2()),
						new Vec2f(targetB.x() + targetB.width() * 0.5f, targetB.y()));
			}
			Color color = edge instanceof BandEdge ? Color.LIGHT_GRAY : Color.LIGHT_BLUE;
			routes.add(new BandRoute(new Route(curve), color, TypedCollections.INVALID_SET, r_s, r_t));
		}
	}
}
