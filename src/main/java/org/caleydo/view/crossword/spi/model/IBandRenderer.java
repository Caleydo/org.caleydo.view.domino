/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.spi.model;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.view.crossword.api.model.TypedSet;

/**
 * a renderer of a band including a label and the represented ids
 *
 * @author Samuel Gratzl
 *
 */
public interface IBandRenderer extends ILabeled {
	void render(GLGraphics g, float w, float h, IBandHost host);

	void renderPick(GLGraphics g, float w, float h, IBandHost host);

	/**
	 * the represented ids of this this band
	 *
	 * @return
	 */
	TypedSet getIds();

	/**
	 * return the id type represented by the {@link #getIds()}
	 * 
	 * @return
	 */
	IDType getIdType();

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
		int getSelected(TypedSet ids, SelectionType type);

		/**
		 * return the {@link IGLElementContext}
		 *
		 * @return
		 */
		IGLElementContext getContext();
	}
}

