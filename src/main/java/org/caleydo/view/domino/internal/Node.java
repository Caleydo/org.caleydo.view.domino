/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDCreator;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.base.IUniqueObject;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IMouseLayer;
import org.caleydo.core.view.opengl.layout2.IPopupLayer;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.EVisScaleType;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation.ILocator;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementMetaData;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.api.model.typed.ITypedComparator;
import org.caleydo.view.domino.api.model.typed.TypedCollections;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.internal.data.IDataValues;
import org.caleydo.view.domino.internal.data.TransposedDataValues;
import org.caleydo.view.domino.internal.dnd.DragElement;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.dnd.NodeGroupDragInfo;
import org.caleydo.view.domino.internal.event.HideNodeEvent;
import org.caleydo.view.domino.internal.undo.CmdComposite;
import org.caleydo.view.domino.internal.undo.MergeNodesCmd;
import org.caleydo.view.domino.internal.undo.RemoveNodeGroupCmd;
import org.caleydo.view.domino.internal.undo.ZoomCmd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Node extends GLElementContainer implements IGLLayout2, ILabeled, IDropGLTarget, IPickingListener,
		IUniqueObject {
	/**
	 *
	 */
	private static final String DATA_SCALE_FACTOR = "data";

	static final float BORDER = 1;

	private final int id = IDCreator.createVMUniqueID(Node.class);

	private final Node origin;
	private final Node[] neighbors = new Node[4];
	private String label;

	private IDataValues data;

	private TypedGroupList dimData;
	private TypedGroupSet dimUnderlying;

	private TypedGroupList recData;
	private TypedGroupSet recUnderlying;

	// visualizationtype -> scale factor
	// if type is size dependent -> global factor
	// else local factor
	private final Map<String, Vec2f> scaleFactors = new HashMap<>();

	private ESetOperation dropSetOperation = null;


	private String visualizationType;

	private boolean mouseOver;

	private boolean isPreviewing = false;


	public Node(IDataValues data) {
		this(null, data, data.getLabel(), data.getDefaultGroups(EDimension.DIMENSION), data
				.getDefaultGroups(EDimension.RECORD));
	}

	public Node(Node clone) {
		this.origin = clone;
		this.data = clone.data;
		this.label = clone.label;
		this.visualizationType = clone.visualizationType;
		this.dimUnderlying = clone.dimUnderlying;
		this.recUnderlying = clone.recUnderlying;
		copyScaleFactors(clone);
		setData(clone.dimData, clone.recData);
		init();
	}

	public Node(Node origin, IDataValues data, String label, TypedGroupSet dimGroups, TypedGroupSet recGroups) {
		this.origin = origin;
		this.visualizationType = origin == null ? null : origin.visualizationType;
		this.data = data;
		this.label = label;
		this.dimUnderlying = dimGroups;
		this.recUnderlying = recGroups;
		if (origin != null)
			copyScaleFactors(origin);
		// guessShift(dimGroups.size(), recGroups.size());
		setData(fixList(dimGroups), fixList(recGroups));
		init();
	}

	/**
	 * @param isPreviewing
	 *            setter, see {@link isPreviewing}
	 */
	public void setPreviewing(boolean isPreviewing) {
		if (this.isPreviewing == isPreviewing)
			return;
		float bak = getDetachedOffset();
		this.isPreviewing = isPreviewing;
		float new_ = getDetachedOffset();
		if (bak != new_ && findBlock() != null)
			findBlock().updatedNode(Node.this, bak, new_);
	}

	/**
	 * @return the isPreviewing, see {@link #isPreviewing}
	 */
	public boolean isPreviewing() {
		return isPreviewing;
	}

	@Override
	public int getID() {
		return id;
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


	/**
	 * @param d
	 * @param r
	 * @return
	 */
	public static Vec2f initialScaleFactors(Vec2f viewSize, float d, float r) {
		final float factor = 0.5f;
		float maxw = viewSize.x() * factor;
		float maxh = viewSize.y() * factor;
		if (d < maxw && r < maxh)
			return new Vec2f(1, 1);

		float fw = maxw / d;
		float fh = maxh / r;
		float f = Math.min(fw, fh);
		return new Vec2f(f, f);
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
		if (mouseOver) {
			g.drawText(b.getStateString(this, EDimension.RECORD), 0, -12, w - 2, 10, VAlign.RIGHT);
			g.drawText(b.getStateString(this, EDimension.DIMENSION), w + 2, h - 12, 100, 10);
		}

		if (dropSetOperation == null) {
			super.renderImpl(g, w, h);
		} else
			renderDropHints(g, w, h);
	}

	private void renderDropHints(GLGraphics g, float w, float h) {
		EDimension dim = getLinearDimension(w, h);
		if (dim == null) {
			renderRectDropHints(g, w, h);
		} else {
			renderLinearDropHints(g, w, h, dim);
		}
	}

	private static EDimension getLinearDimension(float w, float h) {
		float aspectRatio = w / h;
		if (0.95f <= aspectRatio && aspectRatio <= 1.05f)
			return null;
		return EDimension.get(w > h);
	}

	private final ESetOperation toSetType(Vec2f l) {
		Vec2f size = getSize();
		final int c = ESetOperation.values().length;
		int index;
		EDimension dim = getLinearDimension(size.x(), size.y());
		if (dim == null) {
			final int rows = (int) Math.sqrt(c + c % 2);
			final int cols = (int) Math.ceil(c / (float) rows);
			int row = Math.min((int) ((l.y() / size.y()) * rows), rows - 1);
			int col = Math.min((int) ((l.x() / size.x()) * cols), cols - 1);
			index = Math.min(row * cols + col, c - 1);
		} else {
			float ratio = dim.select(l) / dim.select(size);
			index = Math.min((int) (ratio * c), c - 1);
		}
		if (index < 0)
			return ESetOperation.UNION;
		return ESetOperation.values()[index];
	}

	private void renderLinearDropHints(GLGraphics g, float w, float h, EDimension dim) {
		ESetOperation[] vs = ESetOperation.values();
		if (dim.isHorizontal()) {
			float wi = w / vs.length;
			float hi = Math.min(h * 0.8f, wi);
			for (int i = 0; i < vs.length; ++i) {
				final float x = i * wi + (wi - hi) * 0.5f;
				final ESetOperation op = vs[i];
				APlaceholder.renderDropZone(g, i * wi, 0, wi, h, Color.LIGHT_GRAY);
				g.fillImage(op.toIcon(), x, (h - hi) * 0.5f, hi, hi);
				if (op == dropSetOperation)
					g.color(Color.BLACK).drawRoundedRect(x, (h - hi) * 0.5f, hi, hi, 5);
			}
		} else {
			float hi = h / vs.length; // union double
			float wi = Math.min(w * 0.8f, hi);
			for (int i = 0; i < vs.length; ++i) {
				final float y = i * hi + (hi - wi) * 0.5f;
				final ESetOperation op = vs[i];
				APlaceholder.renderDropZone(g, 0, i * hi, w, hi, Color.LIGHT_GRAY);
				g.fillImage(op.toIcon(), (w - wi) * 0.5f, y, wi, wi);
				if (op == dropSetOperation)
					g.color(Color.BLACK).drawRoundedRect((w - wi) * 0.5f, y, wi, wi, 5);
			}
		}
	}

	private void renderRectDropHints(GLGraphics g, float w, float h) {
		ESetOperation[] vs = ESetOperation.values();
		int rows = (int) Math.sqrt(vs.length + vs.length % 2);
		int cols = (int) Math.ceil(vs.length / (float) rows);
		int k = 0;
		float wi = w / cols;
		float hi = h / rows;
		float si = Math.min(wi * 0.8f, hi * 0.8f);
		outer: for (int j = 0; j < rows; ++j) {
			float y = j * hi + (hi - si) * 0.5f;
			for (int i = 0; i < cols; ++i) {
				ESetOperation op = vs[k++];
				float x = i * wi + (wi - si) * 0.5f;
				APlaceholder.renderDropZone(g, i * wi, j * hi, wi, hi, Color.LIGHT_GRAY);
				g.fillImage(op.toIcon(), x, y, si, si);
				if (op == dropSetOperation)
					g.color(Color.BLACK).drawRoundedRect(x, y, si, si, 5);
				if (k >= vs.length)
					break outer;
			}
		}
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (getVisibility() != EVisibility.PICKABLE)
			return;
		super.renderPickImpl(g, w, h);
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
			dropSetOperation = null;
			mouseOver = false;
			repaint();
			break;
		case MOUSE_WHEEL:
			Vec2f shift = ScaleLogic.shiftLogic((IMouseEvent) pick, getSize());
			findParent(Domino.class).getUndo().push(new ZoomCmd(this, shift, null));
			break;
		default:
			break;
		}
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		if (has(EDimension.DIMENSION) && has(EDimension.RECORD))
			return false;
		Node b = toNode(item);
		if (b == this || b == null
				|| (item.getInfo() instanceof NodeDragInfo && ((NodeDragInfo) item.getInfo()).getNode() == this))
			return false;
		if (b.has(EDimension.DIMENSION) && b.has(EDimension.RECORD))
			return false;
		for (EDimension dim : EDimension.values())
			if (!getIdType(dim).getIDCategory().isOfCategory(b.getIdType(dim)))
				return false;
		return true;
	}

	/**
	 * @param item
	 * @return
	 */
	private Node toNode(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof NodeDragInfo)
			return ((NodeDragInfo) info).getNode();
		else if (info instanceof NodeGroupDragInfo)
			return ((NodeGroupDragInfo) info).getNode();
		else if (Nodes.canExtract(item))
			return Nodes.extract(item);
		return null;
	}

	@Override
	public void onItemChanged(IDnDItem item) {
		final ESetOperation type = toSetType(toRelative(item.getMousePos()));
		if (this.dropSetOperation != type) {
			this.dropSetOperation = type;
			repaint();
		}
		final Domino domino = findParent(Domino.class);
		if (domino == null)
			return;
		DragElement current = domino.getCurrentlyDraggedVis();
		if (current == null)
			return;
		current.setVisibility(EVisibility.HIDDEN);
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.COPY;
	}

	@Override
	public void onDrop(IDnDItem item) {
		Vec2f mousePos = toRelative(item.getMousePos());

		IDragInfo info = item.getInfo();
		if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			mergeNode(g.getGroup().toNode(), mousePos, item.getType() == EDnDType.MOVE ? g.getGroup() : null);
		} else if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			Node n = g.getNode();
			if (item.getType() == EDnDType.COPY)
				n = new Node(n);
			mergeNode(n, mousePos, null);
		} else {
			Node node = Nodes.extract(item);
			if (node != null)
				mergeNode(node, mousePos, null);
		}
		dropSetOperation = null;
		repaint();
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
	 * @param groupToRemove
	 */
	private void mergeNode(Node node, Vec2f mousePos, NodeGroup groupToRemove) {
		final ESetOperation type = toSetType(mousePos);
		Domino domino = findParent(Domino.class);
		final UndoStack undo = domino.getUndo();
		final MergeNodesCmd cmd = new MergeNodesCmd(this, type, node);
		if (groupToRemove != null) {
			undo.push(CmdComposite.chain(cmd, new RemoveNodeGroupCmd(groupToRemove)));
		} else
			undo.push(cmd);
	}

	public IDataValues getDataValues() {
		return data;
	}

	public void setDataValues(IDataValues data) {
		this.data = data;
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

	/**
	 * @param label
	 *            setter, see {@link label}
	 */
	public void setLabel(String label) {
		this.label = label;
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

		int n = 0;
		List<NodeGroup> left = new ArrayList<>();
		for (TypedListGroup dimGroup : dimGroups) {
			NodeGroup above = null;
			int i = 0;
			for (TypedListGroup recGroup : recGroups) {
				final NodeGroup child = getOrCreate(n);
				child.setVisibility(EVisibility.PICKABLE);
				n++;
				child.setData(dimGroup, recGroup);
				child.setNeighbor(EDirection.NORTH, above);
				if (above != null)
					above.setNeighbor(EDirection.SOUTH, child);
				above = child;
				if (left.size() > i) {
					left.get(i).setNeighbor(EDirection.EAST, child);
					child.setNeighbor(EDirection.WEST, left.get(i));
				}
				if (i < left.size())
					left.set(i++, child);
				else
					left.add(i++, child);

			}
		}

		{
			final List<GLElement> subList = this.asList().subList(n, size());
			if (!subList.isEmpty()) {
				for (NodeGroup g : Iterables.filter(subList, NodeGroup.class)) {
					g.prepareRemoveal();
					g.setVisibility(EVisibility.NONE);
				}
				// subList.clear(); // don't clear just hide
			}
		}

		if (context != null) {
			updateSize(true);
			relayout();
		}
	}

	Iterable<NodeGroup> nodeGroups() {
		// all visible node groups
		return Iterables.filter(Iterables.filter(this, EVisibility.PICKABLE), NodeGroup.class);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		if (!scaleFactors.containsKey(DATA_SCALE_FACTOR)) {
			Vec2f size = findParent(MiniMapCanvas.class).getSize();
			final Vec2f v = initialScaleFactors(size, dimData.size(), recData.size());

			Blocks blocks = findParent(Blocks.class);
			v.setX(blocks.getRulerScale(this.dimUnderlying.getIdType(), v.x()));
			v.setY(blocks.getRulerScale(this.recUnderlying.getIdType(), v.y()));

			scaleFactors.put(DATA_SCALE_FACTOR, v);
		}
		updateSize(false);
	}


	private void updateSize(boolean adaptDetached) {
		verifyScaleFactors();
		Vec2f new_ = addBorders(scaleSize(originalSize()));

		setSize(new_.x(), new_.y());
	}

	/**
	 * @param scaleSize
	 * @return
	 */
	private Vec2f addBorders(Vec2f s) {
		final int dims = getData(EDimension.DIMENSION).getGroups().size();
		final int recs = getData(EDimension.RECORD).getGroups().size();
		// 2 border per group + 2 extra between each group
		s.setX(s.x() + (dims + dims - 1) * 2 * BORDER);
		s.setY(s.y() + (recs + recs - 1) * 2 * BORDER);
		return s;
	}

	private Vec2f originalSize() {
		float[] xi = getSizes(EDimension.DIMENSION);
		float[] yi = getSizes(EDimension.RECORD);

		float w = sum(xi);
		float h = sum(yi);
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
		String type = getScaleFactorKey();
		if (this.scaleFactors.containsKey(type))
			scale = this.scaleFactors.get(type);
		else
			scale = new Vec2f(1, 1);
		return scale;
	}

	public void copyScaleFactors(Node node, EDimension dim) {
		for (Map.Entry<String, Vec2f> entry : node.scaleFactors.entrySet()) {
			Vec2f old = scaleFactors.get(entry.getKey());
			if (old == null)
				old = new Vec2f(1, 1);
			if (dim.isHorizontal())
				old.setX(entry.getValue().x());
			else
				old.setY(entry.getValue().y());
			this.scaleFactors.put(entry.getKey(), old);
		}
		if (context != null)
			updateSize(false);
		relayout();
	}

	/**
	 * @param opposite
	 * @param scale
	 */
	public void setDataScaleFactor(EDimension dim, float scale) {
		Vec2f s;
		if (this.scaleFactors.containsKey(DATA_SCALE_FACTOR))
			s = this.scaleFactors.get(DATA_SCALE_FACTOR);
		else
			s = new Vec2f(1, 1);
		if (dim.isHorizontal())
			s.setX(scale);
		else
			s.setY(scale);

		this.scaleFactors.put(DATA_SCALE_FACTOR, s);
		if (GLElementFactories.getMetaData(getVisualizationType()).getScaleType() == EVisScaleType.DATADEPENDENT) {
			updateSize(false);
			relayout();
		}
	}

	public float getDataScaleFactor(EDimension dim) {
		if (this.scaleFactors.containsKey(DATA_SCALE_FACTOR))
			return dim.select(this.scaleFactors.get(DATA_SCALE_FACTOR));
		else
			return 1;
	}

	private float[] getSizes(EDimension dim) {
		List<NodeGroup> lefts = getGroupNeighbors(EDirection.getPrimary(dim.opposite()));
		float[] r = new float[lefts.size()];
		int i = 0;
		for (NodeGroup l : lefts) {
			double size = l.getDesc(dim).size(l.getData(dim).size());
			r[i++] = (float) size;
		}
		return r;
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



	public EDimension getSingleGroupingDimension() {
		final List<TypedListGroup> dimGroups = dimData.getGroups();
		final List<TypedListGroup> recGroups = recData.getGroups();
		if (dimGroups.size() > 1 && recGroups.size() > 1)
			return null;
		if (dimGroups.size() > 1)
			return EDimension.DIMENSION;
		return EDimension.RECORD;
	}

	public boolean canRemoveGroup(NodeGroup nodeGroup) {
		if (this.groupCount() == 1)
			return false;
		EDimension dim = getSingleGroupingDimension();
		return (dim != null);
	}

	/**
	 * @return
	 */
	public int groupCount() {
		return Iterables.size(nodeGroups());
	}

	/**
	 * @param dim
	 * @param l
	 */
	public void setUnderlyingData(EDimension dim, TypedGroupSet data) {
		if (dim.isDimension())
			dimUnderlying = data;
		else
			recUnderlying = data;
		triggerResort(dim);

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
		case WEST:
			offset = 0;
			shift = 1;
			size = rGroups;
			break;
		case EAST:
			offset = (dGroups - 1) * rGroups;
			shift = 1;
			size = rGroups;
			break;
		case NORTH:
			offset = 0;
			shift = rGroups;
			size = dGroups;
			break;
		case SOUTH:
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
		float dimSpace = (dims.length + dims.length - 1) * 2 * BORDER;
		float recSpace = (recs.length + recs.length - 1) * 2 * BORDER;
		// 2 border per group + 2 extra between each group
		float fw = (w - dimSpace) / sum(dims);
		float fh = (h - recSpace) / sum(recs);

		int k = 0;
		float x = 0;
		for (int i = 0; i < dims.length; ++i) {
			float y = 0;
			float wi = dims[i] * fw + BORDER * 2;
			for (int j = 0; j < recs.length; ++j) {
				float hi = recs[j] * fh + BORDER * 2;
				IGLLayoutElement child = children.get(k++);
				child.setBounds(x, y, wi, hi);
				y += hi + BORDER * 2;
			}
			x += wi + BORDER * 2;
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
		return !TypedCollections.isInvalid(getUnderlyingData(dim));
	}

	/**
	 * @param opposite
	 * @return
	 */
	public IDType getIdType(EDimension dim) {
		return getUnderlyingData(dim).getIdType();
	}

	public TypedGroupSet getUnderlyingData(EDimension dim) {
		return dim.select(dimUnderlying, recUnderlying);
	}

	public int compare(EDimension dim, int a, int b) {
		// check existence
		TypedGroupSet groups = getUnderlyingData(dim);
		if (!groups.isEmpty()) {
			boolean hasA = a >= 0 && groups.contains(a);
			boolean hasB = b >= 0 && groups.contains(b);
			int r;
			if ((r = Boolean.compare(!hasA, !hasB)) != 0)
				return r;
			if (!hasA && !hasB)
				return 0;
			// check groups
			int groupA = indexOf(groups, a);
			int groupB = indexOf(groups, b);
			if ((r = Integer.compare(groupA, groupB)) != 0)
				return r;
		}
		// check values
		return this.data.compare(dim, a, b, getUnderlyingData(dim.opposite()));
	}

	public ITypedComparator getComparator(final EDimension dim) {
		return new ITypedComparator() {
			@Override
			public IDType getIdType() {
				return getUnderlyingData(dim).getIdType();
			}

			@Override
			public int compare(Integer o1, Integer o2) {
				return Node.this.compare(dim, o1, o2);
			}
		};
	}

	public void stratifyByMe(EDimension dim) {
		findBlock().stratifyBy(this, dim);
	}
	public void sortByMe(EDimension dim) {
		findBlock().sortBy(this, dim);
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
	 * @param pickable
	 */
	public void setContentPickable(boolean pickable) {
		for (NodeGroup g : Iterables.filter(this, NodeGroup.class)) {
			g.setContentPickable(pickable);
		}
	}

	public ILocator getGroupLocator(final EDimension dim) {
		final List<NodeGroup> groups = getGroupNeighbors(EDirection.getPrimary(dim.opposite()));
		return new GLLocation.ALocator() {
			@Override
			public GLLocation apply(int dataIndex) {
				NodeGroup g = groups.get(dataIndex);
				double offset = dim.select(g.getLocation()) + BORDER;
				double size = dim.select(g.getSize()) - BORDER * 2;
				return new GLLocation(offset, size);
			}

			@Override
			public Set<Integer> unapply(GLLocation location) {
				Set<Integer> r = new TreeSet<>();
				for (int i = 0; i < groups.size(); ++i) {
					NodeGroup g = groups.get(i);
					double offset = dim.select(g.getLocation()) + BORDER;
					double size = dim.select(g.getSize()) - BORDER * 2;
					if (offset + size < location.getOffset())
						continue;
					if (offset > location.getOffset2())
						break;
					r.add(i);
				}
				return ImmutableSet.copyOf(r);
			}
		};
	}

	public ILocator getLocator(final EDimension dim) {
		TypedGroupList data = getData(dim);
		final List<NodeGroup> groups = getGroupNeighbors(EDirection.getPrimary(dim.opposite()));
		int offset = 0;
		List<TypedListGroup> gropus2 = data.getGroups();
		final List<GroupLocator> locators = new ArrayList<>(gropus2.size());

		for (int i = 0; i < gropus2.size(); ++i) {
			int size = gropus2.get(i).size();
			final NodeGroup g = groups.get(i);
			float loffset = dim.select(g.getLocation()) + BORDER;
			float lsize = dim.select(g.getSize()) - BORDER * 2;
			final ILocator loc = g.getLocator(dim);
			if (GLLocation.NO_LOCATOR == loc) // one no location, all no location
				return GLLocation.NO_LOCATOR;
			GroupLocator gl = new GroupLocator(new GLLocation(loffset, lsize), offset, size, GLLocation.shift(
					loc, loffset));
			offset += size;
			locators.add(gl);
		}
		return new GLLocation.ALocator() {
			@Override
			public GLLocation apply(int dataIndex) {
				for (GroupLocator loc : locators) {
					if (loc.in(dataIndex)) {
						return loc.apply(dataIndex);
					}
				}
				return GLLocation.UNKNOWN;
			}

			@Override
			public Set<Integer> unapply(GLLocation location) {
				Set<Integer> r = new HashSet<>();
				for (GroupLocator loc : locators) {
					if (loc.in(location))
						r.addAll(loc.unapply(location));
				}
				return GLLocation.UNKNOWN_IDS;
			}
		};

	}

	private static class GroupLocator extends GLLocation.ALocator {
		private final GLLocation loc;
		private final int dataOffset;
		private final int dataSize;
		private final ILocator locator;

		public GroupLocator(GLLocation loc, int dataOffset, int dataSize, ILocator locator) {
			this.loc = loc;
			this.dataOffset = dataOffset;
			this.dataSize = dataSize;
			this.locator = locator;
		}

		/**
		 * @param location
		 * @return
		 */
		public boolean in(GLLocation location) {
			double o = location.getOffset();
			double o2 = location.getOffset2();
			if (loc.getOffset2() < o || loc.getOffset() > o2)
				return false;
			return true;
		}

		public boolean in(int dataIndex) {
			return dataIndex < (dataOffset + dataSize);
		}

		@Override
		public GLLocation apply(int dataIndex) {
			return locator.apply(dataIndex - dataOffset);
		}

		@Override
		public Set<Integer> unapply(GLLocation location) {
			Set<Integer> d = locator.unapply(location);
			List<Integer> r = new ArrayList<>(d.size());
			for (Integer di : d)
				r.add(di + dataOffset);
			return ImmutableSet.copyOf(r);
		}
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

		TypedGroupSet g = recUnderlying;
		this.recUnderlying = dimUnderlying;
		this.dimUnderlying = g;
		TypedGroupList t = this.recData;
		this.recData = dimData;
		this.dimData = t;
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

		// remove all children drag sources
		final IMouseLayer m = context.getMouseLayer();
		for (NodeGroup g : nodeGroups()) {
			m.removeDragSource(g);
		}
		m.removeDropTarget(this);
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
	 * @param s
	 */
	public void selectDefaultVisualization(GLElementFactorySwitcher s) {
		if (visualizationType != null && trySetVisualizationType(s, visualizationType))
			return;

		// if (origin != null && )
		if (origin != null && trySetVisualizationType(s, origin.getVisualizationType()))
			return;

		Collection<String> list = this.data.getDefaultVisualization();
		for (String target : list) {
			if (trySetVisualizationType(s, target))
				return;
		}
	}

	private static boolean trySetVisualizationType(GLElementFactorySwitcher s, String target) {
		if (target == null)
			return false;
		for (GLElementSupplier supp : s) {
			if (target.equals(supp.getId())) {
				s.setActive(supp);
				return true;
			}
		}
		return false;
	}

	public String getVisualizationType() {
		return getVisualizationType(false);
	}
	/**
	 * @return
	 */
	public String getVisualizationType(boolean guess) {
		final String default_ = guess ? visualizationType : null;
		if (isEmpty()) {
			return default_;
		}
		NodeGroup g = (NodeGroup) get(0);
		final GLElementFactorySwitcher s = g.getSwitcher();
		return s == null ? default_ : s.getActiveId();
	}

	public void setVisualizationType(String id) {
		int active = findVisTypeIndex(id);
		if (active < 0) // invalid type
			return;
		visualizationType = id;

		String bak = getRepresentableSwitcher().getActiveId();
		if (Objects.equals(bak, id)) // no change
			return;

		findBlock().setVisualizationType(this, id);
	}

	void setVisualizationTypeImpl(String id) {
		int active = findVisTypeIndex(id);
		float was = getDetachedOffset();
		for (NodeGroup g : nodeGroups()) {
			GLElementFactorySwitcher s = g.getSwitcher();
			s.setActive(active);
		}
		updateSize(true);
		relayout();
		findBlock().updatedNode(Node.this, was, getDetachedOffset());
	}


	public float getDetachedOffset() {
		if (isPreviewing)
			return Block.DETACHED_OFFSET * 2;
		IGLElementMetaData metaData = GLElementFactories.getMetaData(getVisualizationType());
		boolean needDetached = metaData != null && metaData.getScaleType() == EVisScaleType.FIX;
		return needDetached ? Block.DETACHED_OFFSET : 0;
	}

	@Override
	public String toString() {
		return getLabel();
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
		Vec2f oldScale = getScaleFactor();
		Vec2f new_ = scaleSize(raw.copy()).plus(new Vec2f(shiftX, shiftY));

		final GLElementFactorySwitcher switcher = getRepresentableSwitcher();
		GLElementDimensionDesc dimDesc = switcher.getActiveDesc(EDimension.DIMENSION);
		GLElementDimensionDesc recDesc = switcher.getActiveDesc(EDimension.RECORD);

		// not allowed to change
		if (!dimDesc.isValid(new_.x(), dimData.size()))
			new_.setX(raw.x() * oldScale.x());
		if (!recDesc.isValid(new_.y(), recData.size()))
			new_.setY(raw.y() * oldScale.y());

		float sx = new_.x() / raw.x();
		float sy = new_.y() / raw.y();
		String type = getScaleFactorKey();
		scaleFactors.put(type, new Vec2f(sx, sy));

		new_ = addBorders(new_);
		setSize(new_.x(), new_.y());
		relayout();
	}

	/**
	 *
	 */
	private void verifyScaleFactors() {
		String type = getScaleFactorKey();
		if (!scaleFactors.containsKey(type))
			return; // no stored scale factors -> valid

		final GLElementFactorySwitcher switcher = getRepresentableSwitcher();
		if (switcher == null)
			return;
		GLElementDimensionDesc dimDesc = switcher.getActiveDesc(EDimension.DIMENSION);
		GLElementDimensionDesc recDesc = switcher.getActiveDesc(EDimension.RECORD);

		Vec2f scaleSize = scaleSize(originalSize().copy());
		// not allowed to change reset
		Vec2f scale = scaleFactors.get(type);
		if (!dimDesc.isValid(scaleSize.x(), dimData.size()))
			scale.setX(1);
		if (!recDesc.isValid(scaleSize.y(), recData.size()))
			scale.setY(1);
	}

	private String getScaleFactorKey() {
		String type = getVisualizationType();
		final IGLElementMetaData metaData = GLElementFactories.getMetaData(type);
		if (metaData != null && metaData.getScaleType() == EVisScaleType.DATADEPENDENT)
			type = DATA_SCALE_FACTOR;
		return type;
	}

	public void shiftTo(EDimension dim, float v) {
		v = v - dim.select(getSize());
		shiftBy(dim.select(v, 0), dim.select(0, v));
	}

	public Rect getDetachedRectBounds() {
		Rect r = getRectBounds().clone();
		return r;
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setDetachedBounds(float x, float y, float w, float h) {
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

	/**
	 * @return
	 */
	public Color getColor() {
		return data.getColor();
	}

	public void shiftLocation(float x, float y) {
		Vec2f l = getLocation();
		setLocation(l.x()+x, l.y()+y);
	}

	/**
	 *
	 */
	public void showInFocus() {
		IPopupLayer popup = context.getPopupLayer();
		FocusOverlay overlay = new FocusOverlay(this);
		Vec2f size = overlay.getPreferredSize();
		Vec2f total = popup.getSize();
		Rect bounds = focusBounds(size, total);
		popup.show(overlay, bounds);
	}

	private static Rect focusBounds(Vec2f size, Vec2f total) {
		Vec2f avail = total.times(0.8f);

		float wi = avail.x() / size.x();
		float hi = avail.y() / size.y();
		Vec2f target;
		if (wi < hi) {
			target = size.times(wi);
		} else {
			target = size.times(hi);
		}

		float w = target.x();
		float h = target.y();
		return new Rect((total.x() - w) * 0.5f, (total.y() - h) * 0.5f, w, h);
	}

	/**
	 * @param dimData2
	 * @param dimension
	 * @return
	 */
	public TypedGroupSet getSubGroupData(TypedListGroup group, EDimension dim) {
		boolean stratified = findBlock().isStratified(this, dim);
		final TypedGroupSet underlying = getUnderlyingData(dim);
		boolean hasMultipleGroups = underlying.getGroups().size() > 1;
		if (stratified || !hasMultipleGroups)
			return new TypedGroupSet(group.asSet());

		return underlying.subSet(group);
	}
}
