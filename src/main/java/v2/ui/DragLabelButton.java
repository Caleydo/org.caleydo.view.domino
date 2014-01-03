/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.ui;

import org.caleydo.core.id.IDCategory;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;

import v2.Node;
import v2.data.LabelDataValues;

/**
 * @author Samuel Gratzl
 *
 */
public class DragLabelButton extends ADragButton {
	private final LabelDataValues data;

	public DragLabelButton(IDCategory category) {
		this.data = new LabelDataValues(category);
		setRenderer(GLRenderers.drawText("L", VAlign.CENTER));
		setTooltip(data.getLabel());
	}

	@Override
	protected Node createNode() {
		return new Node(data);
	}

}
