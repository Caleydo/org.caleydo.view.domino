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
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.data.StratificationDataValue;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;

import com.google.common.collect.ImmutableSortedSet;


/**
 * @author Samuel Gratzl
 *
 */
public class SelectionInfo extends AItem {
	private static final int LABEL_WIDTH = 50;
	private final SelectionManager manager;

	/**
	 * @param undo
	 */
	public SelectionInfo(SelectionManager manager, UndoStack undo) {
		super(undo);
		this.manager = manager;
		if (dim.isHorizontal())
			setSize(200, 20);
		else
			setSize(20, 200);
	}

	public IDCategory getIDCategory() {
		return manager.getIDType().getIDCategory();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		float f = w / 100; // FIXME
		int items = getNumSelectedItems();
		if (dim.isHorizontal()) {
			g.drawText(getLabel(), 2, 2, LABEL_WIDTH - 4, h - 6);
			g.color(0.95f).fillRect(LABEL_WIDTH, 0, w - LABEL_WIDTH, h);
			g.color(SelectionType.SELECTION.getColor()).fillRect(LABEL_WIDTH+2, 2, items * f, h-4);
			g.color(Color.BLACK).drawRect(LABEL_WIDTH, 0, w - LABEL_WIDTH, h);
		} else {
			g.color(0.95f).fillRect(LABEL_WIDTH, 0, w - LABEL_WIDTH, h);
			g.color(SelectionType.SELECTION.getColor()).fillRect(2, LABEL_WIDTH+2, w-4, items * f);
			g.color(Color.BLACK).drawRect(0, LABEL_WIDTH, w, h - LABEL_WIDTH);
		}
		super.renderImpl(g, w, h);
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		Vec2f relative = toRelative(event.getMousePos());
		float pos = dim.select(relative);
		if (pos > LABEL_WIDTH && getNumSelectedItems() > 0) {
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
}
