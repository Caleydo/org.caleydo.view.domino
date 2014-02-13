/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.api.model.typed.util.BitSetSet;
import org.caleydo.view.domino.internal.MiniMapCanvas.IHasMiniMap;
import org.caleydo.view.domino.internal.dnd.DragElement;
import org.caleydo.view.domino.internal.ui.Ruler;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class Blocks extends GLElementContainer implements ICallback<SelectionType>, IHasMiniMap, IGLLayout2 {

	private final ICallback<MiniMapCanvas> viewportchange = new ICallback<MiniMapCanvas>() {
		@Override
		public void on(MiniMapCanvas data) {
			updateAccordingToMiniMap();
		}
	};
	public Blocks(NodeSelections selections) {
		selections.onBlockSelectionChanges(this);
		setLayout(this);
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

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		if (getParent() instanceof MiniMapCanvas) {
			MiniMapCanvas c = (MiniMapCanvas) getParent();
			c.addOnViewPortChange(viewportchange);
		}
	}

	@Override
	protected void takeDown() {
		if (getParent() instanceof MiniMapCanvas) {
			MiniMapCanvas c = (MiniMapCanvas) getParent();
			c.removeOnViewPortChange(viewportchange);
		}
		super.takeDown();
	}

	void updateAccordingToMiniMap() {
		// MiniMapCanvas c = (MiniMapCanvas) getParent();
		// Rectangle2D r = c.getClippingRect().asRectangle2D();
		// EVisibility ifVisible = findParent(Domino.class).getTool() == EToolState.BANDS ? EVisibility.PICKABLE
		// : EVisibility.VISIBLE;
		// Vec2f loc = getLocation();
		// for (Block elem : getBlocks()) {
		// Rect b = elem.getRectBounds().clone();
		// b.xy(b.xy().plus(loc));
		// boolean v = r.intersects(b.asRectangle2D());
		// elem.setVisibility(v ? ifVisible : EVisibility.HIDDEN);
		// }
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		if (getParent() instanceof MiniMapCanvas)
			updateAccordingToMiniMap();
		return false;
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

	public Iterable<Ruler> rulers() {
		return Iterables.filter(this, Ruler.class);
	}

	/**
	 * @param tool
	 */
	public void setTool(EToolState tool) {
		for (Block b : getBlocks()) {
			b.setTool(tool);
		}
	}

	public void zoom(Vec2f shift, Vec2f mousePos) {
		for (Block block : getBlocks()) {
			block.zoom(shift, null);
			shiftZoomLocation(block, mousePos, shift);
		}
		for (Ruler ruler : rulers()) {
			ruler.zoom(shift);
			shiftZoomLocation(ruler, mousePos, shift);
		}
		getParent().getParent().relayout();
	}

	private void shiftZoomLocation(GLElement elem, Vec2f mousePos, Vec2f shift) {
		Rect b = elem.getRectBounds();
		if (b.contains(mousePos)) // inner
			return;

		float x = b.x();
		float y = b.y();

		if (mousePos.x() < b.x())
			x += shift.x();
		else if (mousePos.x() > b.x2())
			x -= shift.x();

		if (mousePos.y() < b.y())
			y += shift.y();
		else if (mousePos.y() > b.y2())
			y -= shift.y();

		elem.setLocation(x, y);
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

	@Override
	public Rect getBoundingBox() {
		Rect r = null;
		for(Block b : getBlocks()) {
			if (r == null)
				r = shiftedBoundingBox(b);
			else {
				r = Rect.union(r, shiftedBoundingBox(b));
			}
		}
		for (Ruler b : rulers()) {
			if (r == null)
				r = b.getRectBounds();
			else
				r = Rect.union(r, b.getRectBounds());
		}
		return r;
	}

	/**
	 * @param b
	 * @return
	 */
	private Rect shiftedBoundingBox(Block b) {
		Rect bb = b.getBoundingBox();
		Vec2f loc = b.getLocation();
		if (bb != null) {
			bb.xy(bb.xy().plus(loc));
		}
		return bb;
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

	@Override
	public void renderMiniMap(GLGraphics g) {
		for(Block block : getBlocks()) {
			Vec2f loc = block.getLocation();
			for (Node n : block.nodes()) {
				Rect bounds = n.getRectBounds();
				g.color(n.getColor()).fillRect(loc.x() + bounds.x(), loc.y() + bounds.y(), bounds.width(),
						bounds.height());
			}
		}
		for (Ruler ruler : rulers()) {
			g.color(Color.LIGHT_GRAY).fillRect(ruler.getRectBounds());
		}
	}

	/**
	 * @param category
	 * @param shift
	 */
	public void moveRuler(IDCategory category, Vec2f shift) {
		for (Ruler ruler : rulers()) {
			if (ruler.getIDCategory().equals(category)) {
				ruler.shiftLocation(shift);
				break;
			}
		}
		getParent().getParent().relayout();
	}

	/**
	 * @param category
	 * @param scale
	 */
	public void zoomRuler(IDCategory category, float scale) {
		for (Ruler ruler : rulers()) {
			if (ruler.getIDCategory().equals(category)) {
				ruler.zoom(scale);
				break;
			}
		}
		for (Block block : getBlocks()) {
			block.zoom(category, scale);
		}
	}

	/**
	 * @param idCategory
	 * @return
	 */
	public boolean hasRuler(IDCategory category) {
		for (Ruler ruler : rulers()) {
			if (ruler.getIDCategory().equals(category)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param ruler
	 */
	public void addRuler(Ruler ruler) {
		add(ruler);
	}

	/**
	 * @param ruler
	 */
	public void removeRuler(Ruler ruler) {
		remove(ruler);

	}

	/**
	 * @param category
	 * @return
	 */
	public int getVisibleItemCount(IDCategory category) {
		IDType primary = category.getPrimaryMappingType();
		Set<Integer> ids = new BitSetSet();
		for (Block block : getBlocks())
			block.addVisibleItems(category, ids, primary);
		return ids.size();
	}

	/**
	 * @param block
	 * @param dim
	 * @return
	 */
	public List<Block> explode(Block block, EDimension dim) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param blocks
	 * @param dim
	 * @return
	 */
	public Block combine(List<Block> blocks, EDimension dim) {
		// TODO Auto-generated method stub
		return null;
	}
}
