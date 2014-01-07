/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2.toolbar;

import java.net.URL;
import java.util.List;

import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin.ISelectionMixinCallback;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.tourguide.vis.EntityTypeSelector;

import v2.Domino;
import v2.ui.DragLabelButton;
import v2.ui.DragSelectionButton;

import com.google.common.collect.Iterables;

/**
 * @author Samuel Gratzl
 *
 */
public class LeftToolBar extends GLElementContainer implements IGLLayout2, ISelectionCallback, ISelectionMixinCallback {

	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

	/**
	 *
	 */
	public LeftToolBar() {
		setLayout(this);
		setRenderer(GLRenderers.fillRect(Color.LIGHT_BLUE));

		addButton("Move", Resources.ICON_STATE_MOVE);
		addButton("Select", Resources.ICON_STATE_SELECT);

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
		Domino domino = findParent(Domino.class);
		switch (button.getTooltip()) {
		case "Move":
			domino.setContentPickable(false);
			break;
		case "Select":
			domino.setContentPickable(true);
			break;
		default:
			break;
		}
	}

	/**
	 * @param string
	 * @param iconSortDim
	 */
	private void addButton(String string, URL iconSortDim) {
		GLButton b = new GLButton();
		b.setCallback(this);
		b.setRenderer(GLRenderers.fillImage(iconSortDim));
		b.setTooltip(string);
		this.add(b);
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		for (DragSelectionButton b : Iterables.filter(this, DragSelectionButton.class)) {
			if (b.getManager() == manager) {
				b.setEnabled(manager.getNumberOfElements(SelectionType.SELECTION) > 0);
			}
		}
	}
}
