/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;

import v2.data.IDataValues;


/**
 * @author Samuel Gratzl
 *
 */
public class SingleNodeGroup extends NodeGroup {

	public SingleNodeGroup(Node parent, IDataValues data) {
		super(parent, data);
	}

	@Override
	public String getLabel() {
		return getNode().getLabel();
	}

	@Override
	public void onDropped(IDnDItem info) {
		// nothing
	}

	@Override
	public boolean canBeRemoved() {
		return false;
	}
}
