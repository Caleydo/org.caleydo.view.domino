/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.MultiSelectionManagerMixin;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.util.PickingPool;
import org.caleydo.core.view.opengl.picking.IPickingLabelProvider;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingListenerComposite;
import org.caleydo.view.domino.api.model.typed.TypedGroupList;
import org.caleydo.view.domino.api.model.typed.TypedID;
import org.caleydo.view.domino.api.model.typed.TypedList;
import org.caleydo.view.domino.api.model.typed.TypedSet;
import org.caleydo.view.domino.spi.model.IBandRenderer.IBandHost;
import org.caleydo.view.domino.spi.model.IBandRenderer.SourceTarget;

import v2.band.ABand;
import v2.band.BandFactory;

import com.jogamp.common.util.IntIntHashMap;
import com.jogamp.common.util.IntIntHashMap.Entry;

/**
 * @author Samuel Gratzl
 *
 */
public class DetachedAdapter implements MultiSelectionManagerMixin.ISelectionMixinCallback, IPickingListener,
		IPickingLabelProvider, IBandHost {
	private float shift;
	private boolean isDetached;

	private PickingPool pickingPool;
	private final IntIntHashMap pickingOffsets = new IntIntHashMap();

	private ABand left;
	private ABand right;

	private final Node host;
	private final EDimension dim;

	@DeepScan
	private final MultiSelectionManagerMixin selections = new MultiSelectionManagerMixin(this);

	public DetachedAdapter(Node host, EDimension dim) {
		this.host = host;
		this.dim = dim;
	}

	/**
	 * @param isDetached
	 *            setter, see {@link isDetached}
	 */
	public void setDetached(boolean isDetached) {
		this.isDetached = isDetached;
		if (!isDetached)
			shift = 0;
	}

	/**
	 * @return the isDetached, see {@link #isDetached}
	 */
	public boolean isDetached() {
		return isDetached;
	}

	public void init(IGLElementContext context) {
		pickingPool = new PickingPool(context, PickingListenerComposite.concat(this, context.getSWTLayer()
				.createTooltip(this)));
	}

	public void takeDown() {
		pickingPool.clear();
		pickingPool = null;
	}

	/**
	 * @param shift
	 *            setter, see {@link shift}
	 */
	public void setShift(float shift) {
		this.shift = shift;
	}

	/**
	 * @return the shift, see {@link #shift}
	 */
	public float getShift() {
		return shift;
	}


	public void createBand(Node left, Node right) {
		if (!isDetached)
			return;
		if (left != null && !left.isDetached(dim)) {
			this.left = create(left, host);
		} else
			this.left = null;
		if (right != null)
			this.right = create(host, right);
		else
			this.right = null;

	}

	/**
	 * @param left2
	 * @param host2
	 * @return
	 */
	private ABand create(Node s, Node t) {
		EDimension d = dim.opposite();
		TypedGroupList sData = s.getData(d);
		TypedGroupList tData = t.getData(d);

		Rect ra = s.getRectBounds();
		Rect rb = t.getRectBounds();
		String label = s.getLabel() + " x " + s.getLabel();
		final INodeLocator sNodeLocator = s.getNodeLocator(d);
		final INodeLocator tNodeLocator = t.getNodeLocator(d);

		ABand band = BandFactory.create(label, sData, tData, ra, rb, sNodeLocator, tNodeLocator, d, d);
		return band;
	}
	@Override
	public String getLabel(Pick pick) {
		int[] split = split(pick.getObjectID());
		ABand route = getRoute(split[0]);
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
			select(getRoute(split[0]), split[1], SelectionType.MOUSE_OVER, true);
			break;
		case MOUSE_OUT:
			clear(getRoute(split[0]), split[1], SelectionType.MOUSE_OVER);
			break;
		case RIGHT_CLICKED:
			getRoute(split[0]).changeLevel(!((IMouseEvent) pick).isCtrlDown()); // FIXME
			host.repaint();
			break;
		default:
			break;
		}
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
	 * @param subIndex
	 * @param selection
	 * @param clear
	 *            whether to clear before
	 */
	private void select(ABand route, int subIndex, SelectionType type, boolean clear) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = getOrCreate(route.getIdType(st));
			if (clear)
				manager.clearSelection(type);
			manager.addToType(type, route.getIds(st, subIndex));
			selections.fireSelectionDelta(manager);
		}
		host.repaint();
	}

	private void clear(ABand route, int subIndex, SelectionType type) {
		if (route == null)
			return;
		for (SourceTarget st : SourceTarget.values()) {
			SelectionManager manager = selections.get(route.getIdType(st));
			if (manager == null)
				return;
			manager.clearSelection(type);
			selections.fireSelectionDelta(manager);
		}
	}
	/**
	 * @param objectID
	 * @return
	 */
	private ABand getRoute(int index) {
		if (index == 1 || left == null)
			return right;
		return left;
	}

	public void renderImpl(GLGraphics g, float w, float h) {
		if (!isDetached)
			return;
		Vec2f loc = host.getLocation();
		g.save().move(-loc.x(), -loc.y());
		float z = g.z();
		if (left != null) {
			g.incZ(0.002f);
			left.render(g, w, h, this);
		}
		if (right != null) {
			g.incZ(0.002f);
			right.render(g, w, h, this);
		}
		g.incZ(z - g.z());
		g.restore();
	}

	public void renderPickImpl(GLGraphics g, float w, float h) {
		if (!isDetached)
			return;
		g.incZ(0.05f);
		g.color(Color.RED);
		Vec2f loc = host.getLocation();
		g.save().move(-loc.x(), -loc.y());
		int i = 0;
		int j = 0;
		pickingOffsets.clear();
		if (left != null) {
			pickingOffsets.put(i++, j);
			j = left.renderPick(g, w, h, this, pickingPool, j);
		}
		if (right != null) {
			pickingOffsets.put(i++, j);
			j = right.renderPick(g, w, h, this, pickingPool, j);
		}
		g.restore();
		g.incZ(-0.05f);
		g.color(Color.BLACK);
	}

	@Override
	public IGLElementContext getContext() {
		return host.getContext();
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
		SelectionManager manager = selections.get(idType);
		if (manager == null) {
			manager = new SelectionManager(idType);
			selections.add(manager);
		}
		return manager;
	}

	@Override
	public void onSelectionUpdate(SelectionManager manager) {
		host.repaint();
	}

	/**
	 * @param f
	 */
	public void incShift(float f) {
		setShift(shift + f);
	}

}
