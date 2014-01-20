/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.dnd;

import gleem.linalg.Vec2f;
import v2.Block;

/**
 * @author Samuel Gratzl
 *
 */
public class BlockDragInfo extends ADragInfo {
	private final Block block;

	public BlockDragInfo(Vec2f mousePos, Block block) {
		super(mousePos);
		this.block = block;
	}

	/**
	 * @return the block, see {@link #block}
	 */
	public Block getBlock() {
		return block;
	}

	@Override
	public String getLabel() {
		return block.getLabel();
	}

	@Override
	protected Vec2f getSize() {
		return block.getSize();
	}

}
