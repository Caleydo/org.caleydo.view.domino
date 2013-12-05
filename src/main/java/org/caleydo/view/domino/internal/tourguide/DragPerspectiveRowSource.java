/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.domino.internal.dnd.PerspectiveDragInfo;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.IPerspectiveScoreRow;
import org.caleydo.view.tourguide.api.model.SingleIDPerspectiveRow;

/**
 * @author Samuel Gratzl
 *
 */
public class DragPerspectiveRowSource implements IDragGLSource {

	private final AScoreRow row;

	/**
	 * @param row
	 */
	public DragPerspectiveRowSource(AScoreRow row) {
		this.row = row;
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		assert row instanceof IPerspectiveScoreRow;
		Perspective p = ((IPerspectiveScoreRow) row).asPerspective();
		Integer refernceId = (row instanceof SingleIDPerspectiveRow) ? ((SingleIDPerspectiveRow) row).getDimensionID()
				: null;
		return new PerspectiveDragInfo(p, refernceId, ((IPerspectiveScoreRow) row).getDimension());
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
