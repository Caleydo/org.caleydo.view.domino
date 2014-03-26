/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import gleem.linalg.Vec2f;

import java.util.Objects;

import org.caleydo.view.rnb.internal.RnB;
import org.caleydo.view.rnb.internal.Node;

/**
 * @author Samuel Gratzl
 *
 */
public class ZoomCmd implements IMergeAbleCmd {
	private final Node node;
	private final Vec2f shift;
	private final Vec2f mousePos;

	public ZoomCmd(Vec2f shift, Vec2f mousePos) {
		this(null, shift, mousePos);
	}

	public ZoomCmd(Node node, Vec2f shift, Vec2f mousePos) {
		this.node = node;
		this.shift = shift;
		this.mousePos = mousePos;
	}

	@Override
	public String getLabel() {
		return "Zoom";
	}

	@Override
	public ICmd run(RnB domino) {
		if (node != null)
			node.getBlock().zoom(shift, node);
		else
			domino.zoom(shift, mousePos);
		return new ZoomCmd(node, shift.times(-1), mousePos);
	}

	/**
	 * @param undo
	 * @return
	 */
	@Override
	public boolean merge(ICmd cmd) {
		if (!(cmd instanceof ZoomCmd))
			return false;
		ZoomCmd undo = (ZoomCmd) cmd;
		if (undo.node == this.node && similar(mousePos, undo.mousePos)) {
			this.shift.add(undo.shift);
			return true;
		}
		return false;
	}

	private static boolean similar(Vec2f a, Vec2f b) {
		if (Objects.equals(a, b))
			return true;
		// within 5 pixel distance;
		return a.minus(b).lengthSquared() < 5 * 5;
	}

}
