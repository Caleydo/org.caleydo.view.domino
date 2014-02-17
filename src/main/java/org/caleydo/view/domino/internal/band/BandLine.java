/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.util.spline.ITesselatedPolygon;
import org.caleydo.view.domino.internal.band.IBandHost.SourceTarget;

import com.google.common.primitives.Floats;

public class BandLine {
	private final PolyLine top;
	private final PolyLine bottom;
	private final Vec2f[] connectionLines;

	private final boolean stubified;

	public BandLine(List<Vec2f> top, List<Vec2f> bottom, int fixFirstLastN) {
		this(new PolyLine(top, fixFirstLastN), new PolyLine(bottom, fixFirstLastN), false);
	}

	public BandLine(PolyLine top, PolyLine bottom, boolean stubified) {
		this.top = top;
		this.bottom = bottom;
		this.stubified = stubified;
		this.connectionLines = connectionLines(top,bottom);
	}

	/**
	 * @return
	 */
	public boolean isInvalid() {
		if (top.isInvalid())
			return true;
		if (bottom.isInvalid())
			return true;
		for (Vec2f p : connectionLines) {
			if (Float.isNaN(p.x()) || Float.isNaN(p.y()))
				return true;
		}
		return false;
	}

	/**
	 * @return
	 */
	public BandLine asStubified() {
		if (stubified)
			return this;
		return new BandLine(top.stubifyByDistance(30), bottom.stubifyByDistance(30), true);
	}

	private Vec2f[] connectionLines(PolyLine top, PolyLine bottom) {
		Vec2f[] connectionLines = new Vec2f[top.size()];
		for (int i = 0; i < top.size(); ++i) {
			Vec2f l = bottom.get(i).minus(top.get(i));
			connectionLines[i] = l;
		}
		return connectionLines;
	}

	public float getDistance(boolean first) {
		return connectionLines[first ? 0 : (connectionLines.length - 1)].length();
	}

	public List<Vec2f> computeLine(float v0, float v1) {
		final int l = top.size();
		assert l == bottom.size();
		List<Vec2f> r = new ArrayList<>(l);

		for (int i = 0; i < l; ++i) {
			float t = top.getPercentage(i);
			float b = bottom.getPercentage(i);
			// compute distance based ratio
			float r1 = v0 * (1 - t) + v1 * t;
			float r2 = v0 * (1 - b) + v1 * b;
			// average between the two
			float ratio = (r1 + r2) * 0.5f;
			// shift the point according to the ratio
			r.add(top.get(i).addScaled(ratio, connectionLines[i]));
		}
		return r;
	}

	public IBandArea computeArea(float s1, float s2, float t1, float t2) {
		List<Vec2f> top = computeLine(Math.min(s1, s2), Math.min(t1, t2));
		List<Vec2f> bottom = computeLine(Math.max(s1, s2), Math.max(t1, t2));
		if (!stubified) {
			if (isSimilar(top, bottom))
				return new PolyAreaLine(top);
			return new PolyArea(top, bottom);
		}

		int split = top.size() / 2;
		return new StubifiedArea(top, bottom, split, this.top.getPercentages(), this.bottom.getPercentages(), 1);
	}

	public IBandArea computeStub(SourceTarget type, float f1, float f2) {
		BandLine s = asStubified();
		final float unmappedShift = 1.5f;

		List<Vec2f> top;
		List<Vec2f> bottom;

		if (type == SourceTarget.SOURCE) {
			top = s.computeLine(Math.min(f1, f2), unmappedShift);
			bottom = s.computeLine(Math.max(f1, f2), unmappedShift);
			int split = top.size() / 2;
			top = top.subList(0, split);
			bottom = bottom.subList(0, split);
		} else {
			top = s.computeLine(unmappedShift, Math.min(f1, f2));
			bottom = s.computeLine(unmappedShift, Math.max(f1, f2));
			int split = top.size() / 2;
			top = top.subList(split, top.size());
			bottom = bottom.subList(split, top.size());
		}
		{
			if (isSimilar(top, bottom))
				return new PolyAreaLine(top);
			return new PolyArea(top, bottom);
		}
	}

	/**
	 * @param top2
	 * @param bottom2
	 * @return
	 */
	private static boolean isSimilar(List<Vec2f> a, List<Vec2f> b) {
		Vec2f a0 = a.get(0);
		Vec2f b0 = b.get(0);
		Vec2f an = a.get(a.size() - 1);
		Vec2f bn = b.get(b.size() - 1);
		if (a0.minus(b0).lengthSquared() < 2 * 2 && an.minus(bn).lengthSquared() < 2 * 2)
			return true;
		return false;
	}

	public boolean intersects(Rectangle2D bounds) {
		Vec2f[] c = new Vec2f[] { top.get(0), top.get(top.size() - 1), bottom.get(0), bottom.get(top.size() - 1) };
		Rect outer = new Rect();
		outer.x(Floats.min(c[0].x(), c[1].x(), c[2].x(), c[3].x()) + 5);
		outer.x2(Floats.max(c[0].x(), c[1].x(), c[2].x(), c[3].x()) - 5);
		outer.y(Floats.min(c[0].y(), c[1].y(), c[2].y(), c[3].y()) + 5);
		outer.y2(Floats.max(c[0].y(), c[1].y(), c[2].y(), c[3].y()) - 5);

		Rectangle2D r = outer.asRectangle2D();
		if (!r.intersects(bounds))
			return false;

		// TODO check sheared
		return true;
	}

	public interface IBandArea extends ITesselatedPolygon {
		boolean intersects(Rectangle2D bounds);

		/**
		 * @return
		 */
		Rect getBoundingBox();
	}

	/**
	 * @param b
	 * @param c
	 * @return
	 */
	public Vec2f getPoint(boolean first, boolean top) {
		PolyLine l = top ? this.top : this.bottom;
		return first ? l.get(0) : l.get(l.size() - 1);
	}
}