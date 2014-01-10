/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.ui;

import static org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc.inRange;

import java.util.List;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.perspective.table.TableDoubleLists;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.DoubleFunctions;
import org.caleydo.core.util.function.DoubleStatistics;
import org.caleydo.core.util.function.IDoubleFunction;
import org.caleydo.core.util.function.IDoubleList;
import org.caleydo.core.util.function.MappedDoubleList;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc.DescBuilder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation.ILocator;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.typed.TypedList;

import com.google.common.base.Function;

/**
 * render a single element as an axis, e.g. to represent a PCP
 *
 * @author Samuel Gratzl
 *
 */
public class AxisElementFactory implements IGLElementFactory2 {

	@Override
	public String getId() {
		return "axis";
	}

	@Override
	public GLElement create(GLElementFactoryContext context) {
		if (hasTablePerspective(context))
			return new AxisElement(context.getData());

		EDimension dim = context.get(EDimension.class, EDimension.RECORD);
		double min = context.getDouble("min", Double.NaN);
		double max = context.getDouble("max", Double.NaN);

		if (context.get(TypedList.class, null) != null && context.get("id2double", Function.class, null) != null) {
			TypedList data = context.get(TypedList.class, null);
			@SuppressWarnings("unchecked")
			Function<Integer, Double> f = context.get("id2double", Function.class, null);
			return new AxisElement(dim, data, f, min, max);
		}
		if (context.get(IDoubleList.class, null) != null) {
			IDoubleList data = context.get(IDoubleList.class, null);
			return new AxisElement(dim, data, min, max);
		}
		return null;

	}

	/**
	 * @param context
	 * @return
	 */
	private boolean hasTablePerspective(GLElementFactoryContext context) {
		TablePerspective d = context.getData();
		if (d == null)
			return false;
		if (!DataSupportDefinitions.homogenousColumns.apply(d))
			return false;
		return d.getNrDimensions() == 1 || d.getNrRecords() == 1;
	}

	@Override
	public boolean apply(GLElementFactoryContext context) {
		if (context.get(TypedList.class, null) != null && context.get("id2double", Function.class, null) != null)
			return true;
		if (context.get(IDoubleList.class, null) != null)
			return true;
		return hasTablePerspective(context);
	}

	@Override
	public GLElementDimensionDesc getDesc(EDimension dim, GLElement elem) {
		AxisElement l = (AxisElement) elem;
		return l.getDesc(dim);
	}

	private static final class AxisElement extends PickableGLElement implements
			MultiSelectionManagerMixin.ISelectionMixinCallback, ILocator {

		@DeepScan
		private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);
		private final EDimension dim;
		private final List<Integer> data;
		private final IDoubleList list;
		private final IDoubleFunction normalize;

		public AxisElement(TablePerspective t) {
			this(EDimension.get(t.getNrDimensions() == 1), t, EDimension.get(t.getNrDimensions() == 1)
					.select(t.getRecordPerspective(), t.getDimensionPerspective()).getVirtualArray());
		}

		public AxisElement(EDimension dim, TablePerspective t, VirtualArray va) {
			this(dim, TableDoubleLists.asNormalizedList(t), 0, 1, new TypedList(va.getIDs(), va.getIdType()));
		}

		public AxisElement(EDimension dim, TypedList data, Function<Integer, Double> f, double min, double max) {
			this(dim, new MappedDoubleList<Integer>(data, f), min, max, data);
		}

		public AxisElement(EDimension dim, IDoubleList data, double min, double max) {
			this(dim, data, min, max, null);
		}

		public AxisElement(EDimension dim, IDoubleList data, double min, double max, TypedList ids) {
			this.dim = dim;
			this.data = ids;
			this.list = data;
			if (Double.isNaN(min) || Double.isNaN(max)) {
				DoubleStatistics stats = DoubleStatistics.of(list);
				if (Double.isNaN(min))
					min = stats.getMin();
				if (Double.isNaN(max))
					max = stats.getMax();
			}
			this.normalize = DoubleFunctions.normalize(min, max);
			selections.add(new SelectionManager(ids.getIdType()));
		}



		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			SelectionManager manager = selections.get(0);
			int n = list.size();
			float o = dim.opposite().select(w, h) * 0.1f;
			if (dim.isHorizontal()) {
				g.color(Color.BLACK).drawLine(0, h * 0.5f, h, h * 0.5f);

			} else {
				g.color(Color.BLACK).drawLine(w * 0.5f, 0, w * 0.5f, h);
			}
			for (int i = 0; i < n; ++i) {
				double v = normalize.apply(list.getPrimitive(i));
				if(Double.isNaN(v))
					v = 0;
				SelectionType t = data == null ? null : manager.getHighestSelectionType(data.get(i));
				if (t == null) {
					g.color(0, 0, 0, 0.5f);
				} else {
					final Color c = t.getColor();
					g.color(c.r, c.g, c.b, 0.5f);
				}
				if (dim.isHorizontal()) {
					g.drawLine(w * (float) v, o, w * (float) v, h - o);
				} else {
					g.drawLine(o, h * (float) v, w - o, h * (float) v);
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
			DescBuilder b = GLElementDimensionDesc.newBuilder();
			if (this.dim != dim)
				return b.before(inRange(20, 10, Double.POSITIVE_INFINITY)).build();
			return b.before(inRange(200, 50, Double.POSITIVE_INFINITY)).locateUsing(this).build();

		}

		@Override
		public void onSelectionUpdate(SelectionManager manager) {
			repaint();
		}

		@Override
		public GLLocation apply(int dataIndex) {
			float total = dim.select(getSize());
			double n = normalize.apply(list.getPrimitive(dataIndex));
			return new GLLocation(n * total, 1);
		}

		@Override
		public GLLocation apply(Integer input) {
			return GLLocation.applyPrimitive(this, input);
		}

		@Override
		public List<GLLocation> apply(Iterable<Integer> dataIndizes) {
			return GLLocation.apply(this, dataIndizes);
		}

	}
}
