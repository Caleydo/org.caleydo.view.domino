/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import java.util.Collections;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.MappedDoubleList;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class A1DDataDomainValues extends ADataDomainDataValues {
	protected final EDimension main;

	protected final TypedID id;
	protected final TypedGroupSet singleGroup;

	/**
	 * @param t
	 */
	public A1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data.getLabel(), data);
		Perspective o = main.opposite().select(data.getDimensionPerspective(), data.getRecordPerspective());
		assert o.getVirtualArray().size() == 1;
		this.id = new TypedID(o.getVirtualArray().get(0), o.getIdType());
		this.main = main;

		this.singleGroup = new TypedGroupSet(TypedGroupList.createUngroupedGroup(new TypedSet(Collections
				.singleton(id.getId()), TypedCollections.INVALID_IDTYPE)));
	}

	protected abstract TypedGroupSet getGroups();

	/**
	 * @return the main, see {@link #main}
	 */
	public EDimension getMain() {
		return main;
	}

	@Override
	public void fill(Builder b, TypedListGroup dimData, TypedListGroup recData) {
		EDimension dim = main;
		TypedListGroup data = main.select(dimData, recData);
		boolean transposed = data.getIdType() == this.singleGroup.getIdType();
		if (transposed) {
			dim = dim.opposite();
			data = main.opposite().select(dimData, recData);
		}
		TypedList single = TypedCollections.singletonList(id);
		super.fillHeatMap(b, dim.select(data, single), dim.select(single, data));
		b.put(EDimension.class, dim);
		b.put(TypedListGroup.class, data);
		b.put("idType", data.getIdType());
		b.put("axis.min", 0);
		b.put("axis.max", 1);
		final Function<Integer, Double> toNormalized = new Function<Integer, Double>() {
			@Override
			public Double apply(Integer input) {
				return (double) getNormalized(input.intValue());
			}
		};
		b.put("id2double", toNormalized);
		b.put(Histogram.class, createHist(data));
		final Color[] hc = getHistColors();
		if (hc != null)
			b.put("distribution.colors", hc);
		final String[] hl = getHistLabels();
		if (hl != null)
			b.put("distribution.labels", hl);
		b.put(IDoubleList.class, new MappedDoubleList<>(data, toNormalized));
	}

	/**
	 * @return
	 */
	protected abstract String[] getHistLabels();

	/**
	 * @return
	 */
	protected abstract Color[] getHistColors();

	/**
	 * @param data
	 * @return
	 */
	protected abstract Histogram createHist(TypedListGroup data);

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim == main ? getGroups() : singleGroup;
	}

	public float getNormalized(int id) {
		return getNormalized(main.select(id, this.id.getId()), main.select(this.id.getId(), id));
	}
}
