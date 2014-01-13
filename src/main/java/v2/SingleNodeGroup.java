/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;


/**
 * @author Samuel Gratzl
 *
 */
public class SingleNodeGroup extends NodeGroup {

	public SingleNodeGroup(Node parent) {
		super(parent);
	}

	@Override
	public String getLabel() {
		return getNode().getLabel();
	}

	@Override
	public void onDropped(IDnDItem info) {
		getNode().showAgain();
	}

	@Override
	public boolean canBeRemoved() {
		return false;
	}
}
