/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import static org.caleydo.core.view.opengl.layout2.animation.Transitions.LINEAR;
import static org.caleydo.view.crossword.internal.Settings.TOOLBAR_BACKGROUND;
import static org.caleydo.view.crossword.internal.Settings.TOOLBAR_TEXT_COLOR;
import static org.caleydo.view.crossword.internal.Settings.TOOLBAR_TEXT_HEIGHT;
import static org.caleydo.view.crossword.internal.Settings.TOOLBAR_WIDTH;

import java.net.URL;
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
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
import org.caleydo.core.view.listener.RemoveTablePerspectiveEvent;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions.IMoveTransition;
import org.caleydo.core.view.opengl.layout2.animation.Transitions;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLFlowLayout;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactories.GLElementSupplier;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext.Builder;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactorySwitcher.ELazyiness;
import org.caleydo.core.view.opengl.layout2.renderer.Borders;
import org.caleydo.core.view.opengl.layout2.renderer.Borders.IBorderGLRenderer;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.layout2.renderer.RoundedRectRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.crossword.internal.Resources;
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
		setLayoutData(GLLayoutDatas.combine(tablePerspective, info));

		this.onPick(this);
		this.setVisibility(EVisibility.PICKABLE);
		setAnimateByDefault(false);

		GLElementFactorySwitcher switcher = createContent(tablePerspective);
		switcher.onActiveChanged(info);
		this.add(switcher);
		this.border = Borders.createBorder(tablePerspective.getDataDomain().getColor());
		this.add(new ReScaleBorder(info).setRenderer(border));
		this.add(animated(true,new Header(tablePerspective, switcher.size() > 1 ? switcher.createButtonBar() : null)));
		this.add(animated(false, new VerticalToolBar(null, this, tablePerspective)));

		onVAUpdate(tablePerspective);

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

	private GLElement animated(boolean  hor, GLElement elem) {
		IMoveTransition move;
		if (hor)
			move = new MoveTransitions.MoveTransitionBase(Transitions.NO,LINEAR,Transitions.NO,LINEAR);
		else
			move = new MoveTransitions.MoveTransitionBase(LINEAR,Transitions.NO,LINEAR,Transitions.NO);
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
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			EventPublisher.trigger(new DataSetSelectedEvent(getTablePerspective()));
			border.setColor(border.getColor().darker());
			info.setHovered(true);
			break;
		case MOUSE_OUT:
			// doesn't work EventPublisher.trigger(new DataSetSelectedEvent((TablePerspective) null));
			border.setColor(border.getColor().brighter());
			info.setHovered(false);
			break;
		case CLICKED:
			if (event.isCtrlDown() && !pick.isAnyDragging()) {
				context.getSWTLayer().setCursor(SWT.CURSOR_HAND);
				pick.setDoDragging(true);
			}
			break;
		case DRAGGED:
			if (!pick.isDoDragging())
				return;
			info.shift(pick.getDx(), pick.getDy());
			break;
		case MOUSE_RELEASED:
			if (pick.isDoDragging()) {
				context.getSWTLayer().setCursor(-1);
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
 "crossword",
				Predicates.alwaysTrue());
		GLElementFactorySwitcher swicher = new GLElementFactorySwitcher(extensions, ELazyiness.DESTROY);
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
			parent.addAll(getTablePerspective().getDimensionSubTablePerspectives());
			break;
		case "Split Y":
			parent.addAll(getTablePerspective().getRecordSubTablePerspectives());
			break;
		default:
			break;
		}
	}

	private static class Header extends GLElementContainer {
		private final ILabeled label;

		public Header(ILabeled label, GLElementContainer buttonBar) {
			setLayout(new GLFlowLayout(true, 2, GLPadding.ONE));
			if (buttonBar != null) {
				add(new GLElement()); // spacer
				this.add(buttonBar);
			}
			this.label = label;
			setVisibility(EVisibility.PICKABLE);
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(TOOLBAR_BACKGROUND());
			RoundedRectRenderer.render(g, 0, 0, w, h, Math.min(w, h) * 0.3f, 3, RoundedRectRenderer.FLAG_FILL
					| RoundedRectRenderer.FLAG_TOP);
			float start;
			if (size() > 1) {
				start = get(1).getLocation().x();
			} else
				start = w;
			g.textColor(TOOLBAR_TEXT_COLOR()).drawText(label, 2, 1, start - 2 - 4, TOOLBAR_TEXT_HEIGHT)
					.textColor(Color.BLACK);
			super.renderImpl(g, w, h);
		}
	}

	private static class VerticalToolBar extends GLElementContainer {
		public VerticalToolBar(GLElementContainer buttonBar, GLButton.ISelectionCallback callback, TablePerspective data) {
			setLayout(new GLFlowLayout(false, 2, GLPadding.ONE));
			addButton("Close", Resources.deleteIcon(), callback, true);
			addButton("Split X", Resources.cutDimension(), callback, data.getDimensionPerspective().getVirtualArray()
					.getGroupList().size() > 1);
			addButton("Split Y", Resources.cutRecord(), callback, data.getRecordPerspective().getVirtualArray()
					.getGroupList().size() > 1);
			if (buttonBar != null) {
				buttonBar.setLayout(GLLayouts.flowVertical(2));
				float bak = buttonBar.getSize().x();
				buttonBar.setSize(-1, bak);
				add(new GLElement()); // spacer
				this.add(buttonBar);
			}
			setSize(-1, TOOLBAR_WIDTH);
			setVisibility(EVisibility.PICKABLE); // pickable for my parent to have common area
		}

		private void addButton(String tooltip, URL icon, ISelectionCallback callback, boolean enable) {
			GLButton b = new GLButton();
			b.setTooltip(tooltip);
			b.setHoverEffect(GLRenderers.drawRoundedRect(Color.WHITE));
			if (enable) {
				b.setRenderer(GLRenderers.fillImage(icon));
				b.setCallback(callback);
			} else {
				b.setVisibility(EVisibility.VISIBLE);
				b.setRenderer(new DisabledImageRenderer(icon));
			}

			b.setSize(-1, TOOLBAR_WIDTH);
			this.add(b);
		}


		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			g.color(TOOLBAR_BACKGROUND());
			RoundedRectRenderer.render(g, 0, 0, w, h, Math.min(w, h) * 0.3f, 3, RoundedRectRenderer.FLAG_FILL
					| RoundedRectRenderer.FLAG_TOP_LEFT | RoundedRectRenderer.FLAG_BOTTOM_LEFT);
			super.renderImpl(g, w, h);
		}
	}

	private static class DisabledImageRenderer implements IGLRenderer {
		private final URL url;

		public DisabledImageRenderer(URL url) {
			this.url = url;
		}

		@Override
		public void render(GLGraphics g, float w, float h, GLElement parent) {
			g.fillImage(g.getTexture(url), 0, 0, w, h, Color.GRAY);
		}
	}

	/**
	 * @return
	 */
	CrosswordMultiElement getMultiElement() {
		return findParent(CrosswordMultiElement.class);
	}
}
