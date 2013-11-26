/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.internal.ui;

import static org.caleydo.core.view.opengl.layout2.animation.Transitions.LINEAR;

import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.TablePerspectiveSelectionMixin;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.data.DataSetSelectedEvent;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
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
import org.caleydo.view.crossword.api.model.TablePerspectiveMetaData;
import org.caleydo.view.crossword.api.model.TypedSet;
import org.caleydo.view.crossword.api.ui.CrosswordMultiElement;
import org.caleydo.view.crossword.internal.event.ChangePerspectiveEvent;
import org.caleydo.view.crossword.internal.ui.dialogs.ChangePerspectiveDialog;
import org.caleydo.view.crossword.internal.ui.menu.PerspectiveMenuElement;
import org.caleydo.view.crossword.internal.ui.menu.SwitcherMenuElement;
import org.caleydo.view.crossword.internal.util.BitSetSet;
import org.caleydo.view.crossword.spi.config.ElementConfig;
import org.eclipse.swt.SWT;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
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
	private final static int SWITCHER = 0;
	private final static int BORDER = 1;
	private final static int CORNER = 2;
	private final static int DIM_PERSPECTIVE_MENU = 3;
	private final static int REC_PERSPECTIVE_MENU = 4;
	@DeepScan
	private final TablePerspectiveSelectionMixin selection;


	private TypedSet recordIds;
	private final IIDTypeMapper<Integer, Integer> record2primary;
	private TypedSet dimensionIds;
	private final IIDTypeMapper<Integer, Integer> dimension2primary;

	@DeepScan
	private final CrosswordLayoutInfo info;

	private final IBorderGLRenderer border;

	private final TablePerspectiveMetaData metaData;

	public CrosswordElement(TablePerspective tablePerspective, TablePerspectiveMetaData metaData, ElementConfig config) {
		this(tablePerspective, metaData, config, null);
	}

	public CrosswordElement(TablePerspective tablePerspective, TablePerspectiveMetaData metaData, ElementConfig config,
			CrosswordElement parent) {
		this.selection = new TablePerspectiveSelectionMixin(tablePerspective, this);
		this.info = new CrosswordLayoutInfo(this, config);
		this.metaData = metaData;

		this.onPick(this);
		this.setVisibility(EVisibility.PICKABLE);
		setAnimateByDefault(false);

		GLElementFactorySwitcher switcher = createContent(tablePerspective);

		if (parent != null) {
			record2primary = parent.record2primary;
			dimension2primary = parent.dimension2primary;
			this.info.initFromParent(parent.info);
			switcher.setActive(parent.getSwitcher().getActive());
		} else {
			record2primary = resolvePrimaryMapper(tablePerspective.getRecordPerspective().getIdType());
			dimension2primary = resolvePrimaryMapper(tablePerspective.getDimensionPerspective().getIdType());
		}

		switcher.onActiveChanged(info);
		this.add(switcher);
		this.border = Borders.createBorder(tablePerspective.getDataDomain().getColor());
		this.add(new ReScaleBorder(info).setRenderer(border));
		final SwitcherMenuElement closeSwitcher = new SwitcherMenuElement(this, config);
		closeSwitcher.onPick(this);
		closeSwitcher.setSwitcher(switcher);
		this.add(animated(true, true, closeSwitcher));
		this.add(animated(true, false,
				new PerspectiveMenuElement(tablePerspective, config, true, this, metaData.getDimension()).onPick(this)));
		this.add(animated(false, true,
				new PerspectiveMenuElement(tablePerspective, config, false, this, metaData.getRecord()).onPick(this)));
		setLayoutData(GLLayoutDatas.combine(tablePerspective, info));
		onVAUpdate(tablePerspective);
	}

	/**
	 * @param idType
	 * @return
	 */
	private static IIDTypeMapper<Integer, Integer> resolvePrimaryMapper(IDType idType) {
		IDMappingManager manager = IDMappingManagerRegistry.get().getIDMappingManager(idType);
		return manager.getIDTypeMapper(idType, idType.getIDCategory().getPrimaryMappingType());
	}

	/**
	 * @return the info, see {@link #info}
	 */
	public CrosswordLayoutInfo getInfo() {
		return info;
	}

	public ElementConfig getConfig() {
		return info.getConfig();
	}

	private GLElementFactorySwitcher getSwitcher() {
		return (GLElementFactorySwitcher) get(SWITCHER);
	}

	/**
	 * set a new {@link TablePerspective}
	 *
	 * @param tablePerspective
	 */
	public void setTablePerspective(TablePerspective tablePerspective) {
		TablePerspective current = getTablePerspective();
		if (current.equals(tablePerspective))
			return;
		assert current.getDataDomain() == tablePerspective.getDataDomain();
		this.selection.setTablePerspective(tablePerspective);

		GLElementFactorySwitcher switcher = getSwitcher();
		{
			// remember last active
			int active = switcher.getActive();

			switcher = createContent(tablePerspective);
			switcher.setActive(active);
			switcher.onActiveChanged(info);
			this.set(SWITCHER, switcher);
		}
		((SwitcherMenuElement) get(CORNER)).setSwitcher(switcher);
		((PerspectiveMenuElement) get(DIM_PERSPECTIVE_MENU)).setTablePerspective(tablePerspective, this);
		((PerspectiveMenuElement) get(REC_PERSPECTIVE_MENU)).setTablePerspective(tablePerspective, this);
		setLayoutData(GLLayoutDatas.combine(tablePerspective, info));
		onVAUpdate(tablePerspective);

		relayoutParent();
	}

	/**
	 * convert the ids in a perspective to a set
	 *
	 * @param set
	 * @param perspective
	 */
	private TypedSet convert(Perspective perspective, int total, IIDTypeMapper<Integer, Integer> mapper) {
		VirtualArray va = perspective.getVirtualArray();
		Set<Integer> ids = mapper.apply(va.getIDs());
		int size = va.size();
		if (size == 0)
			return new TypedSet(ImmutableSortedSet.<Integer> of(), mapper.getTarget());
		if (size < total / 4) { // less than 25% -> use ordinary instead of BitSet
			return new TypedSet(ImmutableSortedSet.copyOf(ids), mapper.getTarget());
		} else { // use BitSet
			return new TypedSet(new BitSetSet(ids), mapper.getTarget());
		}
	}

	/**
	 * @param type
	 * @return
	 */
	public TypedSet getIDs(EDimension type) {
		switch (type) {
		case DIMENSION:
			return dimensionIds;
		case RECORD:
			return recordIds;
		}
		throw new IllegalStateException();
	}

	private static GLElement animated(boolean hor, boolean vert, GLElement elem) {
		ITransition horT = hor ? LINEAR : Transitions.NO;
		ITransition verT = vert ? LINEAR : Transitions.NO;
		IMoveTransition move = new MoveTransitions.MoveTransitionBase(verT, horT, verT, horT);
		elem.setLayoutData(GLLayoutDatas.combine(DEFAULT_DURATION, move));
		return elem;
	}

	@Override
	public <T> T getLayoutDataAs(java.lang.Class<T> clazz, Supplier<? extends T> default_) {
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
			if (!pick.isDoDragging() || !info.getConfig().canMove())
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
		ImmutableList<GLElementSupplier> extensions = GLElementFactories.getExtensions(builder.build(), "crossword",
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

	/**
	 * @return the metaData, see {@link #metaData}
	 */
	public TablePerspectiveMetaData getMetaData() {
		return metaData;
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
		recordIds = convert(tablePerspective.getRecordPerspective(), nrRecords, record2primary);
		dimensionIds = convert(tablePerspective.getDimensionPerspective(), nrDimensions, dimension2primary);

		CrosswordMultiElement p = getMultiElement();
		if (p != null)
			p.onConnectionsChanged(this);
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		CrosswordMultiElement parent = getMultiElement();
		switch (button.getTooltip()) {
		case "Close":
			if (context instanceof IMultiTablePerspectiveBasedView)
				EventPublisher.trigger(new RemoveTablePerspectiveEvent(getTablePerspective(),
						(IMultiTablePerspectiveBasedView) context));
			parent.remove(this);
			break;
		case "Split X":
			parent.splitDim(this);
			((PerspectiveMenuElement) get(3)).setTablePerspective(getTablePerspective(), this);
			break;
		case "Split Y":
			parent.splitRec(this);
			((PerspectiveMenuElement) get(4)).setTablePerspective(getTablePerspective(), this);
			break;
		case "Change Perspective X":
			context.getSWTLayer().run(
					new ChangePerspectiveDialog(getTablePerspective().getDimensionPerspective(), this));
			break;
		case "Change Perspective Y":
			context.getSWTLayer().run(new ChangePerspectiveDialog(getTablePerspective().getRecordPerspective(), this));
			break;
		default:
			break;
		}
	}

	@ListenTo(sendToMe = true)
	private void onChangePerspectiveEvent(ChangePerspectiveEvent event) {
		Perspective new_ = event.getNew_();
		final TablePerspective tablePerspective = getTablePerspective();
		Perspective old;
		boolean isDim = !tablePerspective.getRecordPerspective().getIdType().equals(event.getIdType());
		if (!isDim) {
			old = tablePerspective.getRecordPerspective();
		} else {
			old = tablePerspective.getDimensionPerspective();
		}
		if (old.equals(new_))
			return;

		getMultiElement().changePerspective(this, isDim, new_);
	}

	/**
	 * @return
	 */
	CrosswordMultiElement getMultiElement() {
		return findParent(CrosswordMultiElement.class);
	}

}
