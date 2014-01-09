/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation.ILocator;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.graph.EProximityMode;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.MappingCaches;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;

import v2.data.IDataValues;
import v2.data.TransposedDataValues;
import v2.event.HideNodeEvent;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Node extends GLElementContainer implements IGLLayout2, ILabeled, IDropGLTarget, IPickingListener {
	static final float BORDER = 2;

	private Node[] neighbors = new Node[4];

	private final String label;
	private final IDataValues data;

	private TypedGroupList dimData;
	private TypedGroupSet dimGroups;

	private TypedGroupList recData;
	private TypedGroupSet recGroups;

	private EProximityMode proximityMode = EProximityMode.ATTACHED;

	private final Vec2f shift = new Vec2f();

	private boolean highlightDropArea;

	public Node(IDataValues data) {
		this(data, data.getLabel(), data.getDefaultGroups(EDimension.DIMENSION), data
				.getDefaultGroups(EDimension.RECORD));
	}

	public Node(Node clone) {
		this.data = clone.data;
		this.label = clone.label;
		this.dimGroups = clone.dimGroups;
		this.recGroups = clone.recGroups;
		this.shift.set(clone.shift);
		setData(clone.dimData, clone.recData);
		init();
	}

	public Node(IDataValues data, String label, TypedGroupSet dimGroups, TypedGroupSet recGroups) {
		this.data = data;
		this.label = label;
		this.dimGroups = dimGroups;
		this.recGroups = recGroups;
		guessShift(dimGroups.size(), recGroups.size());
		setData(fixList(dimGroups), fixList(recGroups));
		init();
	}

	/**
	 * @param size
	 * @param size2
	 */
	private void guessShift(float d, float r) {
		Vec2f s = initialSize(d, r);
		shift.set(s.x() - d, s.y() - r);
	}

	/**
	 * @param d
	 * @param r
	 * @return
	 */
	public static Vec2f initialSize(float d, float r) {
		final float c = 250;
		if (d < c && r < c)
			return new Vec2f(d, r);
		float aspectRatio = (d) / r;
		float di, ri;
		if (d > r) {
			di = c;
			ri = di / aspectRatio;
		} else {
			ri = c;
			di = ri * aspectRatio;
		}
		return new Vec2f(di, ri);
	}

	/**
	 * @param dimGroups2
	 * @return
	 */
	private TypedGroupList fixList(TypedGroupSet data) {
		if (data.isEmpty())
			return TypedGroupList.createUngrouped(new TypedList(ImmutableList.of(TypedCollections.INVALID_ID), data
					.getIdType()));
		return data.asList();
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
	protected void renderImpl(GLGraphics g, float w, float h) {
		final Block b = findBlock();
		g.lineWidth(2);
		g.color(has(EDimension.DIMENSION) ? b.getStateColor(this, EDimension.DIMENSION) : Color.BLACK);
		g.drawLine(0, 0, w, 0).drawLine(0, h, w, h);
		g.color(has(EDimension.RECORD) ? b.getStateColor(this, EDimension.RECORD) : Color.BLACK);
		g.drawLine(0, 0, 0, h).drawLine(w, 0, w, h);
		g.lineWidth(1);
		super.renderImpl(g, w, h);

		if (highlightDropArea) {
			g.incZ().incZ().incZ();
			// test

			g.decZ().decZ().decZ();
		}
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDropTarget(this);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDropTarget(this);
			highlightDropArea = false;
			break;
		case MOUSE_WHEEL:
			findBlock().zoom((IMouseEvent) pick, this);
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
		if (d instanceof NodeGroupDragInfo)
			return false;
		final Node b = d.getBaseNode();
		if (b == this || b == null)
			return false;
		if (has(EDimension.DIMENSION) && has(EDimension.RECORD))
			return false;
		for (EDimension dim : EDimension.values())
			if (!getIdType(dim).getIDCategory().isOfCategory(b.getIdType(dim)))
				return false;
		return true;
	}

	@Override
	public void onItemChanged(IDnDItem item) {
		if (!highlightDropArea) {
			highlightDropArea = true;
			repaint();
		}
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}

	@Override
	public void onDrop(IDnDItem item) {
		Vec2f mousePos = toRelative(item.getMousePos());

		IDragInfo info = item.getInfo();
		if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			dropNode(g.getGroup().toNode(), mousePos);
		} else if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			dropNode(item.getType() == EDnDType.COPY ? new Node(g.getNode()) : g.getNode(), mousePos);
		} else if (info instanceof MultiNodeGroupDragInfo) {
			// won't work
		} else {
			Node node = Nodes.extract(item);
			dropNode(node, mousePos);
		}
	}

	public Set<EDimension> dimensions() {
		boolean dim = has(EDimension.DIMENSION);
		boolean rec = has(EDimension.RECORD);
		if (dim && rec)
			return Sets.immutableEnumSet(EDimension.DIMENSION, EDimension.RECORD);
		if (dim && !rec)
			return Sets.immutableEnumSet(EDimension.DIMENSION);
		if (!dim && rec)
			return Sets.immutableEnumSet(EDimension.RECORD);
		return Collections.emptySet();
	}

	/**
	 * @param node
	 * @param mousePos
	 */
	private void dropNode(Node node, Vec2f mousePos) {
		Domino domino = findParent(Domino.class);
		domino.removeNode(node);
		EDimension dim = getSingleGroupingDimension();
		TypedGroupList m = node.getData(dim);
		integrate(m, toSetType(mousePos.y(), getSize().y()));
	}

	private final ESetOperation toSetType(float v, float total) {
		// TODO Auto-generated method stub
		return ESetOperation.OR;
	}

	public void removeMe() {
		Domino domino = findParent(Domino.class);
		domino.removeNode(this);
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
			for (NodeGroup g : Iterables.filter(this, NodeGroup.class))
				g.prepareRemoveal();
			this.clear();
			SingleNodeGroup single = new SingleNodeGroup(this, data);
			single.setData(dimGroups.get(0), recGroups.get(0));
			this.add(single);
		} else {
			if (size() == 1) {
				for (NodeGroup g : Iterables.filter(this, NodeGroup.class))
					g.prepareRemoveal();
				this.clear();// maybe was a single
			}

			int n = 0;
			List<NodeGroup> left = new ArrayList<>();
			for (TypedListGroup dimGroup : dimGroups) {
				NodeGroup above = null;
				int i = 0;
				for (TypedListGroup recGroup : recGroups) {
					final NodeGroup child = getOrCreate(n);
					n++;
					child.setData(dimGroup, recGroup);
					child.setNeighbor(EDirection.ABOVE, above);
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

			{
				final List<GLElement> subList = this.asList().subList(n, size());
				for (NodeGroup g : Iterables.filter(subList, NodeGroup.class))
					g.prepareRemoveal();
				subList.clear(); // clear rest
			}
		}

		updateSize(dimData, recData);
		relayout();
	}

	private void updateSize(TypedGroupList dimData, TypedGroupList recData) {
		final float w = Math.max(10, dimData.size()) + BORDER * 2 * (1 + dimData.getGroups().size());
		final float h = Math.max(10, recData.size()) + BORDER * 2 * (1 + recData.getGroups().size());
		setSize(w + shift.x(), h + shift.y());
	}

	public void integrate(TypedGroupList group, ESetOperation setOperation) {
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

	public void merge(Collection<NodeGroup> groups) {
		if (groups.size() < 2)
			return;
		EDimension dim = getSingleGroupingDimension();
		if (dim == null)
			return;
		List<TypedListGroup> d = new ArrayList<>(dim.select(dimData, recData).getGroups());
		List<TypedSetGroup> r = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		for (NodeGroup g : new ArrayList<>(groups)) {
			final TypedListGroup gd = g.getData(dim);
			r.add(gd.asSet());
			int index = d.indexOf(gd);
			indices.add(index);
			d.remove(index);
			g.prepareRemoveal();
		}
		TypedSetGroup mg = new TypedSetGroup(TypedSet.union(r), StringUtils.join(
				Collections2.transform(r, Labels.TO_LABEL), ", "), mixColors(r));
		d.add(mg.asList());
		TypedGroupList l = new TypedGroupList(d);
		setGroups(dim, l.asSet());
		triggerResort(dim);
	}


	/**
	 * @param selection
	 * @return
	 */
	public boolean canMerge(Collection<NodeGroup> groups) {
		if (groups.size() < 2)
			return false;
		EDimension dim = getSingleGroupingDimension();
		if (dim == null)
			return false;
		return true;
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
		final TypedGroupList select = dim.select(dimData, recData);
		int old = select.size();
		List<TypedListGroup> d = new ArrayList<>(select.getGroups());
		final TypedListGroup toRemove = group.getData(dim);
		d.remove(toRemove);

		if (d.isEmpty()) {
			removeMe();
			return;
		}

		final float factor = 1 - (toRemove.size()) / (float) old;
		if (dim.isDimension())
			shift.setX(shift.x() * factor);
		else
			shift.setY(shift.y() * factor);

		TypedGroupList l = new TypedGroupList(d);
		setGroups(dim, l.asSet());
		triggerResort(dim);
	}

	public boolean canRemoveGroup(NodeGroup nodeGroup) {
		EDimension dim = getSingleGroupingDimension();
		return (dim != null);
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
	 * @return
	 */
	private Iterable<NodeGroup> groups() {
		return Iterables.filter(this, NodeGroup.class);
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

	public void updateNeighbor(EDirection dir, Node neighbor) {
		List<NodeGroup> myGroups = getGroupNeighbors(dir);
		List<NodeGroup> neighborGroups = neighbor == null ? Collections.<NodeGroup> emptyList() : neighbor
				.getGroupNeighbors(dir.opposite());
		if (myGroups.size() == neighborGroups.size()) {
			for (int i = 0; i < myGroups.size(); ++i) {
				final NodeGroup ng = neighborGroups.get(i);
				final NodeGroup g = myGroups.get(i);
				g.setNeighbor(dir, ng);
				ng.setNeighbor(dir.opposite(), g);
			}
		} else {
			for (NodeGroup g : myGroups) {
				g.setNeighbor(dir, null);
			}
			for (NodeGroup g : neighborGroups) {
				g.setNeighbor(dir.opposite(), null);
			}
		}
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
		final List<TypedListGroup> dimG = dimData.getGroups();
		final List<TypedListGroup> recG = recData.getGroups();
		float wi = (w - BORDER * 2 * (1 + dimG.size())) / dimData.size();
		float hi = (h - BORDER * 2 * (1 + recG.size())) / recData.size();
		float x = 0;
		float xi = BORDER;
		for (TypedListGroup dim : dimG) {
			float y = 0;
			float yi = BORDER;
			for (TypedListGroup rec : recG) {
				IGLLayoutElement child = children.get(i++);
				child.setBounds(xi + x * wi, yi + y * hi, wi * dim.size() + BORDER * 2, hi * rec.size() + BORDER * 2);
				y += rec.size();
				yi += BORDER * 2;
			}
			x += dim.size();
			xi += BORDER * 2;
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
		findBlock().sortBy(this, dim);
	}

	/**
	 * @param dimension
	 */
	public void limitToMe(EDimension dim) {
		findBlock().limitTo(this, dim);
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

	/**
	 *
	 */
	public void selectAll() {
		for (NodeGroup g : Iterables.filter(this, NodeGroup.class))
			g.selectMe();
	}

	/**
	 * @return
	 */
	public EProximityMode getProximityMode() {
		return proximityMode;
	}

	/**
	 * @param pickable
	 */
	public void setContentPickable(boolean pickable) {
		for (NodeGroup g : groups()) {
			g.setContentPickable(pickable);
		}
	}

	GLLocation shiftLocation(EDimension dim, GLLocation l, int group, int offset) {
		final float border = BORDER * (2 * group + 2);
		final TypedGroupList d = getData(dim);
		float total = dim.select(getSize()) - BORDER * 2 * (1 + d.getGroups().size());
		float groupOffset = total * (offset / (float) d.size());

		return new GLLocation(l.getOffset() + border + groupOffset, l.getSize());
	}

	public ILocator getLocator(final EDimension dim) {
		TypedGroupList data = getData(dim);
		List<NodeGroup> groups = getGroupNeighbors(EDirection.getPrimary(dim.opposite()));
		int offset = 0;
		List<TypedListGroup> gropus2 = data.getGroups();
		final List<Pair<Integer, ILocator>> locators = new ArrayList<>(gropus2.size());

		for (int i = 0; i < gropus2.size(); ++i) {
			int size = gropus2.get(i).size();
			offset += size;
			locators.add(Pair.make(offset, (ILocator) groups.get(i).getDesc(dim)));
		}
		return new GLLocation.ALocator() {
			@Override
			public GLLocation apply(int dataIndex) {
				int offset = 0;
				int locIndex = 0;
				for (Pair<Integer, ILocator> loc : locators) {
					if (loc.getFirst() > dataIndex) {
						return shiftLocation(dim, loc.getSecond().apply(dataIndex - offset), locIndex, offset);
					}
					offset = loc.getFirst();
					locIndex++;
				}
				return GLLocation.UNKNOWN;
			}
		};

	}

	/**
	 * @param x
	 * @param y
	 */
	public void shiftSize(float x, float y, boolean set) {
		Vec2f s = getSize().copy();
		Vec2f b = s.copy();
		s.setX(Math.max(s.x() + x, 10));
		s.setY(Math.max(s.y() + y, 10));
		setSize(s.x(), s.y());
		Vec2f act_shift = s.minus(b);
		setLayoutData(s.minus(b));
		if (set)
			this.shift.set(act_shift);
		else
			this.shift.add(act_shift);
		relayout();
	}

	public void shiftSize(EDimension dim, float v, boolean set) {
		Vec2f s = getSize().copy();
		Vec2f b = s.copy();
		s.setX(Math.max(s.x() + dim.select(v, 0), 10));
		s.setY(Math.max(s.y() + dim.select(0, v), 10));
		setSize(s.x(), s.y());
		Vec2f act_shift = s.minus(b);
		setLayoutData(s.minus(b));
		if (set)
			this.shift.set(dim.select(0, 1), v);
		else
			this.shift.add(act_shift);
		relayout();
	}

	/**
	 * @param dimension
	 * @return
	 */
	public boolean isAlone(EDimension dim) {
		return getNeighbor(EDirection.getPrimary(dim)) == null
				&& getNeighbor(EDirection.getPrimary(dim).opposite()) == null;
	}

	/**
	 *
	 */
	public Vec2f getShift() {
		return shift;
	}

	/**
	 * @param nodeGroup
	 * @return
	 */
	public Vec2f getShiftRatio(NodeGroup group) {
		float xr = ((float) group.getData(EDimension.DIMENSION).size()) / dimData.size();
		float yr = ((float) group.getData(EDimension.RECORD).size()) / recData.size();
		return new Vec2f(shift.x() * xr, shift.y() * yr);
	}

	/**
	 *
	 */
	public void transpose() {

		findBlock().replace(this, asTransposed());
	}

	/**
	 * @return
	 */
	public Node asTransposed() {
		Node n = new Node(TransposedDataValues.transpose(data), label, recGroups, dimGroups);
		n.shift.setX(shift.y());
		n.shift.setY(shift.x());
		return n;
	}

	/**
	 * @param selection
	 */
	public void removeSlice(Set<NodeGroup> selection) {
		// TODO Auto-generated method stub

	}

	@ListenTo(sendToMe = true)
	private void onHideNodeEvent(HideNodeEvent event) {
		setVisibility(EVisibility.HIDDEN);
	}
	/**
	 *
	 */
	public void showAgain() {
		setVisibility(EVisibility.VISIBLE);
	}

	/**
	 * @param r
	 */
	public void selectByBounds(Rectangle2D r) {
		Vec2f l = getLocation();
		r = new Rectangle2D.Double(r.getX() - l.x(), r.getY() - l.y(), r.getWidth(), r.getHeight());
		for (NodeGroup node : Iterables.filter(this, NodeGroup.class)) {
			final Rectangle2D ri = node.getRectangleBounds();
			if (ri.intersects(r)) {
				node.selectMe();
			}
		}
	}
}
