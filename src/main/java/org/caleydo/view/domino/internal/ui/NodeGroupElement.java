/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin.ISelectionMixinCallback;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDCreator;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.base.IUniqueObject;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.Borders;
import org.caleydo.core.view.opengl.layout2.renderer.Borders.IBorderGLRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.DominoGraph;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.dnd.SubNodeDragInfo;
import org.caleydo.view.domino.internal.event.HidePlaceHoldersEvent;
import org.caleydo.view.domino.spi.model.graph.INode;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeGroupElement extends GLElementDecorator implements IDragGLSource, ILabeled, ISelectionMixinCallback,
		IPickingListener, IUniqueObject {
	protected static final int BORDER = 2;

	private final int id = IDCreator.createVMUniqueID(NodeGroupElement.class);

	private final PickingBarrier barrier;
	private final INodeUI nodeUI;
	private final IBorderGLRenderer border;

	private TypedListGroup dimData;
	private TypedListGroup recData;

	private boolean isDefault = false;

	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

	public NodeGroupElement(INode node) {
		this.nodeUI = node.createUI();
		this.barrier = new PickingBarrier(nodeUI.asGLElement());
		this.border = Borders.createBorder(Color.BLACK);
		setContent(this.barrier);
		setVisibility(EVisibility.PICKABLE);
		onPick(this);
		selections.add(DominoNodeLayer.newNodeGroupSelectionManager());
		selections.add(DominoGraph.newNodeSelectionManager());
	}

	@Override
	public int getID() {
		return id;
	}

	public INode asNode() {
		return nodeUI.asNode();
	}

	public NodeElement getParentNode() {
		return (NodeElement) super.getParent();
	}

	@Override
	protected void layoutContent(IGLLayoutElement content, float w, float h, int deltaTimeMs) {
		content.setBounds(BORDER, BORDER, w - 2 * BORDER, h - 2 * BORDER);
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		if (isDefault())
			return new NodeDragInfo(nodeUI.asNode(), event.getMousePos());
		return new SubNodeDragInfo(nodeUI.asNode(), getLabel(), dimData, recData, event.getMousePos());
	}

	/**
	 * @param isDefault
	 *            setter, see {@link isDefault}
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * @return the isDefault, see {@link #isDefault}
	 */
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public void onDropped(IDnDItem info) {
		if (!isDefault() && info.getType() == EDnDType.MOVE) {
			removeGroup();
		}
		EventPublisher.trigger(new HidePlaceHoldersEvent().to(findParent(DominoNodeLayer.class)));
	}

	/**
	 *
	 */
	public void removeGroup() {
		NodeElement p = findParent(NodeElement.class);
		p.removeGroup(dimData, recData);
	}

	@Override
	public GLElement createUI(IDragInfo info) {
		NodeDragInfo d = ((NodeDragInfo) info);
		Vec2f mousePos = d.getMousePos();
		Vec2f loc = getAbsoluteLocation();
		Vec2f offset = mousePos.minus(loc);
		d.setOffset(offset);
		return d.createUI();
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		if (manager.getIDType() == DominoGraph.NODE_IDTYPE)
			return;
		SelectionType s = manager.getHighestSelectionType(id);
		int target = s == null ? 2 : 3;
		if (target == border.getWidth())
			return;
		border.setColor(s == null ? Color.BLACK : s.getColor());
		border.setWidth(s == null ? 2 : 3);
		repaint();
	}

	protected boolean isSelectState() {
		return barrier.getVisibility() == EVisibility.PICKABLE;
	}

	public ENodeUIState getState() {
		return isSelectState() ? ENodeUIState.SELECT : ENodeUIState.MOVE;
	}

	/**
	 * @param move
	 */
	public void setState(ENodeUIState state) {
		if (state == ENodeUIState.MOVE) {
			content.setVisibility(EVisibility.VISIBLE);
		} else
			content.setVisibility(EVisibility.PICKABLE);
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!isSelectState())
				context.getMouseLayer().addDragSource(this);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
			break;
		case MOUSE_RELEASED:
			select(SelectionType.SELECTION, !isSelected(SelectionType.SELECTION), !((IMouseEvent) pick).isCtrlDown());
			break;
		// case RIGHT_CLICKED:
		// findGraph().remove(node);
		// break;
		case DOUBLE_CLICKED:
			// graph.remove(node);
			setState(getState().opposite());
			if (isSelectState()) {
				border.setColor(Color.RED);
				context.getMouseLayer().removeDragSource(this);
			} else {
				border.setColor(Color.BLUE);
				context.getMouseLayer().addDragSource(this);
			}
			break;
		default:
			break;
		}
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			select(SelectionType.MOUSE_OVER, true, true);
			break;
		case MOUSE_OUT:
			select(SelectionType.MOUSE_OVER, false, true);
			break;
		default:
			break;
		}
	}

	public boolean isSelected(SelectionType type) {
		return selections.get(0).checkStatus(type, id);
	}

	/**
	 * @param mouseOver
	 * @param b
	 */
	private void select(SelectionType type, boolean enable, boolean clear) {
		{
			SelectionManager m = selections.get(0);
			if (clear)
				m.clearSelection(type);
			if (enable)
				m.addToType(type, id);
			else
				m.removeFromType(type, id);
			selections.fireSelectionDelta(m);
			onSelectionUpdate(m);
		}
		{
			SelectionManager m = selections.get(1);
			if (clear)
				m.clearSelection(type);
			if (enable)
				m.addToType(type, asNode().getID());
			else
				m.removeFromType(type, asNode().getID());
			selections.fireSelectionDelta(m);
		}
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// g.color(Color.BLACK).drawRect(0, 0, w, h);
		super.renderImpl(g, w, h);
		border.render(g, w, h, this);
		g.incZ();
		g.incZ();
		float wi = Math.max(100, w);
		float x = (w - wi) * 0.5f;
		g.drawText(getLabel(), x, (h - 10) * .5f, wi, 10, VAlign.CENTER);
		g.decZ();
		g.decZ();
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDragSource(this);
		select(SelectionType.SELECTION, false, false);
		super.takeDown();
	}

	@Override
	public String getLabel() {
		StringBuilder b = new StringBuilder();
		b.append(nodeUI.asNode().getLabel());
		boolean isDim = !TypedGroupList.isUngrouped(dimData);
		boolean isRec = !TypedGroupList.isUngrouped(recData);
		if (isDim && !isRec)
			b.append(" ").append(dimData.getLabel());
		else if (isRec && !isDim) {
			b.append(" ").append(recData.getLabel());
		} else if (isRec && isDim) {
			b.append(" ").append(dimData.getLabel()).append("/").append(recData.getLabel());
		}
		return b.toString();
	}

	public void setData(TypedListGroup dimData, TypedListGroup recData) {
		this.dimData = dimData;
		this.recData = recData;
		this.nodeUI.setData(EDimension.DIMENSION, dimData);
		this.nodeUI.setData(EDimension.RECORD, recData);
	}

	/**
	 * @return
	 */
	public boolean canBeRemoved() {
		return !isDefault && !getParentNode().has2DimGroups();
	}

	/**
	 * @param dim
	 * @return
	 */
	public double getSize(EDimension dim) {
		return nodeUI.getSize(dim);
	}

	/**
	 * @return
	 */
	public Vec2f getPreferredSize() {
		Vec2f s = getNodeSize().copy();
		Vec2f z = asNode().getUIState().getZoom();
		s.setX(s.x() * z.x());
		s.setY(s.y() * z.y());
		s.add(new Vec2f(BORDER * 4, BORDER * 4));
		return s;
	}

	protected Vec2f getNodeSize() {
		return new Vec2f((float) nodeUI.getSize(EDimension.DIMENSION), (float) nodeUI.getSize(EDimension.RECORD));
	}

	public enum ENodeUIState {
		MOVE, SELECT;

		/**
		 * @return
		 */
		public ENodeUIState opposite() {
			return this == MOVE ? SELECT : MOVE;
		}
	}
}
