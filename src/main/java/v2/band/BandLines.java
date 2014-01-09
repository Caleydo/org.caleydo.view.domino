/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @author Samuel Gratzl
 *
 */
public class BandLines {
	private final static float SHIFT = 10;
	private final static Vec2f SHIFT_V = new Vec2f(SHIFT, 0);

	public static BandLine create(Rect a, EDimension aDim, Rect b, EDimension bDim) {
		Pair<List<Vec2f>, List<Vec2f>> p;
		if ((p = createImpl(a, aDim, b, bDim)) != null) { // a -> b
			return new BandLine(p.getFirst(), p.getSecond(), 2);
		} else if ((p = createImpl(b, bDim, a, aDim)) != null) { // b -> a
			return new BandLine(Lists.reverse(p.getFirst()), Lists.reverse(p.getSecond()), 2);
		}
		// can't create
		return null;

	}

	private static Pair<List<Vec2f>, List<Vec2f>> createImpl(Rect a, EDimension aDim, Rect b, EDimension bDim) {
		if (aDim == bDim) {
			if (aDim == EDimension.RECORD) {
				Pair<List<Vec2f>, List<Vec2f>> r = createParallel(rot90(a), rot90(b));
				if (r != null)
					return Pair.make(rot90(r.getFirst()), rot90(r.getSecond()));
				return null;
			} else {
				return createParallel(a, b);
			}
		} else {
			if (aDim == EDimension.RECORD) {
				Pair<List<Vec2f>, List<Vec2f>> r = createRotated(rot90(a), rot90(b));
				return Pair.make(rot90(r.getFirst()), rot90(r.getSecond()));
			} else {
				return createRotated(a, b);
			}
		}
	}

	private static Pair<List<Vec2f>, List<Vec2f>> createParallel(Rect s, Rect t) {
		if (s.x2() < t.x() - 50) {
			return createHorizontal(s, t);
		} else {
			final float delta = Math.max(s.height(), t.height()) * 1.5f;
			if (t.x() < s.x2() && t.x() > s.x() && ((s.y2() < t.y() - delta) || (s.y() < t.y2() + delta))) {
				return createQuestion(s, t);
			}
		}
		return null;
	}

	private static Pair<List<Vec2f>, List<Vec2f>> createRotated(Rect s, Rect t) {
		// FIXME dimension -> record
		return b(shifted(s.x2y(), t.x2y()), shifted(s.x2y2(), t.xy()));
	}

	private static Pair<List<Vec2f>, List<Vec2f>> createHorizontal(Rect s, Rect t) {
		if (s.y() == t.y()) { // just two simple points
			return b(c(s.x2y(), t.xy()), c(s.x2y2(), t.xy2()));
		}
		return b(shifted(s.x2y(), t.xy()), shifted(s.x2y2(), t.xy2()));
	}

	private static Pair<List<Vec2f>, List<Vec2f>> createQuestion(Rect s, Rect t) {
		Vec2f st1 = s.x2y();
		Vec2f sl1 = s.x2y2();
		Vec2f tt1 = t.xy();
		Vec2f tl1 = t.xy2();
		// float distance = tt1.y() - st1.y();

		// float ci =
		Vec2f sl3, st3, tl3, tt3, sl2, st2, tl2, tt2;

		float sin45 = (float) Math.sin(Math.PI * 0.25);
		float cos45 = (float) Math.cos(Math.PI * 0.25);

		if (sl1.y() < tt1.y()) {
			Vec2f scenter = add(sl1, SHIFT, 10);
			sl2 = scenter.plus(new Vec2f(cos45 * 10, -sin45 * 10));
			st2 = scenter.plus(new Vec2f(cos45 * (10 + s.height()), -sin45 * (10 + s.height())));
			sl3 = add(scenter, 10, 0);
			st3 = add(scenter, 10 + s.height(), 0);

			Vec2f tcenter = add(tt1, -SHIFT, -10);
			tt2 = tcenter.plus(new Vec2f(-cos45 * 10, sin45 * 10));
			tl2 = tcenter.plus(new Vec2f(-cos45 * (10 + t.height()), sin45 * (10 + t.height())));
			tt3 = add(tcenter, -10, 0);
			tl3 = add(tcenter, -10 + -t.height(), 0);
		} else {
			Vec2f scenter = add(st1, SHIFT, -10);
			st2 = scenter.plus(new Vec2f(cos45 * 10, sin45 * 10));
			sl2 = scenter.plus(new Vec2f(cos45 * (10 + s.height()), sin45 * (10 + s.height())));
			st3 = add(scenter, 10, 0);
			sl3 = add(scenter, 10 + s.height(), 0);

			Vec2f tcenter = add(tl1, -SHIFT, 10);
			tl2 = tcenter.plus(new Vec2f(-cos45 * 10, -sin45 * 10));
			tt2 = tcenter.plus(new Vec2f(-cos45 * (10 + t.height()), -sin45 * (10 + t.height())));
			tl3 = add(tcenter, -10, 0);
			tt3 = add(tcenter, -10 + -t.height(), 0);
		}

		return b(spline(shifted(st1, st2, st3, tt3, tt2, tt1)), spline(shifted(sl1, sl2, sl3, tl3, tl2, tl1)));
	}


	/**
	 * @param shifted
	 * @return
	 */
	private static List<Vec2f> spline(List<Vec2f> shifted) {
		return TesselatedPolygons.spline(shifted, shifted.size() * 3);
	}

	private static Vec2f add(Vec2f v, float x, float y) {
		Vec2f r = v.copy();
		r.setX(r.x() + x);
		r.setY(r.y() + y);
		return r;
	}

	/**
	 * @param x2y
	 * @param xy
	 * @return
	 */
	private static List<Vec2f> shifted(Vec2f... points) {
		assert points.length >= 2;
		Vec2f sshift = points[0].plus(SHIFT_V);
		Vec2f tshift = points[points.length - 1].minus(SHIFT_V);
		// inject the two points
		return ImmutableList.<Vec2f> builder().add(points[0]).add(sshift)
				.add(Arrays.copyOfRange(points, 1, points.length - 1)).add(tshift).add(points[points.length - 1])
				.build();
	}

	private static Pair<List<Vec2f>, List<Vec2f>> b(List<Vec2f> top, List<Vec2f> bottom) {
		return Pair.make(top, bottom);
	}

	private static List<Vec2f> c(Vec2f... points) {
		return ImmutableList.copyOf(points);
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
