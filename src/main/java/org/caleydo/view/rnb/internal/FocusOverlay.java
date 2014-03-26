/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
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
import org.caleydo.view.rnb.api.model.typed.TypedGroupList;
import org.caleydo.view.rnb.api.model.typed.TypedList;
import org.caleydo.view.rnb.internal.data.IDataValues;
import org.caleydo.view.rnb.internal.data.TransposedDataValues;
import org.caleydo.view.rnb.internal.plugin.Settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class FocusOverlay extends GLElementContainer implements IPickingListener, IHasMinSize, ISelectionCallback {

	private GLElementFactorySwitcher switcher;

	private float shiftX, shiftY;

	private String label;
	private IDataValues data;
	private TypedGroupList dimData;
	private TypedGroupList recData;

	public FocusOverlay(Node node) {
		super(GLLayouts.flowVertical(2));

		this.switcher = build(node);
		this.add(createButtonBar(node.getLabel(), switcher));

		final ScrollingDecorator scroll = ScrollingDecorator.wrap(switcher, Settings.SCROLLBAR_WIDTH);
		this.add(scroll);

		scroll.setMinSizeProvider(this);
		setVisibility(EVisibility.PICKABLE);
		onPick(this);

		this.label = node.getLabel();
		this.data = node.getDataValues();
		this.dimData = node.getData(EDimension.DIMENSION);
		this.recData = node.getData(EDimension.RECORD);
	}

	public void transpose() {
		TypedGroupList t = this.dimData;
		this.dimData = this.recData;
		this.recData = t;
		float t2 = shiftX;
		this.shiftX = shiftY;
		this.shiftY = t2;

		this.switcher = buildTransposed(switcher, data, dimData, recData);

		this.clear();

		this.add(createButtonBar(label, switcher));

		final ScrollingDecorator scroll = ScrollingDecorator.wrap(switcher, Settings.SCROLLBAR_WIDTH);
		this.add(scroll);
	}

	/**
	 * @param node
	 * @param switcher2
	 * @return
	 */
	private GLElement createButtonBar(String label, GLElementFactorySwitcher s) {
		GLElementContainer c = new GLElementContainer(GLLayouts.flowHorizontal(2));
		c.add(new GLElement(GLRenderers.drawText(label, VAlign.LEFT, new GLPadding(1, 1, 1, 5))));
		final ButtonBarBuilder b = s.createButtonBarBuilder();
		b.layoutAs(EButtonBarLayout.HORIZONTAL).size(24);
		b.prepend(createTransposeButton());
		b.customCallback(this);
		int size = Iterables.size(s) + 1;
		c.add(b.build().setSize(size * 26, -1));
		c.setSize(-1, 24);
		return c;
	}

	/**
	 * @return
	 */
	private GLElement createTransposeButton() {
		GLButton b = new GLButton();
		b.setCallback(this);
		b.setRenderer(GLRenderers.fillImage(Resources.ICON_TRANSPOSE));
		b.setTooltip("Transpose");
		b.setSize(24, -1);
		return b;
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		if ("Transpose".equals(button.getTooltip())) {
			transpose();
		} else if (size() > 1) {
			// update min size
			get(1).relayout();
		}
	}

	private GLElementFactorySwitcher build(Node node) {
		GLElementFactorySwitcher s = build(node.getDataValues(), node.getData(EDimension.DIMENSION),
				node.getData(EDimension.RECORD));
		node.selectDefaultVisualization(s);
		return s;
	}

	private GLElementFactorySwitcher buildTransposed(GLElementFactorySwitcher ori, IDataValues data, TypedList dimData,
			TypedList recData) {
		data = TransposedDataValues.transpose(data);
		GLElementFactorySwitcher s = build(data, recData, dimData);
		String id = ori.getActiveId();
		for (GLElementSupplier sup : s) {
			if (sup.getId().equals(id)) {
				s.setActive(sup);
				break;
			}
		}
		return s;
	}

	private GLElementFactorySwitcher build(IDataValues data, TypedList dimData, TypedList recData) {
		Builder b = GLElementFactoryContext.builder();
		data.fill(b, dimData, recData, new boolean[4]);
		// if free high else medium
		b.put(EDetailLevel.class, EDetailLevel.HIGH);
		b.set("heatmap.blurNotSelected");
		b.set("heatmap.forceTextures");
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(b.build(),
				"domino." + data.getExtensionID(), data);
		GLElementFactorySwitcher s = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
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
		double w = dim.size(dimData.size()) + shiftX;
		double h = rec.size(recData.size()) + shiftY;
		w = Math.max(200, w);
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
