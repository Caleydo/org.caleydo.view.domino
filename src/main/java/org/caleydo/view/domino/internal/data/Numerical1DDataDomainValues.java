/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.DoubleStatistics;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.MappedDoubleList;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.api.model.typed.util.BitSetSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Numerical1DDataDomainValues extends A1DDataDomainValues {
	private final TypedGroupSet groups;
	private final boolean isInteger;
	private final int bins;
	private final int maxBinSize;
	private final DoubleStatistics stats;

	private final Function<Integer, Double> toRaw = new Function<Integer, Double>() {
		@Override
		public Double apply(Integer input) {
			return getRaw(input.intValue()).doubleValue();
		}
	};

	public Numerical1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data, main);
		Perspective p = main.select(data.getDimensionPerspective(), data.getRecordPerspective());
		this.groups = extractGroups(p);
		this.isInteger = DataSupportDefinitions.dataClass(EDataClass.NATURAL_NUMBER).apply(data);

		this.bins = (int) Math.sqrt(groups.size());
		this.maxBinSize = createHist(groups).getLargestValue();
		org.caleydo.core.util.function.DoubleStatistics.Builder b = DoubleStatistics.builder();
		for (Integer id : groups)
			b.add(toRaw.apply(id));
		this.stats = b.build();
	}

	@Override
	public String getExtensionID() {
		return "numerical.1d";
	}

	private TypedGroupSet extractGroups(Perspective p) {
		Set<Integer> invalid = new BitSetSet();
		TypedSet d = TypedSet.of(p.getVirtualArray());
		for (Integer id : d) {
			float v = getNormalized(id);
			if (Float.isInfinite(v) || Float.isNaN(v))
				invalid.add(id);
		}
		if (invalid.isEmpty())
			return TypedGroupSet.createUngrouped(d);

		TypedSetGroup normal = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(Sets.difference(d, invalid)),
				d.getIdType()), "Normal", getDataDomain().getColor());
		TypedSetGroup invalidG = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(invalid), d.getIdType()), "NaN",
				Color.NOT_A_NUMBER_COLOR);
		return new TypedGroupSet(normal, invalidG);
	}

	@Override
	protected TypedGroupSet getGroups() {
		return groups;
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		// FIXME hack
		if (getLabel().contains("Death"))
			return Arrays.asList("kaplanmaier", "boxandwhiskers", "heatmap");
		return Arrays.asList("boxandwhiskers", "kaplanmaier", "heatmap");
	}

	@Override
	public boolean apply(String input) {
		return super.apply(input) && !"distribution.pie".equals(input);
	}

	protected Histogram createHist(ITypedCollection data) {
		Histogram h = new Histogram(bins);
		for (Integer id : data) {
			float v = getNormalized(id.intValue());
			if (Float.isNaN(v)) {
				h.addNAN(id);
			} else {
				int bucketIndex = toBin(v);
				h.add(bucketIndex, id);
			}
		}
		return h;
	}

	int toBin(float v) {
		// this works because the values in the container are
		// already normalized
		int bucketIndex = (int) (v * bins);
		if (bucketIndex == bins)
			bucketIndex--;
		return bucketIndex;
	}

	protected Color[] getHistColors(Histogram hist, TypedList data) {
		Color[] r = new Color[hist.size()];
		float f = 1.f / (r.length - 1);
		for (int i = 0; i < r.length; ++i) {
			r[i] = new Color(i * f);
		}
		return r;
	}

	protected String[] getHistLabels(Histogram hist, TypedList data) {
		String[] r = new String[hist.size()];
		for (int i = 0; i < r.length; ++i) {
			r[i] = "Bin " + (i + 1);
		}
		return r;
	}

	@Override
	protected void fill(Builder b, TypedList data) {
		super.fill(b, data);
		final Histogram hist = createHist(data);
		b.put(Histogram.class, hist);
		b.put("distribution.colors", getHistColors(hist, data));
		b.put("distribution.labels", getHistLabels(hist, data));
		b.put("distribution.largestBin", maxBinSize);

		b.put("id2double", toRaw);
		final MappedDoubleList<Integer> list = new MappedDoubleList<>(data, toRaw);
		b.put("min", this.stats.getMin());
		b.put("max", this.stats.getMax());
		b.put(IDoubleList.class, list);

		// FIXME hack, if we have positive and negatives to a centered bar plot
		if (stats.getMin() < 0 && stats.getMax() > 0)
			b.put("hbar.center", 0);

		b.put("hbar.id2color", new AlternatingColors(Color.BLACK, Color.LIGHT_GRAY, even(data)));
		// b.set("kaplanmaier.fillCurve");

		b.set("boxandwhiskers.showOutliers");
		b.set("boxandwhiskers.showMinMax");
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

	@Override
	public Float getRaw(int id) {
		Object r = super.getRaw(id);
		if (r instanceof Float)
			return ((Float)r);
		if (r instanceof Integer && isInteger) {
			Integer i = (Integer) r;
			if (i.intValue() == Integer.MIN_VALUE)
				return Float.NaN;
			return i.floatValue();
		}
		if (r instanceof Number)
			return ((Number) r).floatValue();
		return Float.NaN;
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
