/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.typed.TypedList;

import com.google.common.base.Function;
/**
 * @author Samuel Gratzl
 *
 */
public class LabelElementFactory implements IGLElementFactory2 {

	@Override
	public String getId() {
		return "labels";
	}

	@Override
	public GLElement create(GLElementFactoryContext context) {
		EDimension dim = context.get(EDimension.class, EDimension.RECORD);
		IDType idType = context.get(IDType.class, null);
		@SuppressWarnings("unchecked")
		List<Integer> data = context.get(List.class, null);
		TypedList l;
		if (data instanceof TypedList)
			l = ((TypedList) data);
		else
			l = new TypedList(data, idType);
		@SuppressWarnings("unchecked")
		Function<Integer, String> toString = context.get("id2string", Function.class, null);
		return new LabelElement(dim, l, toString, context.get("align", VAlign.class, VAlign.LEFT));
	}

	@Override
	public boolean apply(GLElementFactoryContext context) {
		return context.get(TypedList.class, null) != null
				|| (context.get(List.class, null) != null && context.get(IDType.class, null) != null);
	}

	@Override
	public GLElementDimensionDesc getDesc(EDimension dim, GLElement elem) {
		LabelElement l = (LabelElement) elem;
		return l.getDesc(dim);
	}

	@Override
	public GLElement createParameters(GLElement elem) {
		return null;
	}

	private static final class LabelElement extends PickableGLElement implements
			MultiSelectionManagerMixin.ISelectionMixinCallback {

		private final EDimension dim;
		private final TypedList data;
		private final List<String> labels;
		private VAlign align;

		@DeepScan
		private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

		public LabelElement(EDimension dim, TypedList data, Function<Integer, String> toString, VAlign align) {
			this.dim = dim;
			this.data = data;
			this.align = align;

			selections.add(new SelectionManager(data.getIdType()));
			this.labels = toString == null ? toLabels(data) : toLabels(data, toString);
		}

		/**
		 * @param dimData2
		 * @return
		 */
		private List<String> toLabels(TypedList data) {
			IDType idType = data.getIdType();
			IIDTypeMapper<Integer, String> mapper = IDMappingManagerRegistry.get()
					.getIDMappingManager(idType.getIDCategory())
					.getIDTypeMapper(idType, idType.getIDCategory().getHumanReadableIDType());
			Collection<Set<String>> mapped = mapper.applySeq(data);
			List<String> r = new ArrayList<>(data.size());
			for (Set<String> m : mapped) {
				r.add(m == null ? "Unmapped" : StringUtils.join(m, ", "));
			}
			return r;
		}

		private List<String> toLabels(TypedList data, Function<Integer, String> toString) {
			List<String> r = new ArrayList<>(data.size());
			for (Integer id : data) {
				String label = toString.apply(id);
				r.add(label == null ? "Unnamed" : label);
			}
			return r;
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(Color.WHITE).fillRect(0, 0, w, h);

			if (data.isEmpty()) {
				g.drawText("Labels", 0, (h - 12) * 0.5f, w, 12, VAlign.CENTER);
				return;
			}
			if (dim.isHorizontal()) {
				g.save().move(0, h).gl.glRotatef(-90, 0, 0, 1);
				renderLabels(g, h, w);
				g.restore();
			} else
				renderLabels(g, w, h);

			super.renderImpl(g, w, h);
		}

		/**
		 * @param g
		 * @param h
		 * @param w
		 */
		private void renderLabels(GLGraphics g, float w, float h) {
			SelectionManager manager = selections.get(data.getIdType());
			Set<Integer> mouseOvers = manager.getElements(SelectionType.MOUSE_OVER);
			Set<Integer> selected = manager.getElements(SelectionType.SELECTION);
			final int size = data.size();
			if ((h / size) > 8)
				renderUniform(g,w,h, mouseOvers, selected);
		}

			// Set<Integer> toHighlight = ImmutableSet.copyOf(Sets.intersection(Sets.union(mouseOvers, selected),
			// data.asSet()));
			// else if (toHighlight)
			//
			//
			// float hi = h / size;
			// if (hi >= 4)
			// if (hi >= 3) {
			// for (int i = 0; i < size; ++i) {
			// String l = recLabels.get(i);
			// Integer id = recData.get(i);
			// if (selected.contains(id)) {
			// g.color(SelectionType.SELECTION.getColor().brighter()).fillRect(0, i * hi, w, hi);
			// } else if (mouseOvers.contains(id)) {
			// g.color(SelectionType.MOUSE_OVER.getColor().brighter()).fillRect(0, i * hi, w, hi);
			// }
			// float ti = Math.min(hi - 1, 16);
			// g.drawText(l, 0, i * hi + (hi - ti) * 0.5f, w, ti);
			// }
			// }

		private void renderUniform(GLGraphics g, float w, float h, Set<Integer> mouseOvers, Set<Integer> selected) {
			final int size = data.size();
			float hi = h / size;
			for (int i = 0; i < size; ++i) {
				String l = labels.get(i);
				Integer id = data.get(i);
				if (selected.contains(id)) {
					g.color(SelectionType.SELECTION.getColor().brighter()).fillRect(0, i * hi, w, hi);
				} else if (mouseOvers.contains(id)) {
					g.color(SelectionType.MOUSE_OVER.getColor().brighter()).fillRect(0, i * hi, w, hi);
				}
				float ti = Math.min(hi - 1, 16);
				g.drawText(l, 0, i * hi + (hi - ti) * 0.5f, w, ti, align);
			}
		}

		@Override
		protected void onMouseMoved(Pick pick) {

			super.onMouseMoved(pick);
		}

		@Override
		protected void onClicked(Pick pick) {

			super.onClicked(pick);
		}

		/**
		 * @param dim
		 * @return
		 */
		public GLElementDimensionDesc getDesc(EDimension dim) {
			if (data.isEmpty())
				return GLElementDimensionDesc.newFix(100).minimum(50).build();
			return GLElementDimensionDesc.newCountDependent(10).minimum(10).build();
		}

		@Override
		public void onSelectionUpdate(SelectionManager manager) {
			repaint();
		}

	}

}
