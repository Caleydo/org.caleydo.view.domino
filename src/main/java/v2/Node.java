/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;

import v2.data.IDataValues;

/**
 * @author Samuel Gratzl
 *
 */
public class Node extends GLElementContainer implements IGLLayout2, ILabeled {
	private Node[] neighbors = new Node[4];

	private final String label;
	private final IDataValues data;

	private TypedGroupList dimData;
	private TypedGroupSet dimGroups;

	private TypedGroupList recData;
	private TypedGroupSet recGroups;

	public Node(IDataValues data) {
		this(data, data.getLabel(), data.getDefaultGroups(EDimension.DIMENSION), data
				.getDefaultGroups(EDimension.RECORD));
	}

	public Node(Node clone) {
		this.data = clone.data;
		this.label = clone.label;
		setLayout(this);
		this.dimGroups = clone.dimGroups;
		this.recGroups = clone.recGroups;
		setData(clone.dimData, clone.recData);
	}

	public Node(IDataValues data, String label, TypedGroupSet dimGroups, TypedGroupSet recGroups) {
		this.data = data;
		this.label = label;
		setLayout(this);
		this.dimGroups = dimGroups;
		this.recGroups = recGroups;
		setData(dimGroups.asList(), recGroups.asList());
	}

	/**
	 * @return the label, see {@link #label}
	 */
	@Override
	public String getLabel() {
		return label;
	}

	public void setData(EDimension dim, TypedGroupList data) {
		setData(dim.select(data, dimData), dim.select(recData, data));
	}

	public void setData(TypedGroupList dimData, TypedGroupList recData) {
		this.dimData = dimData;
		this.recData = recData;
		updateGroupNodes(dimData, recData);
	}

	private void updateGroupNodes(TypedGroupList dimData, TypedGroupList recData) {
		final List<TypedListGroup> dimGroups = dimData.getGroups();
		final List<TypedListGroup> recGroups = recData.getGroups();

		if (dimGroups.size() == 1 && recGroups.size() == 1) {
			this.clear();
			SingleNodeGroup single = new SingleNodeGroup(this, data);
			single.setData(dimGroups.get(0), recGroups.get(0));
			this.add(single);
		} else if (size() == 1)
			this.clear();// maybe was a single

		int n = 0;
		List<NodeGroup> left = new ArrayList<>();
		for (TypedListGroup dimGroup : dimGroups) {
			NodeGroup above = null;
			int i = 0;
			for (TypedListGroup recGroup : recGroups) {
				final NodeGroup child = getOrCreate(n);
				n++;
				child.setData(dimGroup, recGroup);
				child.setNeighbor(EDirection.ABOVE,above);
				if (above != null)
					above.setNeighbor(EDirection.BELOW, child);
				above = child;
				if (left.size() > i) {
					left.get(i).setNeighbor(EDirection.RIGHT_OF, child);
					child.setNeighbor(EDirection.LEFT_OF, left.get(i));
				}
				if (i < left.size())
					left.set(i++, child);
				else
					left.add(i++, child);

			}
		}
		this.asList().subList(n, size()).clear(); // clear rest
		setSize(dimData.size(), recData.size());
	}

	public void removeGroup(NodeGroup group) {
		final List<TypedListGroup> dimGroups = dimData.getGroups();
		final List<TypedListGroup> recGroups = recData.getGroups();
		if (dimGroups.size() > 1 && recGroups.size() > 1)
			return; // can't remove 2d groupings
		// FIXME
	}

	/**
	 * @param n
	 * @return
	 */
	private NodeGroup getOrCreate(int n) {
		if (n < size())
			return (NodeGroup) get(n);
		NodeGroup g = new NodeGroup(this, data);
		this.add(g);
		return g;
	}

	public void setNeighbor(EDirection dir, Node neighbor) {
		Node bak = this.neighbors[dir.ordinal()];
		if (bak == neighbor)
			return;
		this.neighbors[dir.ordinal()] = neighbor;

		List<NodeGroup> myGroups = getGroupNeighbors(dir);
		List<NodeGroup> neighborGroups = neighbor == null ? Collections.<NodeGroup> emptyList() : neighbor
				.getGroupNeighbors(dir.opposite());
		if (myGroups.size() == neighborGroups.size()) {
			for (int i = 0; i < myGroups.size(); ++i) {
				myGroups.get(i).setNeighbor(dir, neighborGroups.get(i));
			}
		} else {
			for (NodeGroup g : myGroups) {
				g.setNeighbor(dir, null);
			}
		}

		// symmetric
		if (neighbor != null)
			neighbor.setNeighbor(dir.opposite(), this);
	}

	public Node getNeighbor(EDirection dir) {
		return neighbors[dir.ordinal()];
	}

	public List<NodeGroup> getGroupNeighbors(EDirection dir) {
		final int dGroups = dimData.getGroups().size();
		final int rGroups = recData.getGroups().size();

		int offset = 0;
		int shift = 1;
		int size;
		switch (dir) {
		case LEFT_OF:
			offset = 0;
			shift = 1;
			size = rGroups;
			break;
		case RIGHT_OF:
			offset = (dGroups - 1) * rGroups;
			shift = 1;
			size = rGroups;
			break;
		case ABOVE:
			offset = 0;
			shift = rGroups;
			size = dGroups;
			break;
		case BELOW:
			offset = rGroups - 1;
			shift = rGroups;
			size = dGroups;
			break;
		default:
			return Collections.emptyList();
		}
		List<NodeGroup> r = new ArrayList<>();

		for (int i = 0; i < size; ++i) {
			r.add((NodeGroup) get(offset + i * shift));
		}
		return r;
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		int i = 0;
		final int dGroups = dimData.getGroups().size();
		final int rGroups = recData.getGroups().size();

		float wi = w / dGroups;
		float hi = h / rGroups;
		for (int d = 0; d < dGroups; ++d) {
			for (int r = 0; r < rGroups; ++r) {
				IGLLayoutElement child = children.get(i++);
				child.setBounds(d * wi, r * hi, wi, hi);
			}
		}
		return false;
	}

	public boolean has(EDimension dim) {
		return !TypedCollections.isInvalid(get(dim));
	}

	public TypedSet get(EDimension dim) {
		return dim.select(this.dimGroups, this.recGroups);
	}

	/**
	 * @param opposite
	 * @return
	 */
	public IDType getIdType(EDimension dim) {
		return get(dim).getIdType();
	}

	public TypedGroupSet getGroups(EDimension dim) {
		return dim.select(dimGroups, recGroups);
	}

	public int compare(EDimension dim, int a, int b) {
		// check existence
		TypedSet data = get(dim);
		boolean hasA = a >= 0 && data.contains(a);
		boolean hasB = b >= 0 && data.contains(b);
		int r;
		if ((r = Boolean.compare(!hasA, !hasB)) != 0)
			return r;
		if (!hasA && !hasB)
			return 0;
		// check groups
		TypedGroupSet groups = getGroups(dim);
		int groupA = indexOf(groups, a);
		int groupB = indexOf(groups, b);
		if ((r = Integer.compare(groupA, groupB)) != 0)
			return r;

		// check values
		return this.data.compare(dim, a, b, get(dim.opposite()));
	}

	public ITypedComparator getComparator(final EDimension dim) {
		return new ITypedComparator() {
			@Override
			public IDType getIdType() {
				return get(dim).getIdType();
			}

			@Override
			public int compare(Integer o1, Integer o2) {
				return Node.this.compare(dim, o1, o2);
			}
		};
	}

	/**
	 * @param groups
	 * @param a
	 * @return
	 */
	private static int indexOf(TypedGroupSet groups, int a) {
		List<TypedSetGroup> g = groups.getGroups();
		for (int i = 0; i < g.size(); ++i)
			if (g.get(i).contains(a))
				return i;
		return -1;
	}

}
