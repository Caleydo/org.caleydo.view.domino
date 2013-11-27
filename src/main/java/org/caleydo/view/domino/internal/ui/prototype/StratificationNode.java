/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationNode implements INode {
	private final Perspective data;
	private final TypedSet ids;

	public StratificationNode(Perspective data) {
		this.data = data;
		this.ids = TypedSet.of(data.getVirtualArray());
	}

	/**
	 * @return the data, see {@link #data}
	 */
	public Perspective getData() {
		return data;
	}

	/**
	 * @return the ids, see {@link #ids}
	 */
	@Override
	public TypedSet getData(EDimension dim) {
		return dim.select(null, ids);
	}
}
