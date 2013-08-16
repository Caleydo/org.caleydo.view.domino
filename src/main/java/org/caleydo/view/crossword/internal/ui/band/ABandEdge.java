/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal.ui.band;

import gleem.linalg.Vec2f;

import java.util.Set;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.crossword.internal.ui.CrosswordElement;
import org.caleydo.view.crossword.internal.util.SetUtils;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author Samuel Gratzl
 *
 */
public abstract class ABandEdge extends DefaultEdge implements IGLRenderer {
	private static final long serialVersionUID = 6090738439785805856L;

	private final Connector source;
	private final Connector target;

	private Set<Integer> overlap;

	private Color color;

	public ABandEdge(Connector source, Connector target, Color color) {
		this.source = source;
		this.target = target;
		this.color = color;
	}

	public ABandEdge(boolean sHor, boolean tHor, Color color) {
		this(new Connector(sHor), new Connector(tHor), color);
	}

	@Override
	protected CrosswordElement getSource() {
		return (CrosswordElement) super.getSource();
	}

	@Override
	protected CrosswordElement getTarget() {
		return (CrosswordElement) super.getTarget();
	}

	public void relayout() {
		Rect s = getSource().getRectBounds();
		Rect t = getTarget().getRectBounds();
		source.relayout(s, t, getSource(), overlap);
		target.relayout(t, s, getTarget(), overlap);
	}

	public void update() {
		Set<Integer> s = source.getIds(getSource());
		Set<Integer> t = target.getIds(getTarget());
		overlap = SetUtils.intersection(s, t);
	}

	@Override
	public void render(GLGraphics g, float w, float h, GLElement parent) {
		g.color(color);
		Vec2f a = source.getPos();
		Vec2f as = source.shift(a);
		Vec2f b = target.getPos();
		Vec2f bs = target.shift(b);
		g.fillPolygon(a, b, bs, as);
	}



	protected static class Connector {
		final boolean horizontal;

		Vec2f position;
		float size;
		float offsetPercentage = 0;
		float sizePercentage = 1;

		/**
		 *
		 */
		public Connector(boolean horizontal) {
			this.horizontal = horizontal;
		}

		Vec2f getPos() {
			Vec2f v = position.copy();
			if (horizontal)
				v.setX(v.x() + size * offsetPercentage);
			else
				v.setY(v.y() + size * offsetPercentage);
			return v;
		}

		/**
		 * @param source
		 * @return
		 */
		public Set<Integer> getIds(CrosswordElement source) {
			return horizontal ? source.getDimensionIds() : source.getRecordIds();
		}

		/**
		 * @param selfO
		 * @param source
		 */
		public void relayout(Rect self, Rect opposite, CrosswordElement selfO, Set<Integer> overlap) {
			size = horizontal ? self.width() : self.height();
			position = self.xy();
			if (horizontal && self.y2() < opposite.y())
				position.setY(self.y2());
			else if (!horizontal && self.x2() < opposite.x())
				position.setX(self.x2());

			final Set<Integer> myIds = getIds(selfO);
			sizePercentage = overlapPercentage(myIds, overlap);
			offsetPercentage = offsetPercentage(myIds, overlap);
		}

		/**
		 * @param ids
		 * @return
		 */
		protected float overlapPercentage(Set<Integer> ids, Set<Integer> overlap) {
			int max = ids.size();
			int have = overlap.size();
			return have / (float) max;
		}

		protected float offsetPercentage(Set<Integer> ids, Set<Integer> overlap) {
			return (1 - sizePercentage) * 0.5f;
		}

		/**
		 * @param a
		 * @return
		 */
		public Vec2f shift(Vec2f a) {
			a = a.copy();
			if (horizontal)
				a.setX(a.x() + size * sizePercentage);
			else
				a.setY(a.y() + size * sizePercentage);
			return a;
		}
	}

}