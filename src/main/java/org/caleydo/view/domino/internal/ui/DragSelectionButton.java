/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.data.StratificationDataValue;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class DragSelectionButton extends ADragButton implements IGLRenderer {

	private final SelectionManager manager;
	private int numberOfElements;

	public DragSelectionButton(SelectionManager manager) {
		this.manager =manager;
		setRenderer(this);
		setTooltip("Extract Selected " + getLabel(manager));
	}

	private String getLabel(SelectionManager manager) {
		return manager.getIDType().getIDCategory().getCategoryName();
	}
	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		return new NodeDragInfo(event.getMousePos(), createNode());
	}

	private Node createNode() {
		Set<Integer> elements = manager.getElements(SelectionType.SELECTION);
		TypedSet data = new TypedSet(elements, manager.getIDType());
		StratificationDataValue d = new StratificationDataValue("Selected " + getLabel(manager), data,
				EDimension.DIMENSION);
		return new Node(d);
	}

	@Override
	public void render(GLGraphics g, float w, float h, GLElement parent) {
		g.drawText("S", 0, 0, w, h, VAlign.CENTER);
		g.drawText("" + numberOfElements, 0, h * 0.6f, w, h * 0.5f, VAlign.RIGHT);
	}

	/**
	 * @return the manager, see {@link #manager}
	 */
	public SelectionManager getManager() {
		return manager;
	}

	/**
	 * @param numberOfElements
	 */
	public void setNumberOfElements(int numberOfElements) {
		if (this.numberOfElements == numberOfElements)
			return;
		this.numberOfElements = numberOfElements;
		this.setEnabled(numberOfElements > 0);
		setTooltip("Extract " + numberOfElements + " Selected " + getLabel(manager));
		repaint();
	}
}
