/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IRemoteDragInfoUICreator;
import org.caleydo.view.domino.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public class TablePerspectiveRemoveDragCreator implements IRemoteDragInfoUICreator {

	private Domino rnb;

	/**
	 * @param rnb
	 */
	public TablePerspectiveRemoveDragCreator(Domino rnb) {
		this.rnb = rnb;
	}

	@Override
	public GLElement createUI(IDragInfo info) {
		if (info instanceof TablePerspectiveDragInfo) {
			TablePerspectiveDragInfo p = (TablePerspectiveDragInfo)info;
			TablePerspective tp = p.getTablePerspective();
			Vec2f size = new Vec2f(Math.max(10, p.getTablePerspective().getNrDimensions()), Math.max(10, p
					.getTablePerspective().getNrRecords()));
			boolean isSingle = tp.getNrDimensions() == 1 || tp.getNrRecords() == 1;
			if (isSingle)
				size = new Vec2f(100, 100);
			return new DragElement(p.getLabel(), size, this.rnb, info);
		}
		if (info instanceof PerspectiveDragInfo) {
			PerspectiveDragInfo p = (PerspectiveDragInfo) info;
			// EDimension dim = p.getDim();
			// int s = p.getPerspective().getVirtualArray().size();
			// Vec2f size = new Vec2f(dim.select(s, 10), dim.select(10, s));
			return new DragElement(p.getLabel(), new Vec2f(100, 100), this.rnb, info);
		}
		if (info instanceof SetDragInfo) {
			SetDragInfo p = (SetDragInfo) info;
			// EDimension dim = p.getDim();
			// int s = p.getSet().size();
			// Vec2f size = new Vec2f(dim.select(s, 10), dim.select(10, s));
			return new DragElement(p.getLabel(), new Vec2f(100, 100), this.rnb, info);
		}
		return null;
	}

}
