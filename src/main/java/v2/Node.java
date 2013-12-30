/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.MappingCaches;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;

import v2.data.IDataValues;

import com.google.common.collect.Collections2;

/**
 * @author Samuel Gratzl
 *
 */
public class Node extends GLElementContainer implements IGLLayout2, ILabeled, IDropGLTarget, IPickingListener {
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
		this.dimGroups = clone.dimGroups;
		this.recGroups = clone.recGroups;
		setData(clone.dimData, clone.recData);
		init();
	}

	public Node(IDataValues data, String label, TypedGroupSet dimGroups, TypedGroupSet recGroups) {
		this.data = data;
		this.label = label;
		this.dimGroups = dimGroups;
		this.recGroups = recGroups;
		setData(dimGroups.asList(), recGroups.asList());
		init();
	}

	/**
	 *
	 */
	private void init() {
		setLayout(this);
		setVisibility(EVisibility.PICKABLE);
		onPick(this);
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDropTarget(this);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDropTarget(this);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		if (!(item.getInfo() instanceof ADragInfo))
			return false;
		ADragInfo d = (ADragInfo) item.getInfo();
		final Node b = d.getBaseNode();
		if (b == this)
			return false;
		if (has(EDimension.DIMENSION) && has(EDimension.RECORD))
			return false;
		for (EDimension dim : EDimension.values())
			if (!getIdType(dim).getIDCategory().isOfCategory(b.getIdType(dim)))
				return false;
		return true;
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}

	@Override
	public void onDrop(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			dropNode(g.getGroup().toNode());
		} else if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			dropNode(item.getType() == EDnDType.COPY ? new Node(g.getNode()) : g.getNode());
		} else {
			Node node = Nodes.extract(item);
			dropNode(node);
		}
	}



	/**
	 * @param node
	 */
	private void dropNode(Node node) {
		Domino domino = findParent(Domino.class);
		domino.removeNode(node);
		EDimension dim = getSingleGroupingDimension();
		TypedGroupList m = node.getData(dim);
		integrate(m);
	}

	@Override
	public void onItemChanged(IDnDItem item) {

	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDropTarget(this);
		super.takeDown();
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

	public TypedGroupList getData(EDimension dim) {
		return dim.select(dimData, recData);
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

		setSize(Math.max(20, Math.min(dimData.size(), 200)), Math.max(20, Math.min(recData.size(), 200)));
	}

	public void integrate(TypedGroupList group) {
		EDimension dim = getSingleGroupingDimension();
		if (dim == null)
			return;
		IIDTypeMapper<Integer, Integer> mapper = MappingCaches.findMapper(group.getIdType(), getIdType(dim));
		if (mapper == null)
			return;

		List<TypedListGroup> d = new ArrayList<>(getData(dim).getGroups());
		for (TypedListGroup g : group.getGroups()) {
			final TypedListGroup group2 = new TypedSetGroup(mapper.apply(group), mapper.getTarget(), g.getLabel(),
					g.getColor()).asList();
			d.add(group2);
		}

		TypedGroupList l = new TypedGroupList(d);
		setGroups(dim, l.asSet());
		triggerResort(dim);
	}

	public void merge(NodeGroup... groups) {
		if (groups.length < 2)
			return;
		EDimension dim = getSingleGroupingDimension();
		if (dim == null)
			return;
		List<TypedListGroup> d = new ArrayList<>(dim.select(dimData, recData).getGroups());
		List<TypedSetGroup> r = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		for (NodeGroup g : groups) {
			final TypedListGroup gd = g.getData(dim);
			r.add(gd.asSet());
			int index = d.indexOf(gd);
			indices.add(index);
			d.remove(index);
		}
		TypedSetGroup mg = new TypedSetGroup(TypedSet.union(r), StringUtils.join(
				Collections2.transform(r, Labels.TO_LABEL), ", "), mixColors(r));
		d.add(mg.asList());
		TypedGroupList l = new TypedGroupList(d);
		setGroups(dim, l.asSet());
		triggerResort(dim);
	}

	/**
	 * @param r
	 * @return
	 */
	private static Color mixColors(List<TypedSetGroup> data) {
		float r = 0;
		float g = 0;
		float b = 0;
		float a = 0;
		for (TypedSetGroup group : data) {
			Color c = group.getColor();
			r += c.r;
			g += c.g;
			b += c.b;
			a += c.a;
		}
		float f = 1.f / data.size();
		return new Color(r * f, g * f, b * f, a * f);
	}

	public EDimension getSingleGroupingDimension() {
		final List<TypedListGroup> dimGroups = dimData.getGroups();
		final List<TypedListGroup> recGroups = recData.getGroups();
		if (dimGroups.size() > 1 && recGroups.size() > 1)
			return null;
		if (dimGroups.size() > 1)
			return EDimension.DIMENSION;
		return EDimension.RECORD;
	}

	public void removeGroup(NodeGroup group) {
		EDimension dim = getSingleGroupingDimension();
		if (dim == null)
			return; // no single grouping
		List<TypedListGroup> d = new ArrayList<>(dim.select(dimData, recData).getGroups());
		d.remove(group.getData(dim));
		TypedGroupList l = new TypedGroupList(d);
		setGroups(dim, l.asSet());
		triggerResort(dim);
	}

	/**
	 * @param dim
	 * @param l
	 */
	private void setGroups(EDimension dim, TypedGroupSet data) {
		if (dim.isDimension())
			dimGroups = data;
		else
			recGroups = data;

	}

	private void triggerResort(EDimension dim) {
		findBlock().resort(this, dim);
	}

	private Block findBlock() {
		return findParent(Block.class);
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
		float wi = w / dimData.size();
		float hi = h / recData.size();
		float x = 0;
		for (TypedListGroup dim : dimData.getGroups()) {
			float y = 0;
			for (TypedListGroup rec : recData.getGroups()) {
				IGLLayoutElement child = children.get(i++);
				child.setBounds(x * wi, y * hi, wi * dim.size(), hi * rec.size());
				y += rec.size();
			}
			x += dim.size();
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

	public void sortByMe(EDimension dim) {
		findBlock().sortByMe(this, dim);
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
