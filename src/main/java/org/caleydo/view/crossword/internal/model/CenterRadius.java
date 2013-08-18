/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.model;

/**
 * @author Samuel Gratzl
 *
 */
public final class CenterRadius {
	private final float center;
	private final float radius;

	/**
	 * @param center
	 * @param radius
	 */
	public CenterRadius(float center, float radius) {
		this.center = center;
		this.radius = radius;
	}

	/**
	 * @return the center, see {@link #center}
	 */
	public float getCenter() {
		return center;
	}

	/**
	 * @return the radius, see {@link #radius}
	 */
	public float getRadius() {
		return radius;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(center);
		result = prime * result + Float.floatToIntBits(radius);
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
		CenterRadius other = (CenterRadius) obj;
		if (Float.floatToIntBits(center) != Float.floatToIntBits(other.center))
			return false;
		if (Float.floatToIntBits(radius) != Float.floatToIntBits(other.radius))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CenterRadius [center=");
		builder.append(center);
		builder.append(", radius=");
		builder.append(radius);
		builder.append("]");
		return builder.toString();
	}

}
