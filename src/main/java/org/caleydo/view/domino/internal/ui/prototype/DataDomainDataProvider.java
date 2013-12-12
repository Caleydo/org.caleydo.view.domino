/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.view.domino.api.model.typed.TypedList;

/**
 * @author Samuel Gratzl
 *
 */
public class DataDomainDataProvider {
	private final ATableBasedDataDomain d;

	public DataDomainDataProvider(TablePerspective t) {
		this(t.getDataDomain());
	}

	/**
	 * @param dataDomain
	 */
	public DataDomainDataProvider(ATableBasedDataDomain dataDomain) {
		this.d = dataDomain;
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

	private Perspective asPerspective(TypedList data) {
		PerspectiveInitializationData init = new PerspectiveInitializationData();
		init.setData(data);
		Perspective d = new Perspective(this.d, data.getIdType());
		d.init(init);
		return d;
	}
}
