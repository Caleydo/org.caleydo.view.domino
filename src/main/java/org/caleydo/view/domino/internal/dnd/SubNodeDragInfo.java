/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.view.domino.api.model.typed.ITypedCollection;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class SubNodeDragInfo extends NodeDragInfo {
	private final String label;
	private final ITypedCollection dimData;
	private final ITypedCollection recData;

	public SubNodeDragInfo(INode node, String label, ITypedCollection dimData, ITypedCollection recData, Vec2f mousePos) {
		super(node, mousePos);
		this.label = label;
		this.dimData = dimData;
		this.recData = recData;
	}

	@Override
	public INode apply(EDnDType type) {
		return node.extract(label, dimData, recData);
	}
}
