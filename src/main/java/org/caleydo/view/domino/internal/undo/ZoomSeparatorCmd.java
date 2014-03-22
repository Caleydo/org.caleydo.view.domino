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
public class ZoomSeparatorCmd implements IMergeAbleCmd {

	private final Separator separator;
	private float shift;

	public ZoomSeparatorCmd(Separator separator, float shift) {
		this.separator = separator;
		this.shift = shift;
	}

	@Override
	public String getLabel() {
		return "Zoom Separator";
	}

	@Override
	public ICmd run(Domino domino) {
		separator.zoom(shift);
		domino.getBands().relayout();
		return new ZoomSeparatorCmd(separator, -shift);
	}

	/**
	 * @param undo
	 * @return
	 */
	@Override
	public boolean merge(ICmd cmd) {
		if (!(cmd instanceof ZoomSeparatorCmd))
			return false;
		ZoomSeparatorCmd undo = (ZoomSeparatorCmd) cmd;
		if (undo.separator == this.separator) {
			this.shift += undo.shift;
			return true;
		}
		return false;
	}

}

