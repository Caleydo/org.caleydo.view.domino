/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.event;

import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.core.id.IDType;

/**
 * @author Samuel Gratzl
 *
 */
public class ChangePerspectiveEvent extends ADirectedEvent {

	private final IDType idType;
	private final Perspective new_;

	public ChangePerspectiveEvent(IDType idType, Perspective to) {
		this.idType = idType;
		this.new_ = to;
	}

	/**
	 * @return the idType, see {@link #idType}
	 */
	public IDType getIdType() {
		return idType;
	}

	/**
	 * @return the new_, see {@link #new_}
	 */
	public Perspective getNew_() {
		return new_;
	}
}
