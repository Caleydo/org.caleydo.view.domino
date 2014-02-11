/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.ui.Ruler;
import org.caleydo.view.domino.internal.undo.RemoveRulerCmd;
import org.caleydo.view.domino.internal.undo.SetRulerMaxCmd;
import org.caleydo.view.domino.internal.undo.TransposeRulerCmd;

/**
 * @author Samuel Gratzl
 *
 */
public class RulerTools extends AItemTools {

	private final Ruler ruler;

	/**
	 * @param undo
	 */
	public RulerTools(UndoStack undo, Ruler ruler) {
		super(undo);
		this.ruler = ruler;

		addButton("Set size to currently visible count", Resources.ICON_LIMIT_DATA_REC);
		addButton("Set size to 100", Resources.ICON_LIMIT_DATA_REC);
		addButton("Transpose", Resources.ICON_TRANSPOSE);
		addButton("Remove", Resources.ICON_DELETE);
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		switch (button.getTooltip()) {
		case "Transpose":
			undo.push(new TransposeRulerCmd(ruler));
			break;
		case "Remove":
			undo.push(new RemoveRulerCmd(ruler));
			break;
		case "Set size to currently visible count":
			undo.push(new SetRulerMaxCmd(ruler, findParent(Domino.class).getVisibleItemCount(ruler.getIDCategory())));
			break;
		case "Set size to 100":
			undo.push(new SetRulerMaxCmd(ruler, 100));
			break;
		default:
			break;
		}
	}

}
