/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.dnd.SetDragInfo;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.PathwayPerspectiveRow;

import com.google.common.collect.ImmutableSet;

/**
 * @author Samuel Gratzl
 *
 */
public class DragSetRowSource implements IDragGLSource {

	private final AScoreRow row;

	/**
	 * @param row
	 */
	public DragSetRowSource(AScoreRow row) {
		this.row = row;
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		assert row instanceof PathwayPerspectiveRow;
		final PathwayPerspectiveRow r = (PathwayPerspectiveRow) row;
		TypedSet s = new TypedSet(ImmutableSet.copyOf(r.of(null)), r.getIdType());
		return new SetDragInfo(r.getLabel(), s, EDimension.DIMENSION);
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
