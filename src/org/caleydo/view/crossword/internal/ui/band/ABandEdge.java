package org.caleydo.view.crossword.internal.ui.band;

import gleem.linalg.Vec2f;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.crossword.internal.ui.CrosswordElement;
import org.jgrapht.graph.DefaultEdge;

public abstract class ABandEdge extends DefaultEdge implements IGLRenderer {
	private static final long serialVersionUID = 6090738439785805856L;

	private final Connector source = new Connector();
	private final Connector target = new Connector();

	private Color color;

	public ABandEdge(boolean sHor, boolean tHor, Color color) {
		source.horizontal = sHor;
		target.horizontal = tHor;
		this.color = color;
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
		source.relayout(getSource());
		target.relayout(getTarget());
	}

	public void update() {

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

	private static class Connector {
		Vec2f position;
		float size;
		boolean horizontal = true;
		float offsetPercentage = 0;
		float sizePercentage = 1;

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
		 */
		public void relayout(CrosswordElement source) {
			Rect bounds = source.getRectBounds();
			position = bounds.xy();
			size = horizontal ? bounds.width() : bounds.height();
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

		float getSize() {
			return size * sizePercentage;
		}
	}

}