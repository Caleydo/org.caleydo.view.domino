/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;

import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IPopupLayer;
import org.caleydo.core.view.opengl.layout2.IPopupLayer.IPopupElement;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.basic.IScrollBar;
import org.caleydo.core.view.opengl.layout2.basic.IScrollBar.IScrollBarCallback;
import org.caleydo.core.view.opengl.layout2.basic.ScrollBar;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.undo.ZoomCmd;
import org.eclipse.swt.SWT;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class MiniMapCanvas extends GLElementContainer implements IGLLayout2, IScrollBarCallback {
	private static final int AUTO_SHIFT_AREA = 50;
	private static final int AUTO_SHIFT = 25;
	private static final int AUTO_SHIFT_TIMER = 150;

	private final Collection<ICallback<MiniMapCanvas>> onViewPortChange = new ArrayList<>();
	private final ScrollBarImpl vertical;
	private final ScrollBarImpl horizontal;
	private final float scrollBarWidth = 10;

	private MiniMap miniMap;

	private Vec2f shift = new Vec2f(0, 0);

	private Vec2f autoShift = new Vec2f(0, 0);
	private int autoShiftTimer = 0;


	public MiniMapCanvas() {
		setLayout(this);

		vertical = new ScrollBarImpl(new ScrollBar(false));
		vertical.scrollBar.setCallback(this);
		vertical.scrollBar.setWidth(scrollBarWidth);
		horizontal = new ScrollBarImpl(new ScrollBar(true));
		horizontal.scrollBar.setCallback(this);
		horizontal.scrollBar.setWidth(scrollBarWidth);
	}

	@Override
	protected void init(IGLElementContext context) {
		horizontal.pickingId = context.registerPickingListener(horizontal.scrollBar);
		vertical.pickingId = context.registerPickingListener(vertical.scrollBar);
		super.init(context);
	}

	@Override
	protected void takeDown() {
		context.unregisterPickingListener(horizontal.pickingId);
		context.unregisterPickingListener(vertical.pickingId);
		super.takeDown();
	}

	public void addOnViewPortChange(ICallback<MiniMapCanvas> listener) {
		onViewPortChange.add(listener);
	}

	public void removeOnViewPortChange(ICallback<MiniMapCanvas> listener) {
		onViewPortChange.remove(listener);
	}

	private void fireViewPortChange() {
		for (ICallback<MiniMapCanvas> listener : onViewPortChange)
			listener.on(this);
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		for (IGLLayoutElement child : children) {
			if (child.asElement() instanceof IManuallyShifted)
				child.setBounds(0, 0, w - scrollBarWidth, h - scrollBarWidth);
			else
				child.setBounds(-shift.x(), -shift.y(), w - scrollBarWidth, h - scrollBarWidth);
		}

		Rect r = getPaintingCanvas();
		final float offsetX = shift.x() - r.x() + 50;
		final float offsetY = shift.y() - r.y() + 50;
		horizontal.scrollBar.setBounds(offsetX, w - scrollBarWidth, r.width() + 100);
		vertical.scrollBar.setBounds(offsetY, h - scrollBarWidth, r.height() + 100);

		if (miniMap != null)
			miniMap.updateBounds();
		return false;
	}

	/**
	 * @return
	 */
	public Rect getClippingRect() {
		Vec2f s = getSize();
		return new Rect(shift.x(), shift.y(), s.x() - scrollBarWidth, s.y() - scrollBarWidth);
	}


	public Rect getPaintingCanvas() {
		Vec2f s = getSize();
		Rect r = new Rect(shift.x(), shift.y(), s.x() - scrollBarWidth, s.y() - scrollBarWidth);
		for (IHasMiniMap mini : Iterables.filter(this, IHasMiniMap.class)) {
			final Rect b = mini.getBoundingBox();
			if (mini instanceof IManuallyShifted && b != null) {
				b.xy(b.xy().plus(shift));
			}
			r = Rect.union(r, b);
		}
		return r;
	}

	/**
	 * @param dx
	 * @param dy
	 */
	public void shiftViewport(float dx, float dy) {
		shift.add(new Vec2f(dx, dy));
		relayout();
		for (IManuallyShifted m : Iterables.filter(this, IManuallyShifted.class))
			m.setShift(shift.x(), shift.y());
		fireViewPortChange();
	}

	/**
	 * @param rectBounds
	 */
	public void scrollInfoView(Rect bounds) {
		Rect visible = getClippingRect();
		// already visible
		if (visible.contains(bounds))
			return;
		float xShift = 0;

		// offset to the border
		final int offset = 30;

		if (bounds.x() > visible.x2())
			xShift = bounds.x2() + offset - visible.x2();
		else if (bounds.x2() < visible.x())
			xShift = bounds.x() - offset - visible.x();

		float yShift = 0;
		if (bounds.y() > visible.y2())
			yShift = bounds.y2() + offset - visible.y2();
		else if (bounds.y2() < visible.y())
			yShift = bounds.y() - offset - visible.y();
		shiftViewport(xShift, yShift);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		clipStart(g, w, h);
		super.renderImpl(g, w, h);
		clipEnd(g);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		clipStart(g, w, h);
		super.renderPickImpl(g, w, h);
		clipEnd(g);
	}

	private void clipStart(GLGraphics g, float w, float h) {
		final GL2 gl = g.gl;

		{
			g.move(0, h - scrollBarWidth);
			g.pushName(horizontal.pickingId);
			if (g.isPickingPass())
				horizontal.scrollBar.renderPick(g, w - scrollBarWidth, scrollBarWidth, this);
			else
				horizontal.scrollBar.render(g, w - scrollBarWidth, scrollBarWidth, this);
			g.popName();
			g.move(0, -h + scrollBarWidth);
		}
		{
			g.move(w - scrollBarWidth, 0);
			g.pushName(vertical.pickingId);
			if (g.isPickingPass())
				vertical.scrollBar.renderPick(g, scrollBarWidth, h - scrollBarWidth, this);
			else
				vertical.scrollBar.render(g, scrollBarWidth, h - scrollBarWidth, this);
			g.popName();
			g.move(-w + scrollBarWidth, 0);
		}

		gl.glPushAttrib(GL2.GL_ENABLE_BIT);
		{
			double[] clipPlane1 = new double[] { 1.0, 0.0, 0.0, 0 };
			double[] clipPlane3 = new double[] { -1.0, 0.0, 0.0, w - scrollBarWidth };
			gl.glClipPlane(GL2ES1.GL_CLIP_PLANE0, clipPlane1, 0);
			gl.glClipPlane(GL2ES1.GL_CLIP_PLANE1, clipPlane3, 0);
			gl.glEnable(GL2ES1.GL_CLIP_PLANE0);
			gl.glEnable(GL2ES1.GL_CLIP_PLANE1);
		}
		{
			double[] clipPlane2 = new double[] { 0.0, 1.0, 0.0, 0 };
			double[] clipPlane4 = new double[] { 0.0, -1.0, 0.0, h - scrollBarWidth };
			gl.glClipPlane(GL2ES1.GL_CLIP_PLANE2, clipPlane2, 0);
			gl.glClipPlane(GL2ES1.GL_CLIP_PLANE3, clipPlane4, 0);
			gl.glEnable(GL2ES1.GL_CLIP_PLANE2);
			gl.glEnable(GL2ES1.GL_CLIP_PLANE3);
		}
	}

	private void clipEnd(GLGraphics g) {
		g.gl.glPopAttrib();
	}

	@Override
	public float getHeight(IScrollBar scrollBar) {
		if (horizontal != null && horizontal.scrollBar == scrollBar)
			return getSize().x();
		else
			return getSize().y();
	}

	@Override
	public void onScrollBarMoved(IScrollBar scrollBar, float value) {
		Rect r = getPaintingCanvas();
		if (horizontal.scrollBar == scrollBar) {
			final float ox = value + r.x() - 50;
			shiftViewport(ox - shift.x(), 0);
		} else {
			final float oy = value + r.y() - shift.y() - 50;
			shiftViewport(0, oy);
		}
	}

	private class MiniMap extends PickableGLElement implements IHasMinSize {
		static final float MINI_MAP_FACTOR = 0.2f;
		private boolean hovered;
		/**
		 *
		 */
		public MiniMap() {
			setPicker(null);
		}

		@Override
		public Vec2f getMinSize() {
			Rect r = getPaintingCanvas();
			Vec2f s = r.size();
			s.scale(MINI_MAP_FACTOR);
			return s;
		}

		private Rect getVisibleRect() {
			Rect r = getClippingRect();
			Rect p = getPaintingCanvas();
			// System.out.println(r + " " + p);
			r.xy(r.xy().minus(p.xy()));
			r.xy(r.xy().times(MINI_MAP_FACTOR));
			r.size(r.size().times(MINI_MAP_FACTOR));
			return r;
		}

		public void updateBounds() {
			if (!isVisible())
				return;
			Vec2f r = getMinSize();
			// update my parent size
			findParent(IPopupElement.class).setContentSize(r.x(), r.y());
		}

		@Override
		protected void onMouseOver(Pick pick) {
			hovered = true;
			repaint();
		}

		@Override
		protected void onMouseOut(Pick pick) {
			hovered = false;
			repaint();
		}

		@Override
		protected void onDragDetected(Pick pick) {
			context.getSWTLayer().setCursor(SWT.CURSOR_HAND);
			if (pick.isAnyDragging())
				return;
			pick.setDoDragging(true);
		}

		@Override
		protected void onDragged(Pick pick) {
			if (!pick.isDoDragging())
				return;
			// TODO move the viewport
			Rect old = getVisibleRect();
			shiftViewport(pick.getDx() / MINI_MAP_FACTOR, pick.getDy() / MINI_MAP_FACTOR);
			float shiftX = Math.min(0, old.x() + pick.getDx());
			float shiftY = Math.min(0, old.y() + pick.getDy());
			findParent(IPopupElement.class).shift(shiftX, shiftY);
			updateBounds();
		}

		@Override
		protected void onMouseWheel(Pick pick) {
			IMouseEvent event = (IMouseEvent) pick;
			UndoStack undo = MiniMapCanvas.this.findParent(Domino.class).getUndo();
			if (event.getWheelRotation() != 0)
				undo.push(new ZoomCmd(ScaleLogic.shiftLogic(event, new Vec2f(100, 100)), toMousePos(pick
						.getPickedPoint())));
		}

		/**
		 * @param pickedPoint
		 * @return
		 */
		private Vec2f toMousePos(Vec2f pos) {
			pos = toRelative(pos);
			Rect p = getPaintingCanvas();
			pos = pos.times(1.f / MINI_MAP_FACTOR);
			pos.minus(p.xy());
			return pos;
		}

		@Override
		protected void onMouseReleased(Pick pick) {
			context.getSWTLayer().setCursor(-1);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.save();
			g.gl.glScalef(MINI_MAP_FACTOR, MINI_MAP_FACTOR, 1);
			Rect p = getPaintingCanvas();
			g.move(-p.x(), -p.y());
			g.color(1, 1, 1, 0.5f).fillRect(p);

			float sx = -shift.x();
			float sy = -shift.y();
			// g.move(sx, sy);
			for (IHasMiniMap mini : Iterables.filter(MiniMapCanvas.this, IHasMiniMap.class)) {
				if (!mini.getVisibility().doRender())
					continue;
				if (mini instanceof IManuallyShifted) {
					g.move(-sx, -sy);
					mini.renderMiniMap(g);
					g.move(sx, sy);
				} else {
					// Vec2f loc = mini.getLocation();
					// g.move(loc.x(), loc.y());
					mini.renderMiniMap(g);
					// g.move(-loc.x(), -loc.y());
				}
			}

			Rect r = getClippingRect();
			g.color(Color.BLACK);
			if (hovered)
				g.lineWidth(2);
			g.drawRect(r.x(), r.y(), r.width(), r.height());
			g.lineWidth(1);
			g.color(0, 0, 1, 0.08f);
			g.fillRect(p.x(), p.y(), p.width(), r.y() - p.y());
			g.fillRect(p.x(), r.y2(), p.width(), p.height() + p.y() - r.y2());
			g.fillRect(p.x(), r.y(), r.x() - p.x(), r.height());
			g.fillRect(r.x2(), r.y(), p.width() + p.x() - r.x2(), r.height());

			g.restore();
			super.renderImpl(g, w, h);
		}

		@Override
		protected void renderPickImpl(GLGraphics g, float w, float h) {
			if (getVisibility() == EVisibility.PICKABLE) {
				Rect p = getPaintingCanvas();
				Rect r = getClippingRect();
				r = r.times(MINI_MAP_FACTOR);
				g.fillRect(r.x() - p.x() * MINI_MAP_FACTOR + 10, r.y() - p.y() * MINI_MAP_FACTOR + 10, r.width() - 20,
						r.height() - 20);
			}
		}
		/**
		 * @return
		 */
		public boolean isVisible() {
			return context != null;
		}

	}

	public interface IHasMiniMap {
		void renderMiniMap(GLGraphics g);

		Rect getBoundingBox();

		Vec2f getLocation();

		EVisibility getVisibility();
	}

	public interface IManuallyShifted {
		void setShift(float x, float y);
	}



	/**
	 *
	 */
	public void toggleShowMiniMap() {
		final IPopupLayer popup = context.getPopupLayer();
		if (this.miniMap != null && this.miniMap.isVisible()) {
			popup.hide(miniMap);
			this.miniMap = null;
		} else {
			this.miniMap = new MiniMap();
			Vec2f size = this.miniMap.getMinSize();
			Vec2f mySize = getSize();
			Vec2f loc = getAbsoluteLocation();
			float x = loc.x() + mySize.x() - size.x() - 4 - scrollBarWidth;
			float y = loc.y() + mySize.y() - size.y() - 4 - scrollBarWidth;
			popup.show(miniMap, new Rect(x, y, size.x(), size.y()), IPopupLayer.FLAG_CLOSEABLE
					| IPopupLayer.FLAG_BORDER
					| IPopupLayer.FLAG_MOVEABLE);
		}
	}

	@Override
	public void layout(int deltaTimeMs) {
		if (autoShiftTimer >= 0) {
			autoShiftTimer -= deltaTimeMs;
			if (autoShiftTimer <= 0) {
				shiftViewport(autoShift.x(), autoShift.y());
				autoShiftTimer = AUTO_SHIFT_TIMER;
			}
		}
		super.layout(deltaTimeMs);
	}

	/**
	 * idea if the cursor stands at a corner
	 *
	 * @param mousePos
	 */
	public void autoShift(Vec2f mousePos) {
		Vec2f mouse = toRelative(mousePos);
		Vec2f size = getSize();

		if (mouse.x() < AUTO_SHIFT_AREA)
			autoShift.setX(-AUTO_SHIFT);
		else if (mouse.x() >= (size.x() - AUTO_SHIFT_AREA))
			autoShift.setX(AUTO_SHIFT);
		else
			autoShift.setX(0);
		if (mouse.y() < AUTO_SHIFT_AREA)
			autoShift.setY(-AUTO_SHIFT);
		else if (mouse.y() >= (size.y() - AUTO_SHIFT_AREA))
			autoShift.setY(AUTO_SHIFT);
		else
			autoShift.setY(0);
		if (autoShift.x() != 0 || autoShift.y() != 0) {
			autoShiftTimer = AUTO_SHIFT_TIMER;
		} else
			autoShiftTimer = -1;
	}

	public void stopAutoShift() {
		autoShiftTimer = -1;
	}

	private class ScrollBarImpl {
		final IScrollBar scrollBar;
		int pickingId;

		public ScrollBarImpl(IScrollBar scrollBar) {
			this.scrollBar = scrollBar;
		}
	}
}
