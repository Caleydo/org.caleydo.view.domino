/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import org.caleydo.core.id.IDType;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedID {
	private final int id;
	private final IDType idType;

	public TypedID(int id, IDType idType) {
		this.id = id;
		this.idType = idType;
	}

	/**
	 * @return the id, see {@link #id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the idType, see {@link #idType}
	 */
	public IDType getIdType() {
		return idType;
	}
}
