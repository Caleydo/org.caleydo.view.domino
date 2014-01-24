/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.AGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.internal.band.ABand;
import org.caleydo.view.domino.internal.band.BandFactory;
import org.caleydo.view.domino.internal.data.VisualizationTypeOracle;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.BlockDragInfo;
import org.caleydo.view.domino.internal.event.HideNodeEvent;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class Block extends GLElementContainer implements IGLLayout2, IPickingListener {
	private final List<LinearBlock> linearBlocks = new ArrayList<>();

	private boolean fadeOut = false;
	private int fadeOutTime = 0;

	private boolean armed;

	private final IDragGLSource source = new IDragGLSource() {
		@Override
		public IDragInfo startSWTDrag(IDragEvent event) {
			return findDomino().startSWTDrag(event,Block.this);
		}

		@Override
		public void onDropped(IDnDItem info) {
			if (info.getInfo() instanceof BlockDragInfo) {
				for (Block block : ((BlockDragInfo) info.getInfo()).getBlocks())
					block.showAgain();
			}
		}

		@Override
		public GLElement createUI(IDragInfo info) {
			if (info instanceof ADragInfo)
				return ((ADragInfo) info).createUI(findDomino());
			return null;
		}
	};

	public Block(Node node) {
		setLayout(this);
		node.setLocation(0, 0);
		addFirstNode(node);

		onPick(this);
	}


	@Override
	public void pick(Pick pick) {
		final NodeSelections domino = findDomino().getSelections();
		IMouseEvent event = (IMouseEvent) pick;
		boolean ctrl = event.isCtrlDown();
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			domino.select(SelectionType.MOUSE_OVER, this, false);
			context.getMouseLayer().addDragSource(source);
			repaint();
			break;
		case MOUSE_OUT:
			domino.clear(SelectionType.MOUSE_OVER, (Block) null);
			context.getMouseLayer().removeDragSource(source);
			repaint();
			break;
		case CLICKED:
			armed = true;
			break;
		case MOUSE_RELEASED:
			if (armed) {
				if (domino.isSelected(SelectionType.SELECTION, this))
					domino.clear(SelectionType.SELECTION, ctrl ? this : null);
				else
					domino.select(SelectionType.SELECTION, this, ctrl);
				repaint();
				armed = false;
			}
			break;
		default:
			break;
		}
	}

	Domino findDomino() {
		return findParent(Domino.class);
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDragSource(source);
		super.takeDown();
	}

	/**
	 * @param fadeOut
	 *            setter, see {@link fadeOut}
	 */
	public void setFadeOut(boolean fadeOut) {
		if (this.fadeOut == fadeOut)
			return;
		this.fadeOut = fadeOut;
		if (fadeOut)
			this.fadeOutTime = 300;
		repaint();
	}

	@Override
	public void layout(int deltaTimeMs) {
		if (fadeOutTime > 0) {
			fadeOutTime -= deltaTimeMs;
			repaint();
		}
		super.layout(deltaTimeMs);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		Collection<Vec2f> outline = getOutline();
		g.color(Color.WHITE).fillPolygon(TesselatedPolygons.polygon2(outline));

		super.renderImpl(g, w, h);
		Domino domino = findDomino();

		if (domino.isShowDebugInfos())
			g.color(Color.BLUE).drawRect(0, 0, w, h);

		if (domino.isShowBlockLabels()) {
			for (LinearBlock b : linearBlocks)
				b.renderLabels(g);
		}

		NodeSelections selections = domino.getSelections();
		if (selections.isSelected(SelectionType.SELECTION, this))
			g.color(SelectionType.SELECTION.getColor()).lineWidth(3);
		else if (selections.isSelected(SelectionType.MOUSE_OVER, this))
			g.color(SelectionType.MOUSE_OVER.getColor()).lineWidth(3);
		else
			g.color(Color.LIGHT_GRAY);
		g.incZ().drawPath(getOutline(), true).decZ();
		g.lineWidth(1);

		if (fadeOut) {
			fadeOutElements(g, w, h);
		}
	}

	/**
	 * @return
	 */
	private Collection<Vec2f> getOutline() {
		Collection<Vec2f> r = new ArrayList<>(3);
		// if (this.size() == 1) {
		// Rect b = get(0).getRectBounds();
		// r.add(b.xy());
		// r.add(b.x2y());
		// r.add(b.x2y2());
		// r.add(b.xy2());
		// return r;
		// }
		// if (linearBlocks.size() == 1) {
		// LinearBlock b = linearBlocks.get(0);
		// Rect r1 = b.getNode(true).getRectBounds();
		// Rect r2 = b.getNode(false).getRectBounds();
		// r.add(r1.xy());
		// if (b.getDim().isHorizontal())
		// r.add(r1.x2y());
		// else
		// r.add(r2.x2y());
		// r.add(r2.x2y2());
		// r.add(r2.xy2());
		// return r;
		// }

		Node start = nodes().iterator().next();
		// search for a start point
		while (start.getNeighbor(EDirection.NORTH) != null)
			start = start.getNeighbor(EDirection.NORTH);
		while (start.getNeighbor(EDirection.WEST) != null)
			start = start.getNeighbor(EDirection.WEST);

		EDirection act = EDirection.EAST;
		Node actN = start;
		follow(actN, act, start, r);
		return r;
	}

	/**
	 * follow along the nodes neighbors to get the outline
	 *
	 * @param node
	 * @param dir
	 * @param start
	 * @param r
	 */
	private static void follow(Node node, EDirection dir, Node start, Collection<Vec2f> r) {
		Node next = node.getNeighbor(dir);
		if (node == start && dir == EDirection.EAST && !r.isEmpty()) // starting point
			return;
		if (next == null) { // no neighbor change direction
			r.add(corner(node.getRectBounds(), dir));
			follow(node, dir.rot90(), start, r);
		} else if (next.getNeighbor(dir.opposite().rot90()) != null) {
			r.add(corner(node.getRectBounds(), dir));
			follow(next.getNeighbor(dir.opposite().rot90()), dir.opposite().rot90(), start, r);
		} else {
			follow(next, dir, start, r);
		}
	}

	/**
	 * @param rectBounds
	 * @param dir
	 * @return
	 */
	private static Vec2f corner(Rect r, EDirection dir) {
		switch (dir) {
		case EAST:
			return r.x2y();
		case NORTH:
			return r.xy();
		case SOUTH:
			return r.x2y2();
		case WEST:
			return r.xy2();
		}
		throw new IllegalStateException();
	}

	private Shape getOutlineShape() {
		if (this.size() == 1)
			return get(0).getRectangleBounds();
		if (linearBlocks.size() == 1) {
			LinearBlock b = linearBlocks.get(0);
			Rect r1 = b.getNode(true).getRectBounds();
			Rect r2 = b.getNode(false).getRectBounds();
			Rectangle2D.Float r = new Rectangle2D.Float();
			Rectangle2D.union(r1.asRectangle2D(), r2.asRectangle2D(), r);
			return r;
		}
		Collection<Vec2f> outline = getOutline();
		Polygon r = new Polygon();
		for (Vec2f p : outline)
			r.addPoint((int) p.x(), (int) p.y());
		return r;
	}

	private void fadeOutElements(GLGraphics g, float w, float h) {
		final float alpha = 1 - (Math.max(0, fadeOutTime) / 300.f);
		g.color(1, 1, 1, Math.min(alpha, 0.8f));

		g.incZ(3).fillPolygon(TesselatedPolygons.polygon2(getOutline())).incZ(-3);
	}



	/**
	 * @param node
	 * @param n
	 */
	public void replace(Node node, Node with) {
		assert size() == 1 && get(0) == node;

		this.remove(node);
		if (node != with)
			findDomino().cleanup(node);
		linearBlocks.clear();
		addFirstNode(with);
		updateBands();
	}

	public Collection<Placeholder> addPlaceholdersFor(Node node) {
		List<Placeholder> r = new ArrayList<>();
		for (LinearBlock block : linearBlocks) {
			block.addPlaceholdersFor(node, r);
		}
		return r;
	}

	public void addNode(Node neighbor, EDirection dir, Node node, boolean detached) {
		this.add(node);
		LinearBlock block = getBlock(neighbor, dir.asDim());
		if (block == null)
			return;
		block.add(neighbor, dir, node, detached);
		EDimension other = dir.asDim().opposite();
		if (node.has(other.opposite()))
			linearBlocks.add(new LinearBlock(other, node));
		realign(neighbor);
		updateBlock();
		shiftBlock(dir, node);
	}

	private void shiftBlock(EDirection dir, Node node) {
		Vec2f loc = getLocation();
		if (dir == EDirection.WEST)
			setLocation(loc.x() - node.getDetachedRectBounds().width(), loc.y());
		else if (dir == EDirection.NORTH)
			setLocation(loc.x(), loc.y() - node.getDetachedRectBounds().height());
	}

	private void shiftRemoveBlock(Node node, EDimension dim) {
		Vec2f loc = getLocation();
		if (dim.isHorizontal())
			setLocation(loc.x() + node.getDetachedRectBounds().width(), loc.y());
		else
			setLocation(loc.x(), loc.y() + node.getDetachedRectBounds().height());
	}

	public void realign(Node startPoint) {
		realign(startPoint, null);
	}
	/**
	 * @param neighbor
	 */
	private void realign(Node startPoint, EDimension commingFrom) {
		LinearBlock dim = commingFrom == EDimension.DIMENSION ? null : getBlock(startPoint, EDimension.DIMENSION);
		LinearBlock rec = commingFrom == EDimension.RECORD ? null : getBlock(startPoint, EDimension.RECORD);
		if (rec != null) {
			rec.alignAlong(startPoint);
			for (Node node : rec) {
				if (node != startPoint)
					realign(node, EDimension.RECORD);
			}
		}
		if (dim != null) {
			dim.alignAlong(startPoint);
			for (Node node : dim) {
				if (node != startPoint)
					realign(node, EDimension.DIMENSION);
			}
		}
	}

	private void addFirstNode(Node node) {
		this.add(node);
		for (EDimension dim : EDimension.values()) {
			if (!node.has(dim.opposite()))
				continue;
			linearBlocks.add(new LinearBlock(dim, node));
		}
		if (context != null)
			updateSize();
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		updateSize();
	}

	public void updatedNode(Node node) {
		realign(node);
		updateBands();
		shiftToZero();
		updateSize();

		// autoStratify(node);
	}

	/**
	 * @param node
	 */
	private void autoStratify(Node node) {
		if (this.size() != 1) // just in single case
			return;
		if (!VisualizationTypeOracle.stratifyByDefault(node.getVisualizationType()))
			return;
		for (EDimension dim : EDimension.values()) {
			if (!node.has(dim))
				continue;
			LinearBlock b = getBlock(node, dim.opposite());
			if (b == null)
				continue;
			if (b.isStratisfied())
				continue;
			if (node.getUnderlyingData(dim).getGroups().size() <= 1)
				continue;
			sortBy(node, dim);
		}
	}

	/**
	 *
	 */
	private void updateSize() {
		Rectangle2D r = null;
		for (LinearBlock elem : linearBlocks) {
			if (r == null) {
				r = elem.getBounds();
			} else
				Rectangle2D.union(r, elem.getBounds(), r);
		}
		if (r == null)
			return;
		setSize((float) r.getWidth(), (float) r.getHeight());
	}

	/**
	 * @param event
	 */
	public void zoom(IMouseEvent event, Node just) {

		Vec2f s = just == null ? new Vec2f(100, 100) : just.getSize();
		Vec2f shift = ScaleLogic.shiftLogic(event, s);
		float shiftX = shift.x();
		float shiftY = shift.y();

		if (just != null) {
			just.shiftBy(shiftX, shiftY);
			for (EDimension d : just.dimensions()) {
				d = d.opposite();
				if (just.isDetached(d))
					continue;
				LinearBlock b = getBlock(just, d);
				if (b == null) // error
					continue;
				float x = d.select(0, shiftX);
				float y = d.select(shiftY, 0);
				for (Node node : b)
					if (node != just && !node.isDetached(d))
						node.shiftBy(x, y);
			}
			realign(just);
		} else {
			for (Node node : nodes())
				node.shiftBy(shiftX, shiftY);
			Node first = nodes().iterator().next();
			realign(first);
		}
		updateBlock();
		updateBands();
	}

	/**
	 *
	 */
	private void shiftToZero() {
		Vec2f offset = new Vec2f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		for (Node n : nodes()) {
			Vec2f l = n.getLocation();
			if (l.x() < offset.x() && !n.isDetached(EDimension.RECORD))
				offset.setX(l.x());
			if (l.y() < offset.y() && !n.isDetached(EDimension.DIMENSION))
				offset.setY(l.y());
		}
		if (offset.x() == 0 && offset.y() == 0)
			return;

		for (Node n : nodes()) {
			Vec2f l = n.getLocation();
			n.setLocation(l.x() - offset.x(), l.y() - offset.y());
		}
	}

	/**
	 * @return
	 */
	Iterable<Node> nodes() {
		return Iterables.filter(this, Node.class);
	}

	public boolean removeNode(Node node) {
		if (this.size() == 1) {
			this.remove(node);
			return true;
		}

		for (EDimension dim : EDimension.values()) {
			if (!node.has(dim.opposite()))
				continue;
			LinearBlock block = getBlock(node, dim);
			if (block != null) {
				if (block.size() == 1)
					linearBlocks.remove(block);
				else {
					int index = block.remove(node);
					if (index == 0) {
						realign(block.get(0));
						shiftRemoveBlock(node, dim);
					} else
						realign(block.get(index - 1));
				}
			}
		}
		this.remove(node);
		updateBlock();
		return this.isEmpty();
	}

	/**
	 * @param node
	 * @param dim
	 */
	public void removeBlock(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		if (block == null)
			return;
		List<Node> nodes = new ArrayList<>(block);
		for (Node n : nodes) {
			n.removeMe();
		}
	}

	private LinearBlock getBlock(Node node, EDimension dim) {
		for (LinearBlock block : linearBlocks) {
			if (block.getDim() == dim && block.contains(node))
				return block;
		}
		// throw new IllegalStateException();
		return null;
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		final Map<GLElement, ? extends IGLLayoutElement> lookup = Maps.uniqueIndex(children,
				AGLLayoutElement.TO_GL_ELEMENT);
		for (LinearBlock block : linearBlocks)
			block.doLayout(lookup);
		return false;
	}

	/**
	 * @param node
	 * @return
	 */
	public boolean containsNode(Node node) {
		return this.asList().contains(node);
	}

	/**
	 * @param node
	 * @param dim
	 */
	public void resort(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		block.update();
		block.apply();
		updateBlock();
	}

	private void updateBlock() {
		shiftToZero();
		updateSize();
		updateBands();
	}



	private void updateBands() {
		for (Node n : nodes())
			n.updateBands();
		findDomino().updateBands();
	}

	/**
	 * @param node
	 * @param dim
	 */
	public void sortByImpl(Node node, EDimension dim, boolean forceStratify) {
		LinearBlock block = getBlock(node, dim.opposite());
		if (block == null)
			return;
		block.sortBy(node, forceStratify);
		realign(node);
		updateBlock();
	}

	public void sortBy(Node node, EDimension dim) {
		sortByImpl(node, dim, false);
	}

	public void stratifyBy(Node node, EDimension dim) {
		sortByImpl(node, dim, true);
	}

	public void limitTo(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		block.limitDataTo(node);
		realign(node);
		updateBlock();
	}

	/**
	 * @param subList
	 * @param routes
	 */
	public void createBandsTo(List<Block> blocks, List<ABand> routes) {
		for (LinearBlock lblock : linearBlocks) {
			for (Block block : blocks) {
				for (LinearBlock rblock : block.linearBlocks) {
					if (isCompatible(lblock.getIdType(), rblock.getIdType()))
						createRoute(this, lblock, block, rblock, routes);
				}
			}
		}

	}


	private void createRoute(Block a, LinearBlock la, Block b, LinearBlock lb, List<ABand> routes) {
		TypedGroupList sData = la.getData(true);
		TypedGroupList tData = lb.getData(false);

		Rect ra = a.getAbsoluteBounds(la);
		Rect rb = b.getAbsoluteBounds(lb);

		String label = la.getNode(true).getLabel() + " x " + lb.getNode(false).getLabel();

		final INodeLocator sNodeLocator = la.getNodeLocator(true);
		final INodeLocator tNodeLocator = lb.getNodeLocator(false);
		final EDimension sDir = la.getDim().opposite();
		final EDimension tDir = lb.getDim().opposite();

		String id = toId(la.getNode(true), lb.getNode(false));
		ABand band = BandFactory.create(label, sData, tData, ra, rb, sNodeLocator, tNodeLocator, sDir, tDir, id);
		if (band == null)
			return;
		routes.add(band);
	}


	/**
	 * @param node
	 * @param node2
	 * @return
	 */
	private static String toId(Node a, Node b) {
		int ai = a.hashCode();
		int bi = b.hashCode();
		if (ai < bi)
			return ai + "X" + bi;
		return bi + "X" + ai;
	}

	/**
	 * @param la
	 * @return
	 */
	public Rect getAbsoluteBounds(LinearBlock b) {
		Rect r = new Rect(b.getBounds());
		r.xy(toAbsolute(r.xy()));
		return r;
	}

	private static boolean isCompatible(IDType a, IDType b) {
		return a.getIDCategory().isOfCategory(b);
	}



	/**
	 * @param pickable
	 */
	public void setContentPickable(boolean pickable) {
		for (Node n : nodes())
			n.setContentPickable(pickable);
	}

	/**
	 * @param node
	 * @param dimension
	 * @return
	 */
	public Color getStateColor(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		if (block == null)
			return Color.WHITE;
		return block.getStateColor(node);
	}

	public String getStateString(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		if (block == null)
			return "";
		return block.getStateString(node);
	}

	/**
	 * @return the linearBlocks, see {@link #linearBlocks}
	 */
	public List<LinearBlock> getLinearBlocks() {
		return Collections.unmodifiableList(linearBlocks);
	}

	/**
	 * @param r
	 */
	public void selectByBounds(Rectangle2D r, EToolState tool) {
		r = (Rectangle2D) r.clone(); // local copy

		Vec2f l = getLocation(); // to relative coordinates;
		r = new Rectangle2D.Double(r.getX() - l.x(), r.getY() - l.y(), r.getWidth(), r.getHeight());
		if (tool == EToolState.BANDS) {
			if (getOutlineShape().intersects(r)) {
				final NodeSelections domino = findDomino().getSelections();
				if (!domino.isSelected(SelectionType.SELECTION, this))
					domino.select(SelectionType.SELECTION, this, true);
				repaint();
			}
		} else {
			for (Node node : nodes()) {
				if (node.getRectangleBounds().intersects(r)) {
					node.selectByBounds(r);
				}
			}
		}
	}

	/**
	 * @param node
	 */
	public void tranposedNode(Node node) {
		replace(node, node);
		updatedNode(node);
	}

	public void removeMe() {
		findDomino().removeBlock(this);
	}

	/**
	 * @return
	 */
	public String getLabel() {
		return StringUtils.join(Iterators.transform(nodes().iterator(), Labels.TO_LABEL), ", ");
	}

	/**
	 *
	 */
	public void showAgain() {
		setVisibility(EVisibility.PICKABLE);
	}

	@ListenTo(sendToMe = true)
	private void onHideNodeEvent(HideNodeEvent event) {
		context.getMouseLayer().removeDragSource(source);
		setVisibility(EVisibility.HIDDEN);
	}

	/**
	 *
	 */
	public void selectAll() {
		for (Node n : nodes())
			n.selectAll();
	}

	/**
	 * @param tool
	 */
	public void setTool(EToolState tool) {
		switch (tool) {
		case BANDS:
			for (Node n : nodes()) {
				n.setVisibility(EVisibility.VISIBLE);
			}
			break;
		default:
			setFadeOut(false);
			repaint();
			for (Node n : nodes()) {
				n.setVisibility(EVisibility.PICKABLE);
				n.setContentPickable(tool == EToolState.SELECT);
			}
		}
		setVisibility(tool == EToolState.BANDS ? EVisibility.PICKABLE : EVisibility.VISIBLE);
	}
}
