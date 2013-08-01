/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import java.util.List;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.TablePerspectiveSelectionMixin;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.renderer.Borders;
import org.caleydo.core.view.opengl.layout2.renderer.Borders.IBorderGLRenderer;
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
		TablePerspectiveSelectionMixin.ITablePerspectiveMixinCallback, IPickingListener, IGLLayout {

	private final TablePerspective tablePerspective;

	@DeepScan
	private final TablePerspectiveSelectionMixin selection;

	@DeepScan
	private final CrosswordLayoutInfo info;

	private boolean dragged;

	private final IBorderGLRenderer border;

	public CrosswordElement(TablePerspective tablePerspective) {
		setLayout(this);
		this.tablePerspective = tablePerspective;
		this.selection = new TablePerspectiveSelectionMixin(tablePerspective, this);
		this.info = new CrosswordLayoutInfo(this);
		setLayoutData(GLLayoutDatas.combine(tablePerspective, info));
		GLElementFactorySwitcher switcher = createContent(tablePerspective);
		switcher.onActiveChanged(info);
		this.add(switcher);
		this.add(switcher.createButtonBar());
		this.border = Borders.createBorder(tablePerspective.getDataDomain().getColor());
		this.add(new ReScaleBorder(info).setRenderer(border));
		this.onPick(this);
		this.setVisibility(EVisibility.PICKABLE);
	}

	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {
		CrosswordLayout.doElementLayout(children, w, h);
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
			border.setColor(border.getColor().darker());
			break;
		case MOUSE_OUT:
			border.setColor(border.getColor().brighter());
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
			info.shift(pick.getDx(), pick.getDy());
			break;
		case MOUSE_RELEASED:
			if (dragged) {
				context.getSWTLayer().setCursor(-1);
				dragged = false;
				break;
			}
			break;
		case MOUSE_WHEEL:
			info.zoom(event);
			break;
		default:
			break;
		}
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
		GLElementFactorySwitcher swicher = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
		return swicher;
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
