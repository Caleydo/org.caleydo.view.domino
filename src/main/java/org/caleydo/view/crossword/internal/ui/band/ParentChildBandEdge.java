/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.band;

import java.util.Set;

import org.caleydo.core.util.color.Color;

/**
 * @author Samuel Gratzl
 *
 */
public class ParentChildBandEdge extends ABandEdge {
	private static final long serialVersionUID = -5991401091218527493L;

	public ParentChildBandEdge(boolean sHor, int startIndex, boolean tHor) {
		super(new ParentConnector(sHor, startIndex), new Connector(tHor), new Color(0.9f, 0f, 0f, 0.5f));
	}

	private static class ParentConnector extends Connector {
		private final int startIndex;

		public ParentConnector(boolean horizontal, int startIndex) {
			super(horizontal);
			this.startIndex = startIndex;
		}

		@Override
		protected float offsetPercentage(Set<Integer> ids, Set<Integer> overlap) {
			return ((float) startIndex / ids.size());
		}
	}

}
