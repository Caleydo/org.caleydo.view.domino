/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.api.ui.band;

import static org.caleydo.core.util.function.ExpressionFunctions.compose;
import gleem.linalg.Vec2f;

import java.util.List;

import org.caleydo.core.util.function.ExpressionFunctions.EMonoOperator;
import org.caleydo.core.util.function.IDoubleFunction;
import org.caleydo.core.util.function.InterpolatingFunctions;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.spline.ITesselatedPolygon;
import org.caleydo.core.view.opengl.util.spline.TesselationRenderer;

import com.google.common.base.Function;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ranges;

/**
 * @author Samuel Gratzl
 *
 */
public class Route implements ITesselatedPolygon {
	private final Vec2f[] curve;
	private final Vec2f[] normals;
	private final float[] distances;

	private IDoubleFunction radiusInterpolator = InterpolatingFunctions.constant(1);

	public Route(List<Vec2f> curve) {
		this.curve = curve.toArray(new Vec2f[0]);
		this.normals = normals(this.curve);
		this.distances = distances(this.curve);
	}

	/**
	 * @param radiusInterpolator
	 *            setter, see {@link radiusInterpolator}
	 */
	public void setRadiusInterpolator(IDoubleFunction radiusInterpolator) {
		this.radiusInterpolator = radiusInterpolator;
	}

	/**
	 * compute the normals
	 *
	 * @param curve
	 * @return
	 */
	private static Vec2f[] normals(Vec2f[] curve) {
		final int last = curve.length - 1;
		Vec2f[] normals = new Vec2f[curve.length];

		normals[0] = normal(curve[0], curve[1]);
		for (int i = 1; i < last; ++i) {
			// average from start and end
			normals[i] = normal(curve[i - 1], curve[i]).plus(normal(curve[i], curve[i + 1]));
			normals[i].scale(0.5f);
			normals[i].normalize();
		}
		normals[last] = normal(curve[last - 1], curve[last]);
		return normals;
	}

	/**
	 * compute the prefix distances 0..zero, last..total
	 *
	 * @param curve
	 * @return
	 */
	private static float[] distances(Vec2f[] curve) {
		final int last = curve.length - 1;
		float[] distances = new float[curve.length];

		distances[0] = 0;
		for (int i = 1; i <= last; ++i) {
			distances[i] = distances[i - 1] + curve[i - 1].minus(curve[i]).length();
		}
		return distances;
	}

	private static Vec2f normal(Vec2f a, Vec2f b) {
		Vec2f d = b.minus(a);
		d.normalize();
		d.set(-d.y(), d.x());
		return d;
	}

	private Iterable<Vec2f> shiftCurve(final IDoubleFunction radius) {
		final int last = curve.length - 1;
		final float distanceFactor = 1.f / distances[last];
		return Iterables.transform(Ranges.closed(0, last).asSet(DiscreteDomains.integers()),
				new Function<Integer, Vec2f>() {
					@Override
					public Vec2f apply(Integer input) {
						int i = input.intValue();

						final float t = distances[i] * distanceFactor;
						final float r = (float) radius.apply(t);

						Vec2f p = curve[i].addScaled(r, normals[i]);
						return p;
					}
				});
	}

	@Override
	public void draw(GLGraphics g) {
		g.drawPath(shiftCurve(radiusInterpolator), false);
		g.drawPath(shiftCurve(negatedRadiusInterpolator()), false);
	}

	/**
	 * @return
	 */
	private IDoubleFunction negatedRadiusInterpolator() {
		return compose(EMonoOperator.NEGATE, radiusInterpolator);
	}

	@Override
	public void fill(GLGraphics g, TesselationRenderer renderer) {
		// render the shifted top curve concatenated with the reverse of the bottom curve
		renderer.render2(
				g,
				Iterables.concat(shiftCurve(radiusInterpolator),
						Lists.reverse(Lists.newArrayList(shiftCurve(negatedRadiusInterpolator())))));
	}

	@Override
	public int size() {
		return curve.length * 2;
	}
}
