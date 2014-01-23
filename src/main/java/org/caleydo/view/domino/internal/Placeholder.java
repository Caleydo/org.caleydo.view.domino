/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.DragElement;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.dnd.NodeGroupDragInfo;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.jogamp.opengl.util.texture.Texture;

/**
 * @author Samuel Gratzl
 *
 */
public class Placeholder extends PickableGLElement implements IDropGLTarget, IPickingLabelProvider {
	private final Node neighbor;
	private final EDirection dir;
	private final boolean transpose;
	private boolean isDetached;

	private Node preview;
	private final float offset;

	public Placeholder(Node neighbor, EDirection dir, boolean transpose) {
		this(neighbor, dir, transpose, 0);
	}

	public Placeholder(Node neighbor, EDirection dir, boolean transpose, float detachedOffset) {
		this.neighbor = neighbor;
		this.dir = dir;
		this.transpose = transpose;
		this.isDetached = detachedOffset > 0;
		Vec2f size = neighbor.getSize();
		Vec2f loc = neighbor.getAbsoluteLocation();
		final int c = 50;
		final float offset = detachedOffset;
		this.offset = offset + c;
		switch (dir) {
		case NORTH:
			setBounds(loc.x(), loc.y() - c - offset, size.x(), c);
			break;
		case SOUTH:
			setBounds(loc.x(), loc.y() + size.y() + offset, size.x(), c);
			break;
		case WEST:
			setBounds(loc.x() - c - offset, loc.y(), c, size.y());
			break;
		case EAST:
			setBounds(loc.x() + size.x() + offset, loc.y(), c, size.y());
			break;
		}
	}

	@Override
	protected void takeDown() {
		context.getMouseLayer().removeDropTarget(this);
		super.takeDown();
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		onPick(context.getSWTLayer().createTooltip(this));
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		IDragInfo info = item.getInfo();
		return info instanceof ADragInfo || Nodes.canExtract(item);
	}

	@Override
	public void onDrop(IDnDItem item) {
		if (preview != null) {
			final Domino domino = findParent(Domino.class);
			domino.persistPreview(dir.opposite(), preview);
			if (item.getType() != EDnDType.COPY && item.getInfo() instanceof NodeDragInfo) {
				domino.removeNode(((NodeDragInfo) item.getInfo()).getNode());
			}
		}
		preview = null;
	}

	/**
	 * @param item
	 * @return
	 */
	private Node extractNode(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			return g.getGroup().toNode();
		} else if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			return new Node(g.getNode());
		} else {
			Node node = Nodes.extract(item);
			return node;
		}
	}

	private void dropNode(Node node) {
		Domino domino = findParent(Domino.class);
		if (transpose) {
			node.transposeMe();
		}
		domino.addPreview(neighbor, dir, node, isDetached, offset);
	}

	@Override
	public void onItemChanged(IDnDItem item) {
		final Domino domino = findParent(Domino.class);
		if (domino == null)
			return;
		DragElement current = domino.getCurrentlyDraggedVis();
		if (current == null)
			return;
		current.setVisibility(EVisibility.HIDDEN);

		updatePreview(item, toRelative(item.getMousePos()));
	}

	/**
	 * @param item
	 * @param relative
	 */
	private void updatePreview(IDnDItem item, Vec2f relative) {
		if (preview == null) {
			preview = extractNode(item);
			if (preview == null)
				return;
			dropNode(preview);
		}
		if (preview == null || preview.getRepresentableSwitcher() == null)
			return;
		int index = toIndex(relative);
		List<GLElementSupplier> l = Lists.newArrayList(preview.getRepresentableSwitcher());
		String target = l.get(index).getId();
		if (target.equals(preview.getVisualizationType()))
			return;
		preview.setVisualizationType(target);
		repaint();
	}

	@Override
	public String getLabel(Pick pick) {
		if (preview == null)
			return null;
		GLElementFactorySwitcher s = preview.getRepresentableSwitcher();
		return s == null ? null : s.getActiveSupplier().getLabel();
	}

	/**
	 *
	 */
	private void deletePreview() {
		if (preview == null)
			return;
		Domino domino = findParent(Domino.class);
		domino.removePreview(preview);
		preview = null;
	}

	@Override
	protected void onMouseOver(Pick pick) {
		context.getMouseLayer().addDropTarget(this);
		setzDelta(2.f);
	}

	@Override
	protected void onMouseOut(Pick pick) {
		context.getMouseLayer().removeDropTarget(this);
		deletePreview();
		setzDelta(0.f);
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		return EDnDType.MOVE;
	}

	private int toIndex(Vec2f l) {
		EDimension dim = dir.asDim().opposite();
		float total = dim.select(getSize());
		float ratio = (dim.select(l) - total * 0.1f) / (total * 0.8f);
		int size = Iterators.size(preview.getRepresentableSwitcher().iterator());
		return Math.min((int) (ratio * size), size - 1);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		final Color c = isDetached ? Color.LIGHT_BLUE : Color.LIGHT_GRAY;
		if (preview != null) {
			final GLElementFactorySwitcher switcher = preview.getRepresentableSwitcher();
			List<GLElementSupplier> l = Lists.newArrayList(switcher);
			if (dir.isVertical()) { // in the other direction is the long one
				float wi = w * 0.8f / l.size();
				float hi = Math.min(h * 0.8f, wi);
				for (int i = 0; i < l.size(); ++i) {
					final GLElementSupplier item = l.get(i);
					final float x = w * 0.1f + wi * i + (wi - hi) * 0.5f;
					renderDropZone(g, w * 0.1f + wi * i, 0, wi, h, c);
					g.fillImage(item.getIcon(), x, (h - hi) * 0.5f, hi, hi);
					if (switcher.getActiveSupplier() == item)
						g.color(Color.BLACK).drawRoundedRect(x, (h - hi) * 0.5f, hi, hi, 5);
				}
			} else {
				float hi = h * 0.8f / l.size();
				float wi = Math.min(w * 0.8f, hi);
				for (int i = 0; i < l.size(); ++i) {
					final GLElementSupplier item = l.get(i);
					final float y = h * 0.1f + hi * i + (hi - wi) * 0.5f;
					renderDropZone(g, 0, h * 0.1f + hi * i, w, hi, c);
					g.fillImage(item.getIcon(), (w - wi) * 0.5f, y, wi, wi);
					if (switcher.getActiveSupplier() == item)
						g.color(Color.BLACK).drawRoundedRect((w - wi) * 0.5f, y, wi, wi, 5);
				}
			}
		} else {
			renderDropZone(g, 0, 0, w, h, c);
		}
	}

	static void renderDropZone(GLGraphics g, float x, float y, float w, float h, Color c) {
		g.color(c);
		g.fillRoundedRect(x, y, w, h, 5);
		{
			GL2 gl = g.gl;
			gl.glPushAttrib(GL2.GL_TEXTURE_BIT);
			Texture tex = g.getTexture(Resources.ICON_HATCHING);
			tex.enable(gl);
			tex.bind(gl);
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

			g.color(Color.WHITE);
			gl.glBegin(GL2GL3.GL_QUADS);
			gl.glTexCoord2f(0, 0);
			gl.glVertex3f(x, y, g.z());
			gl.glTexCoord2f(w / 32, 0);
			gl.glVertex3f(x + w, y, g.z());
			gl.glTexCoord2f(w / 32, h / 32);
			gl.glVertex3f(x + w, y + h, g.z());
			gl.glTexCoord2f(0, h / 32);
			gl.glVertex3f(x, y + h, g.z());
			gl.glEnd();

			tex.disable(gl);
			gl.glPopAttrib();
		}
		g.lineStippled(true).lineWidth(2);
		g.color(Color.GRAY).drawRoundedRect(x, y, w, h, 5);
		g.lineStippled(false).lineWidth(1);
	}

}
