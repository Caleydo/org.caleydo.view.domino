/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.AGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation.ILocator;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSets;

import v2.band.Band;
import v2.band.BandLine;
import v2.band.BandLines;

import com.google.common.collect.Iterables;
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
		setRenderer(GLRenderers.drawRect(Color.BLUE));
		addFirstNode(node);
	}



	/**
	 * @param node
	 * @param n
	 */
	public void replace(Node node, Node with) {
		assert size() == 1 && get(0) == node;

		this.remove(node);
		findParent(Domino.class).cleanupNode(node);
		linearBlocks.clear();
		addFirstNode(with);
		findParent(Domino.class).updateBands();
	}

	public Collection<Placeholder> addPlaceholdersFor(Node node) {
		List<Placeholder> r = new ArrayList<>();
		for (LinearBlock block : linearBlocks) {
			block.addPlaceholdersFor(node, r);
		}
		return r;
	}

	public void addNode(Node neighbor, EDirection dir, Node node) {
		this.add(node);
		LinearBlock block = getBlock(neighbor, dir.asDim());
		if (block == null)
			return;
		block.add(neighbor, dir, node);
		EDimension other = dir.asDim().opposite();
		if (node.has(other.opposite()))
			linearBlocks.add(new LinearBlock(other, node));
		updateBlock(block);
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
		float shiftY = rec == 0 ? 0 : event.getWheelRotation() * sizeFactor(s.x());

		incSizes(shiftX, shiftY, just);
		findParent(Domino.class).updateBands();
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

	public void incSizes(float x, float y, Node just) {
		if (just == null) {
			for (Node n : nodes()) {
				shiftNode(n, x, y);
			}
			Set<LinearBlock> b = filterSingleBlocks();
			if (!b.isEmpty()) {
				LinearBlock f = b.iterator().next();
				Node n = f.get(0);
				shiftBlocks(f, n, b, new Vec2f(0, 0));

				shiftToZero();
			}
		} else {
			just.shiftSize(x, y, false);
			Set<LinearBlock> b = filterSingleBlocks();
			if (!b.isEmpty()) {
				for (LinearBlock bi : b) {
					if (!bi.contains(just))
						continue;
					float xi = bi.getDim().select(0, x);
					float yi = bi.getDim().select(y, 0); // propagate the other direction
					for (Node n : bi) {
						if (n != just)
							shiftNode(n, xi, yi);
					}
				}
				for (LinearBlock bi : b) {
					if (bi.contains(just)) {
						shiftBlocks(bi, just, b, new Vec2f(0, 0));
						break;
					}
				}
				shiftToZero();
			}
		}
		updateSize();
	}

	/**
	 *
	 */
	private void shiftToZero() {
		Vec2f offset = new Vec2f(0, 0);
		for (Node n : nodes()) {
			Vec2f l = n.getLocation();
			if (l.x() < offset.x())
				offset.setX(l.x());
			if (l.y() < offset.y())
				offset.setY(l.y());
		}
		if (offset.x() == 0 && offset.y() == 0)
			return;

		for (Node n : nodes()) {
			Vec2f l = n.getLocation();
			n.setLocation(l.x() - offset.x(), l.y() - offset.y());
		}
	}

	private void shiftNode(Node n, float x, float y) {
		n.shiftSize(x, y, false);
	}

	private void shiftBlocks(LinearBlock block, Node start, Set<LinearBlock> b, Vec2f shift) {
		b.remove(block);
		shift = block.shift(start, shift);
		if (b.isEmpty())
			return;
		for (Node node : block) {
			for (LinearBlock bblock : new HashSet<>(b)) {
				if (bblock.contains(node)) {
					shiftBlocks(bblock, node, b, shift);
				}
			}
		}
	}

	/**
	 * @return
	 */
	private Iterable<Node> nodes() {
		return Iterables.filter(this, Node.class);
	}

	/**
	 * @return
	 */
	private Set<LinearBlock> filterSingleBlocks() {
		Set<LinearBlock> r = new HashSet<>();
		for (LinearBlock b : linearBlocks) {
			if (b.size() > 1)
				r.add(b);
		}
		return r;
	}

	public boolean removeNode(Node node) {
		for (EDimension dim : EDimension.values()) {
			if (!node.has(dim.opposite()))
				continue;
			LinearBlock block = getBlock(node, dim);
			if (block.size() == 1)
				linearBlocks.remove(block);
			block.remove(node);
		}
		this.remove(node);
		shiftToZero();
		updateSize();
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
		updateBlock(block);
	}

	private void updateBlock(LinearBlock block) {
		findParent(Domino.class).updateBands();
		shiftToZero();
		updateSize();
	}

	/**
	 * @param node
	 * @param dim
	 */
	public void sortBy(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		block.sortBy(node);

		updateBlock(block);
	}

	public void limitTo(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		block.limitDataTo(node);

		updateBlock(block);
	}

	/**
	 * @param subList
	 * @param routes
	 */
	public void createBandsTo(List<Block> blocks, List<Band> routes) {
		for (LinearBlock lblock : linearBlocks) {
			for (Block block : blocks) {
				for (LinearBlock rblock : block.linearBlocks) {
					if (isCompatible(lblock.getIdType(), rblock.getIdType()))
						createRoute(this, lblock, block, rblock, routes);
				}
			}
		}

	}


	private void createRoute(Block a, LinearBlock la, Block b, LinearBlock lb, List<Band> routes) {
		TypedGroupList sData = la.getData(true);
		TypedGroupList tData = lb.getData(false);
		MultiTypedSet shared = TypedSets.intersect(sData.asSet(), tData.asSet());
		if (shared.isEmpty())
			return;

		ILocator sLocator = la.getLocator(true);
		ILocator tLocator = lb.getLocator(false);

		Rect ra = a.getAbsoluteBounds(la);
		Rect rb = b.getAbsoluteBounds(lb);

		BandLine line = BandLines.create(ra, la.getDim(), rb, lb.getDim());
		if (line == null)
			return;

		String label = la.getNode(true).getLabel() + " x " + lb.getNode(false).getLabel();

		Band band = new Band(line, label, shared, sData, tData, sLocator, tLocator, la.getDim().opposite(), lb.getDim()
				.opposite());
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

	/**
	 * @return the linearBlocks, see {@link #linearBlocks}
	 */
	public List<LinearBlock> getLinearBlocks() {
		return Collections.unmodifiableList(linearBlocks);
	}

	/**
	 * @param r
	 */
	public void selectByBounds(Rectangle2D r) {
		r = (Rectangle2D) r.clone(); // local copy

		Vec2f l = getLocation(); // to relative coordinates;
		r = new Rectangle2D.Double(r.getX() - l.x(), r.getY() - l.y(), r.getWidth(), r.getHeight());
		for (Node node : nodes()) {
			if (node.getRectangleBounds().intersects(r)) {
				node.selectByBounds(r);
			}
		}
	}
}
