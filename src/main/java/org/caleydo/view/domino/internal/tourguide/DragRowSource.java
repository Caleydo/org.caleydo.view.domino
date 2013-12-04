/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.domino.internal.dnd.TablePerspectiveDragInfo;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.ITablePerspectiveScoreRow;

/**
 * @author Samuel Gratzl
 *
 */
public class DragRowSource implements IDragGLSource {

	private final AScoreRow row;

	/**
	 * @param row
	 */
	public DragRowSource(AScoreRow row) {
		this.row = row;
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		TablePerspective t = ((ITablePerspectiveScoreRow) row).asTablePerspective();
		return new TablePerspectiveDragInfo(t);
	}

	@Override
	public void onDropped(IDnDItem info) {
		//
	}

	@Override
	public GLElement createUI(IDragInfo info) {
		return null;
	}

}
