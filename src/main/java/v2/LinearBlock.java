/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.domino.api.model.graph.EDirection;

/**
 * @author Samuel Gratzl
 *
 */
public class LinearBlock extends AbstractCollection<Node> {
	private final EDimension dim;
	private final List<Node> nodes = new ArrayList<>();

	public LinearBlock(EDimension dim, Node node) {
		this.dim = dim;
		this.nodes.add(node);
	}


	public Rectangle2D getBounds() {
		Rectangle2D r = null;
		for (Node elem : nodes) {
			if (r == null) {
				r = elem.getRectangleBounds();
			} else
				Rectangle2D.union(r, elem.getRectangleBounds(), r);
		}
		return r;
	}

	public IDType getIdType() {
		return nodes.get(0).getIdType(dim.opposite());
	}

	/**
	 * @param node
	 * @param r
	 */
	public void addPlaceholdersFor(Node node, List<Placeholder> r) {
		IDType idtype = node.getIdType(dim.opposite());
		if (!isCompatible(idtype, getIdType()))
			return;
		Node n = nodes.get(0);
		if (n != node)
			r.add(new Placeholder(n, EDirection.getPrimary(dim)));
		n = nodes.get(nodes.size()-1);
		if (n != node)
			r.add(new Placeholder(n, EDirection.getPrimary(dim).opposite()));
	}

	private static boolean isCompatible(IDType a, IDType b) {
		return a.getIDCategory().isOfCategory(b);
	}
	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public Iterator<Node> iterator() {
		return nodes.iterator();
	}

	@Override
	public boolean add(Node node) {
		this.add(this.nodes.get(this.nodes.size() - 1), EDirection.getPrimary(dim).opposite(), node);
		return true;
	}

	/**
	 * @param node
	 */
	public void remove(Node node) {
		int index = nodes.indexOf(node);
		this.nodes.remove(index);
		Vec2f shift;
		if (dim.isHorizontal()) {
			shift = new Vec2f(-node.getSize().x(), 0);
		} else {
			shift = new Vec2f(0, -node.getSize().y());
		}
		shift(index, nodes.size(), shift);
	}


	/**
	 * @param neighbor
	 * @param dir
	 * @param node
	 */
	public void add(Node neighbor, EDirection dir, Node node) {
		int index = nodes.indexOf(neighbor);
		assert index >= 0;

		Node old = neighbor.getNeighbor(dir);
		neighbor.setNeighbor(dir, node);
		node.setNeighbor(dir, old);

		Rect bounds = neighbor.getRectBounds();
		Vec2f shift;
		if (dir.isPrimaryDirection()) {
			if (dim.isHorizontal()) {
				node.setLocation(bounds.x() - node.getSize().x(), bounds.y());
				shift = new Vec2f(-node.getSize().x(), 0);
			} else {
				node.setLocation(bounds.x(), bounds.y() - node.getSize().x());
				shift = new Vec2f(0, -node.getSize().y());
			}
			shift(0, index, shift);
		} else {
			if (dim.isHorizontal()) {
				node.setLocation(bounds.x2(), bounds.y());
				shift = new Vec2f(node.getSize().x(), 0);
			} else {
				node.setLocation(bounds.x(), bounds.y2());
				shift = new Vec2f(0, node.getSize().y());
			}
			shift(index + 1, nodes.size(), shift);
		}
		this.nodes.add(index + 1, node);
	}

	private void shift(int from, int to, Vec2f shift) {
		for (int i = from; i < to; ++i) {
			final Node nnode = nodes.get(i);
			Vec2f loc = nnode.getLocation();
			nnode.setLocation(loc.x() + shift.x(), loc.y() + shift.y());
		}
	}


	public void updateNeighbors() {
		Node prev = null;
		EDirection dir = EDirection.getPrimary(dim).opposite();
		for (Node node : nodes) {
			node.setNeighbor(dir, prev);
			prev = node;
		}
	}

	public void doLayout(Map<GLElement, ? extends IGLLayoutElement> lookup) {

	}

	/**
	 * @param neighbor
	 * @return
	 */
	public boolean contains(Node node) {
		return nodes.contains(node);
	}

}
