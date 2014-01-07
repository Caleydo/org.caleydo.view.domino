/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.ui;

import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.api.model.typed.TypedSet;

import v2.Node;
import v2.data.StratificationDataValue;

/**
 * @author Samuel Gratzl
 *
 */
public class DragSelectionButton extends ADragButton {

	private final SelectionManager manager;

	public DragSelectionButton(SelectionManager manager) {
		this.manager =manager;
		setRenderer(GLRenderers.drawText("E", VAlign.CENTER));
		setTooltip("Extract Selected " + getLabel(manager));
	}

	private String getLabel(SelectionManager manager) {
		return manager.getIDType().getIDCategory().getCategoryName();
	}

	@Override
	protected Node createNode() {
		Set<Integer> elements = manager.getElements(SelectionType.SELECTION);
		TypedSet data = new TypedSet(elements, manager.getIDType());
		StratificationDataValue d = new StratificationDataValue(getLabel(manager), data, EDimension.DIMENSION);
		return new Node(d);
	}

	/**
	 * @return the manager, see {@link #manager}
	 */
	public SelectionManager getManager() {
		return manager;
	}
}
