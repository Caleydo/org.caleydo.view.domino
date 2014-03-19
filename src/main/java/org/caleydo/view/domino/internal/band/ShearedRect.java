/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.domino.internal.band;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.geom.Rect;

/**
 * a custom implementation of a rect to avoid awt
 *
 * @author Samuel Gratzl
 *
 */
public final class ShearedRect implements Cloneable {
	private float x, y, x2, y2;
	private float shearX, shearY;

	public ShearedRect() {

	}

	public ShearedRect(float x, float y, float x2, float y2, float shearX, float shearY) {
		this.x = x;
		this.y = y;
		this.x2 = x2;
		this.y2 = y2;
		this.shearX = shearX;
		this.shearY = shearY;
	}

	/**
	 * @param detachedRectBounds
	 */
	public ShearedRect(Rect r) {
		this(r.x(), r.y(), r.x2(), r.y2(), 0, 0);
	}

	/**
	 * @return the x, see {@link #x}
	 */
	public float x() {
		return x;
	}

	/**
	 * @return the {@link #x}+{@link #width()}
	 */
	public float x2() {
		return x2;
	}

	/**
	 * @return the y, see {@link #y}
	 */
	public float y() {
		return y;
	}

	/**
	 * @return {@link #y}+{@link #height()}
	 */
	public float y2() {
		return y2;
	}


	/**
	 * @return the width, see {@link #width}
	 */
	public float width() {
		return x2 - x;
	}


	/**
	 * @return the height, see {@link #height}
	 */
	public float height() {
		return y2 - y + shearY;
	}

	public Vec2f xy() {
		return new Vec2f(x, y);
	}

	public Vec2f xy2() {
		return new Vec2f(x - shearX, y2 - shearY);
	}

	public Vec2f x2y2() {
		return new Vec2f(x2, y2);
	}

	public Vec2f x2y() {
		return new Vec2f(x2 - shearX, y - shearY);
	}

	public Vec2f size() {
		return new Vec2f(width(), height());
	}

	public ShearedRect rot90() {
		return new ShearedRect(y, x, y + (y2 - y), x + (x2 - x), shearY, shearX);
	}

	@Override
	public ShearedRect clone() {
		try {
			return (ShearedRect) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(y2);
		result = prime * result + Float.floatToIntBits(x2);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShearedRect other = (ShearedRect) obj;
		if (Float.floatToIntBits(x2) != Float.floatToIntBits(other.x2))
			return false;
		if (Float.floatToIntBits(y2) != Float.floatToIntBits(other.y2))
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShearedRect(").append(x).append(',');
		builder.append(y).append(',');
		builder.append(x2).append(',');
		builder.append(y2).append(')');
		return builder.toString();
	}
}
