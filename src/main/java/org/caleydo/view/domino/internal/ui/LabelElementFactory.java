/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
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
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation.ILocator;
import org.caleydo.core.view.opengl.layout2.manage.IGLElementFactory2;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.core.view.opengl.util.text.ETextStyle;
import org.caleydo.view.domino.api.model.typed.TypedList;

import com.google.common.base.Function;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Ranges;
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
		boolean boxHighlights = context.is("boxHighlights");
		return new LabelElement(dim, l, toString, context.get("align", VAlign.class, VAlign.LEFT), boxHighlights);
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

	private static final class LabelElement extends GLElement implements
			MultiSelectionManagerMixin.ISelectionMixinCallback, IPickingLabelProvider, IPickingListener, ILocator {
		private static final int MAX_TEXT_SIZE = 16;
		private static final int MIN_TEXT_SIZE = 6;
		private static final int PADDING = 4;

		private final EDimension dim;
		private final TypedList data;
		private final List<String> labels;
		private VAlign align;

		@DeepScan
		private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);
		private final boolean boxHighlights;

		private float selectStart = Float.NaN;
		private float selectEndPrev = Float.NaN;
		private float selectEnd = Float.NaN;

		public LabelElement(EDimension dim, TypedList data, Function<Integer, String> toString, VAlign align,
				boolean boxHighlights) {
			this.dim = dim;
			this.data = data;
			this.align = align;
			this.boxHighlights = boxHighlights;

			selections.add(new SelectionManager(data.getIdType()));
			this.labels = toString == null ? toLabels(data) : toLabels(data, toString);

			setVisibility(EVisibility.PICKABLE);
			onPick(this);
		}

		@Override
		protected void init(IGLElementContext context) {
			onPick(context.getSWTLayer().createTooltip(this));
			super.init(context);
		}

		@Override
		public String getLabel(Pick pick) {
			if (data.isEmpty())
				return null;
			Vec2f xy = toRelative(pick.getPickedPoint());
			float ratio = dim.select(xy) / dim.select(getSize());
			int index = (int) (ratio * (data.size() - 1));
			return labels.get(index);
		}

		@Override
		public void pick(Pick pick) {
			if (data.isEmpty())
				return;
			SelectionManager manager = selections.get(0);
			Vec2f xy = toRelative(pick.getPickedPoint());
			float v = dim.select(xy) / dim.select(getSize());
			boolean ctrlDown = ((IMouseEvent) pick).isCtrlDown();
			switch (pick.getPickingMode()) {
			case MOUSE_MOVED:
				int index = Math.min((int) (v * (data.size())), data.size() - 1);
				{
					manager.clearSelection(SelectionType.MOUSE_OVER);
					manager.addToType(SelectionType.MOUSE_OVER, data.get(index));
					selections.fireSelectionDelta(manager);
					repaint();
				}
				break;
			case DRAG_DETECTED:
				pick.setDoDragging(true);
				selectStart = selectEndPrev = v;
				if (!ctrlDown) {
					manager.clearSelection(SelectionType.SELECTION);
					selections.fireSelectionDelta(manager);
					repaint();
				}
				break;
			case DRAGGED:
				if (pick.isDoDragging()) {
					selectEnd = v;
					updateSelections(manager, ctrlDown);
				}
				break;
			case MOUSE_RELEASED:
				if (pick.isDoDragging()) {
					selectEnd = v;
					updateSelections(manager, ctrlDown);
					selectStart = selectEnd = selectEndPrev = Float.NaN;
				}
				break;
			case MOUSE_OUT:
				manager.clearSelection(SelectionType.MOUSE_OVER);
				selections.fireSelectionDelta(manager);
				repaint();
				break;
			default:
				break;
			}
		}

		private void updateSelections(SelectionManager manager, boolean ctrlDown) {
			if (!ctrlDown) {
				manager.clearSelection(SelectionType.SELECTION);
				update(manager, selectStart, selectEnd, false);
			} else {
				update(manager, selectEnd, selectEndPrev, true);
			}
			selections.fireSelectionDelta(manager);
			selectEndPrev = selectEnd;
			repaint();
		}

		private void update(SelectionManager manager, float a, float b, boolean toggle) {
			int min = (int) (Math.min(a, b) * (data.size() - 1));
			int max = (int) (Math.max(a, b) * (data.size() - 1));
			for (int i = min; i <= max; ++i) {
				final Integer id = data.get(i);
				boolean add = !toggle || !manager.checkStatus(SelectionType.SELECTION, id);
				if (add)
					manager.addToType(SelectionType.SELECTION, id);
				else
					manager.removeFromType(SelectionType.SELECTION, id);
			}
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

			if (data.isEmpty()) {
				g.color(Color.WHITE).fillRect(0, 0, w, h);
				g.color(Color.BLACK).drawRect(1, 1, w - 2, h - 2);
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
			BitSet mouseOvers = toBitSet(manager.getElements(SelectionType.MOUSE_OVER));
			BitSet selected = toBitSet(manager.getElements(SelectionType.SELECTION));
			final int size = data.size();
			if ((h / size) > MIN_TEXT_SIZE) {
				renderSelection(g, w, h, mouseOvers, selected);
				renderUniform(g, w, h);
			} else if (boxHighlights && (!mouseOvers.isEmpty() || !selected.isEmpty())) {
				renderBoxHighlights(g, w, h, mouseOvers, selected);
			} else {
				final float textHeight = Math.min(w - 20, 14);
				renderSelection(g, w, h, mouseOvers, selected);
				g.drawRotatedText("No Space", w - 10, 0, h, textHeight, VAlign.CENTER, ETextStyle.PLAIN, 90);
			}
		}

		/**
		 * @param elements
		 * @return
		 */
		private BitSet toBitSet(Set<Integer> elements) {
			BitSet s = new BitSet(data.size());
			for (Integer elem : elements) {
				int i = data.indexOf(elem);
				if (i >= 0)
					s.set(i);
			}
			return s;
		}

		private void renderBoxHighlights(GLGraphics g, float w, float h, BitSet mouseOvers, BitSet selected) {
			final int size = data.size();
			final float datahi = h / size;
			selected.andNot(mouseOvers);
			BitSet any = (BitSet) mouseOvers.clone();
			any.or(selected);

			final int anyS = any.cardinality();
			float hi = h / anyS;
			if (hi > MIN_TEXT_SIZE) {
				// render boxes
				float ti = Math.min(hi, MAX_TEXT_SIZE);
				List<Block> blocks = new ArrayList<>();
				addAll(mouseOvers, blocks, h, ti, datahi, true);
				addAll(selected, blocks, h, ti, datahi, false);

				Collections.sort(blocks);

				optimize(blocks, h);

				for (Block block : blocks) {
					block.render(g, w, labels, align);
				}
			} else
				renderSelection(g, w, h, mouseOvers, selected);
		}

		/**
		 * @param set
		 * @param blocks
		 * @param h
		 * @param ti
		 * @param hi
		 */
		private void addAll(BitSet set, List<Block> blocks, float h, float ti, float datahi, boolean mouseOver) {
			for (int index = set.nextSetBit(0); index != -1; index = set.nextSetBit(index + 1)) {
				float y2 = datahi * index;
				int start = index;
				while (set.get(index + 1))
					index++;
				final float th = ti * (index - start + 1);
				final float dh = datahi * (index - start + 1);
				final float y = Math.max(0, Math.min(y2 - (th - dh) * 0.5f, h - th));
				blocks.add(new Block(y, th, y2, dh, mouseOver, start, index + 1));
			}
		}

		/**
		 * @param blocks
		 */
		private void optimize(List<Block> blocks, float h) {
			final int size = blocks.size();
			Block prev = blocks.get(0);
			for (int i = 1; i < size; ++i) {
				Block b = blocks.get(i);
				final boolean notLast = i < (size - 1);
				float toMoveUp = prev.y2() - b.y;
				prev = b;
				if (toMoveUp > 0 && (!notLast || (blocks.get(i + 1).y - b.y2()) > toMoveUp)) { // move me down, if I'm
																								// the last one
					toMoveUp -= moveDown(blocks, i, toMoveUp, h);
				}
				if (toMoveUp > 0) {
					// move me 50% up
					float upMoved = moveUp(blocks, i, toMoveUp * 0.5f);
					// move rest down
					float movedDown = 0;
					if (notLast)
						movedDown = moveDown(blocks, i + 1, toMoveUp - upMoved, h);
					// can't move all down
					if ((movedDown + upMoved) < toMoveUp)
						// move rest up again
						moveUp(blocks, i, toMoveUp - (movedDown + upMoved));
				}
			}
		}

		private float moveUp(List<Block> blocks, int index, float up) {
			Block b = blocks.get(index);
			final boolean first = index == 0;
			if (first)
				up = Math.min(b.y, up);
			b.move(-up);
			if (!first) {
				Block prev = blocks.get(index - 1);
				if (prev.y2() >= b.y) {
					// move the other up
					final float toMove = prev.y2() - b.y;
					float up2 = moveUp(blocks, index - 1, toMove);
					if (up2 < toMove) { // can't move the previous up
						up = up2; // move me down again as much as needed
						b.move(toMove - up2);
					}
				}
			}
			return up;
		}

		private float moveDown(List<Block> blocks, int index, float down, float h) {
			Block b = blocks.get(index);
			final boolean last = index == (blocks.size() - 1);
			if (last)
				down = Math.min(h - b.y2(), down);
			b.move(down);
			if (!last) {
				Block next = blocks.get(index + 1);
				if (next.y <= b.y2()) {
					// move the other down
					final float toMove = b.y2() - next.y2();
					float down2 = moveDown(blocks, index + 1, toMove, h);
					if (down2 < toMove) { // can't move the other down
						down = down2; // move we up again as much as needed
						b.move(-(toMove - down2));
					}
				}
			}
			return down;
		}

		private static class Block implements Comparable<Block> {
			float y;
			final float h;
			final float dy;
			final float dh;
			private final boolean mouseOver;
			private final int start, end;

			public Block(float y, float h, float dy, float dh, boolean mouseOver, int start, int end) {
				this.y = y;
				this.h = h;
				this.dy = dy;
				this.dh = dh;
				this.mouseOver = mouseOver;
				this.start = start;
				this.end = end;
			}

			public float y2() {
				return y + h;
			}

			public void move(float dy) {
				this.y += dy;
			}

			public void render(GLGraphics g, float w, List<String> labels, VAlign align) {
				renderPointingBox(g, mouseOver, y, h, dy, dh, w, align);
				float x = align == VAlign.RIGHT ? 0 : PADDING;
				float wi = w - x - (align == VAlign.LEFT ? 0 : PADDING);
				g.drawText(labels.subList(start, end), x, y, wi, h - 1, 1, align, ETextStyle.PLAIN);
			}

			@Override
			public int compareTo(Block o) {
				return Float.compare(y, o.y);
			}
		}

		private void renderUniform(GLGraphics g, float w, float h) {
			final int size = data.size();
			float hi = h / size;
			float ti = Math.min(hi - 1, MAX_TEXT_SIZE);
			float x = align == VAlign.RIGHT ? 0 : PADDING;
			float wi = w - x - (align == VAlign.LEFT ? 0 : PADDING);
			for (int i = 0; i < size; ++i) {
				String l = labels.get(i);
				g.drawText(l, x, i * hi + (hi - ti) * 0.5f, wi, ti, align);
			}
		}

		private void renderSelection(GLGraphics g, float w, float h, BitSet mouseOvers, BitSet selected) {
			final float hi = h / data.size();
			selected.andNot(mouseOvers);
			boolean showOutline = hi > 5;
			g.lineWidth(3);
			renderSelection(g, w, h, hi, selected, SelectionType.SELECTION, !showOutline);
			renderSelection(g, w, h, hi, mouseOvers, SelectionType.MOUSE_OVER, !showOutline);
			g.lineWidth(1);
		}

		private static void renderPointingBox(GLGraphics g, boolean mouseOver, float y, float h, float y2, float h2,
				float w, VAlign valign) {
			SelectionType type = mouseOver ? SelectionType.MOUSE_OVER : SelectionType.SELECTION;
			g.color(type.getColor().brighter());

			float o1 = 15;
			float w1 = w - 15;
			List<Vec2f> points;
			switch (valign) {
			case LEFT:
				points = Arrays.asList(new Vec2f(0, y), new Vec2f(w1, y), new Vec2f(w, y2), new Vec2f(w, y2 + h2),
						new Vec2f(w1, y + h), new Vec2f(0, y + h));
				break;
			case RIGHT:
				points = Arrays.asList(new Vec2f(o1, y), new Vec2f(w, y), new Vec2f(w, y + h), new Vec2f(o1, y + h),
						new Vec2f(0, y2 + h2), new Vec2f(0, y2));
				break;
			default:
				points = Arrays.asList(new Vec2f(o1, y), new Vec2f(w, y), new Vec2f(w1, y), new Vec2f(w, y2),
						new Vec2f(w, y2 + h2), new Vec2f(w1, y + h), new Vec2f(0, y2 + h2), new Vec2f(0, y2));
				break;
			}
			g.fillPolygon(TesselatedPolygons.polygon2(points));
		}


		private static void renderSelection(GLGraphics g, float w, float h, float hi, BitSet set, SelectionType type,
				boolean fill) {
			Color c = type.getColor();
			if (fill)
				g.color(c.r, c.g, c.b, 0.5f);
			else
				g.color(c);
			for (int i = set.nextSetBit(0); i != -1; i = set.nextSetBit(i + 1)) {
				int start = i;
				while (set.get(i + 1))
					i++;
				if (fill)
					g.fillRect(0, start * hi, w, hi * (i + 1 - start));
				else
					g.drawRect(0, start * hi, w, hi * (i + 1 - start));
			}
		}


		/**
		 * @param dim
		 * @return
		 */
		public GLElementDimensionDesc getDesc(EDimension dim) {
			if (data.isEmpty() || dim != this.dim)
				return GLElementDimensionDesc.newFix(100).minimum(50).build();
			return GLElementDimensionDesc.newCountDependent(boxHighlights ? 1 : 10).locateUsing(this).build();
		}

		@Override
		public GLLocation apply(int dataIndex, boolean topLeft) {
			float size = dim.select(getSize());
			float per = size / this.data.size();
			return new GLLocation(dataIndex * per, per);
		}

		@Override
		public GLLocation apply(Integer input, Boolean topLeft) {
			return GLLocation.applyPrimitive(this, input, topLeft);
		}

		@Override
		public Set<Integer> unapply(GLLocation location) {
			float size = dim.select(getSize());
			float per = size / this.data.size();
			int start = (int)Math.floor(location.getOffset() / per);
			int end = (int)Math.ceil(location.getOffset2() / per);
			return Ranges.closed(start, end).asSet(DiscreteDomains.integers());
		}

		@Override
		public void onSelectionUpdate(SelectionManager manager) {
			repaint();
		}

	}

}
