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
import org.caleydo.view.domino.internal.tourguide.ui.EntityTypeSelector;
import org.caleydo.view.domino.internal.ui.DragLabelButton;
import org.caleydo.view.domino.internal.ui.DragSelectionButton;

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
		setRenderer(GLRenderers.fillRect(Color.LIGHT_BLUE));

		addToolButtons();
		this.add(new GLElement());

		addUndoRedoButtons();

		this.add(new GLElement());

		for (IDCategory cat : EntityTypeSelector.findAllUsedIDCategories()) {
			addDragLabelsButton(cat);
			final SelectionManager manager = new SelectionManager(cat.getPrimaryMappingType());
			DragSelectionButton b = new DragSelectionButton(manager);
			this.add(b);
			b.setEnabled(false);
			selections.add(manager);
		}
	}

	/**
	 *
	 */
	private void addUndoRedoButtons() {
		final GLButton undoB = new GLButton();
		final IGLRenderer uEnabled = GLRenderers.fillImage(Resources.ICON_UNDO);
		final IGLRenderer uDisabled = GLRenderers.fillImage(Resources.ICON_UNDO_DISABLED);
		undoB.setRenderer(undo.isUndoEmpty() ? uDisabled : uEnabled);
		undoB.setTooltip("Undo");
		undoB.setCallback(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				undo.undo();
			}
		});
		this.add(undoB);

		final GLButton redoB = new GLButton();
		final IGLRenderer rEnabled = GLRenderers.fillImage(Resources.ICON_REDO);
		final IGLRenderer rDisabled = GLRenderers.fillImage(Resources.ICON_REDO_DISABLED);
		redoB.setRenderer(undo.isRedoEmpty() ? rDisabled : rEnabled);
		redoB.setTooltip("Redo");
		redoB.setCallback(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				undo.redo();
			}
		});
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
		replay.setTooltip("undo all operation and redo them step by step");
		replay.setCallback(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				playStop();
			}
		});
		this.add(replay);
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
			child.setBounds(0, y, w, w);
			y += w + 3;
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
}
