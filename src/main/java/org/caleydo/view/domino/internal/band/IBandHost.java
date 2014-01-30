/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.band;

import java.util.BitSet;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public interface IBandHost {
	/**
	 * return the number of elements that are selected according to the current {@link SelectionType}
	 * 
	 * @param ids
	 * @param type
	 * @return
	 */
	TypedSet getSelected(TypedSet ids, SelectionType type);

	boolean isSelected(TypedID id, SelectionType type);

	BitSet isSelected(TypedList ids, SelectionType type);

	public enum SourceTarget {
		SOURCE, TARGET;

		public float select(float source, float target) {
			return this == SOURCE ? source : target;
		}

		public int select(int source, int target) {
			return this == SOURCE ? source : target;
		}

		public <T> T select(T source, T target) {
			return this == SOURCE ? source : target;
		}
	}
}
