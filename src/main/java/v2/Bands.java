/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.spi.model.IBandRenderer.IBandHost;
import org.caleydo.view.domino.spi.model.IBandRenderer.SourceTarget;

import v2.band.Band;
import v2.data.IDataValues;
import v2.data.StratificationDataValue;

import com.jogamp.common.util.IntIntHashMap;
import com.jogamp.common.util.IntIntHashMap.Entry;

/**
 * a dedicated layer for the bands for better caching behavior
 *
 * @author Samuel Gratzl
 *
 */
public class Bands extends GLElement implements MultiSelectionManagerMixin.ISelectionMixinCallback, IBandHost,
		IPickingListener, IPickingLabelProvider, IDragGLSource {
	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);


	private final List<Band> routes = new ArrayList<>();

	private PickingPool pickingPool;
	private final IntIntHashMap pickingOffsets = new IntIntHashMap();

	private int currentDragPicking;

	public Bands() {
	}

	public void update() {
		routes.clear();
		Domino domino = findParent(Domino.class);
		List<Block> blocks = domino.getBlocks();
		final int length = blocks.size();

		// create bands
		Collection<Rectangle2D> bounds = new ArrayList<>();
		for (int i = 0; i < length; ++i) {
			final Block block = blocks.get(i);
			block.createBandsTo(blocks.subList(i + 1, length), routes);
			for (LinearBlock b : block.getLinearBlocks())
				bounds.add(block.getAbsoluteBounds(b).asRectangle2D());
		}

		// collected the bounds check what we have to stubify
		for (Band band : routes) {
			for (Rectangle2D bound : bounds) {
				if (band.intersects(bound)) {
					band.stubify();
					break;
				}
			}
		}

	}

	@Override
	protected void layoutImpl(int deltaTimeMs) {
		super.layoutImpl(deltaTimeMs);
		update();
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
		int[] split = split(pick.getObjectID());
		Band route = getRoute(split[0]);
		if (route == null)
			return "";
		return route.getLabel(split[1]);
	}

	@Override
	public void pick(Pick pick) {
		int[] split = split(pick.getObjectID());
		switch (pick.getPickingMode()) {
		case CLICKED:
			select(getRoute(split[0]), split[1], SelectionType.SELECTION, !((IMouseEvent) pick).isCtrlDown());
			break;
		case MOUSE_OVER:
			context.getMouseLayer().addDragSource(this);
			this.currentDragPicking = pick.getObjectID();
			select(getRoute(split[0]), split[1], SelectionType.MOUSE_OVER, true);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDragSource(this);
			clear(getRoute(split[0]), split[1], SelectionType.MOUSE_OVER);
			break;
		case RIGHT_CLICKED:
			getRoute(split[0]).changeLevel(!((IMouseEvent) pick).isCtrlDown()); // FIXME
			repaint();
			break;
		default:
			break;
		}
	}

	@Override
	public GLElement createUI(IDragInfo info) {
		return null;
	}

	@Override
	public void onDropped(IDnDItem info) {
		// nothing to do
	}

	@Override
	public IDragInfo startSWTDrag(IDragEvent event) {
		int[] split = split(currentDragPicking);
		Band route = getRoute(split[0]);
		TypedSet ids = route.getIds(SourceTarget.SOURCE, split[1]);
		IDataValues v = new StratificationDataValue(route.getLabel(),ids,route.getDimension(SourceTarget.SOURCE));
		return new NodeDragInfo(event.getMousePos(), new Node(v));
	}

	/**
	 * @param objectID
	 * @return
	 */
	private int[] split(int objectID) {
		Entry last = null;
		for (Entry entry : pickingOffsets) {
			if (entry.value > objectID)
				break;
			last = entry;
		}
		assert last != null;
		return new int[] { last.key, objectID - last.value };
	}

	/**
	 * @param route
	 * @param type
	 */
	private void clear(Band route, int subIndex, SelectionType type) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = selections.getSelectionManager(route.getIds(st, subIndex).getIdType());
			if (manager == null)
				return;
			manager.clearSelection(type);
			selections.fireSelectionDelta(manager);
		}
	}

	/**
	 * @param route
	 * @param subIndex
	 * @param selection
	 * @param clear
	 *            whether to clear before
	 */
	private void select(Band route, int subIndex, SelectionType type, boolean clear) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = getOrCreate(route.getIdType(st, subIndex));
			if (clear)
				manager.clearSelection(type);
			manager.addToType(type, route.getIds(st, subIndex));
			selections.fireSelectionDelta(manager);
		}
		repaint();
	}

	/**
	 * @param objectID
	 * @return
	 */
	private Band getRoute(int index) {
		if (index < 0 || index >= routes.size())
			return null;
		return routes.get(index);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		Vec2f loc = getAbsoluteLocation();
		g.save().move(-loc.x(), -loc.y());
		float z = g.z();
		for (Band edge : routes) {
			g.incZ(0.002f);
			edge.render(g, w, h, this);
		}
		g.incZ(z - g.z());
		g.restore();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		g.color(Color.BLUE);
		super.renderPickImpl(g, w, h);
		g.color(Color.BLACK);
		if (getVisibility() == EVisibility.PICKABLE) {
			g.incZ(0.05f);
			g.color(Color.RED);
			Vec2f loc = getAbsoluteLocation();
			g.save().move(-loc.x(), -loc.y());
			int j = 0;
			pickingOffsets.clear();
			for (int i = 0; i < routes.size(); i++) {
				pickingOffsets.put(i, j);
				j = routes.get(i).renderPick(g, w, h, this, pickingPool, j);
			}
			g.restore();
			g.incZ(-0.05f);
			g.color(Color.BLACK);
		}
	}

	@Override
	public IGLElementContext getContext() {
		return context;
	}

	@Override
	public TypedSet getSelected(TypedSet ids, SelectionType type) {
		if (ids.isEmpty())
			return new TypedSet(Collections.<Integer> emptySet(), ids.getIdType());
		SelectionManager manager = getOrCreate(ids.getIdType());
		Set<Integer> active = manager.getElements(type);
		if (active.isEmpty())
			return new TypedSet(Collections.<Integer> emptySet(), ids.getIdType());
		return ids.intersect(new TypedSet(active, ids.getIdType()));
	}

	@Override
	public boolean isSelected(TypedID id, SelectionType type) {
		SelectionManager manager = getOrCreate(id.getIdType());
		return manager.checkStatus(type, id.getId());
	}

	@Override
	public BitSet isSelected(TypedList ids, SelectionType type) {
		if (ids.isEmpty())
			return new BitSet(0);
		SelectionManager manager = getOrCreate(ids.getIdType());
		Set<Integer> active = manager.getElements(type);
		if (active.isEmpty())
			return new BitSet(0);

		BitSet r = new BitSet(ids.size());
		for (int i = 0; i < ids.size(); ++i)
			r.set(i, active.contains(ids.get(i)));
		return r;
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


}
