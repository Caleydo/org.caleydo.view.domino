/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.data;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;

import com.google.common.base.Function;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class A1DDataDomainValues extends ADataDomainDataValues implements Function<Integer, Color> {
	protected final EDimension main;

	protected final TypedID id;
	protected final TypedGroupSet singleGroup;

	private final Function<Integer, String> id2Label = new Function<Integer, String>() {
		@Override
		public String apply(Integer input2) {
			return getLabel(input2);
		}
	};
	/**
	 * @param t
	 */
	public A1DDataDomainValues(TablePerspective data, EDimension main) {
		super(data.getLabel(), data);
		Perspective o = main.opposite().select(data.getDimensionPerspective(), data.getRecordPerspective());
		assert o.getVirtualArray().size() == 1;
		this.id = new TypedID(o.getVirtualArray().get(0), o.getIdType());
		this.main = main;

		this.singleGroup = TypedGroupSet.createUngrouped(TypedCollections.singleton(id.getId(),
				TypedCollections.INVALID_IDTYPE));
	}


	protected abstract TypedGroupSet getGroups();

	/**
	 * @return the main, see {@link #main}
	 */
	public EDimension getMain() {
		return main;
	}

	protected Object getDescription() {
		if (main.isRecord())
			return this.d.getTable().getDataClassSpecificDescription(id.getId(), 0);
		else
			return this.d.getTable().getDataClassSpecificDescription(0, id.getId());
	}

	@Override
	public void fill(Builder b, TypedList dimData, TypedList recData, boolean[] existNeigbhor, boolean mediumTranspose) {
		EDimension dim = main;
		TypedList data = main.select(dimData, recData);
		boolean transposed = data.getIdType() == this.singleGroup.getIdType();
		if (transposed) {
			dim = dim.opposite();
			data = main.opposite().select(dimData, recData);
		}
		b.put(EDimension.class, dim);
		b.set("boxandwhiskers.vertical", mediumTranspose ? dim.isHorizontal() : dim.isVertical());
		b.set("distribution.hist.vertical", mediumTranspose ? dim.isHorizontal() : dim.isVertical());
		fill(b, data, dim, existNeigbhor);
	}

	/**
	 * @param b
	 * @param data
	 * @param dim
	 */
	protected void fill(Builder b, TypedList data, EDimension dim, boolean[] existNeigbhor) {
		b.put(TypedList.class, data);
		b.put(IDType.class, data.getIdType());
		b.put("idType", data.getIdType());
		b.put("id2color", this);
		b.put("id2label", id2Label);
	}

	@Override
	public TypedGroupSet getDefaultGroups(EDimension dim) {
		return dim == main ? getGroups() : singleGroup;
	}

	public float getNormalized(int id) {
		return getNormalized(main.select(id, this.id.getId()), main.select(this.id.getId(), id));
	}

	public Object getRaw(int id) {
		return getRaw(main.select(id, this.id.getId()), main.select(this.id.getId(), id));
	}

	protected String getLabel(Integer id) {
		if (isInvalid(id))
			return "";
		Object raw = getRaw(id);
		return raw2string(raw);
	}

	@Override
	public Color apply(Integer id) {
		EDimension d = main.opposite();
		return apply(d.select(id.intValue(), this.id.getId()), d.select(this.id.getId(), id.intValue()));
	}
}
