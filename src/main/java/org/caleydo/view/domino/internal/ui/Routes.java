/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.api.model.BandRoute;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.ui.band.Route;
import org.caleydo.view.domino.internal.ui.DominoBandLayer.IBandRoutesProvider;
import org.caleydo.view.domino.internal.ui.model.BandEdge;
import org.caleydo.view.domino.internal.ui.model.DominoGraph;
import org.caleydo.view.domino.internal.ui.model.IEdge;
import org.caleydo.view.domino.internal.ui.prototype.INode;
import org.caleydo.view.domino.spi.model.IBandRenderer;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class Routes implements IBandRoutesProvider {
	private List<IBandRenderer> routes = new ArrayList<>();
	private Runnable callback;
	@Override
	public List<? extends IBandRenderer> getBandRoutes() {
		return routes;
	}

	@Override
	public void setCallback(Runnable toCall) {
		this.callback = toCall;
	}

	/**
	 * @param graph
	 * @param lookup
	 */
	public void update(DominoGraph graph, Function<INode, IGLLayoutElement> lookup) {
		routes.clear();
		for (IEdge edge : graph.edgeSet()) {
			IGLLayoutElement source = lookup.apply(edge.getSource());
			IGLLayoutElement target = lookup.apply(edge.getTarget());

			final Rect sourceB = source.getRectBounds();
			final Rect targetB = target.getRectBounds();
			final EDimension dim = edge.getDirection(edge.getSource()).asDim();

			List<Vec2f> curve;
			if (dim == EDimension.RECORD) {
				curve = rot90(createCurve(rot90(sourceB),rot90(targetB),dim.opposite()));
			} else
				curve = createCurve(sourceB,targetB,dim);
			if (curve.isEmpty())
				continue;

			Color color = edge instanceof BandEdge ? Color.LIGHT_GRAY : Color.LIGHT_BLUE;
			// if (curve.size() > 2)
			// curve = TesselatedPolygons.spline(curve, 5);

			final float r_s = dim.opposite().select(sourceB.width(), sourceB.height()) * 0.5f;
			final float r_t = dim.opposite().select(targetB.width(), targetB.height()) * 0.5f;
			routes.add(new BandRoute(new Route(curve), color, TypedCollections.INVALID_SET, r_s, r_t));
		}
		if (callback != null)
			callback.run();
	}

	/**
	 * @param sourceB
	 * @param targetB
	 * @param dim
	 * @return
	 */
	private List<Vec2f> createCurve(Rect s, Rect t, EDimension dim) {
		assert dim == EDimension.DIMENSION;
		if (s.x() > t.x()) {
			Rect tmp = s;
			s = t;
			t = tmp;
		}

		if (Math.abs(s.x2()-t.x()) < 4)
			return Collections.emptyList();

		Vec2f sv = new Vec2f(s.x2(), s.y() + s.height() * 0.5f);
		Vec2f tv = new Vec2f(t.x(), t.y() + t.height() * 0.5f);
		if (sv.y() == tv.y())
			return Arrays.asList(sv, tv);

		Vec2f shift = new Vec2f(20, 0);
		Vec2f s2 = sv.plus(shift);
		Vec2f t2 = tv.minus(shift);

		return Arrays.asList(sv, s2, t2, tv);
	}

	/**
	 * @param curve
	 * @return
	 */
	private static List<Vec2f> rot90(List<Vec2f> curve) {
		List<Vec2f> r = new ArrayList<>(curve.size());
		for (Vec2f in : curve)
			r.add(new Vec2f(in.y(), in.x()));
		return r;
	}

	/**
	 * @param sourceB
	 * @return
	 */
	private static Rect rot90(Rect a) {
		return new Rect(a.y(), a.x(), a.height(), a.width());
	}
}
