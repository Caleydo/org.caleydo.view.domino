/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder;
import org.caleydo.core.view.opengl.layout2.manage.ButtonBarBuilder.EButtonBarLayout;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.domino.api.model.graph.EProximityMode;
import org.caleydo.view.domino.internal.plugin.Settings;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class FocusOverlay extends GLElementContainer implements IPickingListener, IHasMinSize, ISelectionCallback {

	private final int dimSize;
	private final int recSize;
	private final GLElementFactorySwitcher switcher;

	private float shiftX, shiftY;

	public FocusOverlay(Node node) {
		super(GLLayouts.flowVertical(2));

		this.switcher = build(node);
		this.add(createButtonBar(node, switcher));

		final ScrollingDecorator scroll = ScrollingDecorator.wrap(switcher, Settings.SCROLLBAR_WIDTH);
		this.add(scroll);

		scroll.setMinSizeProvider(this);
		setVisibility(EVisibility.PICKABLE);
		onPick(this);

		this.dimSize = node.getData(EDimension.DIMENSION).size();
		this.recSize = node.getData(EDimension.RECORD).size();
	}

	/**
	 * @param node
	 * @param switcher2
	 * @return
	 */
	private GLElement createButtonBar(Node node, GLElementFactorySwitcher s) {
		GLElementContainer c = new GLElementContainer(GLLayouts.flowHorizontal(2));
		c.add(new GLElement(GLRenderers.drawText(node.getLabel())));
		final ButtonBarBuilder b = s.createButtonBarBuilder();
		b.layoutAs(EButtonBarLayout.HORIZONTAL).size(24);
		b.customCallback(this);
		int size = Iterables.size(s);
		c.add(b.build().setSize(size * 26, -1));
		c.setSize(-1, 24);
		return c;
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		//update min size
		get(1).relayout();
	}

	public GLElementFactorySwitcher build(Node node) {
		Builder b = GLElementFactoryContext.builder();
		node.data.fill(b, node.getData(EDimension.DIMENSION), node.getData(EDimension.RECORD));
		// if free high else medium
		b.put(EDetailLevel.class, EDetailLevel.HIGH);
		b.set("heatmap.blurNotSelected");
		b.set("heatmap.forceTextures");
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(b.build(), "domino."
				+ node.data.getExtensionID(), Predicates.and(node.data, EProximityMode.FREE));
		GLElementFactorySwitcher s = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
		node.selectDefaultVisualization(s);
		return s;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(Color.WHITE).fillRect(0, 0, w, h);
		super.renderImpl(g, w, h);
	}

	@Override
	public Vec2f getMinSize() {
		GLElementDimensionDesc dim = switcher.getActiveDesc(EDimension.DIMENSION);
		GLElementDimensionDesc rec = switcher.getActiveDesc(EDimension.RECORD);
		double w = dim.size(dimSize) + shiftX;
		double h = rec.size(recSize) + shiftY;
		w = Math.max(20, w);
		h = Math.max(20, h);
		return new Vec2f((float) w, (float) h);
	}

	public Vec2f getPreferredSize() {
		Vec2f s = getMinSize();
		s.setY(s.y() + 24);
		return s;
	}

	@Override
	public void pick(Pick pick) {
		switch(pick.getPickingMode()) {
		case MOUSE_WHEEL:
			onScale((IMouseEvent)pick);
			break;
		default:
			break;
		}
	}


	/**
	 * @param iMouseEvent
	 */
	private void onScale(IMouseEvent event) {
		Vec2f shift = ScaleLogic.shiftLogic(event, switcher.getSize());
		this.shiftX += shift.x();
		this.shiftY += shift.y();
		get(1).relayout();
	}
}
