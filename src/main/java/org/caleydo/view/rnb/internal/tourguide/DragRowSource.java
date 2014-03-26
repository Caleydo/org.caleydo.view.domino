/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.tourguide;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.rnb.api.model.typed.TypedSet;
import org.caleydo.view.rnb.internal.Node;
import org.caleydo.view.rnb.internal.data.IDataValues;
import org.caleydo.view.rnb.internal.data.LabelDataValues;
import org.caleydo.view.rnb.internal.dnd.NodeDragInfo;
import org.caleydo.view.rnb.internal.dnd.PerspectiveDragInfo;
import org.caleydo.view.rnb.internal.dnd.SetDragInfo;
import org.caleydo.view.rnb.internal.dnd.TablePerspectiveDragInfo;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.IPerspectiveScoreRow;
import org.caleydo.view.tourguide.api.model.ITablePerspectiveScoreRow;
import org.caleydo.view.tourguide.api.model.PathwayPerspectiveRow;
import org.caleydo.view.tourguide.api.model.SingleIDPerspectiveRow;

import com.google.common.collect.ImmutableSet;

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
		if (row instanceof PathwayPerspectiveRow) {
			final PathwayPerspectiveRow r = (PathwayPerspectiveRow) row;
			TypedSet s = new TypedSet(ImmutableSet.copyOf(r.of(null)), r.getIdType());
			return new SetDragInfo(r.getLabel(), s, EDimension.DIMENSION);
		} else if (row instanceof IPerspectiveScoreRow) {
			Perspective p = ((IPerspectiveScoreRow) row).asPerspective();
			Integer refernceId = (row instanceof SingleIDPerspectiveRow) ? ((SingleIDPerspectiveRow) row)
					.getDimensionID() : null;
			return new PerspectiveDragInfo(p, refernceId, ((IPerspectiveScoreRow) row).getDimension());
		} else if (row instanceof ITablePerspectiveScoreRow) {
			TablePerspective t = ((ITablePerspectiveScoreRow) row).asTablePerspective();
			return new TablePerspectiveDragInfo(t);
		} else if (row instanceof LabelScoreRow) {
			IDataValues v = new LabelDataValues(((LabelScoreRow) row).getCategory());
			return new NodeDragInfo(event.getMousePos(), new Node(v));
		}
		return null;
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
