/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation.ILocator;

import v2.band.EBandMode;

/**
 * @author Samuel Gratzl
 *
 */
public interface INodeLocator {
	GLLocation apply(EBandMode mode, int index);
}

class NodeLocator implements INodeLocator {
	private final GLLocation node;
	private final ILocator groupLocator;
	private final ILocator detailLocator;

	public NodeLocator(GLLocation node, ILocator groupLocator, ILocator detailLocator) {
		this.node = node;
		this.groupLocator = groupLocator;
		this.detailLocator = detailLocator;
	}

	@Override
	public GLLocation apply(EBandMode mode, int index) {
		switch (mode) {
		case OVERVIEW:
			return node;
		case GROUPS:
			return groupLocator.apply(index);
		case DETAIL:
			return detailLocator.apply(index);
		}
		throw new IllegalStateException();
	}

}