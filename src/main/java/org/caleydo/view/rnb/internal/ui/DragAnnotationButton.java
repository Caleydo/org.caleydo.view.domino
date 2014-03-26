/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.ui;

import org.caleydo.view.rnb.internal.Resources;
import org.caleydo.view.rnb.internal.UndoStack;

/**
 * @author Samuel Gratzl
 *
 */
public class DragAnnotationButton extends ADragItemButton {

	public DragAnnotationButton() {
		super(Resources.ICON_ANNOTATION, "Add Annotation Text Field");
	}

	@Override
	protected AItem createInstance(UndoStack undo) {
		return new AnnotationItem(undo);
	}
}
