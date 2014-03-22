/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.toolbar;

import java.util.List;

import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin.ISelectionMixinCallback;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLKeyListener.IKeyEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.RadioController;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.view.domino.internal.Domino;
import org.caleydo.view.domino.internal.EToolState;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.UndoStack;
import org.caleydo.view.domino.internal.tourguide.DataTourGuideAdapter;
import org.caleydo.view.domino.internal.tourguide.StratifiationTourGuideAdapter;
import org.caleydo.view.domino.internal.tourguide.ui.EntityTypeSelector;
import org.caleydo.view.domino.internal.ui.DragAnnotationButton;
import org.caleydo.view.domino.internal.ui.DragLabelButton;
import org.caleydo.view.domino.internal.ui.DragRulerButton;
import org.caleydo.view.domino.internal.ui.DragSelectionButton;
import org.caleydo.view.domino.internal.ui.DragSeparatorButton;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class LeftToolBar extends GLElementContainer implements IGLLayout2, ISelectionCallback, ISelectionMixinCallback {

	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

	private final UndoStack undo;

	private boolean isAutoRedoing;
	private int timeToNextRedo;

	/**
	 * @param undo
	 *
	 */
	public LeftToolBar(UndoStack undo) {
		this.undo = undo;
		setLayout(this);

		addToolButtons();
		this.add(new Separator());

		addUndoRedoButtons();

		this.add(new Separator());

		for (IDCategory cat : EntityTypeSelector.findAllUsedIDCategories()) {
			addDragLabelsButton(cat);
			final SelectionManager manager = new SelectionManager(cat.getPrimaryMappingType());
			DragSelectionButton b = new DragSelectionButton(manager);
			this.add(b);
			b.setEnabled(false);

			DragRulerButton r = new DragRulerButton(manager);
			this.add(r);

			selections.add(manager);
		}

		this.add(new Separator());
		addExtraButtons();
	}


	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		g.color(findParent(Domino.class).getTool().getColor()).fillRect(0, 0, w, h);
		super.renderImpl(g, w, h);
	}

	/**
	 *
	 */
	private void addUndoRedoButtons() {
		final ISelectionCallback callback = new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				switch (button.getTooltip()) {
				case "Undo":
					undo.undo();
					break;
				case "Redo":
					undo.redo();
					break;
				case "Undo all operations and redo them step by step":
					playStop();
					break;
				case "Clear Undo History":
					undo.clearUndo();
					break;
				}
			}
		};

		final GLButton undoB = new GLButton();
		final IGLRenderer uEnabled = GLRenderers.fillImage(Resources.ICON_UNDO);
		final IGLRenderer uDisabled = GLRenderers.fillImage(Resources.ICON_UNDO_DISABLED);
		undoB.setRenderer(undo.isUndoEmpty() ? uDisabled : uEnabled);
		undoB.setTooltip("Undo");
		undoB.setCallback(callback);
		this.add(undoB);

		final GLButton redoB = new GLButton();
		final IGLRenderer rEnabled = GLRenderers.fillImage(Resources.ICON_REDO);
		final IGLRenderer rDisabled = GLRenderers.fillImage(Resources.ICON_REDO_DISABLED);
		redoB.setRenderer(undo.isRedoEmpty() ? rDisabled : rEnabled);
		redoB.setTooltip("Redo");
		redoB.setCallback(callback);
		this.add(redoB);

		this.undo.onChanged(new ICallback<UndoStack>() {

			@Override
			public void on(UndoStack data) {
				undoB.setRenderer(undo.isUndoEmpty() ? uDisabled : uEnabled);
				redoB.setRenderer(undo.isRedoEmpty() ? rDisabled : rEnabled);
			}
		});

		GLButton replay = new GLButton();
		replay.setRenderer(GLRenderers.fillImage(Resources.ICON_REPLAY));
		replay.setTooltip("Undo all operations and redo them step by step");
		replay.setCallback(callback);
		this.add(replay);

		GLButton clearUndo = new GLButton();
		clearUndo.setRenderer(GLRenderers.fillImage(Resources.ICON_UNDO_CLEAR));
		clearUndo.setTooltip("Clear Undo History");
		clearUndo.setCallback(callback);
		this.add(clearUndo);
	}

	/**
	 *
	 */
	private void addExtraButtons() {
		final ISelectionCallback callback = new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				Domino domino = findParent(Domino.class);
				switch (button.getTooltip()) {
				case "Show Tour Guides":
					DataTourGuideAdapter.show();
					StratifiationTourGuideAdapter.show();
					// call again to get focus
					DataTourGuideAdapter.show();
					break;
				case "Show/Hide Debug Infos":
					domino.setShowDebugInfos(!domino.isShowDebugInfos());
					break;
				case "Show/Hide Block Labels":
					domino.setShowBlockLabels(!domino.isShowBlockLabels());
					break;
				case "Show/Hide Group Labels":
					domino.setShowGroupLabels(!domino.isShowGroupLabels());
					break;
				case "Show/Hide Mini Map":
					domino.toggleShowMiniMap();
					break;
				}
			}
		};
		GLButton b = new GLButton();
		b.setRenderer(GLRenderers.fillImage(Resources.ICON_MINI_MAP));
		b.setTooltip("Show/Hide Mini Map");
		b.setCallback(callback);
		this.add(b);

		b = new GLButton();
		b.setRenderer(GLRenderers.fillImage(Resources.ICON));
		b.setTooltip("Show Tour Guides");
		b.setCallback(callback);
		this.add(b);

		b = new GLButton();
		b.setRenderer(GLRenderers.fillImage(Resources.ICON_SHOW_HIDE_DEBUG));
		b.setTooltip("Show/Hide Debug Infos");
		b.setCallback(callback);
		this.add(b);

		b = new GLButton();
		b.setRenderer(GLRenderers.fillImage(Resources.ICON_SHOW_HIDE_LABELS));
		b.setTooltip("Show/Hide Block Labels");
		b.setCallback(callback);
		this.add(b);

		b = new GLButton();
		b.setRenderer(GLRenderers.fillImage(Resources.ICON_SHOW_HIDE_LABELS));
		b.setTooltip("Show/Hide Group Labels");
		b.setCallback(callback);
		this.add(b);

		this.add(new DragSeparatorButton());
		this.add(new DragAnnotationButton());
	}

	/**
	 *
	 */
	protected void playStop() {
		if (this.isAutoRedoing) {
			this.isAutoRedoing = false;
			return;
		}
		this.isAutoRedoing = true;
		timeToNextRedo = 300;
		undo.undoAll();
	}

	@Override
	public void layout(int deltaTimeMs) {
		if (isAutoRedoing) {
			timeToNextRedo -= deltaTimeMs;
			if (timeToNextRedo <= 0) {
				undo.redo();
				timeToNextRedo = 300;
			}
			if (undo.isRedoEmpty()) {
				isAutoRedoing = false;
			}
		}
		super.layout(deltaTimeMs);
	}

	/**
	 *
	 */
	private void addToolButtons() {
		RadioController c = new RadioController(this);
		for (EToolState tool : EToolState.values()) {
			GLButton b = new GLButton();
			b.setLayoutData(tool);
			IGLRenderer m = GLRenderers.fillImage(tool.toIcon());
			b.setRenderer(m);
			b.setSelectedRenderer(concat(m, GLRenderers.drawRoundedRect(Color.BLACK)));
			b.setTooltip(tool.getLabel());
			c.add(b);
			this.add(b);
		}
	}

	/**
	 * @param e
	 */
	public void keyPressed(IKeyEvent e) {
		if (e.isKey('m') || e.isKey('M'))
			setTool(EToolState.MOVE);
		else if (e.isKey('s') || e.isKey('S'))
			setTool(EToolState.SELECT);
		else if (e.isKey('b') || e.isKey('B'))
			setTool(EToolState.BANDS);
		else if (e.isControlDown() && (e.isKey('z') || e.isKey('Z')))
			undo.undo();
		else if (e.isControlDown() && (e.isKey('y') || e.isKey('Y')))
			undo.redo();

	}

	/**
	 * @param move
	 */
	private void setTool(EToolState tool) {
		for (GLButton b : Iterables.filter(this, GLButton.class)) {
			if (b.getLayoutDataAs(EToolState.class, null) == tool)
				b.setSelected(true);
		}
	}

	/**
	 * @param cat
	 */
	private void addDragLabelsButton(IDCategory category) {
		DragLabelButton b = new DragLabelButton(category);
		this.add(b);
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		float y = 0;
		for (IGLLayoutElement child : children) {
			if (child.asElement() instanceof Separator) {
				child.setBounds(0, y - 1, w, 10);
				y += 9;
			} else {
				child.setBounds(0, y, w, w);
				y += w + 3;
			}

		}
		return false;
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		findParent(Domino.class).setTool(button.getLayoutDataAs(EToolState.class, null));
	}


	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		for (DragSelectionButton b : Iterables.filter(this, DragSelectionButton.class)) {
			if (b.getManager() == manager) {
				b.setNumberOfElements(manager.getNumberOfElements(SelectionType.SELECTION));
			}
		}
	}

	public void onShowHideRuler(IDCategory category, boolean show) {
		for (DragRulerButton b : Iterables.filter(this, DragRulerButton.class)) {
			if (b.getIDCategory() == category) {
				b.setDragMode(!show);
			}
		}
	}

	public static IGLRenderer concat(final IGLRenderer... renderers) {
		return new IGLRenderer() {

			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
				for (IGLRenderer r : renderers) {
					r.render(g, w, h, parent);
				}
			}
		};
	}

	private final static class Separator extends GLElement {
		public Separator() {
			setSize(-1, 10);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.lineWidth(2).color(Color.LIGHT_GRAY).drawLine(0, h * 0.5f, w, h * 0.5f).lineWidth(1);
			super.renderImpl(g, w, h);
		}
	}
}
