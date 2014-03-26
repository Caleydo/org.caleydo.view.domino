/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.DataSetSelectedEvent;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.MappedDoubleList;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.rnb.api.model.typed.TypedCollections;
import org.caleydo.view.rnb.api.model.typed.TypedGroupSet;
import org.caleydo.view.rnb.api.model.typed.TypedList;
import org.caleydo.view.rnb.api.model.typed.TypedSet;
import org.caleydo.view.rnb.api.model.typed.TypedSetGroup;
import org.caleydo.view.rnb.internal.util.Utils;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationDataValue implements IDataValues, Function<Integer, Color> {
	private final IDataDomain reference;
	private final String label;
	private final EDimension main;

	private final TypedGroupSet singleGroup;
	private final TypedGroupSet groups;
	private final int maxBinSize;



	public StratificationDataValue(Perspective data, EDimension dim, Integer referenceId) {
		this.main = dim;
		this.label = data.getLabel();
		this.singleGroup = TypedGroupSet.createUngrouped(TypedCollections.INVALID_SINGLETON_SET);
		this.groups = new TypedGroupSet(Utils.extractSetGroups(data, referenceId, dim));
		this.reference = data.getDataDomain();
		this.maxBinSize = Categorical1DDataDomainValues.largestGroup(groups);
	}

	public StratificationDataValue(String label, TypedSet data, EDimension main) {
		this.main = main;
		this.label = label;
		this.singleGroup = TypedGroupSet.createUngrouped(TypedCollections.INVALID_SINGLETON_SET);
		this.groups = TypedGroupSet.createUngrouped(data);
		this.reference = null;
		this.maxBinSize = Categorical1DDataDomainValues.largestGroup(groups);
	}

	public StratificationDataValue(String label, TypedGroupSet data, EDimension main) {
		this.main = main;
		this.label = label;
		this.singleGroup = TypedGroupSet.createUngrouped(TypedCollections.INVALID_SINGLETON_SET);
		this.groups = data;
		this.reference = null;
		this.maxBinSize = Categorical1DDataDomainValues.largestGroup(groups);
	}


	@Override
	public String getExtensionID() {
		return "stratification";
	}

	protected List<TypedSetGroup> groups() {
		return groups.getGroups();
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData, boolean[] existNeigbhor, boolean mediumTranspose) {
		final boolean swapped = dimData.getIdType() != getDefaultGroups(EDimension.DIMENSION).getIdType();

		final EDimension dim = swapped ? main.opposite() : main;
		b.put(EDimension.class, dim);

		b.set("boxandwhiskers.vertical", mediumTranspose ? dim.isHorizontal() : dim.isVertical());
		b.set("distribution.hist.vertical", mediumTranspose ? dim.isHorizontal() : dim.isVertical());

		final TypedList data = dim.select(dimData, recData);
		fill(b, data);
	}

	void fill(Builder b, final TypedList data) {
		b.put(TypedList.class, data);
		b.put("idType",data.getIdType());
		b.put(IDType.class, data.getIdType());

		b.put("min", 0);
		b.put("max", groups().size() - 1);
		final Function<Integer, Double> toIndex = new Function<Integer, Double>() {
			@Override
			public Double apply(Integer input) {
				return (double) indexOf(input);
			}
		};
		b.put("id2double", toIndex);
		b.put("id2color", this);
		final Histogram hist = createHist(data);
		b.put(Histogram.class, hist);
		b.put("distribution.colors", getHistColors(hist, data));
		final String[] labels = getHistLabels(hist, data);
		b.put("distribution.labels", labels);
		b.put("distribution.largestBin", maxBinSize);
		b.put("distribution.total", groups.size());
		b.put(IDoubleList.class, new MappedDoubleList<>(data, toIndex));
		b.put("axis.markers", labels);
	}

	/**
	 * @param data
	 * @return
	 */
	private Histogram createHist(TypedList data) {
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

	@Override
	public Color apply(Integer id) {
		for (TypedSetGroup g : groups()) {
			if (g.contains(id))
				return g.getColor();
		}
		return Color.NOT_A_NUMBER_COLOR;
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
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim == main ? groups : singleGroup;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		return indexOf(a) - indexOf(b);
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return Arrays.asList("distribution.hist", "heatmap");
	}

	@Override
	public Color getColor() {
		return Color.LIGHT_RED;
	}

	@Override
	public boolean apply(String input) {
		return !Arrays.asList("labels", "distribution.bar", "kaplanmaier", "boxandwhiskers").contains(input);
	}

	@Override
	public void onSelectionChanged(boolean selected) {
		if (selected && reference != null)
			EventPublisher.trigger(new DataSetSelectedEvent(reference));
	}

}
