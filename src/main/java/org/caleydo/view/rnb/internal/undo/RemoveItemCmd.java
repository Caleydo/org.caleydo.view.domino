/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.ui.AItem;

/**
 * @author Samuel Gratzl
 *
 */
public class RemoveItemCmd implements ICmd {
	private final AItem item;

	public RemoveItemCmd(AItem item) {
		this.item = item;
	}

	@Override
	public String getLabel() {
		return "Remove " + item.getClass().getSimpleName();
	}

	@Override
	public ICmd run(RnB domino) {
		domino.getOutlerBlocks().removeItem(item);
		domino.getBands().relayout();
		return new AddItemCmd(item);
	}

}
