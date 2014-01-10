/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.domino.api.model.graph.EProximityMode;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.internal.util.BitSetSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Numerical1DDataDomainValues extends A1DDataDomainValues {
	private final TypedGroupSet groups;

	public Numerical1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data, main);
		Perspective p = main.select(data.getDimensionPerspective(), data.getRecordPerspective());
		this.groups = extractGroups(p);
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
			return new TypedGroupSet(TypedGroupList.createUngroupedGroup(d));

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
	public Collection<String> getDefaultVisualization(EProximityMode mode) {
		// FIXME hack
		if (getLabel().contains("Death"))
			Arrays.asList("kaplanmaier", "boxandwhiskers", "heatmap");
		return Arrays.asList("boxandwhiskers", "kaplanmaier", "heatmap");
	}

	@Override
	protected Histogram createHist(TypedListGroup data) {
		final int bins = (int) Math.sqrt(data.size());
		Histogram h = new Histogram(bins);
		for (Integer id : data) {
			float v = getNormalized(id.intValue());
			if (Float.isNaN(v)) {
				h.addNAN(id);
			} else {
				// this works because the values in the container are
				// already normalized
				int bucketIndex = (int) (v * bins);
				if (bucketIndex == bins)
					bucketIndex--;
				h.add(bucketIndex, id);
			}
		}
		return h;
	}

	@Override
	protected Color[] getHistColors() {
		return null;
	}

	@Override
	protected String[] getHistLabels() {
		return null;
	}

}
