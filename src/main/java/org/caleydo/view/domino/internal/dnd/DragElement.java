/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import gleem.linalg.Vec2f;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;

import v2.Domino;

import com.google.common.base.Objects;

/**
 * @author Samuel Gratzl
 *
 */
public class DragElement extends GLElement implements IGLRenderer {

	private final String label;

	private final Domino domino;

	private final IDragInfo info;

	private final Vec2f initialSize;

	private Vec2f targetAbsoluteLoc;

	private float vertLength;

	private float horLength;

	private Vec2f hintSizes;

	public DragElement(String label, Vec2f size, Domino domino, IDragInfo info) {
		this(label, size, domino, info, null);
	}

	/**
	 * @param label
	 * @param info
	 */
	public DragElement(String label, Vec2f size, Domino domino, IDragInfo info, IGLRenderer renderer) {
		this.label = label;
		this.domino = domino;
		this.info = info;
		this.initialSize = size;
		setSize(size.x(), size.y());
		setRenderer(Objects.firstNonNull(renderer, this));
	}

	/**
	 * @return the info, see {@link #info}
	 */
	public IDragInfo getInfo() {
		return info;
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		domino.setCurrentlyDragged(this);
	}

	@Override
	protected void takeDown() {
		domino.setCurrentlyDragged(null);
		super.takeDown();
	}

	@Override
	public void layout(int deltaTimeMs) {
		final Vec2f reference = targetAbsoluteLoc;
		if (reference != null) {
			Vec2f my = toRelativeToReference(reference);
			Vec2f old = getLocation();
			if (Float.isNaN(my.x()))
				my.setX(-old.x());
			if (Float.isNaN(my.y()))
				my.setY(-old.y());
			setLocation(-my.x(), -my.y());
		}
		super.layout(deltaTimeMs);
	}

	private Vec2f toRelativeToReference(final Vec2f reference) {
		Vec2f my = getParent().toAbsolute(new Vec2f(0, 0));
		my.sub(reference);
		return my;
	}

	@Override
	public void render(GLGraphics g, float w, float h, GLElement parent) {
		float ri = Math.min(5, Math.min(w, h) * 0.45f);
		g.color(1, 1, 1, 0.75f).fillRoundedRect(0, 0, w, h, ri);
		g.color(Color.BLACK).drawRoundedRect(0, 0, w, h, ri);
		float hi = Math.min(h, 12);
		g.drawText(label, -100, (h - hi) * 0.5f, w + 200, hi, VAlign.CENTER);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);

		if (hintSizes != null) {
			g.lineStippled(true);
			if (!Float.isNaN(hintSizes.x())) {
				g.drawLine(0, 0, 0, hintSizes.x());
			}
			if (!Float.isNaN(hintSizes.y())) {
				g.drawLine(0, 0, hintSizes.y(), 0);
			}
			g.lineStippled(false);
		}

	}

	/**
	 * @param absoluteLocation
	 * @param size
	 */
	public void stickTo(Vec2f targetAbsoluteLoc, Vec2f targetSize, Vec2f hintSizes) {
		setVisibility(EVisibility.VISIBLE);
		this.hintSizes = hintSizes;
		if (targetSize == null)
			targetSize = initialSize;
		if (Float.isNaN(targetSize.x()))
			targetSize.setX(initialSize.x());
		if (Float.isNaN(targetSize.y()))
			targetSize.setY(initialSize.y());
		setSize(targetSize.x(), targetSize.y());

		if (targetAbsoluteLoc == null)
			setLocation(0, 0);
		this.targetAbsoluteLoc = targetAbsoluteLoc;
	}

	/**
	 * @param absoluteLocation
	 * @return
	 */
	public Vec2f getRelativePosition(Vec2f absoluteReference) {
		return toRelativeToReference(absoluteReference);
	}

}
