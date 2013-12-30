/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;

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
	public IDragInfo startSWTDrag(IDragEvent event) {
		return new NodeDragInfo(event.getMousePos(), getNode());
	}

	@Override
	public String getLabel() {
		return getNode().getLabel();
	}

}
