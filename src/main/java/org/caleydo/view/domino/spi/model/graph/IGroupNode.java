/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.spi.model.graph;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.view.domino.api.model.typed.ITypedGroup;

/**
 * @author Samuel Gratzl
 *
 */
public interface IGroupNode extends INode {
	INode getParent();

	ITypedGroup getGroup(EDimension dim);
}
