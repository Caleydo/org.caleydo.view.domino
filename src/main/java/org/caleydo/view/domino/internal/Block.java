/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.Labels;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.AGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.internal.band.ABand;
import org.caleydo.view.domino.internal.band.BandFactory;
import org.caleydo.view.domino.internal.data.VisualizationTypeOracle;
import org.caleydo.view.domino.internal.event.HideNodeEvent;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

/**
 *
 * @author Samuel Gratzl
 *
 */
public class Block extends GLElementContainer implements IGLLayout2 {

	private final List<LinearBlock> linearBlocks = new ArrayList<>();

	public Block(Node node) {
		setLayout(this);
		node.setLocation(0, 0);
		addFirstNode(node);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		Domino domino = findParent(Domino.class);
		if (domino.isShowDebugInfos())
			g.color(Color.BLUE).drawRect(0, 0, w, h);
	}



	/**
	 * @param node
	 * @param n
	 */
	public void replace(Node node, Node with) {
		assert size() == 1 && get(0) == node;

		this.remove(node);
		if (node != with)
			findParent(Domino.class).cleanup(node);
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
		int dim = toDirection(event, EDimension.DIMENSION);
		int rec = toDirection(event, EDimension.RECORD);

		Vec2f s = just == null ? new Vec2f(100, 100) : just.getSize();
		float shiftX = dim == 0 ? 0 : event.getWheelRotation() * sizeFactor(s.x());
		float shiftY = rec == 0 ? 0 : event.getWheelRotation() * sizeFactor(s.y());

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
	 * @param x
	 * @return
	 */
	private int sizeFactor(float x) {
		if (x < 100)
			return 5;
		if (x < 500)
			return 10;
		if (x < 1000)
			return 20;
		return 50;
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
		findParent(Domino.class).updateBands();
	}

	/**
	 * @param node
	 * @param dim
	 */
	public void sortBy(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		if (block == null)
			return;
		block.sortBy(node);
		realign(node);
		updateBlock();
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

		ABand band = BandFactory.create(label, sData, tData, ra, rb, sNodeLocator, tNodeLocator, sDir, tDir);
		if (band == null)
			return;
		routes.add(band);
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
	 * convert a {@link IMouseEvent} to a direction information
	 *
	 * @param event
	 * @param dim
	 * @return -1 smaller, +1 larger, and 0 nothing
	 */
	private static int toDirection(IMouseEvent event, EDimension dim) {
		final int w = event.getWheelRotation();
		if (w == 0)
			return 0;
		int factor = w > 0 ? 1 : -1;
		return event.isCtrlDown() || dim.select(event.isAltDown(), event.isShiftDown()) ? factor : 0;
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
			return Color.BLACK;
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
	 * @param selections
	 */
	public void selectByBounds(Rectangle2D r, NodeSelections selections) {
		r = (Rectangle2D) r.clone(); // local copy

		Vec2f l = getLocation(); // to relative coordinates;
		r = new Rectangle2D.Double(r.getX() - l.x(), r.getY() - l.y(), r.getWidth(), r.getHeight());
		for (Node node : nodes()) {
			if (node.getRectangleBounds().intersects(r)) {
				node.selectByBounds(r, selections);
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
		findParent(Domino.class).removeBlock(this);
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
		setVisibility(EVisibility.VISIBLE);
	}

	@ListenTo(sendToMe = true)
	private void onHideNodeEvent(HideNodeEvent event) {
		setVisibility(EVisibility.HIDDEN);
	}

	/**
	 *
	 */
	public void selectAll() {
		for (Node n : nodes())
			n.selectAll();
	}
}
