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
import java.util.Set;

import org.caleydo.view.domino.api.model.typed.util.BitSetSet;
import org.caleydo.view.domino.api.model.typed.util.ConcatedList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroupList extends TypedList implements ITypedGroupCollection {
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
		//more difficult to convert a group to a set, since duplicates in different groups may occur
		Set<Integer> acc = new BitSetSet();
		List<TypedSetGroup> groups = new ArrayList<>();
		for(TypedListGroup g : getGroups()) {
			TypedSetGroup s = g.asSet();
			if (!acc.isEmpty()) {
				s = new TypedSetGroup(ImmutableSet.copyOf(Sets.difference(s, acc)), s.getIdType(),s.getLabel(),s.getColor());
			}
			if (s.isEmpty())
				continue;
			acc.addAll(s);
			groups.add(s);
		}
		if (groups.isEmpty())
			return new TypedGroupSet(TypedGroups.createUngroupedGroup(TypedCollections.empty(getIdType())));
		return new TypedGroupSet(groups);
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
	@Override
	public List<TypedListGroup> getGroups() {
		return groups;
	}

	public TypedListGroup groupAt(int index) {
		int acc = 0;
		for (TypedListGroup group : groups) {
			acc += group.size();
			if (acc > index)
				return group;
		}
		return null;
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
