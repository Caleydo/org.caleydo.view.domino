/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
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
import org.caleydo.view.rnb.api.model.EDirection;
import org.caleydo.view.rnb.api.model.typed.TypedGroupSet;
import org.caleydo.view.rnb.api.model.typed.TypedGroups;
import org.caleydo.view.rnb.api.model.typed.TypedListGroup;
import org.caleydo.view.rnb.internal.data.IDataValues;
import org.caleydo.view.rnb.internal.dnd.ADragInfo;
import org.caleydo.view.rnb.internal.dnd.BlockDragInfo;
import org.caleydo.view.rnb.internal.dnd.NodeDragInfo;
import org.caleydo.view.rnb.internal.dnd.NodeGroupDragInfo;
import org.caleydo.view.rnb.internal.prefs.MyPreferences;
import org.caleydo.view.rnb.internal.ui.PickingBarrier;

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

	NodeGroup(Node parent) {
		this.parent = parent;
		setVisibility(EVisibility.PICKABLE);
		onPick(this);
		this.barrier = new PickingBarrier();
		setContent(barrier);
	}

	@Override
	protected void layoutContent(IGLLayoutElement content, float w, float h, int deltaTimeMs) {
		content.setBounds(Node.BORDER, Node.BORDER, w - 2 * Node.BORDER, h - 2 * Node.BORDER);
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		RnB rnb = findRnB();
		setContentPickable(rnb.getTool() == EToolState.SELECT);
		if (dimData != null && recData != null)
			build();
	}

	public boolean isValid() {
		return context != null;
	}

	private void build() {
		if (context == null)
			return;
		Builder b = GLElementFactoryContext.builder();
		final Node parent = getNode();
		final IDataValues data = parent.getDataValues();
		final boolean[] neighbors = getNeighborExistence();
		boolean transpose = parent.isDependentTranspose();
		if (!neighbors[0] && !neighbors[1] && !neighbors[2] && !neighbors[3])
			transpose = false;
		data.fill(b, dimData, recData, neighbors, transpose);
		// if free high else medium
		initContext(b, parent);

		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(b.build(), "rnb."
 + data.getExtensionID(), data);
		GLElementFactorySwitcher s = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
		parent.selectDefaultVisualization(s);
		barrier.setContent(s);
	}

	private void initContext(Builder b, final Node parent) {
		b.put(EDetailLevel.class,
				parent.isAlone(EDimension.DIMENSION) && parent.isAlone(EDimension.RECORD) ? EDetailLevel.HIGH
						: EDetailLevel.MEDIUM);
		b.set("heatmap.forceTextures");

		// see #114 frame colors
		b.put("kaplanmeier.frameColor", Color.LIGHT_GRAY);
		b.put("distribution.hist.frameColor", Color.LIGHT_GRAY);
		b.put("hbar.frameColor", Color.LIGHT_GRAY);


		// see #100
		b.put("selection.selected", "AUTO_BLUR_OUTLINE");
		b.put("selection.mouseover", "AUTO_FILL_OUTLINE");

		b.set("axis.renderOutsideBounds");
		// see #122
		b.put("axis.valueGlyph", "RECT");
	}

	/**
	 * @return
	 */
	private boolean[] getNeighborExistence() {
		boolean[] r = new boolean[4];
		for (EDirection dir : EDirection.values())
			r[dir.ordinal()] = getNeighbor(dir) != null;
		return r;
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
		final NodeSelections rnb = findRnB().getSelections();
		IMouseEvent event = (IMouseEvent) pick;
		boolean ctrl = event.isCtrlDown();
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!barrier.isPickable())
				context.getMouseLayer().addDragSource(this);
			rnb.select(SelectionType.MOUSE_OVER, this, false);
			selectItems(SelectionType.MOUSE_OVER, false);
			getNode().getDataValues().onSelectionChanged(true);
			repaint();
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
			rnb.clear(SelectionType.MOUSE_OVER, (NodeGroup) null);
			getNode().getDataValues().onSelectionChanged(false);
			clearItems(SelectionType.MOUSE_OVER, false);
			repaint();
			break;
		case CLICKED:
			armed = true;
			break;
		case MOUSE_RELEASED:
			if (armed) {
				if (rnb.isSelected(SelectionType.SELECTION, this)) {
					rnb.clear(SelectionType.SELECTION, ctrl ? this : null);
					clearItems(SelectionType.SELECTION, ctrl);
				} else {
					rnb.select(SelectionType.SELECTION, this, ctrl);
					selectItems(SelectionType.SELECTION, ctrl);
				}
				repaint();
				armed = false;
			}
			break;
		case DOUBLE_CLICKED:
			getNode().selectAll();
			break;
		case RIGHT_CLICKED:
			if (!rnb.isSelected(SelectionType.SELECTION, this)) {
				rnb.select(SelectionType.SELECTION, this, ctrl);
				selectItems(SelectionType.SELECTION, ctrl);
			}
			repaint();
			context.getSWTLayer().showContextMenu(findRnB().getToolBar().asContextMenu());
			break;
		default:
			break;
		}
	}

	private void selectItems(SelectionType type, boolean additional) {
		if (!autoSelectItems()) // no auto select in select and bands mode
			return;
		final Block block = findParent(Block.class);
		for (EDimension dim : parent.dimensions()) {
			TypedListGroup ids = getData(dim);
			block.selectItems(type, ids.getIdType(), ids, additional);
		}
	}

	boolean autoSelectItems() {
		return MyPreferences.isAutoSelectItems() && findRnB().getTool() == EToolState.MOVE;
	}

	private void clearItems(SelectionType type, boolean additional) {
		if (!autoSelectItems())
			return;
		final Block block = findParent(Block.class);
		for (EDimension dim : parent.dimensions()) {
			TypedListGroup ids = getData(dim);
			block.clearItems(type, ids.getIdType(), additional ? ids : null);
		}
	}


	public void selectMe() {
		final NodeSelections rnb = findRnB().getSelections();
		if (!rnb.isSelected(SelectionType.SELECTION, this))
			rnb.select(SelectionType.SELECTION, this, true);
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
		boolean isDim = dimData != null && !TypedGroups.isUngrouped(dimData);
		boolean isRec = recData != null && !TypedGroups.isUngrouped(recData);
		if ((!isDim && !isRec) || MyPreferences.showBlockLabelInGroup())
			b.append(getNode().getLabel());
		if (isDim && !isRec)
			b.append(" ").append(dimData.getLabel());
		else if (isRec && !isDim) {
			b.append(" ").append(recData.getLabel());
		} else if (isRec && isDim) {
			b.append(" ").append(dimData.getLabel()).append("/").append(recData.getLabel());
		}
		// GLElementFactorySwitcher s = getSwitcher();
		// if (s != null)
		// b.append(" shown as " + getSwitcher().getActiveSupplier().getLabel());
		return b.toString();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// Color c = recData.getColor();
		// g.color(c).fillRect(0, 0, w, h);
		final RnB rnb = findRnB();

		if (rnb.isShowDebugInfos()) {
			g.drawText(getLabel(), -100, h * 0.5f - 5, w + 200, 10, VAlign.CENTER);
		}
		super.renderImpl(g, w, h);

		NodeSelections selections = rnb.getSelections();
		g.lineWidth(3);
		if (selections.isSelected(SelectionType.SELECTION, this))
			g.color(SelectionType.SELECTION.getColor()).drawRect(0, 0, w, h);
		else if (selections.isSelected(SelectionType.MOUSE_OVER, this))
			g.color(SelectionType.MOUSE_OVER.getColor()).drawRect(0, 0, w, h);
		// g.color(Color.LIGHT_GRAY).lineWidth(1).drawRect(0, 0, w, h);
		g.lineWidth(1);


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
		final RnB rnb = findRnB();
		return rnb.startSWTDrag(event, this);
	}

	public NodeGroup findNeigbhor(EDirection dir, Set<NodeGroup> selected) {
		NodeGroup g = this;
		while (g != null && !selected.contains(g))
			g = g.getNeighbor(dir);
		return g;
	}

	private RnB findRnB() {
		return findParent(RnB.class);
	}

	@Override
	public void onDropped(IDnDItem info) {
		if (info.getInfo() instanceof NodeDragInfo) {
			getNode().showAgain();
		} else if (info.getInfo() instanceof BlockDragInfo) {
			for (Block block : ((BlockDragInfo) info.getInfo()).getBlocks())
				block.showAgain();
		}

	}

	@Override
	public GLElement createUI(IDragInfo info) {
		if (info instanceof NodeDragInfo || info instanceof NodeGroupDragInfo)
			findRnB().addPlaceholdersFor(getNode());
		if (info instanceof ADragInfo)
			return ((ADragInfo) info).createUI(findRnB());
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
		final Node parent = getNode();
		final TypedGroupSet dim = parent.getSubGroupData(dimData, EDimension.DIMENSION);
		final TypedGroupSet rec = parent.getSubGroupData(recData, EDimension.RECORD);
		Node n = new Node(parent, parent.getDataValues(), getLabel(), dim, rec);
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
	public void prepareRemoveal() {
		barrier.setContent(null);
		this.dimData = null;
		this.recData = null;

		RnB d = findRnB();
		if (d == null)
			return;
		d.getSelections().clear(SelectionType.MOUSE_OVER, (NodeGroup) null);
		d.getSelections().clear(SelectionType.SELECTION, this);

		resetNeighbors();

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
		GLElementDimensionDesc desc = getDesc(dim);
		if (!desc.hasLocation())
			return GLLocation.NO_LOCATOR;
		return desc;
	}

	/**
	 * @return
	 */
	public GLElement createVisParameter() {
		return getSwitcher().createParameter();
	}

	/**
	 *
	 */
	public void reset() {

	}

	/**
	 *
	 */
	public void resetNeighbors() {
		for (int i = 0; i < neighbors.length; ++i)
			neighbors[i] = null;
	}

	@Override
	public String toString() {
		return getLabel();
	}
}
