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


/**
 * layout specific information
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordLayoutInfo {
	private final CrosswordElement parent;

	private double zoomFactor = 1.0f;

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
	public void setZoomFactor(double zoomFactor) {
		if (this.zoomFactor == zoomFactor)
			return;
		this.zoomFactor = zoomFactor;
		parent.getParent().relayout();
	}

	/**
	 * @return the zoomFactor, see {@link #zoomFactor}
	 */
	public double getZoomFactor() {
		return zoomFactor;
	}
}
