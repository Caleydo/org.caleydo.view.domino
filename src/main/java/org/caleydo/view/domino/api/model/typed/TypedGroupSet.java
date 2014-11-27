/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.caleydo.view.domino.api.model.typed.util.BitSetSet;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroupSet extends TypedSet implements ITypedGroupCollection {
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
	@Override
	public List<TypedSetGroup> getGroups() {
		return groups;
	}

	public static TypedGroupSet createUngrouped(TypedSet set) {
		return new TypedGroupSet(TypedGroups.createUngroupedGroup(set));
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

	/**
	 * @param group
	 * @return
	 */
	public TypedGroupSet subSet(ITypedCollection sub) {
		assert sub.getIdType() == getIdType();
		final int ngroups = groups.size();
		if (ngroups == 1) {// just a single one
			TypedSetGroup first = groups.get(0);
			return new TypedGroupSet(new TypedSetGroup(sub.asSet(), first.getLabel(), first.getColor()));
		}
		List<Set<Integer>> gids = new ArrayList<>(ngroups);
		for (int i = 0; i < ngroups; ++i)
			gids.add(new BitSetSet());
		BitSetSet others = new BitSetSet();
		outer: for (Integer id : sub) {
			for (int i = 0; i < ngroups; ++i) {
				if (groups.get(i).contains(id)) {
					gids.get(i).add(id);
					continue outer;
				}
			}
			others.add(id);
		}
		List<TypedSetGroup> ggroups = new ArrayList<>(ngroups + 1);
		for (int i = 0; i < ngroups; ++i) {
			Set<Integer> ids = gids.get(i);
			if (ids.isEmpty())
				continue;
			TypedSetGroup g = groups.get(i);
			ggroups.add(new TypedSetGroup(new TypedSet(ids, g.getIdType()), g.getLabel(), g.getColor()));
		}
		if (!others.isEmpty()) {
			ggroups.add(TypedGroups.createUngroupedGroup(new TypedSet(others, getIdType())));
		}
		return new TypedGroupSet(ggroups);
	}


}
