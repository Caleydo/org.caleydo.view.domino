/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.spi.model;

import java.util.BitSet;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * a renderer of a band including a label and the represented ids
 *
 * @author Samuel Gratzl
 *
 */
public interface IBandRenderer extends ILabeled {
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

	void render(GLGraphics g, float w, float h, IBandHost host);

	void renderPick(GLGraphics g, float w, float h, IBandHost host);

	/**
	 * the represented ids of this this band
	 *
	 * @return
	 */
	TypedSet getIds(SourceTarget type);

	/**
	 * return the id type represented by the {@link #getIds()}
	 *
	 * @return
	 */
	IDType getIdType(SourceTarget type);

	/**
	 * the host of the band
	 *
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

		/**
		 * return the {@link IGLElementContext}
		 *
		 * @return
		 */
		IGLElementContext getContext();
	}
}

