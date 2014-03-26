/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.rnb.internal.data.Categorical1DDataDomainValues;
import org.caleydo.view.rnb.internal.data.Categorical2DDataDomainValues;
import org.caleydo.view.rnb.internal.data.IDataValues;
import org.caleydo.view.rnb.internal.data.Numerical1DDataDomainValues;
import org.caleydo.view.rnb.internal.data.Numerical2DDataDomainValues;
import org.caleydo.view.rnb.internal.data.StratificationDataValue;
import org.caleydo.view.rnb.internal.data.String1DDataDomainValues;
import org.caleydo.view.rnb.internal.dnd.PerspectiveDragInfo;
import org.caleydo.view.rnb.internal.dnd.SetDragInfo;
import org.caleydo.view.rnb.internal.dnd.TablePerspectiveDragInfo;

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
		if (DataSupportDefinitions.dataClass(EDataClass.UNIQUE_OBJECT).apply(t))
			return new String1DDataDomainValues(t, EDimension.RECORD);
		if (DataSupportDefinitions.dataClass(EDataClass.NATURAL_NUMBER, EDataClass.REAL_NUMBER).apply(t))
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
		} else if (info instanceof SetDragInfo) {
			SetDragInfo pinfo = (SetDragInfo) info;
			StratificationDataValue v = new StratificationDataValue(pinfo.getLabel(), pinfo.getSet(), pinfo.getDim());
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
		return info instanceof TablePerspectiveDragInfo || info instanceof PerspectiveDragInfo
				|| info instanceof SetDragInfo;
	}

}
