/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.collection.table.CategoricalTable;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;

/**
 * @author Samuel Gratzl
 *
 */
public class Categorical2DDataDomainValues extends ADataDomainDataValues {
	private TypedGroupSet recGroups;
	private TypedGroupSet dimGroups;

	/**
	 * @param t
	 */
	public Categorical2DDataDomainValues(TablePerspective t) {
		super(t.getDataDomain().getLabel(), t);
		// Pair<TypedGroupSet, TypedGroupSet> r = extractGroups(t);
		this.recGroups = TypedGroupSet.createUngrouped(TypedSet.of(t.getRecordPerspective().getVirtualArray()));
		this.dimGroups = TypedGroupSet.createUngrouped(TypedSet.of(t.getDimensionPerspective().getVirtualArray()));
	}

	@Override
	public String getExtensionID() {
		return "categorical.2d";
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData) {
		super.fillHeatMap(b, dimData, recData);
	}
	/**
	 * @param t2
	 * @return
	 */
	private Pair<TypedGroupSet, TypedGroupSet> extractGroups(TablePerspective t) {
		Multimap<Object, Integer> dimGroups = HashMultimap.create();
		Multimap<Object, Integer> recGroups = HashMultimap.create();

		TypedSet d = TypedSet.of(t.getDimensionPerspective().getVirtualArray());
		TypedSet r = TypedSet.of(t.getRecordPerspective().getVirtualArray());
		List<Multiset<Object>> rows = new ArrayList<>(r.size());
		for (int i = 0; i < r.size(); ++i)
			rows.add(HashMultiset.create());

		for (Integer dim : d) {
			Multiset<Object> col = HashMultiset.create();

			int i = 0;
			for (Integer rec : r) {
				Object v = getRaw(dim, rec);
				col.add(v);
				rows.get(i++).add(v);
			}

			Object mostFrequent = mostFrequent(col);
			dimGroups.put(mostFrequent, dim);
		}

		{
			int i = 0;
			for (Integer rec : r) {
				Multiset<Object> row = rows.get(i++);

				Object mostFrequent = mostFrequent(row);
				recGroups.put(mostFrequent, rec);
			}
		}

		List<CategoryProperty<?>> categories = getCategories(getDataDomain());
		return Pair
				.make(toGroups(dimGroups, categories, d.getIdType()), toGroups(recGroups, categories, r.getIdType()));
	}

	/**
	 * @param dimGroups2
	 * @param categories
	 * @param idType
	 * @return
	 */
	private TypedGroupSet toGroups(Multimap<Object, Integer> groups, List<CategoryProperty<?>> categories,
			IDType idType) {
		List<TypedSetGroup> r = new ArrayList<>(categories.size());
		for (CategoryProperty<?> category : categories) {
			Collection<Integer> ids = groups.get(category.getCategory());
			if (ids == null || ids.isEmpty())
				continue;
			r.add(new TypedSetGroup(ImmutableSet.copyOf(ids), idType, category.getCategoryName(), category.getColor()));
		}
		return new TypedGroupSet(ImmutableList.copyOf(r));
	}

	@SuppressWarnings("unchecked")
	private static List<CategoryProperty<?>> getCategories(ATableBasedDataDomain dataDomain) {
		final CategoricalTable<?> table = (CategoricalTable<?>) dataDomain.getTable();

		CategoricalClassDescription<?> cats = table.getCategoryDescriptions();
		List<?> tmp = cats.getCategoryProperties();
		return (List<CategoryProperty<?>>) tmp;
	}

	/**
	 * @param col
	 * @return
	 */
	private <T> T mostFrequent(Multiset<T> col) {
		T top = null;
		int c = 0;
		for (T elem : col.elementSet()) {
			int elemC = col.count(elem);
			if (elemC > c) {
				top = elem;
				c = elemC;
			}
		}
		return top;
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim.select(dimGroups, recGroups);
	}

	@Override
	public Collection<String> getDefaultVisualization() {
		return Collections.singleton("heatmap");
	}
}
