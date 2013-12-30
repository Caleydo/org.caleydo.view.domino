/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;

import v2.data.IDataValues;

/**
 * @author Samuel Gratzl
 *
 */
public class NodeGroup extends GLElementContainer implements ILabeled, IDragGLSource, IPickingListener {
	private final Node parent;
	private final IDataValues data;

	private final NodeGroup[] neighbors = new NodeGroup[4];
	private TypedListGroup dimData;
	private TypedListGroup recData;
	private boolean hovered;


	public NodeGroup(Node parent, IDataValues data) {
		this.parent = parent;
		this.data = data;
		setVisibility(EVisibility.PICKABLE);
		onPick(this);
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDragSource(this);
			hovered = true;
			repaint();
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
			hovered = false;
			repaint();
			break;
		default:
			break;
		}
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
		setSize(dimData.size(), recData.size());
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
		return b.toString();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(hovered ? Color.RED : Color.LIGHT_RED).fillRect(0, 0, w, h);
		g.color(Color.BLACK).drawRect(0, 0, w, h);
		g.drawText(getLabel(), 0, h * 0.5f - 5, w, 10, VAlign.CENTER);
		super.renderImpl(g, w, h);
	}

	/**
	 * @param dir
	 * @param nodeGroup
	 */
	public void setNeighbor(EDirection dir, NodeGroup neighbor) {
		this.neighbors[dir.ordinal()] = neighbor;
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		return new NodeGroupDragInfo(event.getMousePos(), this);
	}

	@Override
	public void onDropped(IDnDItem info) {
		if (info.getType() == EDnDType.MOVE) {
			parent.removeGroup(this);
		}
	}

	@Override
	public GLElement createUI(IDragInfo info) {
		findParent(Domino.class).addPlaceholdersFor(parent);
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
		return new Node(data, getLabel(), new TypedGroupSet(dimData.asSet()), new TypedGroupSet(recData.asSet()));
	}
}
