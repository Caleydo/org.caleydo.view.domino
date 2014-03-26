/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import org.caleydo.core.id.IDCategory;
import org.caleydo.view.rnb.internal.RnB;

/**
 * @author Samuel Gratzl
 *
 */
public class ZoomRulerCmd implements IMergeAbleCmd {

	private final IDCategory category;
	private float old;
	private float scale;

	public ZoomRulerCmd(IDCategory category, float scale, float old) {
		this.category = category;
		this.scale = scale;
		this.old = old;
	}

	@Override
	public String getLabel() {
		return "Zoom Ruler";
	}

	@Override
	public ICmd run(RnB domino) {
		domino.zoom(category, scale);
		return new ZoomRulerCmd(category, old, scale);
	}

	/**
	 * @param undo
	 * @return
	 */
	@Override
	public boolean merge(ICmd cmd) {
		if (!(cmd instanceof ZoomRulerCmd))
			return false;
		ZoomRulerCmd undo = (ZoomRulerCmd) cmd;
		if (undo.category == this.category) {
			this.scale = undo.scale;
			return true;
		}
		return false;
	}

}

