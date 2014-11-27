/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.undo;

import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;

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
	public ICmd run(Domino domino) {
		node.transposeView();
		return this;
	}

}
