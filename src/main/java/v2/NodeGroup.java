/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import java.util.HashSet;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation.ILocator;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.graph.EProximityMode;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.internal.ui.PickingBarrier;

import v2.event.HideNodeEvent;

import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeGroup extends GLElementDecorator implements ILabeled, IDragGLSource, IPickingListener {
	private final Node parent;

	private final NodeGroup[] neighbors = new NodeGroup[4];
	private TypedListGroup dimData;
	private TypedListGroup recData;

	private final PickingBarrier barrier;
	private boolean armed;


	public NodeGroup(Node parent) {
		this.parent = parent;
		setVisibility(EVisibility.PICKABLE);
		onPick(this);
		this.barrier = new PickingBarrier();
		setContent(barrier);
	}

	@Override
	protected void layoutContent(IGLLayoutElement content, float w, float h, int deltaTimeMs) {
		content.setBounds(Node.BORDER, Node.BORDER, w - Node.BORDER * 2, h - Node.BORDER * 2);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		if (dimData != null && recData != null)
			build();
	}

	public void build() {
		if (context == null)
			return;
		Builder b = GLElementFactoryContext.builder();
		parent.data.fill(b, dimData, recData);
		// if free high else medium
		b.put(EDetailLevel.class, getNode().getProximityMode() == EProximityMode.FREE ? EDetailLevel.HIGH
				: EDetailLevel.MEDIUM);
		b.set("heatmap.blurNotSelected");
		b.set("heatmap.forceTextures");
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(b.build(), "domino."
				+ parent.data.getExtensionID(), parent.getProximityMode());
		GLElementFactorySwitcher s = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
		parent.selectDefaultVisualization(s);
		barrier.setContent(s);
	}

	GLElementFactorySwitcher getSwitcher() {
		GLElementFactorySwitcher s = (GLElementFactorySwitcher) barrier.getContent();
		return s;
	}

	public GLElementDimensionDesc getDesc(EDimension dim) {
		return getSwitcher().getActiveDesc(dim);
	}

	public boolean hasLocator(EDimension dim) {
		return getDesc(dim).hasLocation();
	}

	@Override
	public void pick(Pick pick) {
		final Domino domino = findDomino();
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!barrier.isPickable())
				context.getMouseLayer().addDragSource(this);
			domino.select(SelectionType.MOUSE_OVER, this, false);
			repaint();
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
			domino.clear(SelectionType.MOUSE_OVER, null);
			repaint();
			break;
		case CLICKED:
			armed = true;
			break;
		case MOUSE_RELEASED:
			if (armed) {
				IMouseEvent event = (IMouseEvent) pick;
				boolean ctrl = event.isCtrlDown();
				if (domino.isSelected(SelectionType.SELECTION, this))
					domino.clear(SelectionType.SELECTION, ctrl ? this : null);
				else
					domino.select(SelectionType.SELECTION, this, ctrl);
				repaint();
				armed = false;
			}
			break;
		case DOUBLE_CLICKED:
			getNode().selectAll();
			break;
		default:
			break;
		}
	}

	public void selectMe() {
		final Domino domino = findDomino();
		if (!domino.isSelected(SelectionType.SELECTION, this))
			domino.select(SelectionType.SELECTION, this, true);
		repaint();
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDragSource(this);
		super.takeDown();
	}

	public void setData(TypedListGroup dimData, TypedListGroup recData) {
		this.dimData = dimData;
		this.recData = recData;
		for (int i = 0; i < 4; ++i)
			neighbors[i] = null;
		build();
	}

	/**
	 * @param dimension
	 * @return
	 */
	public TypedListGroup getData(EDimension dim) {
		return dim.select(dimData, recData);
	}

	@Override
	public String getLabel() {
		StringBuilder b = new StringBuilder();
		b.append(parent.getLabel());
		boolean isDim = !TypedGroupList.isUngrouped(dimData);
		boolean isRec = !TypedGroupList.isUngrouped(recData);
		if (isDim && !isRec)
			b.append(" ").append(dimData.getLabel());
		else if (isRec && !isDim) {
			b.append(" ").append(recData.getLabel());
		} else if (isRec && isDim) {
			b.append(" ").append(dimData.getLabel()).append("/").append(recData.getLabel());
		}
		GLElementFactorySwitcher s = getSwitcher();
		if (s != null)
			b.append(" shown as " + getSwitcher().getActiveSupplier().getLabel());
		return b.toString();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// Color c = recData.getColor();
		// g.color(c).fillRect(0, 0, w, h);
		final Domino domino = findDomino();
		g.lineWidth(2);
		if (domino.isSelected(SelectionType.SELECTION, this))
			g.color(SelectionType.SELECTION.getColor());
		else if (domino.isSelected(SelectionType.MOUSE_OVER, this))
			g.color(SelectionType.MOUSE_OVER.getColor());
		else
			g.color(Color.BLACK).lineWidth(1);
		g.drawRect(0, 0, w, h);
		g.lineWidth(1);

		if (domino.isShowDebugInfos()) {
			g.drawText(getLabel(), -100, h * 0.5f - 5, w + 200, 10, VAlign.CENTER);
		}
		super.renderImpl(g, w, h);
	}

	/**
	 * @param dir
	 * @param nodeGroup
	 */
	public void setNeighbor(EDirection dir, NodeGroup neighbor) {
		this.neighbors[dir.ordinal()] = neighbor;
	}

	public NodeGroup getNeighbor(EDirection dir) {
		return neighbors[dir.ordinal()];
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		final Domino domino = findDomino();
		Set<NodeGroup> selected = domino.getSelection(SelectionType.SELECTION);
		selected = new HashSet<>(selected);
		selected.add(this);
		Node single = getSingleNode(selected);
		if (single != null) {
			EventPublisher.trigger(new HideNodeEvent().to(single));
			return new NodeDragInfo(event.getMousePos(), single);
		}
		Set<NodeGroup> s = compress(selected);
		if (s.size() <= 1)
			return new NodeGroupDragInfo(event.getMousePos(), this);
		return new MultiNodeGroupDragInfo(event.getMousePos(), this, s);
	}

	private static Node getSingleNode(Set<NodeGroup> selection) {
		if (selection.isEmpty())
			return null;
		Node node = selection.iterator().next().getNode();
		for (NodeGroup group : selection) {
			Node n = group.getNode();
			if (node != n)
				return null;
		}
		if (node.size() == selection.size()) // all of the element
			return node;
		return null;
	}

	/**
	 * @param selected
	 * @return
	 */
	private Set<NodeGroup> compress(Set<NodeGroup> selected) {
		Set<NodeGroup> linked = new HashSet<>(selected.size());
		compress(this, selected, linked, null);
		return linked;
	}


	private void compress(NodeGroup n, Set<NodeGroup> selected, Set<NodeGroup> linked, EDirection commingFrom) {
		linked.add(n);
		selected.remove(n);
		for (EDirection dir : EDirection.values()) {
			if (dir == commingFrom)
				continue;
			NodeGroup f = n.findNeigbhor(dir, selected);
			if (f != null) {
				compress(f, selected, linked, dir);
			}
		}
	}

	public NodeGroup findNeigbhor(EDirection dir, Set<NodeGroup> selected) {
		NodeGroup g = this;
		while (g != null && !selected.contains(g))
			g = g.getNeighbor(dir);
		return g;
	}

	private Domino findDomino() {
		return findParent(Domino.class);
	}

	@Override
	public void onDropped(IDnDItem info) {
		if (info.getType() == EDnDType.MOVE && info.getInfo() instanceof NodeGroupDragInfo) {
			parent.removeGroup(this);
		}
		if (info.getInfo() instanceof NodeDragInfo) {
			getNode().showAgain();
		}

	}

	@Override
	public GLElement createUI(IDragInfo info) {
		findDomino().addPlaceholdersFor(parent);
		if (info instanceof ADragInfo)
			return ((ADragInfo) info).createUI(findDomino());
		return null;
	}

	/**
	 * @return
	 */
	public Node getNode() {
		return parent;
	}

	/**
	 * @return
	 */
	public Node toNode() {
		Node n = new Node(parent, parent.data, getLabel(), new TypedGroupSet(dimData.asSet()), new TypedGroupSet(
				recData.asSet()));
		return n;
	}

	/**
	 * @return
	 */
	public boolean canBeRemoved() {
		return getNode().canRemoveGroup(this);
	}

	/**
	 *
	 */
	public void removeMe() {
		prepareRemoveal();
		getNode().removeGroup(this);
	}

	/**
	 *
	 */
	public void prepareRemoveal() {
		Domino d = findDomino();
		if (d == null)
			return;
		d.clear(SelectionType.MOUSE_OVER, null);
		d.clear(SelectionType.SELECTION, this);
	}

	public void select(EDirection dir) {
		selectMe();
		NodeGroup g = getNeighbor(dir);
		if (g != null)
			g.select(dir);
	}

	public void setContentPickable(boolean pickable) {
		barrier.setPickable(pickable);
	}

	public ILocator getLocator(final EDimension dim) {
		ILocator desc = getDesc(dim);
		return GLLocation.shift(desc, Node.BORDER);
	}

	/**
	 * @return
	 */
	public GLElement createVisParameter() {
		return getSwitcher().createParameter();
	}
}
