/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;



/**
 * @author Samuel Gratzl
 *
 */
public class SingleNodeGroup extends NodeGroup {

	public SingleNodeGroup(Node parent) {
		super(parent);
	}

	@Override
	public boolean canBeRemoved() {
		return false;
	}
}
