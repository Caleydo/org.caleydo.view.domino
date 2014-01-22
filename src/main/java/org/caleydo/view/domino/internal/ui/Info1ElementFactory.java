/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2;
import org.caleydo.core.view.opengl.util.text.ETextStyle;
import org.caleydo.view.domino.api.model.typed.ITypedGroupCollection;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;

import com.google.common.collect.Sets;
/**
 * @author Samuel Gratzl
 *
 */
public class Info1ElementFactory implements IGLElementFactory2 {

	@Override
	public String getId() {
		return "info1";
	}

	@Override
	public GLElement create(GLElementFactoryContext context) {
		IDType idType = context.get(IDType.class, null);
		@SuppressWarnings("unchecked")
		List<Integer> data = context.get(List.class, null);
		TypedList l;
		if (data instanceof TypedList)
			l = ((TypedList) data);
		else
			l = new TypedList(data, idType);
		return new InfoElement(l);
	}

	@Override
	public boolean apply(GLElementFactoryContext context) {
		return context.get(TypedList.class, null) != null
				|| (context.get(List.class, null) != null && context.get(IDType.class, null) != null);
	}

	@Override
	public GLElementDimensionDesc getDesc(EDimension dim, GLElement elem) {
		return GLElementDimensionDesc.newFix(dim.select(200, 50)).build();
	}

	@Override
	public GLElement createParameters(GLElement elem) {
		return null;
	}

	private static final class InfoElement extends GLElement implements
			MultiSelectionManagerMixin.ISelectionMixinCallback {

		private final TypedList data;
		@DeepScan
		private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

		public InfoElement(TypedList data) {
			this.data = data;
			selections.add(new SelectionManager(data.getIdType()));
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(Color.WHITE).fillRect(0, 0, w, h);

			List<String> lines = new ArrayList<>();
			lines.add("#items: " + data.size());
			TypedSet s = data.asSet();
			SelectionManager manager = selections.get(0);
			int groups = data instanceof ITypedGroupCollection ? ((ITypedGroupCollection) data).getGroups().size() : 1;
			lines.add("#groups: " + groups);

			int selected = Sets.intersection(s, manager.getElements(SelectionType.SELECTION)).size();
			lines.add(String.format("#selected: %d (%.2f%%)", selected, selected * 100 / (float) s.size()));

			int mouseOvers = Sets.intersection(s, manager.getElements(SelectionType.MOUSE_OVER)).size();
			lines.add(String.format("#mouse overs: %d (%.2f%%)", mouseOvers, mouseOvers * 100 / (float) s.size()));

			g.drawText(lines, 0, 0, w, h - 4, 1, VAlign.LEFT, ETextStyle.PLAIN);

			super.renderImpl(g, w, h);
		}

		@Override
		public void onSelectionUpdate(SelectionManager manager) {
			repaint();
		}

	}

}
