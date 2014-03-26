/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.toolbar;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.renderer.RoundedRectRenderer;
import org.caleydo.view.rnb.internal.Resources;
import org.caleydo.view.rnb.internal.UndoStack;
import org.caleydo.view.rnb.internal.ui.AItem;
import org.caleydo.view.rnb.internal.undo.RemoveItemCmd;
import org.caleydo.view.rnb.internal.undo.TransposeItemCmd;

/**
 * @author Samuel Gratzl
 *
 */
public class ItemTools extends AItemTools {

	private final AItem item;

	/**
	 * @param undo
	 */
	public ItemTools(UndoStack undo, AItem item) {
		super(undo);
		this.item = item;

		addButton("Transpose", Resources.ICON_TRANSPOSE);
		addButton("Remove", Resources.ICON_DELETE);

		setzDelta(2.f);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(Color.LIGHT_BLUE);
		RoundedRectRenderer.render(g, 0, 0, w, h, 5, 3, RoundedRectRenderer.FLAG_TOP | RoundedRectRenderer.FLAG_FILL);
		super.renderImpl(g, w, h);
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		switch (button.getTooltip()) {
		case "Transpose":
			undo.push(new TransposeItemCmd(item));
			break;
		case "Remove":
			undo.push(new RemoveItemCmd(item));
			break;
		default:
			break;
		}
	}

}
