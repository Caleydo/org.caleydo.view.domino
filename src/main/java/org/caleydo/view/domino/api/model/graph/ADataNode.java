/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.graph;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.api.model.typed.TypedSet;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ADataNode extends ANode {
	private final TypedSet rec;
	private final TypedSet dim;
	protected boolean transposed = false;

	public ADataNode(String label, TypedSet dim, TypedSet rec) {
		this(label, dim, rec, false);
	}

	public ADataNode(String label, TypedSet dim, TypedSet rec, boolean transposed) {
		super(label);
		this.rec = rec;
		this.dim = dim;
		this.transposed = transposed;
	}

	public ADataNode(ADataNode parent, String label, TypedSet dim, TypedSet rec) {
		super(parent, label);
		this.rec = rec;
		this.dim = dim;
		this.transposed = parent.transposed;
	}

	/**
	 * @param clone
	 */
	public ADataNode(ADataNode clone) {
		super(clone);
		this.rec = clone.rec;
		this.dim = clone.dim;
		this.transposed = clone.transposed;
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
