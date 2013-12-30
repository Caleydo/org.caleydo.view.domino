/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroupSet extends TypedSet {
	private final List<TypedSetGroup> groups;

	public TypedGroupSet(List<TypedSetGroup> groups) {
		super(union(groups), groups.get(0).getIdType());
		this.groups = groups;
	}

	public TypedGroupSet(TypedSetGroup... groups) {
		this(Arrays.asList(groups));
	}

	@Override
	public TypedGroupList asList() {
		return new TypedGroupList(ImmutableList.copyOf(Lists.transform(groups,
				new Function<TypedSetGroup, TypedListGroup>() {
					@Override
					public TypedListGroup apply(TypedSetGroup input) {
						return input.asList();
					}
				})));
	}

	@Override
	public TypedGroupSet asSet() {
		return this;
	}

	/**
	 * @return the groups, see {@link #groups}
	 */
	public List<TypedSetGroup> getGroups() {
		return groups;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedGroupSet other = (TypedGroupSet) obj;
		return Objects.equals(groups, other.groups);
	}


}
