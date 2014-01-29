/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import gleem.linalg.Vec2f;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;

/**
 * @author Samuel Gratzl
 *
 */
public class ZoomCmd implements ICmd {
	private final Node node;
	private final Vec2f shift;

	public ZoomCmd(Vec2f shift) {
		this(null, shift);
	}
	public ZoomCmd(Node node, Vec2f shift) {
		this.node = node;
		this.shift = shift;
	}

	@Override
	public String getLabel() {
		return "Zoom";
	}

	@Override
	public ICmd run(Domino domino) {
		if (node != null)
			node.getBlock().zoom(shift, node);
		else
			domino.zoom(shift);
		return new ZoomCmd(node, shift.times(-1));
	}

}
