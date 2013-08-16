/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import static org.caleydo.core.view.opengl.layout2.animation.Transitions.LINEAR;

import java.util.Set;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.TablePerspectiveSelectionMixin;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.DataSetSelectedEvent;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
import org.caleydo.core.view.listener.RemoveTablePerspectiveEvent;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions.IMoveTransition;
import org.caleydo.core.view.opengl.layout2.animation.Transitions;
import org.caleydo.core.view.opengl.layout2.animation.Transitions.ITransition;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.renderer.Borders;
import org.caleydo.core.view.opengl.layout2.renderer.Borders.IBorderGLRenderer;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.crossword.internal.ui.menu.PerspectiveMenuElement;
import org.caleydo.view.crossword.internal.ui.menu.SwitcherMenuElement;
import org.caleydo.view.crossword.internal.util.BitSetSet;
import org.eclipse.swt.SWT;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

/**
 * the root element of this view holding a {@link TablePerspective}
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordElement extends AnimatedGLElementContainer implements
		TablePerspectiveSelectionMixin.ITablePerspectiveMixinCallback, IPickingListener, GLButton.ISelectionCallback {

	@DeepScan
	private final TablePerspectiveSelectionMixin selection;

	private Set<Integer> recordIds;
	private Set<Integer> dimensionIds;

	@DeepScan
	private final CrosswordLayoutInfo info;

	private final IBorderGLRenderer border;

	public CrosswordElement(TablePerspective tablePerspective) {
		this.selection = new TablePerspectiveSelectionMixin(tablePerspective, this);
		this.info = new CrosswordLayoutInfo(this);
		setLayout(info);

		this.onPick(this);
		this.setVisibility(EVisibility.PICKABLE);
		setAnimateByDefault(false);

		GLElementFactorySwitcher switcher = createContent(tablePerspective);
		switcher.onActiveChanged(info);
		this.add(switcher);
		this.border = Borders.createBorder(tablePerspective.getDataDomain().getColor());
		this.add(new ReScaleBorder(info).setRenderer(border));
		final SwitcherMenuElement closeSwitcher = new SwitcherMenuElement(this);
		closeSwitcher.onPick(this);
		closeSwitcher.setVisualizationSwitcher(switcher);
		this.add(animated(true, true, closeSwitcher));
		this.add(animated(true, false, new PerspectiveMenuElement(tablePerspective, true).onPick(this)));
		this.add(animated(false, true, new PerspectiveMenuElement(tablePerspective, false).onPick(this)));
		setLayoutData(GLLayoutDatas.combine(tablePerspective, info));
		onVAUpdate(tablePerspective);
	}

	public void setTablePerspective(TablePerspective tablePerspective) {
		TablePerspective current = getTablePerspective();
		if (current == tablePerspective)
			return;
		assert current.getDataDomain() == tablePerspective.getDataDomain();
		this.selection.setTablePerspective(tablePerspective);

		// get(0)
		// ((Header)get(1)).setLabel(tablePerspective);
	}

	/**
	 * convert the ids in a perspective to a set
	 *
	 * @param set
	 * @param recordPerspective
	 */
	private Set<Integer> convert(Set<Integer> set, Perspective recordPerspective, int total) {
		VirtualArray va = recordPerspective.getVirtualArray();
		int size = va.size();
		if (size == 0)
			return ImmutableSortedSet.of();
		if (size < total / 4) { // less than 25% -> use ordinary instead of BitSet
			return ImmutableSortedSet.copyOf(va.getIDs());
		} else { // use BitSet
			if (!(set instanceof BitSetSet))
				set = new BitSetSet();
			set.clear();
			set.addAll(va.getIDs());
			return set;
		}
	}

	/**
	 * @return the dimensionIds, see {@link #dimensionIds}
	 */
	public Set<Integer> getDimensionIds() {
		return dimensionIds;
	}

	/**
	 * @return the recordIds, see {@link #recordIds}
	 */
	public Set<Integer> getRecordIds() {
		return recordIds;
	}

	private GLElement animated(boolean hor, boolean vert, GLElement elem) {
		ITransition horT = hor ? LINEAR : Transitions.NO;
		ITransition verT = vert ? LINEAR : Transitions.NO;
		IMoveTransition move = new MoveTransitions.MoveTransitionBase(verT, horT, verT, horT);
		elem.setLayoutData(GLLayoutDatas.combine(DEFAULT_DURATION, move));
		return elem;
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
		boolean isToolBar = pick.getObjectID() > 0;
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!isToolBar) {
				EventPublisher.trigger(new DataSetSelectedEvent(getTablePerspective()));
				border.setColor(border.getColor().darker());
				info.setHovered(true);
			}
			break;
		case MOUSE_OUT:
			// doesn't work EventPublisher.trigger(new DataSetSelectedEvent((TablePerspective) null));
			if (!isToolBar) {
				border.setColor(border.getColor().brighter());
				info.setHovered(info.isSelected());
			}
			break;
		case CLICKED:
			if (((isToolBar && !event.isCtrlDown()) || (!isToolBar && event.isCtrlDown())) && !pick.isAnyDragging()) {
				pick.setDoDragging(true);
			}
			if (isToolBar)
				info.setSelected(true);
			break;
		case DRAGGED:
			if (!pick.isDoDragging())
				return;
			context.getSWTLayer().setCursor(SWT.CURSOR_HAND);
			info.shift(pick.getDx(), pick.getDy());
			break;
		case MOUSE_RELEASED:
			if (pick.isDoDragging()) {
				context.getSWTLayer().setCursor(-1);
			}
			if (isToolBar)
				info.setSelected(event.isCtrlDown()); // multi selection
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
 "crossword",
				Predicates.alwaysTrue());
		GLElementFactorySwitcher swicher = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY,
				GLRenderers.fillRect(tablePerspective.getDataDomain().getColor()));
		return swicher;
	}


	/**
	 * @return the tablePerspective, see {@link #tablePerspective}
	 */
	public TablePerspective getTablePerspective() {
		return selection.getTablePerspective();
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		repaintAll();
	}

	@Override
	public void onVAUpdate(TablePerspective tablePerspective) {
		relayout();

		Table table = tablePerspective.getDataDomain().getTable();
		final int nrRecords = table.depth();
		final int nrDimensions = table.size();
		recordIds = convert(recordIds, tablePerspective.getRecordPerspective(), nrRecords);
		dimensionIds = convert(dimensionIds, tablePerspective.getRecordPerspective(), nrDimensions);

		CrosswordMultiElement p = getMultiElement();
		if (p != null)
			p.onConnectionsChanged(this);
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		CrosswordMultiElement parent = getMultiElement();
		switch (button.getTooltip()) {
		case "Close":
			EventPublisher.trigger(new RemoveTablePerspectiveEvent(getTablePerspective(),
					(IMultiTablePerspectiveBasedView) context));
			parent.remove(this);
			break;
		case "Split X":
			parent.splitDim(this);
			break;
		case "Split Y":
			parent.splitRec(this);
			break;
		default:
			break;
		}
	}

	/**
	 * @return
	 */
	CrosswordMultiElement getMultiElement() {
		return findParent(CrosswordMultiElement.class);
	}
}
