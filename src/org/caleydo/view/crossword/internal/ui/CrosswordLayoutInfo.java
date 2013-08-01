/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IHasGLLayoutData;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.IActiveChangedCallback;

/**
 * layout specific information
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordLayoutInfo implements IActiveChangedCallback {
	private final CrosswordElement parent;

	private float zoomFactorX = 1.0f;
	private float zoomFactorY = 1.0f;

	/**
	 * @param crosswordElement
	 */
	public CrosswordLayoutInfo(CrosswordElement parent) {
		this.parent = parent;
	}

	/**
	 * @param zoomFactor
	 *            setter, see {@link zoomFactor}
	 */
	public boolean setZoomFactor(float zoomFactorX, float zoomFactorY) {
		if (this.zoomFactorX == zoomFactorX && this.zoomFactorY == zoomFactorY)
			return false;
		this.zoomFactorX = zoomFactorX;
		this.zoomFactorY = zoomFactorY;
		parent.getParent().relayout();
		return true;
	}

	@Override
	public void onActiveChanged(int active) {
		// reset to a common zoom factor
		float s = Math.min(zoomFactorX, zoomFactorY);
		if (!setZoomFactor(s, s))
			parent.getParent().relayout(); // the min size may have changed
	}

	/**
	 * @return the zoomFactor, see {@link #zoomFactor}
	 */
	public float getZoomFactorX() {
		return zoomFactorX;
	}

	/**
	 * @return the zoomFactorY, see {@link #zoomFactorY}
	 */
	public float getZoomFactorY() {
		return zoomFactorY;
	}

	/**
	 * @param factor
	 */
	public void zoom(double factor) {
		if (factor == 1.0f || Double.isNaN(factor) || Double.isInfinite(factor) || factor <= 0)
			return;
		this.zoomFactorX = zoomFactorX * (float) factor;
		this.zoomFactorY = zoomFactorY * (float) factor;
		parent.getParent().relayout();
	}


		/**
	 * enlarge the view by moving and rescaling
	 *
	 * @param x
	 *            the dx
	 * @param xDir
	 *            the direction -1 to the left +1 to the right 0 nothing
	 * @param y
	 *            the dy
	 * @param yDir
	 */
	public void enlarge(float x, int xDir, float y, int yDir) {
		Vec2f size = parent.getSize();
		Vec2f loc = parent.getLocation();
		float sx = size.x() + xDir * x;
		float sy = size.y() + yDir * y;
		// convert to scale factor
		sx -= 2; //borders and buttons
		sy -= 16+2;
		Vec2f minSize = getMinSize(parent);
		setZoomFactor(sx / minSize.x(), sy / minSize.y());
		parent.setLocation(loc.x() + (xDir < 0 ? x : 0), loc.y() + (yDir < 0 ? y : 0));
	}

	Vec2f getLocation(IGLLayoutElement elem) {
		return elem.getSetLocation().copy();
	}

	Vec2f getMinSize(IHasGLLayoutData elem) {
		IHasMinSize minSize = elem.getLayoutDataAs(IHasMinSize.class, null);
		if (minSize != null)
			return minSize.getMinSize();
		return elem.getLayoutDataAs(Vec2f.class, new Vec2f(100, 100));
	}

	void scale(Vec2f size) {
		size.setX(size.x() * zoomFactorX);
		size.setY(size.y() * zoomFactorY);
		size.setX(size.x() + 2);
		size.setY(size.y() + 16 + 2); // for buttons and border
	}

	void setBounds(IGLLayoutElement elem, Vec2f loc, Vec2f size) {
		elem.setBounds(loc.x(), loc.y(), size.x(), size.y());
	}
}
