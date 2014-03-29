/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.MappedDoubleList;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.util.gleem.IColored;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.api.model.typed.util.BitSetSet;
import org.caleydo.view.domino.internal.Constants;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Numerical1DMixin {
	private final INumerical1DContainer c;
	private final int bins;
	private final int maxBinSize;
	private final float min, max;
	private final int size;

	private final Function<Integer, Double> toRaw = new Function<Integer, Double>() {
		@Override
		public Double apply(Integer input) {
			return c.getRaw(input.intValue()).doubleValue();
		}
	};

	public Numerical1DMixin(INumerical1DContainer c, TypedGroupSet groups, float min, float max) {
		this.c = c;
		this.bins = (int) Math.sqrt(groups.size());
		this.min = min;
		this.max = max;
		this.maxBinSize = createHist(groups).getLargestValue();
		this.size = groups.size();
	}

	public static TypedGroupSet extractGroups(Perspective p, INumerical1DContainer c) {
		Set<Integer> invalid = new BitSetSet();
		TypedSet d = TypedSet.of(p.getVirtualArray());
		for (Integer id : d) {
			float v = c.getNormalized(id);
			if (Float.isInfinite(v) || Float.isNaN(v))
				invalid.add(id);
		}
		if (invalid.isEmpty())
			return TypedGroupSet.createUngrouped(d);

		TypedSetGroup normal = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(Sets.difference(d, invalid)),
				d.getIdType()), "Normal", c.getColor());
		TypedSetGroup invalidG = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(invalid), d.getIdType()), "NaN",
				Color.NOT_A_NUMBER_COLOR);
		return new TypedGroupSet(normal, invalidG);
	}

	public Collection<String> getDefaultVisualization() {
		// FIXME hack
		if (c.getLabel().contains("Death"))
			return Arrays.asList("kaplanmeier", "boxandwhiskers", "heatmap");
		return Arrays.asList("axis", "boxandwhiskers", "kaplanmeier", "heatmap");
	}

	public Histogram createHist(ITypedCollection data) {
		Histogram h = new Histogram(bins);
		for (Integer id : data) {
			float v = c.getNormalized(id.intValue());
			if (Float.isNaN(v)) {
				h.addNAN(id);
			} else {
				int bucketIndex = toBin(v, bins);
				h.add(bucketIndex, id);
			}
		}
		return h;
	}

	static int toBin(float v, int bins) {
		// this works because the values in the container are
		// already normalized
		int bucketIndex = (int) (v * bins);
		if (bucketIndex == bins)
			bucketIndex--;
		return bucketIndex;
	}

	static Color[] getHistColors(Histogram hist, TypedList data) {
		Color[] r = new Color[hist.size()];
		float f = 1.f / (r.length - 1);
		for (int i = 0; i < r.length; ++i) {
			r[i] = Constants.colorMapping(i * f);
		}
		return r;
	}

	static String[] getHistLabels(Histogram hist, TypedList data) {
		String[] r = new String[hist.size()];
		for (int i = 0; i < r.length; ++i) {
			r[i] = "Bin " + (i + 1);
		}
		return r;
	}

	public void fill(Builder b, TypedList data, EDimension dim, boolean[] existNeigbhor) {
		final Histogram hist = createHist(data);
		b.put(Histogram.class, hist);
		b.put("distribution.colors", getHistColors(hist, data));
		b.put("distribution.labels", getHistLabels(hist, data));
		b.put("distribution.largestBin", maxBinSize);
		b.put("distribution.total", size);

		b.put("id2double", toRaw);
		final MappedDoubleList<Integer> list = new MappedDoubleList<>(data, toRaw);
		b.put("min", min);
		b.put("max", max);
		b.put(IDoubleList.class, list);

		// FIXME hack, if we have positive and negatives to a centered bar plot
		if (min < 0 && max > 0)
			b.put("hbar.bar.center", 0);
		else {
			boolean leftN = existNeigbhor[EDirection.getPrimary(dim.opposite()).ordinal()];
			boolean rightN = existNeigbhor[EDirection.getPrimary(dim.opposite()).opposite().ordinal()];
			b.put("hbar.bar.left", (!rightN || (leftN && rightN)) && !(dim.isHorizontal() && !leftN && !rightN));
		}

		// b.put("hbar.id2color", new AlternatingColors(Color.BLACK, Color.LIGHT_GRAY, even(data)));
		b.put("hbar.id2color", Functions.constant(Color.BLACK));
		b.put("hbar.outline", Color.WHITE);
		// b.set("kaplanmeier.fillCurve");

		b.set("boxandwhiskers.showOutliers");
		b.set("boxandwhiskers.showMinMax");
		b.put("sheatmap.frameColor", Color.LIGHT_GRAY);

		if (dim.isVertical())
			b.set("axis.invertOrder");
	}

	/**
	 * @param data
	 * @return
	 */
	private Set<Integer> even(TypedList data) {
		BitSetSet s = new BitSetSet();
		for (int i = 0; i < data.size(); i += 2)
			s.add(data.get(i));
		return s;
	}

	private static final class AlternatingColors implements Function<Integer, Color> {
		private final Color even, odd;
		private final Set<Integer> isEven;

		public AlternatingColors(Color even, Color odd, Set<Integer> isEven) {
			this.even = even;
			this.odd = odd;
			this.isEven = isEven;
		}

		@Override
		public Color apply(Integer input) {
			if (isEven.contains(input))
				return even;
			return odd;
		}
	}

}

interface INumerical1DContainer extends IColored, ILabeled{

	/**
	 * @param intValue
	 * @return
	 */
	Float getRaw(int intValue);

	/**
	 * @param id
	 * @return
	 */
	float getNormalized(int id);

}
