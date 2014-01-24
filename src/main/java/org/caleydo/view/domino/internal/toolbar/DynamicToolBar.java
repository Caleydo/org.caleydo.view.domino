/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import gleem.linalg.Vec2f;

import java.util.HashSet;
import java.util.Set;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.RoundedRectRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.NodeGroup;
import org.caleydo.view.domino.internal.NodeSelections;

/**
 * a toolbar which sticks to the single node which is hovered
 *
 * @author Samuel Gratzl
 *
 */
public class DynamicToolBar extends GLElementDecorator implements ICallback<SelectionType>, IPickingListener {
	private NodeSelections selections;

	private boolean hideOnMouseOut = false;
	private boolean mouseOver = false;

	private int timerToHide = 0;

	public DynamicToolBar(NodeSelections selections) {
		this.selections = selections;
		this.selections.onSelectionChanges(this);
		setzDelta(2.f);
		setVisibility(EVisibility.PICKABLE);
		setPicker(null);
		onPick(this);
	}

	@Override
	public void on(SelectionType type) {
		Set<NodeGroup> s = selections.getSelection(type);
		if (type != SelectionType.MOUSE_OVER)
			return;
		Node act = getActNode();
		Node node = null;
		if (s.size() == 1)
			node = s.iterator().next().getNode();
		else
			node = NodeSelections.getSingleNode(s);

		if (node == null) {
			if (mouseOver) {
				hideOnMouseOut = true;
			} else
				timerToHide = 200;
			return;
		}
		timerToHide = 0;
		hideOnMouseOut = false;
		if (act == node)
			return;
		setVisibility(EVisibility.PICKABLE);
		setContent(new NodeTools(new HashSet<>(s)).setLayoutData(node));
	}

	@Override
	public void layout(int deltaTimeMs) {
		if (timerToHide > 0) {
			timerToHide -= deltaTimeMs;
			if (timerToHide <= 0) {
				setVisibility(EVisibility.HIDDEN);
				setContent(null);
			}
		}
		super.layout(deltaTimeMs);
	}

	/**
	 * @return
	 */
	private Node getActNode() {
		GLElement c = getContent();
		if (c == null)
			return null;
		Node node = ((NodeTools) c).getSelection().iterator().next().getNode();
		return node;
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			mouseOver = true;
			timerToHide = 0;
			break;
		case MOUSE_OUT:
			mouseOver = false;
			if (hideOnMouseOut) {
				setVisibility(EVisibility.HIDDEN);
				setContent(null);
				hideOnMouseOut = false;
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void layoutContent(IGLLayoutElement content, float w, float h, int deltaTimeMs) {
		final NodeTools tools = (NodeTools) content.asElement();
		Node node = tools.getSelection().iterator().next().getNode();
		Vec2f loc = node.getAbsoluteLocation();
		Vec2f base = getAbsoluteLocation();
		Vec2f rel = loc.minus(base);
		float wi = tools.getWidth(24);
		float wl = node.getSize().x();
		if (wl < wi)
			content.setBounds(rel.x() - (wi - wl) * 0.5f, rel.y() - 24, wi, 24);
		else
			content.setBounds(rel.x(), rel.y() - 24, wl, 24);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (getContent() == null)
			return;
		Rect r = getContent().getRectBounds();
		g.color(Color.LIGHT_BLUE);
		RoundedRectRenderer.render(g, r.x(), r.y(), r.width(), r.height(), 5, 3, RoundedRectRenderer.FLAG_TOP
				| RoundedRectRenderer.FLAG_FILL);

		super.renderImpl(g, w, h);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (getContent() == null)
			return;
		if (getVisibility() != EVisibility.PICKABLE)
			return;
		Rect r = getContent().getRectBounds();
		g.fillRect(r);
		super.renderPickImpl(g, w, h);
	}
}
