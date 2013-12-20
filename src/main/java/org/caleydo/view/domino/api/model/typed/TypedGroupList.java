/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model.typed;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.caleydo.core.util.color.Color;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class TypedGroupList extends TypedList implements ITypedCollection {

	private final List<TypedGroup> groups;

	public TypedGroupList(List<TypedGroup> groups) {
		super(new ConcatedList(groups), groups.get(0).getIdType());
		this.groups = groups;
	}

	private TypedGroupList(TypedList list, List<ITypedGroup> groups) {
		super(list, list.getIdType());
		this.groups = toGroups(list, groups);
	}

	/**
	 * @param list
	 * @param groups2
	 * @return
	 */
	private static List<TypedGroup> toGroups(TypedList list, List<ITypedGroup> groups) {
		List<TypedGroup> r = new ArrayList<>(groups.size());
		int i = 0;
		for(ITypedGroup g : groups) {
			r.add(new TypedGroup(list.subList(i, i+g.size()), g.getColor(),g.getLabel()));
			i+=g.size();
		}
		return ImmutableList.copyOf(r);
	}

	public static TypedGroupList createUngrouped(TypedList list) {
		return create(list, Collections.singletonList(createUngrouped(list.size())));
	}

	public static ITypedGroup createUngrouped(int size) {
		return new GroupDesc("Ungrouped", Color.NEUTRAL_GREY, size);
	}

	public static TypedGroupList create(TypedList list, List<ITypedGroup> groups) {
		return new TypedGroupList(list, groups);
	}

	/**
	 * @return the groups, see {@link #groups}
	 */
	public List<TypedGroup> getGroups() {
		return groups;
	}

	/**
	 * @author Samuel Gratzl
	 *
	 */
	private static final class ConcatedList extends AbstractList<Integer> {
		private final int[] ends;
		private final TypedList[] data;

		public ConcatedList(List<TypedGroup> groups) {
			ends = new int[groups.size()];
			data = new TypedList[ends.length];
			int c = 0;
			for (int i = 0; i < ends.length; ++i) {
				final TypedGroup group = groups.get(i);
				c += group.size();
				ends[i] = c;
				data[i] = group.asList();
			}
		}

		@Override
		public Iterator<Integer> iterator() {
			return Iterables.concat(data).iterator();
		}

		@Override
		public Integer get(int index) {
			for(int i = 0; i < ends.length; ++i) {
				if (index < ends[i]) {
					final TypedList l = data[i];
					return l.get(index - ends[i] - l.size());
				}
			}
			throw new IndexOutOfBoundsException();
		}

		@Override
		public int size() {
			return ends[ends.length - 1];
		}
	}

	public static final class GroupDesc implements ITypedGroup {

		private final String label;
		private final Color color;
		private final int size;

		public GroupDesc(String label, Color color, int size) {
			this.label = label;
			this.color = color;
			this.size = size;
		}

		/**
		 * @return the color, see {@link #color}
		 */
		@Override
		public Color getColor() {
			return color;
		}

		/**
		 * @return the label, see {@link #label}
		 */
		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public int size() {
			return size;
		}

	}
}
