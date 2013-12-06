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
public class TypedID implements IHasIDType {
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
	@Override
	public IDType getIdType() {
		return idType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TypedID [id=");
		builder.append(id);
		builder.append(", idType=");
		builder.append(idType);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((idType == null) ? 0 : idType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedID other = (TypedID) obj;
		if (id != other.id)
			return false;
		if (idType == null) {
			if (other.idType != null)
				return false;
		} else if (!idType.equals(other.idType))
			return false;
		return true;
	}

}
