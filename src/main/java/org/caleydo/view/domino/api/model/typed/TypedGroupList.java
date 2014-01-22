/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.caleydo.view.domino.api.model.typed.util.ConcatedList;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroupList extends TypedList {
	private final List<TypedListGroup> groups;

	public TypedGroupList(List<TypedListGroup> groups) {
		super(new ConcatedList<>(groups), groups.get(0).getIdType());
		this.groups = groups;
	}

	private TypedGroupList(TypedList list, List<? extends ITypedGroup> groups) {
		super(list, list.getIdType());
		this.groups = toGroups(list, groups);
	}

	@Override
	public TypedGroupList asList() {
		return this;
	}

	@Override
	public TypedGroupSet asSet() {
		return new TypedGroupSet(ImmutableList.copyOf(Lists.transform(groups,
				new Function<TypedListGroup, TypedSetGroup>() {
					@Override
					public TypedSetGroup apply(TypedListGroup input) {
						return input.asSet();
					}
				})));
	}

	/**
	 * @param list
	 * @param groups2
	 * @return
	 */
	private static List<TypedListGroup> toGroups(TypedList list, List<? extends ITypedGroup> groups) {
		List<TypedListGroup> r = new ArrayList<>(groups.size());
		int i = 0;
		for (ITypedGroup g : groups) {
			r.add(new TypedListGroup(list.subList(i, i + g.size()), g.getLabel(), g.getColor()));
			i += g.size();
		}
		return ImmutableList.copyOf(r);
	}

	public static TypedGroupList createUngrouped(TypedList list) {
		return create(list, Collections.singletonList(TypedGroups.createUngroupedGroup(list)));
	}

	public static TypedGroupList create(TypedList list, List<? extends ITypedGroup> groups) {
		return new TypedGroupList(list, groups);
	}

	/**
	 * @return the groups, see {@link #groups}
	 */
	public List<TypedListGroup> getGroups() {
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
		TypedGroupList other = (TypedGroupList) obj;
		return Objects.equals(groups, other.groups);
	}

}
