/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.renderer.RoundedRectRenderer;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.ui.Separator;
import org.caleydo.view.domino.internal.undo.RemoveSeparatorCmd;
import org.caleydo.view.domino.internal.undo.TransposeSeparatorCmd;

/**
 * @author Samuel Gratzl
 *
 */
public class SeparatorTools extends AItemTools {

	private final Separator separator;

	/**
	 * @param undo
	 */
	public SeparatorTools(UndoStack undo, Separator separator) {
		super(undo);
		this.separator = separator;

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
			undo.push(new TransposeSeparatorCmd(separator));
			break;
		case "Remove":
			undo.push(new RemoveSeparatorCmd(separator));
			break;
		default:
			break;
		}
	}

}
