/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.internal.Domino;
import org.caleydo.view.rnb.internal.ui.AItem;

/**
 * @author Samuel Gratzl
 *
 */
public class ZoomItemCmd implements IMergeAbleCmd {

	private final AItem item;
	private float shift;

	public ZoomItemCmd(AItem separator, float shift) {
		this.item = separator;
		this.shift = shift;
	}

	@Override
	public String getLabel() {
		return "Zoom " + item.getClass().getSimpleName();
	}

	@Override
	public ICmd run(Domino rnb) {
		item.zoom(shift);
		rnb.getBands().relayout();
		return new ZoomItemCmd(item, -shift);
	}

	/**
	 * @param undo
	 * @return
	 */
	@Override
	public boolean merge(ICmd cmd) {
		if (!(cmd instanceof ZoomItemCmd))
			return false;
		ZoomItemCmd undo = (ZoomItemCmd) cmd;
		if (undo.item == this.item) {
			this.shift += undo.shift;
			return true;
		}
		return false;
	}

}

