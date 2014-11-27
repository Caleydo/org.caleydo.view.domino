/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.data.StratificationDataValue;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.undo.ZoomSelectionInfosCmd;

import com.google.common.collect.ImmutableSortedSet;


/**
 * @author Samuel Gratzl
 *
 */
public class SelectionInfo extends AItem {
	private static final int LABEL_WIDTH = 100;
	private final SelectionManager manager;

	private final int total;
	/**
	 * @param undo
	 */
	public SelectionInfo(SelectionManager manager, Domino domino) {
		super(domino.getUndo());
		this.dim = EDimension.DIMENSION;
		this.manager = manager;
		this.total = Ruler.getTotalMax(getIDCategory());
		Vec2f s = Node.initialScaleFactors(domino.getSize(), total, 1);
		setScaleFactor(s.x() * 0.5f);
	}

	public IDCategory getIDCategory() {
		return manager.getIDType().getIDCategory();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		float f = dim.select(w - LABEL_WIDTH - 4, h - 20 - 4) / total; // FIXME
		int items = getNumSelectedItems();
		if (dim.isHorizontal()) {
			g.drawText(getLabel(), 2, 2, LABEL_WIDTH - 4, h - 6, VAlign.RIGHT);
			g.color(0.95f).fillRect(LABEL_WIDTH, 0, w - LABEL_WIDTH, h);
			g.color(SelectionType.SELECTION.getColor()).fillRect(LABEL_WIDTH+2, 2, items * f, h-4);
			g.drawText("" + items, LABEL_WIDTH, 2, w - LABEL_WIDTH, h - 6, VAlign.CENTER);
			g.color(Color.BLACK).drawRect(LABEL_WIDTH, 0, w - LABEL_WIDTH, h);
			g.drawText("" + total, w + 2, 2, LABEL_WIDTH, h - 6);
		} else {
			g.drawText(getLabel(), -50, 0, w + 100, 14, VAlign.CENTER);
			g.color(0.95f).fillRect(0, 20, w, h - 20);
			g.color(SelectionType.SELECTION.getColor()).fillRect(2, 20 + 2, w - 4, items * f);
			g.drawText("" + items, 0, 20 + (h - 20 - 14) * 0.5f, w, 14, VAlign.CENTER);
			g.color(Color.BLACK).drawRect(0, 20, w, h - 20);
			g.drawText("" + total, -50, h + 2, w + 100, 14, VAlign.CENTER);
		}
		super.renderImpl(g, w, h);
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		Vec2f relative = toRelative(event.getMousePos());
		float pos = dim.select(relative);
		if (((dim.isHorizontal() && pos > LABEL_WIDTH) || (dim.isVertical() && pos > 20)) && getNumSelectedItems() > 0) {
			return new NodeDragInfo(event.getMousePos(), createNode(manager, dim));
		}
		return super.startSWTDrag(event);
	}

	/**
	 * @return
	 */
	private int getNumSelectedItems() {
		return manager.getNumberOfElements(SelectionType.SELECTION);
	}

	private String getLabel() {
		return manager.getIDType().getIDCategory().getCategoryName();
	}

	public static Node createNode(SelectionManager manager, EDimension dim) {
		Set<Integer> elements = manager.getElements(SelectionType.SELECTION);
		TypedSet data = new TypedSet(elements, manager.getIDType());

		String label = getLabel(elements, manager.getIDType());
		StratificationDataValue d = new StratificationDataValue(label, data, dim);
		return new Node(d);
	}

	private static String getLabel(Set<Integer> elements, IDType idType) {
		String label = idType.getIDCategory().getCategoryName();
		IDMappingManager manager = IDMappingManagerRegistry.get().getIDMappingManager(idType);
		IIDTypeMapper<Integer, String> id2label = manager.getIDTypeMapper(idType, idType.getIDCategory()
				.getHumanReadableIDType());
		if (id2label != null) {
			Set<String> r = id2label.apply(elements);
			if (r != null) {
				ImmutableSortedSet<String> b = ImmutableSortedSet.orderedBy(String.CASE_INSENSITIVE_ORDER).addAll(r)
						.build();
				if (b.size() < 3) {
					label = StringUtils.join(b, ", ");
				} else {
					label = StringUtils.join(b.asList().subList(0, 3), ", ") + " ...";
				}
			}
		}
		return label;
	}

	/**
	 * @param f
	 */
	public void setScaleFactor(float f) {
		if (dim.isHorizontal())
			setSize(LABEL_WIDTH + total * f, 20);
		else
			setSize(20, 20 + total * f);
	}

	/**
	 * @return the scaleFactor, see {@link #scaleFactor}
	 */
	public float getScaleFactor() {
		Vec2f s = getSize();
		if (dim.isHorizontal()) {
			return (s.x() - LABEL_WIDTH) / total;
		} else {
			return (s.y() - 20) / total;
		}
	}

	@Override
	protected void zoomCmd(float change) {
		// zoom all selection infos at the same time
		final Domino domino = findParent(Domino.class);
		UndoStack undo = domino.getUndo();
		Vec2f s = getSize();
		float new_;
		if (dim.isHorizontal()) {
			new_ = s.x() + change - LABEL_WIDTH;
		} else {
			new_ = s.y() - 20 + change;
		}
		if (new_ < 10)
			return;
		float factor = new_ / total;
		undo.push(new ZoomSelectionInfosCmd(domino.getOutlerBlocks().selectionInfos(), factor));
	}
}
