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
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.Function2;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;

import com.google.common.primitives.Floats;
import com.jogamp.common.util.IntObjectHashMap;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ADataDomainDataValues implements IDataValues, Function2<Integer, Integer, Color> {
	protected final ATableBasedDataDomain d;
	private final String label;

	private final int dims;
	private final int records;

	private final IntObjectHashMap dimFullCompareCache = new IntObjectHashMap();
	private final IntObjectHashMap recFullCompareCache = new IntObjectHashMap();

	public ADataDomainDataValues(String label, TablePerspective t) {
		this(label, t.getDataDomain());
	}

	/**
	 * @param dataDomain
	 */
	public ADataDomainDataValues(String label, ATableBasedDataDomain dataDomain) {
		this.d = dataDomain;
		this.label = label;
		this.dims = d.getTable().size();
		this.records = d.getTable().depth();
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
		if (dimensionID == null || dimensionID < 0 || dimensionID >= dims)
			return Color.NOT_A_NUMBER_COLOR;
		if (recordID == null || recordID < 0 || recordID >= records)
			return Color.NOT_A_NUMBER_COLOR;
		float[] c = table.getColor(dimensionID, recordID);
		if (c == null)
			return Color.NOT_A_NUMBER_COLOR;
		return new Color(c);
	}

	public Object getRaw(Integer dimensionID, Integer recordID) {
		Table table = getTable();
		if (dimensionID == null || dimensionID < 0 || dimensionID >= dims)
			return null;
		if (recordID == null || recordID < 0 || recordID >= records)
			return null;
		return table.getRaw(dimensionID, recordID);
	}

	public float getNormalized(Integer dimensionID, Integer recordID) {
		Table table = getTable();
		if (dimensionID == null || dimensionID < 0 || dimensionID >= dims)
			return Float.NaN;
		if (recordID == null || recordID < 0 || recordID >= records)
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

	@Override
	public Color apply(Integer recordID, Integer dimensionID) {
		if (isInvalid(recordID) || isInvalid(dimensionID))
			return Color.NOT_A_NUMBER_COLOR;
		// get value
		float[] color = d.getTable().getColor(dimensionID, recordID);
		// to a color
		return new Color(color[0], color[1], color[2], 1.0f);
	}

	private static boolean isInvalid(Integer id) {
		return id == null || id.intValue() < 0;
	}

	@Override
	public int compare(EDimension dim, int a, int b, TypedSet otherData) {
		switch(otherData.size()) {
		case 0 :
			return a - b;
		case 1:
			Integer other = otherData.iterator().next();
			return Floats.compare(getNormalized(dim, a, other), getNormalized(dim, b, other));
		default:
			//
			float a_sum = getCached(dim, a, otherData);
			float b_sum = getCached(dim, b, otherData);
			return Floats.compare(a_sum, b_sum);
		}
	}


	private float getCached(EDimension dim, int a, TypedSet otherData) {
		IntObjectHashMap cache = dim.select(dimFullCompareCache, recFullCompareCache);
		int size = getDefaultGroups(dim.opposite()).size();
		if (otherData.size() != size)
			return sum(dim, a, otherData);
		if (cache.containsKey(a))
			return (Float) cache.get(a);
		float sum = sum(dim, a, otherData);
		cache.put(a, sum);
		return sum;
	}

	private float sum(EDimension dim, int a, TypedSet otherData) {
		// mean values
		float a_sum = 0;
		for (Integer other : otherData) {
			a_sum += getNormalized(dim, a, other);
		}
		return a_sum;
	}

	private float getNormalized(EDimension dim, int a, Integer other) {
		if (dim.isDimension())
			return getNormalized(a, other);
		return getNormalized(other, a);
	}

	/**
	 * @param b
	 * @param dimData
	 * @param recData
	 */
	protected void fillHeatMap(Builder b, TypedList dimData, TypedList recData) {
		b.put("heatmap.dimensions", dimData);
		b.put("heatmap.dimensions.idType", dimData.getIdType());
		b.put("heatmap.records", recData);
		b.put("heatmap.records.idType", recData.getIdType());
		if (dimData.getIdType() != getIDType(EDimension.DIMENSION)) { // swapped
			b.put(Function2.class, Functions2s.swap(this));
		} else
			b.put(Function2.class, this);
	}

	@Override
	public Color getColor() {
		return d.getColor();
	}

	@Override
	public boolean apply(String input) {
		return !"labels".equals(input) && !"distribution.bar".equals(input);
	}
}
