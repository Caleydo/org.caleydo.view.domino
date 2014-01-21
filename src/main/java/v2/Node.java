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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
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
import v2.dnd.MultiNodeGroupDragInfo;
import v2.dnd.NodeDragInfo;
import v2.dnd.NodeGroupDragInfo;
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
	/**
	 *
	 */
	private static final int MIN_SIZE = 30;

	static final float BORDER = 2;

	private Node[] neighbors = new Node[4];
	private float[] offsets = new float[4];

	private final String label;
	IDataValues data;

	private TypedGroupList dimData;
	private TypedGroupSet dimGroups;

	private TypedGroupList recData;
	private TypedGroupSet recGroups;

	@DeepScan
	private DetachedAdapter dimDetached = new DetachedAdapter(this, EDimension.DIMENSION);
	@DeepScan
	private DetachedAdapter recDetached = new DetachedAdapter(this, EDimension.RECORD);

	// visualizationtype -> scale factor
	// if type is size dependent -> global factor
	// else local factor
	private final Map<String, Vec2f> scaleFactors = new HashMap<>();

	private boolean highlightDropArea;

	private final Node origin;

	private String visualizationType;

	private boolean dirtyBands;

	private boolean mouseOver;


	public Node(IDataValues data) {
		this(null, data, data.getLabel(), data.getDefaultGroups(EDimension.DIMENSION), data
				.getDefaultGroups(EDimension.RECORD));
	}

	public Node(Node clone) {
		this.origin = clone;
		this.data = clone.data;
		this.label = clone.label;
		this.visualizationType = clone.visualizationType;
		this.dimGroups = clone.dimGroups;
		this.recGroups = clone.recGroups;
		copyScaleFactors(clone);
		setData(clone.dimData, clone.recData);
		init();
	}

	public Node(Node origin, IDataValues data, String label, TypedGroupSet dimGroups, TypedGroupSet recGroups) {
		this.origin = origin;
		this.visualizationType = origin == null ? null : origin.visualizationType;
		this.data = data;
		this.label = label;
		this.dimGroups = dimGroups;
		this.recGroups = recGroups;
		if (origin != null)
			copyScaleFactors(origin);
		// guessShift(dimGroups.size(), recGroups.size());
		setData(fixList(dimGroups), fixList(recGroups));
		init();
	}

	/**
	 * @return the origin, see {@link #origin}
	 */
	public Node getOrigin() {
		return origin;
	}

	private void copyScaleFactors(Node clone) {
		for (Map.Entry<String, Vec2f> entry : clone.scaleFactors.entrySet())
			this.scaleFactors.put(entry.getKey(), entry.getValue().copy());
	}

	public boolean isDetached(EDimension dim) {
		return getDetachedHandler(dim).isDetached();
	}

	private DetachedAdapter getDetachedHandler(EDimension dim) {
		return dim.select(dimDetached, recDetached);
	}

	public void setDetached(EDimension dim, boolean value) {
		if (isDetached(dim) == value)
			return;
		EProximityMode p = getProximityMode();
		getDetachedHandler(dim).setDetached(value);
		if (p != getProximityMode()) // trigger update of vis
			setData(dimData, recData);
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
		g.color(Color.WHITE).fillRect(0, 0, w, h);

		final Block b = findBlock();
		g.lineWidth(2);
		g.color(has(EDimension.DIMENSION) ? b.getStateColor(this, EDimension.DIMENSION) : Color.BLACK);
		g.drawLine(0, 0, w, 0).drawLine(0, h, w, h);
		g.color(has(EDimension.RECORD) ? b.getStateColor(this, EDimension.RECORD) : Color.BLACK);
		g.drawLine(0, 0, 0, h).drawLine(w, 0, w, h);
		g.lineWidth(1);

		if (mouseOver) {
			g.drawText(b.getStateString(this, EDimension.RECORD), 0, -12, w - 2, 10, VAlign.RIGHT);
			g.drawText(b.getStateString(this, EDimension.DIMENSION), w + 2, h - 12, 100, 10);
		}

		super.renderImpl(g, w, h);

		// render detached bands
		dimDetached.renderImpl(g, w, h);
		recDetached.renderImpl(g, w, h);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);

		// render detached bands
		dimDetached.renderPickImpl(g, w, h);
		recDetached.renderPickImpl(g, w, h);
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDropTarget(this);
			mouseOver = true;
			repaint();
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDropTarget(this);
			highlightDropArea = false;
			mouseOver = false;
			repaint();
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
		if (!(item.getInfo() instanceof NodeDragInfo))
			return false;
		NodeDragInfo d = (NodeDragInfo) item.getInfo();
		final Node b = d.getNode();
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
		dimDetached.takeDown();
		recDetached.takeDown();
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
			if (this.size() == 1 && this.get(0) instanceof SingleNodeGroup) {
				((SingleNodeGroup) this.get(0)).setData(dimGroups.get(0), recGroups.get(0));
			} else {
				for (NodeGroup g : nodeGroups())
					g.prepareRemoveal();
				this.clear();
				SingleNodeGroup single = new SingleNodeGroup(this);
				single.setData(dimGroups.get(0), recGroups.get(0));
				this.add(single);
			}
		} else {
			if (size() == 1) {
				for (NodeGroup g : nodeGroups())
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

		if (context != null) {
			updateSize(true);
			relayout();
		}
	}

	private Iterable<NodeGroup> nodeGroups() {
		return Iterables.filter(this, NodeGroup.class);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		updateSize(false);
		dimDetached.init(context);
		recDetached.init(context);
	}


	private void updateSize(boolean adaptDetached) {
		Vec2f new_ = fixSize(scaleSize(originalSize()));
		final Vec2f act = getSize();
		Vec2f change = new_.minus(act);
		if (dimDetached.isDetached()) {
			dimDetached.incShift(-change.y() * .5f);
			final Vec2f l = getLocation();
			setLocation(l.x(), l.y() - change.y() * .5f);
			// new_.setY(act.y());
		}
		if (recDetached.isDetached()) {
			recDetached.incShift(-change.x() * .5f);
			final Vec2f l = getLocation();
			setLocation(l.x() - change.x() * .5f, l.y());
			// new_.setX(act.x());
		}
		setSize(new_.x(), new_.y());
	}

	private static Vec2f fixSize(Vec2f size) {
		size.setX(Math.max(size.x(), MIN_SIZE));
		size.setY(Math.max(size.y(), MIN_SIZE));
		return size;
	}

	private Vec2f originalSize() {
		float[] xi = getSizes(EDimension.DIMENSION);
		float[] yi = getSizes(EDimension.RECORD);

		float w = BORDER * 2 + sum(xi);
		float h = BORDER * 2 + sum(yi);
		return new Vec2f(w, h);
	}

	private Vec2f scaleSize(Vec2f size) {
		Vec2f scale = getScaleFactor();
		size.setX(scale.x() * size.x());
		size.setY(scale.y() * size.y());
		return size;
	}

	private Vec2f getScaleFactor() {
		Vec2f scale;
		String type = getVisualizationType();
		if (this.scaleFactors.containsKey(type))
			scale = this.scaleFactors.get(type);
		else
			scale = new Vec2f(1, 1);
		return scale;
	}

	private float[] getSizes(EDimension dim) {
		List<NodeGroup> lefts = getGroupNeighbors(EDirection.getPrimary(dim.opposite()));
		float[] r = new float[lefts.size()];
		int i = 0;
		for (NodeGroup l : lefts) {
			double size = l.getDesc(dim).size(l.getData(dim).size());
			r[i++] = (float) size + BORDER * 2;
		}
		return r;
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
		List<TypedListGroup> d = new ArrayList<>(select.getGroups());
		final TypedListGroup toRemove = group.getData(dim);
		d.remove(toRemove);

		if (d.isEmpty()) {
			removeMe();
			return;
		}

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

	public Block getBlock() {
		return findBlock();
	}

	/**
	 * @return
	 */
	private Iterable<NodeGroup> groups() {
		return nodeGroups();
	}
	/**
	 * @param n
	 * @return
	 */
	private NodeGroup getOrCreate(int n) {
		if (n < size())
			return (NodeGroup) get(n);
		NodeGroup g = new NodeGroup(this);
		this.add(g);
		return g;
	}

	public void setNeighbor(EDirection dir, Node neighbor) {
		// setDetached(dir.asDim(), false);

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

	private void updateDetachedBands(final EDimension dim) {
		getDetachedHandler(dim).createBand(getNeighbor(EDirection.getPrimary(dim)),
				getNeighbor(EDirection.getPrimary(dim).opposite()));
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
		float[] dims = getSizes(EDimension.DIMENSION);
		float[] recs = getSizes(EDimension.RECORD);
		float fw = (w - BORDER * 2) / sum(dims);
		float fh = (h - BORDER * 2) / sum(recs);

		int k = 0;
		float x = BORDER;
		for (int i = 0; i < dims.length; ++i) {
			float y = BORDER;
			float wi = dims[i] * fw;
			for (int j = 0; j < recs.length; ++j) {
				float hi = recs[j] * fh;
				IGLLayoutElement child = children.get(k++);
				child.setBounds(x, y, wi, hi);
				y += hi;
			}
			x += wi;
		}
		return false;
	}

	/**
	 * @param dims
	 * @return
	 */
	private static float sum(float... vs) {
		float r = 0;
		for (float v : vs)
			r += v;
		return r;
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
		for (NodeGroup g : nodeGroups())
			g.selectMe();
	}

	/**
	 * @return
	 */
	public EProximityMode getProximityMode() {
		boolean dAlone = isAlone(EDimension.DIMENSION);
		final boolean rAlone = isAlone(EDimension.RECORD);
		if (dAlone && rAlone)
			return EProximityMode.FREE;
		if ((isDetached(EDimension.DIMENSION) || areNeighborsDetached(EDimension.DIMENSION))
				&& (isDetached(EDimension.RECORD) || areNeighborsDetached(EDimension.RECORD)))
			return EProximityMode.DETACHED;
		return EProximityMode.ATTACHED;
	}

	/**
	 * @param dimension
	 * @return
	 */
	private boolean areNeighborsDetached(EDimension dim) {
		Node n = getNeighbor(EDirection.getPrimary(dim));
		Node n2 = getNeighbor(EDirection.getPrimary(dim).opposite());
		return (n == null || n.isDetached(dim)) && (n2 == null || n2.isDetached(dim));
	}

	/**
	 * @param pickable
	 */
	public void setContentPickable(boolean pickable) {
		for (NodeGroup g : groups()) {
			g.setContentPickable(pickable);
		}
	}

	public ILocator getGroupLocator(final EDimension dim) {
		final List<NodeGroup> groups = getGroupNeighbors(EDirection.getPrimary(dim.opposite()));
		return new GLLocation.ALocator() {
			@Override
			public GLLocation apply(int dataIndex) {
				NodeGroup g = groups.get(dataIndex);
				double offset = dim.select(g.getLocation());
				double size = dim.select(g.getSize());
				return new GLLocation(offset, size);
			}
		};
	}

	public ILocator getLocator(final EDimension dim) {
		TypedGroupList data = getData(dim);
		final List<NodeGroup> groups = getGroupNeighbors(EDirection.getPrimary(dim.opposite()));
		int offset = 0;
		List<TypedListGroup> gropus2 = data.getGroups();
		final List<Pair<Integer, ILocator>> locators = new ArrayList<>(gropus2.size());

		for (int i = 0; i < gropus2.size(); ++i) {
			int size = gropus2.get(i).size();
			offset += size;
			final NodeGroup g = groups.get(i);
			float loffset = dim.select(g.getLocation());
			locators.add(Pair.make(offset, GLLocation.shift(g.getLocator(dim), loffset)));
		}
		return new GLLocation.ALocator() {
			@Override
			public GLLocation apply(int dataIndex) {
				int offset = 0;
				for (Pair<Integer, ILocator> loc : locators) {
					if (loc.getFirst() > dataIndex) {
						return loc.getSecond().apply(dataIndex - offset);
					}
					offset = loc.getFirst();
				}
				return GLLocation.UNKNOWN;
			}
		};

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
	public void transpose() {
		transposeMe();
		findBlock().tranposedNode(this);
	}

	public Node transposeMe() {
		this.data = TransposedDataValues.transpose(data);

		TypedGroupSet g = recGroups;
		this.recGroups = dimGroups;
		this.dimGroups = g;
		TypedGroupList t = this.recData;
		this.recData = dimData;
		this.dimData = t;
		boolean tmpR = this.isDetached(EDimension.RECORD);
		this.setDetached(EDimension.RECORD, this.isDetached(EDimension.DIMENSION));
		this.setDetached(EDimension.DIMENSION, tmpR);

		for (Vec2f scale : scaleFactors.values())
			scale.set(scale.y(), scale.x());

		setData(dimData, recData);

		return this;
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
		setVisibility(EVisibility.PICKABLE);
	}

	/**
	 * @param r
	 */
	public void selectByBounds(Rectangle2D r) {
		Vec2f l = getLocation();
		r = new Rectangle2D.Double(r.getX() - l.x(), r.getY() - l.y(), r.getWidth(), r.getHeight());
		for (NodeGroup node : nodeGroups()) {
			final Rectangle2D ri = node.getRectangleBounds();
			if (ri.intersects(r)) {
				node.selectMe();
			}
		}
	}

	/**
	 * @param dimension
	 * @return
	 */
	public void removeBlock() {
		findBlock().removeMe();
	}

	/**
	 * @param s
	 */
	public void selectDefaultVisualization(GLElementFactorySwitcher s) {
		if (visualizationType != null && trySetVisualizationType(s, visualizationType))
			return;

		// if (origin != null && )
		final EProximityMode proximityMode = getProximityMode();
		if (origin != null && proximityMode == origin.getProximityMode()) {
			String target = origin.getVisualizationType();
			if (trySetVisualizationType(s, target))
				return;
		}
		Collection<String> list = this.data.getDefaultVisualization(proximityMode);
		for (String target : list) {
			if (trySetVisualizationType(s, target))
				return;
		}
	}

	private static boolean trySetVisualizationType(GLElementFactorySwitcher s, String target) {
		for (GLElementSupplier supp : s) {
			if (target.equals(supp.getId())) {
				s.setActive(supp);
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public String getVisualizationType() {
		if (isEmpty())
			return null;
		NodeGroup g = (NodeGroup) get(0);
		final GLElementFactorySwitcher s = g.getSwitcher();
		return s == null ? null : s.getActiveId();
	}

	public void setVisualizationType(String id) {
		boolean anyChange = false;
		int active = findVisTypeIndex(id);
		if (active < 0) // invalid type
			return;
		visualizationType = id;
		for (NodeGroup g : nodeGroups()) {
			GLElementFactorySwitcher s = g.getSwitcher();
			anyChange = anyChange || s.getActive() != active;
			s.setActive(active);
		}
		if (!anyChange && size() > 1)
			return;
		updateSize(true);
		relayout();
		findBlock().updatedNode(Node.this);
	}

	/**
	 * @param id
	 * @return
	 */
	private int findVisTypeIndex(String id) {
		NodeGroup g = (NodeGroup) get(0);
		int i = 0;
		for (GLElementSupplier s : g.getSwitcher()) {
			if (s.getId().equals(id))
				return i;
			i++;
		}
		return -1;
	}

	public GLElementFactorySwitcher getRepresentableSwitcher() {
		NodeGroup g = (NodeGroup) get(0);
		return g.getSwitcher();
	}

	/**
	 * @param shiftX
	 * @param shiftY
	 */
	public void shiftBy(float shiftX, float shiftY) {
		if (shiftX == 0 && shiftY == 0)
			return;
		Vec2f raw = originalSize();
		Vec2f new_ = fixSize(scaleSize(raw.copy()).plus(new Vec2f(shiftX, shiftY)));
		// TODO check if the change is valid according to the visualizations

		float sx = new_.x() / raw.x();
		float sy = new_.y() / raw.y();
		String type = getVisualizationType();
		scaleFactors.put(type, new Vec2f(sx, sy));

		final Vec2f act = getSize();
		Vec2f change = new_.minus(act);
		if (dimDetached.isDetached()) {
			dimDetached.incShift(-change.y() * .5f);
			final Vec2f l = getLocation();
			setLocation(l.x(), l.y() - change.y() * .5f);
			// new_.setY(act.y());
		}
		if (recDetached.isDetached()) {
			recDetached.incShift(-change.x() * .5f);
			final Vec2f l = getLocation();
			setLocation(l.x() - change.x() * .5f, l.y());
			// new_.setX(act.x());
		}
		setSize(new_.x(), new_.y());
		relayout();
	}

	public void shiftTo(EDimension dim, float v) {
		v = v - dim.select(getSize());
		shiftBy(dim.select(v, 0), dim.select(0, v));
	}

	public Rect getDetachedRectBounds() {
		Rect r = getRectBounds().clone();
		if (dimDetached.isDetached()) {
			r.x(r.x() - 50);
			r.width(r.width() + 100);
			r.y(r.y() - dimDetached.getShift());
			r.height(r.height() + dimDetached.getShift() * 2);
		}
		if (recDetached.isDetached()) {
			r.y(r.y() - 50);
			r.height(r.height() + 100);
			r.x(r.x() - recDetached.getShift());
			r.width(r.width() + recDetached.getShift() * 2);
		}

		r.x(r.x() - offsets[EDirection.LEFT_OF.ordinal()]);
		r.y(r.y() - offsets[EDirection.ABOVE.ordinal()]);
		r.width(r.width() + offsets[EDirection.LEFT_OF.ordinal()] + offsets[EDirection.RIGHT_OF.ordinal()]);
		r.height(r.height() + offsets[EDirection.ABOVE.ordinal()] + offsets[EDirection.BELOW.ordinal()]);

		return r;
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setDetachedBounds(float x, float y, float w, float h) {
		x += offsets[EDirection.LEFT_OF.ordinal()];
		y += offsets[EDirection.ABOVE.ordinal()];
		w -= offsets[EDirection.LEFT_OF.ordinal()] + offsets[EDirection.RIGHT_OF.ordinal()];
		h -= offsets[EDirection.ABOVE.ordinal()] + offsets[EDirection.BELOW.ordinal()];

		Vec2f size = getSize();
		if (dimDetached.isDetached()) {
			x += 50;
			w -= 100;
			dimDetached.setShift((h - size.y()) * 0.5f);
			y += dimDetached.getShift();
			h = size.y();
		}
		if (recDetached.isDetached()) {
			y += 50;
			h -= 100;
			recDetached.setShift((w - size.x()) * 0.5f);
			x += recDetached.getShift();
			w = size.x();
		}
		setBounds(x, y, w, h);
		relayout();
	}

	/**
	 * @param opposite
	 * @return
	 */
	public INodeLocator getNodeLocator(EDimension d) {
		float offset = d.select(getLocation());
		float size = d.select(getSize());
		return new NodeLocator(new GLLocation(offset, size), getGroupLocator(d), getLocator(d));
	}

	public IGLElementContext getContext() {
		return super.context;
	}

	@Override
	public void layout(int deltaTimeMs) {
		super.layout(deltaTimeMs);

		if (dirtyBands) {
			updateDetachedBands(EDimension.DIMENSION);
			updateDetachedBands(EDimension.RECORD);
			dirtyBands = false;
		}
	}

	/**
	 *
	 */
	public void updateBands() {
		this.dirtyBands = true;
	}

	/**
	 * @return
	 */
	public Color getColor() {
		return data.getColor();
	}

	/**
	 * @param dir
	 * @param i
	 */
	public void setOffset(EDirection dir, float offset) {
		float bak = offsets[dir.ordinal()];
		offsets[dir.ordinal()] = offset;
		if (bak == offset)
			return;
		if (dir.isHorizontal())
			shiftLocation(offset-bak, 0);
		else
			shiftLocation(0,offset-bak);
		Block b = findBlock();
		if (b != null)
			b.updatedNode(this);
	}

	public void shiftLocation(float x, float y) {
		Vec2f l = getLocation();
		setLocation(l.x()+x, l.y()+y);
	}
}
