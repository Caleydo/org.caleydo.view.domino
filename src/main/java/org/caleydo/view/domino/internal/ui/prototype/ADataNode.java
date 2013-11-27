/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.api.model.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ADataNode extends ANode {
	private final TypedSet rec;
	private final TypedSet dim;
	private boolean transposed = false;

	public ADataNode(TypedSet dim, TypedSet rec) {
		this.rec = rec;
		this.dim = dim;
	}

	@Override
	public final void transpose() {
		propertySupport.firePropertyChange(PROP_TRANSPOSE, this.transposed, this.transposed = !this.transposed);
	}

	@Override
	public final TypedSet getData(EDimension dim) {
		return dim.isVertical() == this.transposed ? this.dim : this.rec;
	}
}
