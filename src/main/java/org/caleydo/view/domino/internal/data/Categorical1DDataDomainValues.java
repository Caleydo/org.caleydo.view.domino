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
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.graph.EProximityMode;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.internal.util.Utils;

/**
 * @author Samuel Gratzl
 *
 */
public class Categorical1DDataDomainValues extends A1DDataDomainValues {
	private final TypedGroupSet groups;

	/**
	 * @param t
	 */
	public Categorical1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data, main);
		Perspective p = main.select(data.getDimensionPerspective(), data.getRecordPerspective());
		this.groups = new TypedGroupSet(Utils.extractSetGroups(p, id.getId(), main));
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
	public Collection<String> getDefaultVisualization(EProximityMode mode) {
		return Arrays.asList("distribution.hist", "heatmap");
	}

	/**
	 * @param data
	 * @return
	 */
	protected Histogram createHist(TypedListGroup data) {
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
	protected void fill(Builder b, TypedListGroup data) {
		super.fill(b, data);
		final Histogram hist = createHist(data);
		b.put(Histogram.class, hist);
		b.put("distribution.colors", getHistColors(hist, data));
		b.put("distribution.labels", getHistLabels(hist, data));
	}

	protected Color[] getHistColors(Histogram hist, TypedListGroup data) {
		Color[] r = new Color[groups().size()];
		int i = 0;
		for (TypedSetGroup s : groups()) {
			r[i++] = s.getColor();
		}
		return r;
	}

	protected String[] getHistLabels(Histogram hist, TypedListGroup data) {
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

}
