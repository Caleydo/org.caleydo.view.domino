/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.Set;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.view.domino.internal.dnd.DragElement;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Blocks extends GLElementContainer implements IHasMinSize, ICallback<SelectionType> {

	public Blocks(NodeSelections selections) {
		selections.onBlockSelectionChanges(this);
	}

	@Override
	public void on(SelectionType data) {
		final Domino domino = findParent(Domino.class);
		if (data == SelectionType.SELECTION && domino.getTool() == EToolState.BANDS) {
			Set<Block> s = domino.getSelections().getBlockSelection(SelectionType.SELECTION);
			if (s.isEmpty()) {
				for (Block b : getBlocks())
					b.setFadeOut(false);
			} else if (s.size() == 1) {
				Block sel = s.iterator().next();
				for (Block b : getBlocks())
					b.setFadeOut(b != sel && !canHaveBands(sel, b));
			} else {
				for (Block b : getBlocks())
					b.setFadeOut(!s.contains(b));
			}
		}
	}

	/**
	 * @param sel
	 * @param b
	 * @return
	 */
	private boolean canHaveBands(Block a, Block b) {
		return !Sets.intersection(a.getIDTypes(), b.getIDTypes()).isEmpty();
	}

	public void addBlock(Block b) {
		this.add(b);
		final Domino domino = findParent(Domino.class);
		if (domino.getTool() == EToolState.BANDS) {
			Set<Block> s = domino.getSelections().getBlockSelection(SelectionType.SELECTION);
			b.setFadeOut((s.size() >= 2 && !s.contains(b)) || (s.size() == 1 && !canHaveBands(s.iterator().next(), b)));
		}
	}

	public Iterable<Block> getBlocks() {
		return Iterables.filter(this, Block.class);
	}

	/**
	 * @param tool
	 */
	public void setTool(EToolState tool) {
		for (Block b : getBlocks()) {
			b.setTool(tool);
		}
	}

	public void zoom(Vec2f shift) {
		for (Block block : getBlocks()) {
			block.zoom(shift, null);
		}
		getParent().getParent().relayout();
	}

	@Override
	public Vec2f getMinSize() {
		Rectangle2D r = null;
		for (GLElement b : this) {
			if (r == null) {
				r = b.getRectangleBounds();
			} else
				Rectangle2D.union(r, b.getRectangleBounds(), r);
		}
		if (r == null)
			return new Vec2f(100, 100);
		return new Vec2f((float) r.getMaxX(), (float) r.getMaxY());
	}

	/**
	 * @param relativePosition
	 * @return
	 */
	public Pair<Rect, Vec2f> findSnapTo(Vec2f pos) {
		// grid lines ??
		// linear to a block?
		float x = Float.NaN;
		float w = Float.NaN;
		float x_hint = Float.NaN;
		float y = Float.NaN;
		float h = Float.NaN;
		float y_hint = Float.NaN;

		for (GLElement elem : this) {
			Rect bounds = elem.getRectBounds();
			if (Float.isNaN(x) && inRange(pos.x(), bounds.x())) { // near enough
				x = bounds.x(); // set it as target pos
				w = bounds.width();
				x_hint = bounds.y() - pos.y();
			}
			if (Float.isNaN(x) && inRange(pos.x(), bounds.x2())) { // near enough
				x = bounds.x2(); // set it as target pos
				w = bounds.width();
				x_hint = bounds.y() - pos.y();
			}
			if (inRange(pos.y(), bounds.y())) { // near enough
				y = bounds.y();
				h = bounds.height();
				y_hint = bounds.x() - pos.x();
			}
			if (inRange(pos.y(), bounds.y2())) { // near enough
				y = bounds.y2();
				h = bounds.height();
				y_hint = bounds.x() - pos.x();
			}
		}
		if (Float.isNaN(x) && Float.isNaN(y))
			return null;
		return Pair.make(new Rect(x, y, w, h), new Vec2f(x_hint, y_hint));
	}

	/**
	 * @param x
	 * @param x2
	 * @return
	 */
	private static boolean inRange(float a, float b) {
		return Math.abs(a - b) < 20;
	}

	/**
	 * @param currentlyDraggedVis
	 */
	public void snapDraggedVis(DragElement current) {

		Pair<Rect, Vec2f> stickTo = findSnapTo(current.getRelativePosition(getAbsoluteLocation()));
		if (stickTo == null)
			current.stickTo(null, null, null);
		else {
			Vec2f pos = toAbsolute(stickTo.getFirst().xy());
			current.stickTo(pos, stickTo.getFirst().size(), stickTo.getSecond());
		}
	}

	public void renderMiniMap(GLGraphics g) {
		for(Block block : getBlocks()) {
			Vec2f loc = block.getLocation();
			for (Node n : block.nodes()) {
				Rect bounds = n.getRectBounds();
				g.color(n.getColor()).fillRect(loc.x() + bounds.x(), loc.y() + bounds.y(), bounds.width(),
						bounds.height());
			}
		}
	}
}
