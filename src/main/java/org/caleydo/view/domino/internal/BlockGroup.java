/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.caleydo.core.data.selection.SelectionType;

/**
 * @author Samuel Gratzl
 *
 */
public class BlockGroup implements Iterable<Block> {
	private Set<Block> blocks = new HashSet<>();

	public void group() {
		for (Block b : blocks)
			b.groupBy(this);
	}

	public void ungroup() {
		for (Block b : blocks)
			b.ungroupBy(this);
	}

	@Override
	public Iterator<Block> iterator() {
		return blocks.iterator();
	}

	/**
	 * @param selections
	 */
	public void selectAllBlocks(NodeSelections selections) {
		for (Block b : blocks)
			selections.select(SelectionType.SELECTION, b, true);
	}

}
