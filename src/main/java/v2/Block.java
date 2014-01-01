/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.AGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.api.model.typed.MultiTypedSet;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedSets;

import v2.band.Band;
import v2.band.BandLine;
import v2.band.BandLines;

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
		this.add(node);
		for (EDimension dim : EDimension.values()) {
			if (!node.has(dim.opposite()))
				continue;
			linearBlocks.add(new LinearBlock(dim, node));
		}
		updateSize();
		setRenderer(GLRenderers.drawRect(Color.BLUE));
	}

	public Collection<Placeholder> addPlaceholdersFor(Node node) {
		List<Placeholder> r = new ArrayList<>();
		for (LinearBlock block : linearBlocks) {
			if (!node.has(block.getDim().opposite()))
				continue;
			block.addPlaceholdersFor(node, r);
		}
		return r;
	}

	public void addNode(Node neighbor, EDirection dir, Node node) {
		this.add(node);
		LinearBlock block = getBlock(neighbor, dir.asDim());
		block.add(neighbor, dir, node);
		EDimension other = dir.asDim().opposite();
		if (node.has(other.opposite()))
			linearBlocks.add(new LinearBlock(other, node));

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
		updateSize();
		return this.isEmpty();
	}

	private LinearBlock getBlock(Node node, EDimension dim) {
		for (LinearBlock block : linearBlocks) {
			if (block.getDim() == dim && block.contains(node))
				return block;
		}
		throw new IllegalStateException();
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
		findParent(Domino.class).updateBands();
	}

	/**
	 * @param node
	 * @param dim
	 */
	public void sortByMe(Node node, EDimension dim) {
		LinearBlock block = getBlock(node, dim.opposite());
		block.sortBy(node);
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

		Rect ra = a.getAbsoluteBounds(la);
		Rect rb = b.getAbsoluteBounds(lb);

		BandLine line = BandLines.create(ra, la.getDim(), rb, lb.getDim());
		if (line == null)
			return;

		Band band = new Band(line, shared, sData, tData);
		routes.add(band);
	}

	/**
	 * @param la
	 * @return
	 */
	private Rect getAbsoluteBounds(LinearBlock b) {
		Rect r = new Rect(b.getBounds());
		r.xy(toAbsolute(r.xy()));
		return r;
	}

	private static boolean isCompatible(IDType a, IDType b) {
		return a.getIDCategory().isOfCategory(b);
	}

}
