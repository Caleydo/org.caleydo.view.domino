/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.domino.internal.dnd.PerspectiveDragInfo;
import org.caleydo.view.domino.internal.dnd.TablePerspectiveDragInfo;

import v2.data.Categorical1DDataDomainValues;
import v2.data.Categorical2DDataDomainValues;
import v2.data.IDataValues;
import v2.data.Numerical1DDataDomainValues;
import v2.data.Numerical2DDataDomainValues;
import v2.data.StratificationDataValue;

/**
 * @author Samuel Gratzl
 *
 */
public class Nodes {

	/**
	 * @param tablePerspective
	 * @return
	 */
	public static IDataValues create(TablePerspective t) {
		ATableBasedDataDomain dataDomain = t.getDataDomain();
		if (DataSupportDefinitions.categoricalTables.apply(dataDomain))
			return new Categorical2DDataDomainValues(t);
		if (DataSupportDefinitions.numericalTables.apply(dataDomain))
			return new Numerical2DDataDomainValues(t);
		if (DataSupportDefinitions.categoricalColumns.apply(t))
			return new Categorical1DDataDomainValues(t, EDimension.RECORD);
		if (DataSupportDefinitions.homogenousColumns.apply(t))
			return new Numerical1DDataDomainValues(t, EDimension.RECORD);
		return null;
	}

	public static Node extract(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof TablePerspectiveDragInfo) {
			Node node = new Node(create(((TablePerspectiveDragInfo) info).getTablePerspective()));
			return node;
		} else if (info instanceof PerspectiveDragInfo) {
			PerspectiveDragInfo pinfo = (PerspectiveDragInfo) info;
			StratificationDataValue v = new StratificationDataValue(pinfo.getPerspective(), pinfo.getDim(),
					pinfo.getReferenceID());
			return new Node(v);
		}
		return null;
	}

	/**
	 * @param item
	 * @return
	 */
	public static boolean canExtract(IDnDItem item) {
		IDragInfo info = item.getInfo();
		return info instanceof TablePerspectiveDragInfo || info instanceof PerspectiveDragInfo;
	}

}