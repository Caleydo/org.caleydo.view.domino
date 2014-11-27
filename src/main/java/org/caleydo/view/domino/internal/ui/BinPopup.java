/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.GLSlider;
import org.caleydo.core.view.opengl.layout2.basic.GLSpinner;
import org.caleydo.core.view.opengl.layout2.basic.RadioController;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedGroupSet;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSetGroup;
import org.caleydo.view.domino.internal.Constants;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.data.IDataValues;
import org.caleydo.view.domino.internal.data.INumerical1DContainer;
import org.caleydo.view.domino.internal.data.Numerical1DMixin;
import org.caleydo.view.domino.internal.data.StratificationDataValue;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class BinPopup extends GLElementContainer {

	private final INumerical1DContainer wrappee;
	private final TypedList data;
	private final Domino domino;
	private final EDimension main;

	// either define number of bins, or rounding to some value,
	private boolean useBins = true;
	private int bins;
	private float step = 0.5f;

	public static BinPopup create(Node node, Domino domino) {
		INumerical1DContainer wrappee = (INumerical1DContainer) node.getDataValues();
		EDimension main = EDimension.get(node.has(EDimension.DIMENSION));
		TypedGroupList data = node.getData(main);
		return new BinPopup(wrappee, data, main, domino);
	}
	public BinPopup(INumerical1DContainer wrappee, TypedList data, EDimension main, Domino domino) {
		this.wrappee = wrappee;
		this.data = data;
		this.main = main;
		this.domino = domino;
		this.bins = (int) Math.sqrt(data.size());

		createUI();
		setSize(200, 200);
		setLayout(GLLayouts.flowVertical(2));
		setRenderer(GLRenderers.fillRect(Color.WHITE));
	}

	/**
	 *
	 */
	private void createUI() {
		this.add(new GLElement(GLRenderers.drawText("Bin " + wrappee.getLabel(), VAlign.CENTER)).setSize(-1, 20));
		createProjectionTypeSelector();

		this.add(new DragMeButton());
	}

	/**
	 * @return
	 */
	public IDataValues createProjectedData() {
		assert isReady();
		TypedGroupSet data = createData();
		return new StratificationDataValue(wrappee.getLabel(), data, main);
	}

	/**
	 * @return
	 */
	private TypedGroupSet createData() {
		List<TypedSetGroup> groups = new ArrayList<>();
		int b = bins;
		if (!useBins) { // derive number of bins from step width
			b = 0;
			Vec2f minmax = findMinMax();
			float act = minmax.x();
			for (b = 0; act < minmax.y(); ++b) {
				act += step;
			}
		}
		Histogram h = new Histogram(b);
		for (Integer id : data) {
			float v = wrappee.getNormalized(id.intValue());
			if (Float.isNaN(v)) {
				h.addNAN(id);
			} else {
				int bucketIndex = Numerical1DMixin.toBin(v, b);
				h.add(bucketIndex, id);
			}
		}
		for (int i = 0; i < b; ++i) {
			SortedSet<Integer> ids = h.getIDsForBucket(i);
			if (ids.isEmpty())
				continue;
			String label = "Bin " + (i + 1);
			Color color = Constants.colorMapping(i / (b - 1));
			groups.add(new TypedSetGroup(ids, data.getIdType(), label, color));
		}
		if (h.getNanCount() > 0) {
			String label = "NaN";
			Color color = Color.NOT_A_NUMBER_COLOR;
			groups.add(new TypedSetGroup(h.getNanIDs(), data.getIdType(), label, color));
		}
		return new TypedGroupSet(groups);
	}

	/**
	 * @return
	 */
	private Vec2f findMinMax() {
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;
		for (Integer id : data) {
			Float raw = this.wrappee.getRaw(id);
			if (raw == null || raw.isInfinite() || raw.isNaN())
				continue;
			if (min > raw.floatValue())
				min = raw.floatValue();
			if (max < raw.floatValue())
				max = raw.floatValue();
		}
		return new Vec2f(min, max);
	}

	/**
	 * @return
	 */
	public boolean isReady() {
		return (useBins && bins > 1) || (!useBins && step > 0);
	}

	private void createProjectionTypeSelector() {
		RadioController dim = new RadioController(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				useBins = Boolean.TRUE == button.getLayoutDataAs(Boolean.class, null);
			}
		});

		GLElementContainer l = new GLElementContainer(GLLayouts.flowHorizontal(2));
		GLButton b = new GLButton(EButtonMode.CHECKBOX);
		b.setLayoutData(Boolean.TRUE);
		b.setRenderer(GLButton.createCheckRenderer("Bin Count"));
		dim.add(b);
		l.add(b.setSize(100, -1));

		GLSpinner<Integer> s = GLSpinner.createIntegerSpinner(this.bins, 2, Integer.MAX_VALUE, 1);
		s.setCallback(new GLSpinner.IChangeCallback<Integer>() {
			@Override
			public void onValueChanged(GLSpinner<? extends Integer> spinner, Integer value) {
				bins = value.intValue();
			}
		});
		l.add(s.setSize(-1, 14));
		this.add(l.setSize(-1, 18));

		l = new GLElementContainer(GLLayouts.flowHorizontal(2));

		b = new GLButton(EButtonMode.CHECKBOX);
		b.setLayoutData(Boolean.FALSE);
		b.setRenderer(GLButton.createCheckRenderer("Bin Step Width"));
		dim.add(b);
		l.add(b.setSize(100, -1));

		GLSlider sl = new GLSlider(0, findMinMax().y(), 0.5f);
		sl.setCallback(new GLSlider.ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLSlider slider, float value) {
				step = value;
			}
		});
		l.add(sl.setSize(-1, 14));
		this.add(l.setSize(-1, 18));
	}

	private final class DragMeButton extends ADragButton implements IGLRenderer {

		public DragMeButton() {
			setRenderer(this);
		}

		@Override
		public IDragInfo startSWTDrag(IDragEvent event) {
			return new NodeDragInfo(event.getMousePos(), createNode());
		}

		private Node createNode() {
			return new Node(createProjectedData());
		}

		@Override
		public void render(GLGraphics g, float w, float h, GLElement parent) {
			if (!isReady())
				g.textColor(Color.LIGHT_GRAY);
			float hi = Math.min(18, h - 2);
			g.drawText("Drag Me", 0, 0 + (h - 2 - hi) * 0.5f, w, hi, VAlign.CENTER);
			g.textColor(Color.BLACK);
		}

		@Override
		public GLElement createUI(IDragInfo info) {
			if (info instanceof ADragInfo)
				return ((ADragInfo) info).createUI(domino);
			return null;
		}
	}

}
