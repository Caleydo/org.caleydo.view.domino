/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.AnnotationItem;

/**
 * @author Samuel Gratzl
 *
 */
public class SetAnnotationCmd implements ICmd {
	private final AnnotationItem item;
	private final String text;

	public SetAnnotationCmd(AnnotationItem item, String text) {
		this.item = item;
		this.text = text;
	}

	@Override
	public String getLabel() {
		return "Edit text of annotation";
	}

	@Override
	public ICmd run(Domino domino) {
		String bak = item.getText();
		item.setText(text);
		return new SetAnnotationCmd(item, bak);
	}
}
