/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.domino.internal.dnd.GraphDragInfo;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.dnd.PerspectiveDragInfo;
import org.caleydo.view.domino.internal.dnd.TablePerspectiveDragInfo;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class Nodes {

	/**
	 * @param tablePerspective
	 * @return
	 */
	public static INode create(TablePerspective t) {
		ATableBasedDataDomain dataDomain = t.getDataDomain();
		if (DataSupportDefinitions.categoricalTables.apply(dataDomain))
			return new CategoricalData2DNode(dataDomain);
		if (DataSupportDefinitions.numericalTables.apply(dataDomain))
			return new NumericalData2DNode(dataDomain);
		if (DataSupportDefinitions.categoricalColumns.apply(t))
			return new CategoricalData1DNode(t, EDimension.RECORD);
		if (DataSupportDefinitions.homogenousColumns.apply(t))
			return new NumericalData1DNode(t, EDimension.RECORD);
		return null;
	}

	public static INode extractPrimary(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof NodeDragInfo) {
			NodeDragInfo ni = (NodeDragInfo) info;
			return ni.apply(item.getType());
		} else if (info instanceof TablePerspectiveDragInfo) {
			INode node = Nodes.create(((TablePerspectiveDragInfo) info).getTablePerspective());
			return node;
		} else if (info instanceof PerspectiveDragInfo) {
			PerspectiveDragInfo pinfo = (PerspectiveDragInfo) info;
			StratificationNode node = new StratificationNode(pinfo.getPerspective(), pinfo.getDim(),
					pinfo.getReferenceID());
			return node;
		} else if (info instanceof GraphDragInfo)
			return ((GraphDragInfo) info).apply(item.getType());
		return null;
	}

	/**
	 * @param item
	 * @return
	 */
	public static boolean canExtract(IDnDItem item) {
		IDragInfo info = item.getInfo();
		return info instanceof NodeDragInfo || info instanceof TablePerspectiveDragInfo
				|| info instanceof PerspectiveDragInfo || info instanceof GraphDragInfo;
	}

}
