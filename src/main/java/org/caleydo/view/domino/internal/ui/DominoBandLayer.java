/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui;

import java.util.List;
import java.util.Set;

import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.view.domino.api.model.TypedSet;
import org.caleydo.view.domino.spi.model.IBandRenderer;
import org.caleydo.view.domino.spi.model.IBandRenderer.IBandHost;

/**
 * a dedicated layer for the bands for better caching behavior
 *
 * @author Samuel Gratzl
 *
 */
public class DominoBandLayer extends GLElement implements MultiSelectionManagerMixin.ISelectionMixinCallback,
		IBandHost, IPickingListener, IPickingLabelProvider {
	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

	private PickingPool pickingPool;

	private final IBandRoutesProvider provider;

	public DominoBandLayer(IBandRoutesProvider provider) {
		this.provider = provider;
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		pickingPool = new PickingPool(context, PickingListenerComposite.concat(this, context.getSWTLayer()
				.createTooltip(this)));
	}

	@Override
	protected void takeDown() {
		pickingPool.clear();
		pickingPool = null;
		super.takeDown();
	}

	@Override
	public String getLabel(Pick pick) {
		IBandRenderer route = getRoute(pick.getObjectID());
		if (route == null)
			return "";
		return route.getLabel();
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case CLICKED:
			select(getRoute(pick.getObjectID()), SelectionType.SELECTION, !((IMouseEvent) pick).isCtrlDown());
			break;
		case MOUSE_OVER:
			select(getRoute(pick.getObjectID()), SelectionType.MOUSE_OVER, true);
			break;
		case MOUSE_OUT:
			clear(getRoute(pick.getObjectID()), SelectionType.MOUSE_OVER);
			break;
		default:
			break;
		}
	}

	/**
	 * @param route
	 * @param type
	 */
	private void clear(IBandRenderer route, SelectionType type) {
		if (route == null)
			return;
		SelectionManager manager = selections.getSelectionManager(route.getIds().getIdType());
		if (manager == null)
			return;
		manager.clearSelection(type);
		selections.fireSelectionDelta(manager);
	}

	/**
	 * @param route
	 * @param selection
	 * @param clear
	 *            whether to clear before
	 */
	private void select(IBandRenderer route, SelectionType type, boolean clear) {
		if (route == null)
			return;
		SelectionManager manager = getOrCreate(route.getIdType());
		if (clear)
			manager.clearSelection(type);
		manager.addToType(type, route.getIds());
		selections.fireSelectionDelta(manager);
	}

	/**
	 * @param objectID
	 * @return
	 */
	private IBandRenderer getRoute(int index) {
		List<? extends IBandRenderer> routes = getRoutes();
		if (index < 0 || index >= routes.size())
			return null;
		return routes.get(index);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		for (IBandRenderer edge : getRoutes()) {
			edge.render(g, w, h, this);
		}
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
		List<? extends IBandRenderer> routes = getRoutes();
		for (int i = 0; i < routes.size(); i++) {
			g.pushName(pickingPool.get(i));
			routes.get(i).renderPick(g, w, h, this);
			g.popName();
		}
	}

	private List<? extends IBandRenderer> getRoutes() {
		return provider.getBandRoutes();
	}

	@Override
	public IGLElementContext getContext() {
		return context;
	}

	@Override
	public int getSelected(TypedSet ids, SelectionType type) {
		if (ids.isEmpty())
			return 0;
		SelectionManager manager = getOrCreate(ids.getIdType());
		Set<Integer> active = manager.getElements(type);
		if (active.isEmpty())
			return 0;
		return ids.and(new TypedSet(active, ids.getIdType()));
	}

	/**
	 * @param idType
	 * @return
	 */
	private SelectionManager getOrCreate(IDType idType) {
		SelectionManager manager = selections.getSelectionManager(idType);
		if (manager == null) {
			manager = new SelectionManager(idType);
			selections.add(manager);
		}
		return manager;
	}

	@Override
	protected boolean hasPickAbles() {
		return true; // routes have pickables
	}


	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		repaint();
	}

	public interface IBandRoutesProvider {
		List<? extends IBandRenderer> getBandRoutes();
	}
}
