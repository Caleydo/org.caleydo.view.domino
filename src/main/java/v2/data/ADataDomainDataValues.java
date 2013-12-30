/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.data;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;

import com.google.common.primitives.Floats;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ADataDomainDataValues implements IDataValues {
	protected final ATableBasedDataDomain d;
	private final String label;

	public ADataDomainDataValues(String label, TablePerspective t) {
		this(label, t.getDataDomain());
	}

	/**
	 * @param dataDomain
	 */
	public ADataDomainDataValues(String label, ATableBasedDataDomain dataDomain) {
		this.d = dataDomain;
		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	private Table getTable() {
		return d.getTable();
	}

	public Color getColor(Integer dimensionID, Integer recordID) {
		Table table = getTable();
		if (dimensionID == null || dimensionID < 0 || dimensionID >= table.size())
			return Color.NOT_A_NUMBER_COLOR;
		if (recordID == null || recordID < 0 || recordID >= table.depth())
			return Color.NOT_A_NUMBER_COLOR;
		float[] c = table.getColor(dimensionID, recordID);
		if (c == null)
			return Color.NOT_A_NUMBER_COLOR;
		return new Color(c);
	}

	public Object getRaw(Integer dimensionID, Integer recordID) {
		Table table = getTable();
		if (dimensionID == null || dimensionID < 0 || dimensionID >= table.size())
			return null;
		if (recordID == null || recordID < 0 || recordID >= table.depth())
			return null;
		return table.getRaw(dimensionID, recordID);
	}

	public float getNormalized(Integer dimensionID, Integer recordID) {
		Table table = getTable();
		if (dimensionID == null || dimensionID < 0 || dimensionID >= table.size())
			return Float.NaN;
		if (recordID == null || recordID < 0 || recordID >= table.depth())
			return Float.NaN;
		return table.getNormalizedValue(dimensionID, recordID);
	}

	public IDType getIDType(EDimension dim) {
		return dim.select(d.getDimensionIDType(), d.getRecordIDType());
	}

	/**
	 * @return
	 */
	public ATableBasedDataDomain getDataDomain() {
		return d;
	}

	public TablePerspective asTablePerspective(TypedList dim, TypedList rec) {
		Perspective d = asPerspective(dim);
		Perspective r = asPerspective(rec);
		TablePerspective t = new TablePerspective(this.d, r, d);
		return t;
	}

	public TablePerspective asTablePerspective(TypedGroupList dim, TypedGroupList rec) {
		Perspective d = asPerspective(dim);
		Perspective r = asPerspective(rec);
		TablePerspective t = new TablePerspective(this.d, r, d);
		return t;
	}

	private Perspective asPerspective(TypedList data) {
		PerspectiveInitializationData init = new PerspectiveInitializationData();
		init.setData(data);
		Perspective d = new Perspective(this.d, data.getIdType());
		d.init(init);
		return d;
	}

	private Perspective asPerspective(TypedGroupList data) {
		if (data.getGroups().size() <= 1)
			return asPerspective((TypedList) data);

		PerspectiveInitializationData init = new PerspectiveInitializationData();
		VirtualArray va = new VirtualArray(data.getIdType(), data);
		GroupList groupList = new GroupList();
		for (TypedListGroup g : data.getGroups()) {
			Group group = new Group(g.size(), 0);
			group.setLabel(g.getLabel());
			groupList.append(group);
		}
		va.setGroupList(groupList);

		init.setData(va);

		Perspective d = new Perspective(this.d, data.getIdType());
		d.init(init);
		return d;
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		switch(otherData.size()) {
		case 0 :
			return a - b;
		case 1: {
			Integer other = otherData.iterator().next();
			return Floats.compare(getNormalized(dim, a, other), getNormalized(dim, b, other));
		}
		default:
			// mean values
			float a_sum = 0;
			float b_sum = 0;
			for (Integer other : otherData) {
				a_sum += getNormalized(dim, a, other);
				b_sum += getNormalized(dim, b, other);
			}
			return Floats.compare(a_sum, b_sum);
		}
	}


	private float getNormalized(EDimension dim, int a, Integer other) {
		if (dim.isDimension())
			return getNormalized(a, other);
		return getNormalized(other, a);
	}
}
