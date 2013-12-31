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

import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class BandLineFactory {
	private final static float SHIFT = 20;
	private final static Vec2f SHIFT_V = new Vec2f(SHIFT, 0);

	public static BandLine create(Rect a, EDimension aDim, Rect b, EDimension bDim) {
		// FIXME more
		if (aDim == EDimension.RECORD) {
			Pair<List<Vec2f>, List<Vec2f>> p = createHorizontal(rot90(a), rot90(b));
			return new BandLine(rot90(p.getFirst()), rot90(p.getSecond()));
		} else {
			Pair<List<Vec2f>, List<Vec2f>> p = createHorizontal(a, b);
			return new BandLine(p.getFirst(), p.getSecond());
		}
	}

	private static Pair<List<Vec2f>, List<Vec2f>> createHorizontal(Rect s, Rect t) {
		if (s.y() == t.y()) { //just two simple points
			return b(c(s.x2y(), t.xy()), c(s.x2y2(), t.xy2()));
		}
		return b(shifted(s.x2y(), t.xy()), shifted(s.x2y2(), t.xy2()));
	}

	// private static BandLine createQuestion(Rect s, Rect t) {
	// Vec2f s_wy = s.x2y();
	// Vec2f s_wh = s.x2y2();
	// Vec2f t_xy = t.xy();
	// Vec2f t_xh = t.xy2();
	// float distance = t_xy.y() - s_wh.y();
	// float ci =
	//
	// return b(shifted(s_wy, t_xy), shifted(s_wh, t_xh));
	// }


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

	// private static List<Vec2f> createQuestionMarkCurve(Rect s, Rect t) {
	// Vec2f sa = new Vec2f(s.x2(), s.y() + s.height() * 0.5f);
	// Vec2f ta = new Vec2f(t.x(), t.y() + t.height() * 0.5f);
	//
	// Vec2f shift = new Vec2f(20, 0);
	// Vec2f sb = sa.plus(shift);
	// Vec2f tb = ta.minus(shift);
	//
	// float yd = ta.y() - sa.y();
	// Vec2f sc = sb.plus(new Vec2f(s.height() * 0.5f, s.height() * 0.5f));
	// Vec2f tc = tb.minus(new Vec2f(t.height() * 0.5f, t.height() * 0.5f));
	//
	// Vec2f sd = sc.plus(new Vec2f(-10, yd * 0.45f - sc.y()));
	// Vec2f td = tc.minus(new Vec2f(-10, yd * 0.45f - tc.y()));
	//
	// return Arrays.asList(sa, sb, sc, sd, td, tc, tb, ta);
	// }

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

	// public static BandRoute create(BandBlock sBlock, BandBlock tBlock) {
	// BandRoute r;
	//
	// if ((r = test(sBlock, tBlock)) != null)
	// return r;
	// if ((r = test(tBlock, sBlock)) != null)
	// return r;
	// // complex case
	// return null;
	// }
	//
	// private static BandRoute test(BandBlock sBlock, BandBlock tBlock) {
	// Rect sBounds = sBlock.bounds;
	// Rect tBounds = tBlock.bounds;
	// List<Vec2f> route;
	// boolean sameDim = sBlock.dim == tBlock.dim;
	// // simple right of
	// if (sameDim && sBlock.dim == EDimension.DIMENSION && (route = test(sBounds, tBounds)) != null) {
	// return createImpl(sBlock, tBlock, true, route);
	// }
	// if (sameDim && sBlock.dim == EDimension.RECORD && (route = test(rot90(sBounds), rot90(tBounds))) != null) {
	// return createImpl(sBlock, tBlock, true, rot90(route));
	// }
	// return null;
	//
	// }
	//
	// private static List<Vec2f> test(Rect s, Rect t) {
	// if (s.x2() < t.x() - 100) {
	// return createCurve(s, t);
	// } else {
	// final float delta = Math.max(s.height(), t.height()) * 1.5f;
	// if (t.x() < s.x2() && t.x() > s.x() && ((s.y2() < t.y() - delta) || (s.y() < t.y2() + delta))) {
	// return createQuestionMarkCurve(s, t);
	// }
	// }
	// return null;
	// }
	//
	// /**
	// * @param sourceB
	// * @param targetB
	// * @param dim
	// * @return
	// */
	// private static List<Vec2f> createCurve(Rect s, Rect t) {
	// if (Math.abs(s.x2() - t.x()) < 4)
	// return Collections.emptyList();
	//
	// Vec2f sv = new Vec2f(s.x2() + 10, s.y() + s.height() * 0.5f);
	// Vec2f tv = new Vec2f(t.x() - 10, t.y() + t.height() * 0.5f);
	// if (sv.y() == tv.y())
	// return Arrays.asList(sv, tv);
	//
	// Vec2f shift = new Vec2f(20, 0);
	// Vec2f s2 = sv.plus(shift);
	// Vec2f t2 = tv.minus(shift);
	//
	// return Arrays.asList(sv, s2, t2, tv);
	// }
	//
	// private static List<Vec2f> createQuestionMarkCurve(Rect s, Rect t) {
	// Vec2f sa = new Vec2f(s.x2(), s.y() + s.height() * 0.5f);
	// Vec2f ta = new Vec2f(t.x(), t.y() + t.height() * 0.5f);
	//
	// Vec2f shift = new Vec2f(20, 0);
	// Vec2f sb = sa.plus(shift);
	// Vec2f tb = ta.minus(shift);
	//
	// float yd = ta.y() - sa.y();
	// Vec2f sc = sb.plus(new Vec2f(s.height() * 0.5f, s.height() * 0.5f));
	// Vec2f tc = tb.minus(new Vec2f(t.height() * 0.5f, t.height() * 0.5f));
	//
	// Vec2f sd = sc.plus(new Vec2f(-10, yd * 0.45f - sc.y()));
	// Vec2f td = tc.minus(new Vec2f(-10, yd * 0.45f - tc.y()));
	//
	// return Arrays.asList(sa, sb, sc, sd, td, tc, tb, ta);
	// }
	//
	// public static class RectDim {
	// final EDimension dim;
	// final Rect bounds;
	//
	// public RectDim(EDimension dim, Rect bounds) {
	// this.dim = dim;
	// this.bounds = bounds;
	//
	// }
	//
	// public float getTotal() {
	// return dim.opposite().select(bounds.width(), bounds.height());
	// }
	// }
}
