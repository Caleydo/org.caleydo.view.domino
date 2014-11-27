/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.Set;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.domino.internal.Block;
import org.caleydo.view.domino.internal.Domino;

/**
 * @author Samuel Gratzl
 *
 */
public class BlockDragInfo extends ADragInfo {
	private final Set<Block> blocks;
	private final Block start;

	public BlockDragInfo(Vec2f mousePos, Set<Block> blocks, Block start) {
		super(mousePos);
		this.blocks = blocks;
		this.start = start;
	}

	/**
	 * @return the start, see {@link #start}
	 */
	public Block getStart() {
		return start;
	}

	/**
	 * @return the blocks, see {@link #blocks}
	 */
	public Set<Block> getBlocks() {
		return blocks;
	}
	@Override
	public String getLabel() {
		return start.getLabel();
	}

	@Override
	protected Vec2f getSize() {
		if (start.size() == 1)
			return start.getSize();
		Rectangle2D r = start.getRectangleBounds();
		for (Block elem : blocks) {
			Rectangle2D.union(r, elem.getRectangleBounds(), r);
		}
		return new Vec2f((float) r.getWidth(), (float) r.getHeight());
	}

	/**
	 * @param domino
	 * @return
	 */
	@Override
	public GLElement createUI(Domino domino) {
		if (blocks.size() == 1)
			return super.createUI(domino);
		Vec2f size = getSize();
		final Vec2f shift = start.getLocation();
		return new DragElement(getLabel(), size, domino, this, new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				for (Block b : blocks) {
					Vec2f loc = b.getLocation().minus(shift);
					Vec2f s = b.getSize();

					float ri = Math.min(5, Math.min(s.x(), s.y()) * 0.45f);
					g.color(1, 1, 1, 0.75f).fillRoundedRect(loc.x(), loc.y(), s.x(), s.y(), ri);
					g.color(Color.BLACK).drawRoundedRect(loc.x(), loc.y(), s.x(), s.y(), ri);
					float hi = Math.min(s.y(), 12);
					g.drawText(b.getLabel(), loc.x() - 100, loc.y() + (s.y() - hi) * 0.5f, s.x() + 200, hi,
							VAlign.CENTER);
				}
			}
		});
	}

}
