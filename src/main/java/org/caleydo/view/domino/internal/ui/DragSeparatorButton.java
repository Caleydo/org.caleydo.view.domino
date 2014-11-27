/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.UndoStack;

/**
 * @author Samuel Gratzl
 *
 */
public class DragSeparatorButton extends ADragItemButton {

	public DragSeparatorButton() {
		super(Resources.ICON_SEPARATOR, "Add Separator Line");
	}

	@Override
	protected AItem createInstance(UndoStack undo) {
		return new SeparatorItem(undo);
	}
}
