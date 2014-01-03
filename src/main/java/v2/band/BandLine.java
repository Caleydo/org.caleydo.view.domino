/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.List;

public class BandLine {
	private final PolyLine top;
	private final PolyLine bottom;
	private final Vec2f[] connectionLines;

	public BandLine(List<Vec2f> top, List<Vec2f> bottom, int fixFirstLastN) {
		this(new PolyLine(top, fixFirstLastN), new PolyLine(bottom, fixFirstLastN));
	}

	public BandLine(PolyLine top, PolyLine bottom) {
		this.top = top;
		this.bottom = bottom;
		this.connectionLines = connectionLines(top,bottom);
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

	public PolyArea computeArea(float s1, float s2, float t1, float t2) {
		List<Vec2f> top = computeLine(Math.min(s1, s2), Math.min(t1, t2));
		List<Vec2f> bottom = computeLine(Math.max(s1, s2), Math.max(t1, t2));
		return new PolyArea(top, bottom);
	}
}