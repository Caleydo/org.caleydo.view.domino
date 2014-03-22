/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.ui.Separator;

/**
 * @author Samuel Gratzl
 *
 */
public class RemoveSeparatorCmd implements ICmd {
	private final Separator separator;

	public RemoveSeparatorCmd(Separator separator) {
		this.separator = separator;
	}

	@Override
	public String getLabel() {
		return "Remove Separator";
	}

	@Override
	public ICmd run(Domino domino) {
		domino.getOutlerBlocks().removeSeparator(separator);
		return new AddSeparatorCmd(separator);
	}

}
