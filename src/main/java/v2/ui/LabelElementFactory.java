/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.ui;

import static org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc.inRange;

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
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc.DescBuilder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.typed.TypedListGroup;
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
		TypedListGroup dimData = context.get("dims", TypedListGroup.class, null);
		TypedListGroup recData = context.get("recs", TypedListGroup.class, null);
		return new LabelElement(dimData, recData);
	}

	@Override
	public boolean apply(GLElementFactoryContext context) {
		return context.get("dims", TypedListGroup.class, null) != null
				|| context.get("recs", TypedListGroup.class, null) != null;
	}

	@Override
	public GLElementDimensionDesc getDesc(EDimension dim, GLElement elem) {
		LabelElement l = (LabelElement) elem;
		return l.getDesc(dim);
	}

	private static final class LabelElement extends PickableGLElement implements
			MultiSelectionManagerMixin.ISelectionMixinCallback {

		private final TypedListGroup dimData;
		private final List<String> dimLabels;

		private final TypedListGroup recData;
		private final List<String> recLabels;

		@DeepScan
		private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

		public LabelElement(TypedListGroup dimData, TypedListGroup recData) {
			this.dimData = dimData;
			this.recData = recData;

			selections.add(new SelectionManager(this.dimData.getIdType()));
			this.dimLabels = toLabels(dimData);
			this.recLabels = toLabels(recData);
		}

		/**
		 * @param dimData2
		 * @return
		 */
		private List<String> toLabels(TypedListGroup data) {
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

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(Color.WHITE).fillRect(0, 0, w, h);

			if (dimData.isEmpty() && recData.isEmpty()) {
				g.drawText("Labels", 0, (h - 12) * 0.5f, w, 12, VAlign.CENTER);
				return;
			}
			SelectionManager manager = selections.get(0);
			if (!recData.isEmpty()) {
				final int size = recData.size();
				float hi = h / size;
				if (hi >= 4) {
					Set<Integer> mouseOvers = manager.getElements(SelectionType.MOUSE_OVER);
					Set<Integer> selected = manager.getElements(SelectionType.SELECTION);
					for (int i = 0; i < size; ++i) {
						String l = recLabels.get(i);
						Integer id = recData.get(i);
						if (selected.contains(id)) {
							g.color(SelectionType.SELECTION.getColor().brighter()).fillRect(0, i * hi, w, hi);
						} else if (mouseOvers.contains(id)) {
							g.color(SelectionType.MOUSE_OVER.getColor().brighter()).fillRect(0, i * hi, w, hi);
						}
						float ti = Math.min(hi - 1, 16);
						g.drawText(l, 0, i * hi + (hi - ti) * 0.5f, w, ti);
					}
				}
			}
			super.renderImpl(g, w, h);
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
			TypedListGroup data = dim.select(dimData, recData);
			DescBuilder b = GLElementDimensionDesc.newBuilder();
			if (data.isEmpty())
				return b.before(inRange(100, 50, Double.POSITIVE_INFINITY)).build();
			return b.factor(GLElementDimensionDesc.inRange(10, 10, 100)).build();
		}

		@Override
		public void onSelectionUpdate(SelectionManager manager) {
			repaint();
		}

	}

}
