/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.MappedDoubleList;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.internal.util.Utils;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class Categorical1DDataDomainValues extends A1DDataDomainValues {
	private final TypedGroupSet groups;
	private final int maxBinSize;

	/**
	 * @param t
	 */
	public Categorical1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data, main);
		Perspective p = main.select(data.getDimensionPerspective(), data.getRecordPerspective());
		this.groups = new TypedGroupSet(Utils.extractSetGroups(p, id.getId(), main));

		int maxBinSize = largestGroup(groups);
		this.maxBinSize = maxBinSize;
	}

	static int largestGroup(TypedGroupSet groups) {
		int maxBinSize = 0;
		for (TypedSetGroup g : groups.getGroups())
			maxBinSize = Math.max(g.size(), maxBinSize);
		return maxBinSize;
	}

	@Override
	protected TypedGroupSet getGroups() {
		return groups;
	}

	protected List<TypedSetGroup> groups() {
		return groups.getGroups();
	}

	@Override
	public String getExtensionID() {
		return "categorical.1d";
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return Arrays.asList("distribution.hist", "heatmap");
	}

	/**
	 * @param data
	 * @return
	 */
	protected Histogram createHist(TypedList data) {
		Histogram h = new Histogram(groups().size());
		for (Integer id : data) {
			int index = indexOf(id);
			if (index < 0)
				h.addNAN(id);
			else
				h.add(index, id);
		}
		return h;
	}

	@Override
	protected void fill(Builder b, TypedList data, EDimension dim) {
		super.fill(b, data, dim);
		final Histogram hist = createHist(data);
		b.put(Histogram.class, hist);
		b.put("distribution.largestBin", maxBinSize);
		b.put("distribution.colors", getHistColors(hist, data));
		final String[] labels = getHistLabels(hist, data);
		b.put("distribution.labels", labels);

		final Function<Integer, Double> toNormalized = new Function<Integer, Double>() {
			@Override
			public Double apply(Integer input) {
				return (double) getNormalized(input.intValue());
			}
		};
		b.put("id2double", toNormalized);
		final MappedDoubleList<Integer> list = new MappedDoubleList<>(data, toNormalized);

		b.put("min", 0);
		b.put("max", 1);
		b.put(IDoubleList.class, list);
		b.put("axis.markers", labels);
	}

	protected Color[] getHistColors(Histogram hist, TypedList data) {
		Color[] r = new Color[groups().size()];
		int i = 0;
		for (TypedSetGroup s : groups()) {
			r[i++] = s.getColor();
		}
		return r;
	}

	protected String[] getHistLabels(Histogram hist, TypedList data) {
		String[] r = new String[groups().size()];
		int i = 0;
		for (TypedSetGroup s : groups()) {
			r[i++] = s.getLabel();
		}
		return r;
	}

	public int indexOf(Integer id) {
		int i = 0;
		for (TypedSetGroup g : groups()) {
			if (g.contains(id))
				return i;
			i++;
		}
		return -1;
	}

	@Override
	public boolean apply(String input) {
		return super.apply(input)
				&& !Arrays.asList("labels", "distribution.bar", "kaplanmaier", "boxandwhiskers", "hbar")
						.contains(input);
	}

}
