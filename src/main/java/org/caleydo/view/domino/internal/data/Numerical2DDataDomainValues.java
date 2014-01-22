/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.graph.EProximityMode;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.api.model.typed.util.BitSetSet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Numerical2DDataDomainValues extends ADataDomainDataValues {
	private TypedGroupSet recGroups;
	private TypedGroupSet dimGroups;

	/**
	 * @param t
	 */
	public Numerical2DDataDomainValues(TablePerspective t) {
		super(t.getDataDomain().getLabel(), t);
		Pair<TypedGroupSet, TypedGroupSet> r = extractGroups(t);
		this.recGroups = r.getSecond();
		this.dimGroups = r.getFirst();
	}

	@Override
	public String getExtensionID() {
		return "numerical.2d";
	}

	@Override
	public void fill(Builder b, TypedListGroup dimData, TypedListGroup recData) {
		super.fillHeatMap(b, dimData, recData);
	}

	/**
	 * @param t2
	 * @return
	 */
	private Pair<TypedGroupSet, TypedGroupSet> extractGroups(TablePerspective t) {
		Set<Integer> invalidDim = new BitSetSet();
		Set<Integer> invalidRec = new BitSetSet();
		TypedSet d = TypedSet.of(t.getDimensionPerspective().getVirtualArray());
		TypedSet r = TypedSet.of(t.getRecordPerspective().getVirtualArray());
		for (Integer dim : d) {
			for(Integer rec : r) {
				float v = getNormalized(dim, rec);
				if (Float.isInfinite(v) || Float.isNaN(v)) {
					invalidRec.add(rec);
					invalidDim.add(dim);
				}
			}
		}

		return Pair.make(resolve(invalidDim, d), resolve(invalidRec, r));
	}

	private TypedGroupSet resolve(Set<Integer> invalid, TypedSet d) {
		if (invalid.isEmpty())
			return TypedGroupSet.createUngrouped(d);

		TypedSetGroup normal = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(Sets.difference(d, invalid)),
				d.getIdType()), "Normal", getDataDomain().getColor());
		TypedSetGroup invalidG = new TypedSetGroup(new TypedSet(ImmutableSet.copyOf(invalid), d.getIdType()), "NaN",
				Color.NOT_A_NUMBER_COLOR);
		return new TypedGroupSet(normal, invalidG);
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim.select(dimGroups, recGroups);
	}

	@Override
	public Collection<String> getDefaultVisualization(EProximityMode mode) {
		return Collections.singleton("heatmap");
	}

}
