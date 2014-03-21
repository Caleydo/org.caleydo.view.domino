/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.ScaleLogic;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.data.StratificationDataValue;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.dnd.RulerDragInfo;
import org.caleydo.view.domino.internal.toolbar.RulerTools;
import org.caleydo.view.domino.internal.undo.ZoomRulerCmd;

/**
 * @author Samuel Gratzl
 *
 */
public class Ruler extends GLElementContainer implements IDragGLSource, IPickingListener, IGLLayout2 {
	private final SelectionManager manager;
	private int maxElements;
	private float scaleFactor = 1.f;
	private EDimension dim = EDimension.RECORD;
	private boolean hovered = false;

	public Ruler(SelectionManager manager, UndoStack undo) {
		setLayout(this);
		this.manager = manager;
		maxElements = Math.min(100, getTotalMax(manager.getIDType().getIDCategory()));

		setVisibility(EVisibility.PICKABLE);
		onPick(this);

		this.add(createToolBar(undo));

		updateSize();
	}

	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	/**
	 * @return
	 */
	private GLElement createToolBar(UndoStack undo) {
		RulerTools tools = new RulerTools(undo, this);
		tools.setSize(tools.getWidth(24), 24);
		return tools;
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		final IGLLayoutElement toolbar = children.get(0);
		if (hovered) {
			float wi = toolbar.getSetWidth();
			if (wi > w)
				toolbar.setBounds((w - wi) * 0.5f, -24, wi, 24);
			else
				toolbar.setBounds(w - wi, -24, wi, 24);
		} else
			toolbar.hide();
		return false;
	}

	public IDType getIDType() {
		return manager.getIDType();
	}

	public IDCategory getIDCategory() {
		return manager.getIDType().getIDCategory();
	}

	/**
	 * @param idCategory
	 * @return
	 */
	public static int getTotalMax(IDCategory category) {
		return IDMappingManagerRegistry.get().getIDMappingManager(category)
				.getAllMappedIDs(category.getPrimaryMappingType()).size();
	}

	/**
	 * @return the manager, see {@link #manager}
	 */
	public SelectionManager getManager() {
		return manager;
	}

	public void transpose() {
		this.dim = this.dim.opposite();
		updateSize();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		float max = dim.select(w, h);
		float f = max / maxElements;

		renderSelection(g, w, h, f);
		renderBaseAxis(g, w, h);
		// renderMarkers(g, w, h, f);

		super.renderImpl(g, w, h);
	}
	/**
	 * @param maxElements
	 *            setter, see {@link maxElements}
	 */
	public void setMaxElements(int maxElements) {
		if (this.maxElements == maxElements)
			return;
		if (maxElements == 0) {// no invalid values
			maxElements = 100;
		}
		this.maxElements = maxElements;
		updateSize();
	}

	/**
	 * @return the maxElements, see {@link #maxElements}
	 */
	public int getMaxElements() {
		return maxElements;
	}

	private void updateSize() {
		float new_ = scaleFactor * maxElements;
		if (dim.isHorizontal())
			setSize(new_, 20);
		else
			setSize(20, new_);
		relayout();
	}

	// private void renderMarkers(GLGraphics g, float w, float h, float f) {
	// int markerDelta = determineMarkerStep(maxElements, dim.select(w, h));
	// for (int i = 0; i <= maxElements; i += markerDelta) {
	// float v = f * i;
	// if (dim.isHorizontal()) {
	// g.drawLine(v, 0, v, h*0.5f);
	// } else {
	// g.drawLine(0, v, w*0.5f, v);
	// }
	// }
	// if (dim.isHorizontal()) {
	// g.drawLine(w, 0, w, h * 0.5f);
	// } else {
	// g.drawLine(0, h, w * 0.5f, h);
	// }
	// renderMarkerLabels(g, w, h, f, markerDelta);
	// }

	// private void renderMarkerLabels(GLGraphics g, float w, float h, float f, int markerDelta) {
	// int drawLabelsDelta = markerDelta * Math.round((float) Math.ceil(dim.select(30, 10) / (f * markerDelta)));
	// final float hi = dim.isHorizontal() ? Math.min(h * 0.5f, 10) : Math.min(f * drawLabelsDelta, 10);
	// for (int i = 0; i < maxElements; i += drawLabelsDelta) {
	// float v = f * i;
	// if (dim.isHorizontal()) {
	// g.drawText(i + "", v + 2, h * 0.5f, f * drawLabelsDelta, hi);
	// } else {
	// g.drawText(i + "", 3, v + 1, w - 3, hi);
	// }
	// }
	// if (dim.isHorizontal()) {
	// g.drawText(maxElements + "", w - f * drawLabelsDelta - 2, h * 0.5f, f * drawLabelsDelta, hi, VAlign.RIGHT);
	// } else {
	// g.drawText(maxElements + "", 3, h - hi - 2, w - 3, hi);
	// }
	// }

	private void renderBaseAxis(GLGraphics g, float w, float h) {
		final String label = getMaxElements() + " " + getLabel(manager);
		if (hovered)
			g.lineWidth(3);
		// g.drawRect(0, 0, w, h);
		final int stitch = 12;
		if (dim.isHorizontal()) {
			// g.drawText(label, -306, h - 14, 300, stitch, VAlign.RIGHT);
			g.drawText(label, w + 6, h - 14, 300, stitch);
			g.drawLine(0, h, w, h);
			g.drawLine(0, h, 0, h - stitch);
			g.drawLine(w, h, w, h - stitch);
		} else {
			g.drawText(label, 0, -16, 300, stitch);
			g.drawLine(0, 0, 0, h);
			g.drawLine(0, 0, stitch, 0);
			g.drawLine(0, h, stitch, h);
		}
		g.lineWidth(1);
	}

	private void renderSelection(GLGraphics g, float w, float h, float f) {
		int items = getNumSelectedItems();
		if (items > 0) {
			g.color(SelectionType.SELECTION.getColor());
			if (dim.isHorizontal()) {
				g.fillRect(2, h - 12, items * f, 10);
			} else {
				g.fillRect(2, 2, 10, items * f);
			}
		}
	}

	// private static int determineMarkerStep(int maxElements, float size) {
	// if (maxElements <= 100)
	// return 10; // 10x
	// if (maxElements <= 250)
	// return 25; // 10x
	// if (maxElements <= 500)
	// return 50; // 10x
	// if (maxElements <= 1000)
	// return 100;
	// return 250;
	// // return Math.max(maxElements / 10, 1);
	// }

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_WHEEL:
			final Vec2f bak = getSize();
			Vec2f shift = ScaleLogic.shiftLogic(((IMouseEvent) pick), bak);
			float change = dim.select(shift);
			if (change == 0)
				return;
			float ori = scaleFactor * maxElements;
			float new_ = Math.max(ori + change, 1);
			float scale = new_ / maxElements;

			UndoStack undo = findParent(Domino.class).getUndo();
			undo.push(new ZoomRulerCmd(getIDCategory(), scale, this.scaleFactor));
			break;
		case MOUSE_OVER:
			context.getMouseLayer().addDragSource(this);
			hovered = true;
			relayout();
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
			hovered = false;
			relayout();
			break;
		case RIGHT_CLICKED:
			context.getSWTLayer().showContextMenu(((RulerTools) get(0)).asContextMenu());
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

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		int size = getNumSelectedItems();
		Vec2f relative = toRelative(event.getMousePos());
		float total = dim.select(getSize());
		float pos = dim.select(relative);
		float selected = total * (size / (float) maxElements);
		if (pos <= selected)
			return new NodeDragInfo(event.getMousePos(), createNode());
		return new RulerDragInfo(event.getMousePos(), this);
	}

	/**
	 * @return
	 */
	private int getNumSelectedItems() {
		return manager.getNumberOfElements(SelectionType.SELECTION);
	}

	private String getLabel(SelectionManager manager) {
		return manager.getIDType().getIDCategory().getCategoryName();
	}

	protected Node createNode() {
		Set<Integer> elements = manager.getElements(SelectionType.SELECTION);
		TypedSet data = new TypedSet(elements, manager.getIDType());
		StratificationDataValue d = new StratificationDataValue(getLabel(manager), data, this.dim);
		return new Node(d);
	}

	@Override
	public void onDropped(IDnDItem info) {

	}

	@Override
	public GLElement createUI(IDragInfo info) {
		if (info instanceof ADragInfo) {
			return ((ADragInfo) info).createUI(findParent(Domino.class));
		}
		return null;
	}

	/**
	 * @param shift
	 */
	public void shiftLocation(Vec2f shift) {
		Vec2f loc = getLocation();
		setLocation(loc.x() + shift.x(), loc.y() + shift.y());
	}

	/**
	 * @param shift
	 */
	public void zoom(Vec2f shift) {
		float change = dim.select(shift);
		if (change == 0)
			return;
		float ori = scaleFactor * maxElements;
		float new_ = Math.max(ori + change, 1);
		float scale = new_ / maxElements;

		this.scaleFactor = scale;
		updateSize();
	}

	/**
	 * @param scale
	 */
	public void zoom(float scale) {
		this.scaleFactor = scale;
		updateSize();
	}

	/**
	 * @return the scaleFactor, see {@link #scaleFactor}
	 */
	public float getScaleFactor() {
		return scaleFactor;
	}
}
