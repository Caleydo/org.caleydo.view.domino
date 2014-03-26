/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import org.caleydo.view.rnb.internal.Node;
import org.caleydo.view.rnb.internal.RnB;

/**
 * @author Samuel Gratzl
 *
 */
public class TransposeNodeViewCmd implements ICmd {
	private final Node node;

	public TransposeNodeViewCmd(Node node) {
		this.node = node;
	}

	@Override
	public String getLabel() {
		return "Transpose";
	}

	@Override
	public ICmd run(RnB rnb) {
		node.transposeView();
		return this;
	}

}
