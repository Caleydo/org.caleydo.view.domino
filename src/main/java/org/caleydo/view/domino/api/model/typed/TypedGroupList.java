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

import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;

import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroupList extends TypedList implements ITypedCollection {
	private static final String UNMAPPED = "Unmapped";
	private static final String UNGROUPED = "Ungrouped";

	private final List<TypedListGroup> groups;

	public TypedGroupList(List<TypedListGroup> groups) {
		super(new ConcatedList<>(groups), groups.get(0).getIdType());
		this.groups = groups;
	}

	private TypedGroupList(TypedList list, List<? extends ITypedGroup> groups) {
		super(list, list.getIdType());
		this.groups = toGroups(list, groups);
	}

	/**
	 * @param list
	 * @param groups2
	 * @return
	 */
	private static List<TypedListGroup> toGroups(TypedList list, List<? extends ITypedGroup> groups) {
		List<TypedListGroup> r = new ArrayList<>(groups.size());
		int i = 0;
		for(ITypedGroup g : groups) {
			r.add(new TypedListGroup(list.subList(i, i + g.size()), g.getLabel(), g.getColor()));
			i+=g.size();
		}
		return ImmutableList.copyOf(r);
	}

	public static TypedGroupList createUngrouped(TypedList list) {
		return create(list, Collections.singletonList(new TypedListGroup(list, UNGROUPED, Color.NEUTRAL_GREY)));
	}

	public static TypedListGroup createUnmappedGroup(IDType idType, int size) {
		return new TypedListGroup(new TypedList(new RepeatingList<>(TypedCollections.INVALID_ID, size), idType),
				UNMAPPED, Color.NEUTRAL_GREY);
	}

	public static TypedListGroup createUngroupedGroup(IDType idType, int size) {
		return new TypedListGroup(new TypedList(new RepeatingList<>(TypedCollections.INVALID_ID, size), idType),
				UNGROUPED, Color.NEUTRAL_GREY);
	}

	public static TypedListGroup createUngroupedGroup(TypedList list) {
		return new TypedListGroup(list, UNGROUPED, Color.NEUTRAL_GREY);
	}

	public static TypedSetGroup createUngroupedGroup(TypedSet set) {
		return new TypedSetGroup(set, UNGROUPED, Color.NEUTRAL_GREY);
	}

	public static boolean isUngrouped(ITypedGroup group) {
		return group.getLabel() == UNGROUPED;
	}

	public static boolean isUnmapped(ITypedGroup group) {
		return group.getLabel() == UNMAPPED;
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
