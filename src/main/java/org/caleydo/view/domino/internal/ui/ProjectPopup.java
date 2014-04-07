/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.util.Arrays;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.GLComboBox;
import org.caleydo.core.view.opengl.layout2.basic.RadioController;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.Node;
import org.caleydo.view.domino.internal.data.CorrelatedDataValues;
import org.caleydo.view.domino.internal.data.ECorrelation;
import org.caleydo.view.domino.internal.data.EProjection;
import org.caleydo.view.domino.internal.data.IDataValues;
import org.caleydo.view.domino.internal.data.Numerical2DDataDomainValues;
import org.caleydo.view.domino.internal.data.ProjectedDataValues;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class ProjectPopup extends GLElementContainer {

	private final Numerical2DDataDomainValues wrappee;
	private final TypedList dim;
	private final TypedList rec;
	private final Domino domino;

	private EDimension along = EDimension.DIMENSION;
	private EProjection proj = EProjection.MEAN;
	private ECorrelation correlation = ECorrelation.PEARSON;
	private boolean useProj = true;

	public ProjectPopup(Node node, Domino domino) {
		this((Numerical2DDataDomainValues) node.getDataValues(), node.getData(EDimension.DIMENSION), node
				.getData(EDimension.RECORD), domino);
	}

	public ProjectPopup(Numerical2DDataDomainValues wrappee, TypedList dim, TypedList rec, Domino domino) {
		this.wrappee = wrappee;
		this.dim = dim;
		this.rec = rec;
		this.domino = domino;

		createUI();
		setSize(200, 200);
		setLayout(GLLayouts.flowVertical(2));
		setRenderer(GLRenderers.fillRect(Color.WHITE));
	}

	/**
	 *
	 */
	private void createUI() {
		this.add(new GLElement(GLRenderers.drawText("Project " + wrappee.getLabel(), VAlign.CENTER)).setSize(-1, 20));
		createDimSelector();
		createProjectionTypeSelector();

		this.add(new DragMeButton());
	}

	/**
	 * @return
	 */
	public IDataValues createProjectedData() {
		assert isReady();
		TypedList other = along.select(dim, rec);
		if (useProj)
			return new ProjectedDataValues(wrappee, proj, along, other);
		else
			return new CorrelatedDataValues(wrappee, correlation, along, other);
	}

	/**
	 * @return
	 */
	public boolean isReady() {
		return this.along != null && ((useProj && proj != null) || (!useProj && correlation != null));
	}

	private void createDimSelector() {
		RadioController dim = new RadioController(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				along = button.getLayoutDataAs(EDimension.class, null);
			}
		});
		this.add(new GLElement(GLRenderers.drawText("Dimension:")).setSize(-1, 16));
		for (EDimension d : EDimension.values()) {
			GLButton b = new GLButton(EButtonMode.CHECKBOX);
			IDType idType = wrappee.getIDType(d);
			b.setLayoutData(d);
			b.setRenderer(GLButton.createCheckRenderer(idType.getIDCategory().getCategoryName()));
			dim.add(b);
			this.add(b.setSize(-1, 14));
		}
		dim.setSelected(0);
	}

	private void createProjectionTypeSelector() {
		this.add(new GLElement(GLRenderers.drawText("Projection:")).setSize(-1, 16));
		RadioController dim = new RadioController(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				Class<?> c = button.getLayoutDataAs(Class.class, null);
				useProj = EProjection.class.isAssignableFrom(c);
			}
		});

		GLElementContainer l = new GLElementContainer(GLLayouts.flowHorizontal(2));
		GLButton b = new GLButton(EButtonMode.CHECKBOX);
		b.setLayoutData(EProjection.class);
		b.setRenderer(GLButton.createCheckRenderer("Summarize"));
		dim.add(b);
		l.add(b.setSize(100, -1));

		GLComboBox<EProjection> p = new GLComboBox<>(Arrays.asList(EProjection.values()), GLComboBox.DEFAULT,
				GLRenderers.fillRect(Color.WHITE));
		p.setCallback(new GLComboBox.ISelectionCallback<EProjection>() {
			@Override
			public void onSelectionChanged(GLComboBox<? extends EProjection> widget, EProjection item) {
				proj = item;
			}
		});
		p.setSelected(0);
		l.add(p.setSize(-1, 14));
		this.add(l.setSize(-1, 18));

		l = new GLElementContainer(GLLayouts.flowHorizontal(2));

		b = new GLButton(EButtonMode.CHECKBOX);
		b.setLayoutData(ECorrelation.class);
		b.setRenderer(GLButton.createCheckRenderer("Correlate"));
		dim.add(b);
		l.add(b.setSize(100, -1));

		GLComboBox<ECorrelation> c = new GLComboBox<>(Arrays.asList(ECorrelation.values()), GLComboBox.DEFAULT,
				GLRenderers.fillRect(Color.WHITE));
		c.setCallback(new GLComboBox.ISelectionCallback<ECorrelation>() {
			@Override
			public void onSelectionChanged(GLComboBox<? extends ECorrelation> widget, ECorrelation item) {
				correlation = item;
			}
		});
		c.setSelected(0);
		l.add(c.setSize(-1, 14));
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
