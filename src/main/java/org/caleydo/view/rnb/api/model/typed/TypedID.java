/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.api.model.typed;

import static org.caleydo.view.rnb.api.model.typed.TypedCollections.INVALID_ID;

import java.util.Objects;

import org.caleydo.core.id.IDType;

import com.google.common.base.Function;

/**
 * a typed id is an integer id along with its idtype
 * 
 * @author Samuel Gratzl
 * 
 */
public class TypedID implements IHasIDType {
	public static final Function<TypedID, Integer> TO_ID = new Function<TypedID, Integer>() {
		@Override
		public Integer apply(TypedID input) {
			return input == null ? null : input.getId();
		}
	};
	public static final Function<IHasIDType, IDType> TO_IDTYPE = TypedCollections.TO_IDTYPE;


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
		return Objects.equals(idType, other.idType);
	}

	public static Function<Integer, TypedID> toTypedId(final IDType idType) {
		return new Function<Integer, TypedID>() {
			@Override
			public TypedID apply(Integer input) {
				return new TypedID(input == null ? INVALID_ID : input.intValue(), idType);
			}
		};
	}

}
