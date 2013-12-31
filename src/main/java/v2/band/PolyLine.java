/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.band;

import gleem.linalg.Vec2f;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class PolyLine extends AbstractList<Vec2f> {
	private final List<Vec2f> points;
	private final float[] percentages;
	private final float distance;

	public PolyLine(List<Vec2f> points, int fixFirstLastN) {
		this.points = points;
		this.percentages = distances(points);
		this.distance = percentages[percentages.length - 1];
		// normalize
		for (int i = 0; i < percentages.length; ++i)
			percentages[i] /= this.distance;
		for (int i = 0; i < Math.min(fixFirstLastN, percentages.length / 2); ++i) {
			percentages[i] = 0;
			percentages[percentages.length - 1 - i] = 1;
		}
	}

	float getPercentage(int p) {
		return percentages[p];
	}

	private float[] distances(List<Vec2f> curve) {
		final int last = curve.size() - 1;
		float[] distances = new float[curve.size()];

		distances[0] = 0;
		for (int i = 1; i <= last; ++i) {
			distances[i] = distances[i - 1] + curve.get(i - 1).minus(curve.get(i)).length();
		}
		return distances;
	}

	@Override
	public Iterator<Vec2f> iterator() {
		return points.iterator();
	}

	@Override
	public int size() {
		return points.size();
	}

	@Override
	public Vec2f get(int index) {
		return points.get(index);
	}

}