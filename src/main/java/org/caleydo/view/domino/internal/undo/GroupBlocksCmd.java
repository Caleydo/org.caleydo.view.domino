/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.BlockGroup;
import org.caleydo.view.domino.internal.Domino;

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
	public ICmd run(Domino domino) {
		if (doGrouping) {
			group.group();
		} else {
			group.ungroup();
		}
		return new GroupBlocksCmd(group, !doGrouping);
	}
}
