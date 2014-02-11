/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.dnd.RulerDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class DragRulerButton extends ADragButton {

	private final SelectionManager manager;

	public DragRulerButton(SelectionManager manager) {
		this.manager =manager;
		setRenderer(GLRenderers.fillImage(Resources.ICON_RULER));
		setTooltip("Show / Hide Ruler for " + getLabel());
	}

	private String getLabel() {
		return getIDCategory().getCategoryName();
	}

	public IDCategory getIDCategory() {
		return manager.getIDType().getIDCategory();
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		return new RulerDragInfo(event.getMousePos(), new Ruler(manager, findParent(Domino.class).getUndo()));
	}

	@Override
	public void onDropped(IDnDItem info) {
		setEnabled(false);
		super.onDropped(info);
	}

	/**
	 * @return the manager, see {@link #manager}
	 */
	public SelectionManager getManager() {
		return manager;
	}
}
