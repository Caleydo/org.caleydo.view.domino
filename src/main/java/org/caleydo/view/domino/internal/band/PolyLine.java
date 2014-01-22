/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;

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

	private PolyLine(List<Vec2f> points, float[] percentages, float distance) {
		this.points = points;
		this.percentages = percentages;
		this.distance = distance;
	}

	public PolyLine stubifyByDistance(float distance) {
		return stubify(distance / this.distance);
	}
	/**
	 * @return
	 */
	public PolyLine stubify(float stubFactor) {
		if (stubFactor >= 0.5f)
			return this;
		List<Vec2f> s = new ArrayList<>();
		List<Float> ps = new ArrayList<>();
		{
			int i = 0;
			while (percentages[i] <= stubFactor) {
				s.add(points.get(i));
				ps.add(percentages[i]);
				i++;
			}
			final float last = percentages[i - 1];
			float delta = percentages[i] - last;
			float alpha = (stubFactor - last) / delta;
			if (alpha > 0) {
				final Vec2f lastP = points.get(i - 1);
				final Vec2f nextP = points.get(i);
				s.add(lastP.times(1 - alpha).addScaled(alpha, nextP)); // linear interpolation
				ps.add(stubFactor);
			}
		}
		{
			List<Vec2f> s_r = new ArrayList<>();
			List<Float> ps_r = new ArrayList<>();
			int i = percentages.length - 1;
			while (percentages[i] >= (1 - stubFactor)) {
				s_r.add(points.get(i));
				ps_r.add(percentages[i]);
				i--;
			}
			final float last = percentages[i + 1];
			float delta = last - percentages[i];
			float alpha = (1 - stubFactor - percentages[i]) / delta;
			if (alpha > 0) {
				final Vec2f lastP = points.get(i + 1);
				final Vec2f nextP = points.get(i);
				s_r.add(lastP.times(alpha).addScaled(1 - alpha, nextP)); // linear interpolation
				ps_r.add(1 - stubFactor);
			}
			s.addAll(Lists.reverse(s_r));
			ps.addAll(Lists.reverse(ps_r));
		}

		return new PolyLine(s, Floats.toArray(ps), distance);
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

	/**
	 * @return the percentages, see {@link #percentages}
	 */
	public float[] getPercentages() {
		return percentages;
	}

	/**
	 * @return
	 */
	public boolean isInvalid() {
		for (Vec2f p : points) {
			if (Float.isNaN(p.x()) || Float.isNaN(p.y()))
				return true;
		}
		return false;
	}

}