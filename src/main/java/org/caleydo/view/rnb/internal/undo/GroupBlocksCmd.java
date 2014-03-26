/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.internal.BlockGroup;
import org.caleydo.view.rnb.internal.RnB;

/**
 * @author Samuel Gratzl
 *
 */
public class GroupBlocksCmd implements ICmd {
	private final BlockGroup group;
	private boolean doGrouping;

	public GroupBlocksCmd(BlockGroup group, boolean doGrouping) {
		this.group = group;
		this.doGrouping = doGrouping;
	}

	@Override
	public String getLabel() {
		return "Group " + group + " together";
	}

	@Override
	public ICmd run(RnB rnb) {
		if (doGrouping) {
			group.group();
		} else {
			group.ungroup();
		}
		return new GroupBlocksCmd(group, !doGrouping);
	}
}
