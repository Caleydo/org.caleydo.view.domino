/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.TablePerspectiveSelectionMixin;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.renderer.Borders;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.eclipse.swt.SWT;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

/**
 * the root element of this view holding a {@link TablePerspective}
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordElement extends GLElementContainer implements
		TablePerspectiveSelectionMixin.ITablePerspectiveMixinCallback, IPickingListener {

	private final TablePerspective tablePerspective;

	@DeepScan
	private final TablePerspectiveSelectionMixin selection;

	@DeepScan
	private final CrosswordLayoutInfo info;

	private boolean hovered;
	private boolean dragged;

	public CrosswordElement(TablePerspective tablePerspective) {
		setLayout(GLLayouts.LAYERS);
		this.tablePerspective = tablePerspective;
		this.selection = new TablePerspectiveSelectionMixin(tablePerspective, this);
		this.info = new CrosswordLayoutInfo(this);
		setLayoutData(GLLayoutDatas.combine(tablePerspective, info));
		this.add(createContent(tablePerspective));
		this.onPick(this);
		this.setVisibility(EVisibility.PICKABLE);
		setRenderer(Borders.createBorder(tablePerspective.getDataDomain().getColor()));
	}

	@Override
	public <T> T getLayoutDataAs(java.lang.Class<T> clazz, T default_) {
		T v = get(0).getLayoutDataAs(clazz, null);
		if (v != null)
			return v;
		return super.getLayoutDataAs(clazz, default_);
	}

	IGLElementContext getContext() {
		return context;
	}

	@Override
	public void pick(Pick pick) {
		IMouseEvent event = ((IMouseEvent) pick);
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			hovered = true;
			break;
		case MOUSE_OUT:
			hovered = false;
			break;
		case CLICKED:
			if (event.isCtrlDown() && !pick.isAnyDragging()) {
				context.getSWTLayer().setCursor(SWT.CURSOR_HAND);
				pick.setDoDragging(true);
				dragged = true;
			}
			break;
		case DRAGGED:
			if (!dragged)
				return;
			float dx = pick.getDx();
			float dy = pick.getDy();
			move(dx, dy);
			break;
		case MOUSE_RELEASED:
			if (dragged) {
				context.getSWTLayer().setCursor(-1);
				dragged = false;
				break;
			}
			break;
		case MOUSE_WHEEL:
			if (event.isCtrlDown() && event.getWheelRotation() != 0) {
				setZoomFactor(info.getZoomFactor() * Math.pow(1.2, event.getWheelRotation()));
			}
			break;
		default:
			break;
		}
	}

	/**
	 * @param dx
	 * @param dy
	 */
	private void move(float dx, float dy) {
		if (dx == 0 && dy == 0)
			return;
		Vec2f location = getLocation();
		setLocation(location.x() + dx, location.y() + dy);
	}

	/**
	 * @param zoomFactor
	 *            setter, see {@link zoomFactor}
	 */
	public void setZoomFactor(double zoomFactor) {
		info.setZoomFactor(zoomFactor);
	}

	/**
	 * @param tablePerspective2
	 * @return
	 */
	private static GLElementFactorySwitcher createContent(TablePerspective tablePerspective) {
		Builder builder = GLElementFactoryContext.builder();
		builder.withData(tablePerspective);
		builder.put(EDetailLevel.class, EDetailLevel.MEDIUM);
		builder.set("heatmap.forceTextures"); // force heatmap to use textures
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(builder.build(),
				"crossword.block", Predicates.alwaysTrue());
		return new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
	}

	/**
	 * @return the tablePerspective, see {@link #tablePerspective}
	 */
	public TablePerspective getTablePerspective() {
		return tablePerspective;
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		repaintAll();
	}

	@Override
	public void onVAUpdate(TablePerspective tablePerspective) {
		relayout();
	}
}
