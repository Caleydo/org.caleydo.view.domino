/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * @author Samuel Gratzl
 *
 */
public class OffsetShifts {
	/**
	 * neighbor space info: n (west,north) n x offset,shift
	 */
	private final Table<Node, Node, Vec2f> offsets = HashBasedTable.create();

	public float getOffset(Node a, Node b) {
		Vec2f v = get(a, b);
		return v == null ? 0 : v.x();
	}

	public float getShift(Node a, Node b) {
		Vec2f v = get(a, b);
		return v == null ? 0 : v.y();
	}

	public void setOffset(Node a, Node b, float offset) {
		if (a == null || b == null)
			return;
		Vec2f v = get(a, b);
		if (v == null)
			v = new Vec2f(0, 0);
		v.setX(offset);
		set(a, b, v);
	}

	public void setShift(Node a, Node b, float shift) {
		if (a == null || b == null)
			return;
		Vec2f v = get(a, b);
		if (v == null)
			v = new Vec2f(0, 0);
		v.setY(shift);
		set(a, b, v);
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	private Vec2f get(Node a, Node b) {
		if (a == null || b == null)
			return null;
		if (a.getID() < b.getID())
			return offsets.get(a, b);
		return offsets.get(b, a);
	}

	/**
	 * @param a
	 * @param b
	 * @param v
	 */
	private void set(Node a, Node b, Vec2f v) {
		boolean del = v.x() == 0 && v.y() == 0;
		if (a.getID() < b.getID()) {
			if (del)
				offsets.remove(a, b);
			else
				offsets.put(a, b, v);
		} else {
			if (del)
				offsets.remove(b, a);
			else
				offsets.put(b, a, v);
		}
	}

	/**
	 * @param node
	 * @param n
	 */
	public void remove(Node a, Node b) {
		if (a == null || b == null)
			return;
		if (a.getID() < b.getID()) {
			offsets.remove(a, b);
		} else {
			offsets.remove(b, a);
		}
	}
}
