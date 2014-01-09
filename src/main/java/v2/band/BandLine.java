/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.util.spline.ITesselatedPolygon;

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

	public ITesselatedPolygon computeArea(float s1, float s2, float t1, float t2) {
		List<Vec2f> top = computeLine(Math.min(s1, s2), Math.min(t1, t2));
		List<Vec2f> bottom = computeLine(Math.max(s1, s2), Math.max(t1, t2));
		if (!stubified)
			return new PolyArea(top, bottom);

		int split = top.size() / 2;
		int n = top.size();
		return new PolyAreas(new PolyArea(top.subList(0, split), bottom.subList(0, split)), new PolyArea(top.subList(n
				- split, n), bottom.subList(n - split, n)));
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
}