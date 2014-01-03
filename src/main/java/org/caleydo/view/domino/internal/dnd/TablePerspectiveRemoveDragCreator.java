/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IRemoteDragInfoUICreator;

/**
 * @author Samuel Gratzl
 *
 */
public class TablePerspectiveRemoveDragCreator implements IRemoteDragInfoUICreator {

	@Override
	public GLElement createUI(IDragInfo info) {
		if (info instanceof TablePerspectiveDragInfo) {
			TablePerspectiveDragInfo p = (TablePerspectiveDragInfo)info;
			Vec2f size = new Vec2f(Math.max(10, p.getTablePerspective().getNrDimensions()), Math.max(10, p
					.getTablePerspective().getNrRecords()));
			return new DragElement(p.getLabel(), size);
		}
		if (info instanceof PerspectiveDragInfo) {
			PerspectiveDragInfo p = (PerspectiveDragInfo) info;
			EDimension dim = p.getDim();
			int s = p.getPerspective().getVirtualArray().size();
			Vec2f size = new Vec2f(dim.select(s, 10), dim.select(10, s));
			return new DragElement(p.getLabel(), size);
		}
		return null;
	}

}
