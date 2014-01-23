/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLKeyListener;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource.IDragEvent;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.BlockDragInfo;
import org.caleydo.view.domino.internal.dnd.DragElement;
import org.caleydo.view.domino.internal.dnd.MultiNodeGroupDragInfo;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.dnd.NodeGroupDragInfo;
import org.caleydo.view.domino.internal.dnd.TablePerspectiveRemoveDragCreator;
import org.caleydo.view.domino.internal.event.HideNodeEvent;
import org.caleydo.view.domino.internal.toolbar.LeftToolBar;
import org.caleydo.view.domino.internal.toolbar.ToolBar;

import com.google.common.collect.ImmutableList;

/**
 * @author Samuel Gratzl
 *
 */
public class Domino extends GLElementContainer implements IDropGLTarget, IPickingListener, IGLLayout2, IGLKeyListener {
	private GLElementContainer placeholders;
	private final Bands bands;
	private final Blocks blocks;
	private final ToolBar toolBar;
	private final LeftToolBar leftToolBar;
	private final GLElementContainer content;
	private SelectLayer select;
	private DragElement currentlyDraggedVis;
	private boolean showDebugInfos = true;
	private boolean showMiniMap = false;

	@DeepScan
	private final NodeSelections selections = new NodeSelections();

	/**
	 *
	 */
	public Domino() {
		setLayout(this);

		this.toolBar = new ToolBar(selections);
		this.toolBar.setSize(-1, 24);
		this.add(toolBar);

		this.leftToolBar = new LeftToolBar();
		this.leftToolBar.setSize(24, -1);
		this.add(leftToolBar);

		this.content = new GLElementContainer(GLLayouts.LAYERS);
		content.setVisibility(EVisibility.PICKABLE);
		content.onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				if (pick.getPickingMode() == PickingMode.MOUSE_OUT)
					removePlaceholder();
			}
		});
		ScrollingDecorator scroll = ScrollingDecorator.wrap(content, 10);
		this.add(scroll);

		// fakeData();
		this.blocks = new Blocks();
		scroll.setMinSizeProvider(blocks);
		scroll.setAutoResetViewport(false);
		blocks.setzDelta(0.1f);
		content.add(blocks);

		this.bands = new Bands();
		this.bands.setVisibility(EVisibility.PICKABLE);
		this.bands.setzDelta(0.01f);
		this.bands.onPick(this);
		content.add(this.bands);

		selections.onSelectionChanges(new ICallback<SelectionType>() {
			@Override
			public void on(SelectionType data) {
				NodeDataItem.update(selections.getSelection(SelectionType.MOUSE_OVER),
						selections.getSelection(SelectionType.SELECTION));
			}
		});
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h, IGLLayoutElement parent,
			int deltaTimeMs) {
		children.get(0).setBounds(0, 0, w, 24);
		children.get(1).setBounds(0, 24, 24, h - 24);

		for (IGLLayoutElement elem : children.subList(2, children.size()))
			elem.setBounds(24, 24, w - 24, h - 24);
		return false;
	}

	@Override
	protected void init(IGLElementContext context) {
		context.getMouseLayer().addRemoteDragInfoUICreator(new TablePerspectiveRemoveDragCreator(this));
		super.init(context);
	}

	/**
	 * @return the toolBar, see {@link #toolBar}
	 */
	public ToolBar getToolBar() {
		return toolBar;
	}

	/**
	 * @return the showDebugInfos, see {@link #showDebugInfos}
	 */
	public boolean isShowDebugInfos() {
		return showDebugInfos;
	}

	/**
	 * @param showDebugInfos
	 *            setter, see {@link showDebugInfos}
	 */
	public void setShowDebugInfos(boolean showDebugInfos) {
		this.showDebugInfos = showDebugInfos;
	}

	public static void main(String[] args) {
		GLSandBox.main(args, new Domino());
	}

	@Override
	protected boolean hasPickAbles() {
		return true;
	}

	@Override
	public void pick(Pick pick) {
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDropTarget(this);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDropTarget(this);
			break;
		case MOUSE_WHEEL:
			zoom((IMouseEvent) pick);
			break;
		case DRAG_DETECTED:
			pick.setDoDragging(true);
			this.select = new SelectLayer(bands.toRelative(pick.getPickedPoint()));
			content.add(this.select);
			break;
		case DRAGGED:
			if (pick.isDoDragging() && this.select != null) {
				this.select.dragTo(pick.getDx(), pick.getDy(), ((IMouseEvent) pick).isCtrlDown());
			}
			break;
		case MOUSE_RELEASED:
			if (pick.isDoDragging() && this.select != null) {
				this.select.dragTo(pick.getDx(), pick.getDy(), ((IMouseEvent) pick).isCtrlDown());
				content.remove(this.select);
				this.select = null;
			}
			break;
		default:
			break;
		}
	}

	/**
	 * @param pick
	 */
	private void zoom(IMouseEvent event) {
		if (event.getWheelRotation() == 0)
			return;
		blocks.zoom(event);

		bands.relayout();
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		return item.getInfo() instanceof ADragInfo || Nodes.canExtract(item);
	}

	@Override
	public void onDrop(IDnDItem item) {
		IDragInfo info = item.getInfo();
		if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			dropNode(item, g.getGroup().toNode());
		} else if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			dropNode(item, item.getType() == EDnDType.COPY ? new Node(g.getNode()) : g.getNode());
		} else if (info instanceof MultiNodeGroupDragInfo) {
			MultiNodeGroupDragInfo g = (MultiNodeGroupDragInfo) info;
			Node start = g.getPrimary().toNode();
			Block b = dropNode(item, start);
			rebuild(b, start, g.getPrimary(), g.getGroups(), null);
		} else if (info instanceof BlockDragInfo) {
			dropBlocks(item, ((BlockDragInfo) info).getStart(), ((BlockDragInfo) info).getBlocks());
		} else {
			Node node = Nodes.extract(item);
			dropNode(item, node);
		}
	}

	private void rebuild(Block b, Node asNode, NodeGroup act, Set<NodeGroup> items, EDirection commingFrom) {
		items.remove(act);
		for (EDirection dir : EDirection.values()) {
			if (dir == commingFrom)
				continue;
			NodeGroup next = act.findNeigbhor(dir, items);
			if (next == null)
				continue;
			Node nextNode = next.toNode();
			b.addNode(asNode, dir, nextNode, false);
			rebuild(b, nextNode, next, items, dir);
		}
	}

	private Block dropNode(IDnDItem item, Node node) {
		removeNode(node);
		Block b = new Block(node);
		setBlockDropPosition(item, b);
		blocks.add(b);

		removePlaceholder();
		bands.relayout();
		content.getParent().relayout();
		return b;
	}

	/**
	 * @param item
	 * @param block
	 */
	private void dropBlocks(IDnDItem item, Block start, Set<Block> blocks) {
		if (blocks.size() == 1)
			setBlockDropPosition(item, start);
		else {
			Vec2f old = start.getLocation();
			setBlockDropPosition(item, start);
			Vec2f change = old.minus(start.getLocation());
			for (Block b : blocks) {
				if (b == start)
					continue;
				Vec2f loc = b.getLocation();
				b.setLocation(loc.x() - change.x(), loc.y() - change.y());
			}
		}
		bands.relayout();
		content.getParent().relayout();
	}

	private void setBlockDropPosition(IDnDItem item, Block b) {
		Vec2f pos = blocks.toRelative(item.getMousePos());
		if (currentlyDraggedVis != null)
			pos.add(currentlyDraggedVis.getLocation());
		b.setLocation(pos.x(), pos.y());
	}

	/**
	 * @param node
	 */
	public void removeNode(Node node) {
		Block block = getBlock(node);
		if (block != null && block.removeNode(node)) {
			blocks.remove(block);
			bands.relayout();
		}
		cleanup(node);
	}

	public void cleanup(Node node) {
		selections.cleanup(node);
	}

	/**
	 * @param node
	 */
	private Block getBlock(Node node) {
		for (Block block : blocks.getBlocks())
			if (block.containsNode(node))
				return block;
		return null;
	}

	public void addPlaceholdersFor(Node node) {
		if (placeholders != null)
			return;

		placeholders = new GLElementContainer(new ToRelativeLayout());
		content.add(placeholders);

		final List<GLElement> l = placeholders.asList();
		for (Block block : blocks.getBlocks()) {
			l.addAll(block.addPlaceholdersFor(node));
		}
	}

	@Override
	public void onItemChanged(IDnDItem item) {
		if (placeholders == null) {
			if (item.getInfo() instanceof NodeDragInfo)
				addPlaceholdersFor(((NodeDragInfo) item.getInfo()).getNode());
			else if (item.getInfo() instanceof NodeGroupDragInfo)
				addPlaceholdersFor(((NodeGroupDragInfo) item.getInfo()).getNode());
			else {
				Node node = Nodes.extract(item);
				if (node != null)
					addPlaceholdersFor(node);
			}
		}

		// update drag to grid
		if (currentlyDraggedVis != null) {
			blocks.snapDraggedVis(currentlyDraggedVis);
		}
	}

	@Override
	public EDnDType defaultSWTDnDType(IDnDItem item) {
		if (item.getInfo() instanceof NodeGroupDragInfo)
			return EDnDType.COPY;
		return EDnDType.MOVE;
	}

	/**
	 * @param neighbor
	 * @param dir
	 * @param node
	 * @param transpose
	 * @param detached
	 */
	public void placeAt(Node neighbor, EDirection dir, Node node, boolean detached) {
		removeNode(node);
		Block block = getBlock(neighbor);
		block.addNode(neighbor, dir, node, detached);
	}

	private void removePlaceholder() {
		content.remove(placeholders);
		placeholders = null;
	}

	/**
	 * @return the selections, see {@link #selections}
	 */
	public NodeSelections getSelections() {
		return selections;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);

		if (showMiniMap) {
			g.incZ();
			g.save().move(w * 0.8f - 10, h * 0.8f - 10);
			renderMiniMap(g, w * 0.2f, h * 0.2f);

			g.restore();
			g.decZ();
		}
	}

	private void renderMiniMap(GLGraphics g, float w, float h) {
		g.color(Color.WHITE).fillRect(0, 0, w, h);
		g.color(Color.BLACK).drawRect(0, 0, w, h);
		Vec2f size = getSize();
		computeMiniMapFactor(g, size, w, h);

		blocks.renderMiniMap(g);
		bands.renderMiniMap(g);

		final ScrollingDecorator s = (ScrollingDecorator) content.getParent();
		if (s != null) {
			Rect clip = s.getClipingArea();
			g.color(Color.BLACK).drawRect(clip.x(), clip.y(), clip.width(), clip.height());
		}
	}

	private void computeMiniMapFactor(GLGraphics g, Vec2f size, float w, float h) {
		float wi = w / size.x();
		float hi = h / size.y();
		if (wi < hi) {
			g.move(0, (h - size.y() * wi) * 0.5f).gl.glScalef(wi, wi, 1);
		} else {
			g.move((w - size.x() * hi) * 0.5f, 0).gl.glScalef(hi, hi, 1);
		}
	}

	/**
	 *
	 */
	public void updateBands() {
		bands.relayout();
	}

	/**
	 * @param rect
	 * @param clear
	 */
	public void selectByBounds(Rect rect, boolean clear) {
		if (clear)
			selections.clear(SelectionType.SELECTION, null);

		Rectangle2D r = rect.asRectangle2D();
		for (Block block : blocks.getBlocks()) {
			if (block.getRectangleBounds().intersects(r)) {
				block.selectByBounds(r, selections);
			}
		}
	}

	public void toggleShowMiniMap() {
		this.showMiniMap = !showMiniMap;
		repaint();
	}

	/**
	 * @param dragElement
	 */
	public void setCurrentlyDragged(DragElement dragElement) {
		currentlyDraggedVis = dragElement;
	}

	/**
	 * @return the currentlyDraggedVis, see {@link #currentlyDraggedVis}
	 */
	public DragElement getCurrentlyDraggedVis() {
		return currentlyDraggedVis;
	}

	/**
	 * @return
	 */
	public List<Block> getBlocks() {
		return ImmutableList.copyOf(blocks.getBlocks());
	}

	/**
	 * @param b
	 */
	public void setContentPickable(boolean pickable) {
		blocks.setContentPickable(pickable);
	}

	/**
	 * @param block
	 */
	public void removeBlock(Block block) {
		blocks.remove(block);
		selections.cleanup(block);
		updateBands();
	}

	/**
	 * @param event
	 * @param nodeGroup
	 */
	public IDragInfo startSWTDrag(IDragEvent event, NodeGroup nodeGroup) {
		Set<NodeGroup> selected = selections.getSelection(SelectionType.SELECTION);
		selected = new HashSet<>(selected);
		selected.add(nodeGroup);
		Node single = NodeSelections.getSingleNode(selected);
		if (single != null) {
			EventPublisher.trigger(new HideNodeEvent().to(single));
			return new NodeDragInfo(event.getMousePos(), single);
		}
		Set<Block> blocks = NodeSelections.getFullBlocks(selected);
		if (!blocks.isEmpty()) {
			for (Block block : blocks)
				EventPublisher.trigger(new HideNodeEvent().to(block));
			return new BlockDragInfo(event.getMousePos(), blocks, nodeGroup.getNode().getBlock());
		}
		Set<NodeGroup> s = NodeSelections.compress(nodeGroup, selected);
		if (s.size() <= 1)
			return new NodeGroupDragInfo(event.getMousePos(), nodeGroup);
		return new MultiNodeGroupDragInfo(event.getMousePos(), nodeGroup, s);
	}

	/**
	 * @param leftOf
	 */
	private void moveSelection(EDirection dir, float factor) {
		Set<NodeGroup> selected = selections.getSelection(SelectionType.SELECTION);
		if (selected.isEmpty())
			return;
		Set<Block> blocks = NodeSelections.getFullBlocks(selected);
		if (blocks.isEmpty())
			return;
		Vec2f change;
		switch (dir) {
		case NORTH:
			change = new Vec2f(0, -20);
			break;
		case SOUTH:
			change = new Vec2f(0, 20);
			break;
		case WEST:
			change = new Vec2f(-20, 0);
			break;
		case EAST:
			change = new Vec2f(+20, 0);
			break;
		default:
			throw new IllegalStateException();
		}
		change.scale(factor);
		for (Block block : blocks) {
			Vec2f l = block.getLocation().plus(change);
			block.setLocation(Math.max(l.x(), 0), Math.max(l.y(), 0));
		}

		bands.relayout();
		content.getParent().relayout();
	}

	@Override
	public void keyPressed(IKeyEvent e) {
		float f = e.isAltDown() ? 2 : e.isControlDown() ? 0.5f : 1.f;
		if (e.isKey(ESpecialKey.LEFT))
			moveSelection(EDirection.WEST, f);
		else if (e.isKey(ESpecialKey.RIGHT))
			moveSelection(EDirection.EAST, f);
		else if (e.isKey(ESpecialKey.UP))
			moveSelection(EDirection.NORTH, f);
		else if (e.isKey(ESpecialKey.DOWN))
			moveSelection(EDirection.SOUTH, f);
	}

	@Override
	public void keyReleased(IKeyEvent e) {

	}

	/**
	 * @param neighbor
	 */
	public void removePreview(Node node) {
		removeNode(node);
		removePlaceholder();
		bands.relayout();
	}

	/**
	 * @param dir
	 * @param preview
	 */
	public void persistPreview(EDirection dir, Node preview) {
		preview.setOffset(dir, 0);
		removePlaceholder();
		bands.relayout();
	}

	public void addPreview(Node neighbor, EDirection dir, Node preview, boolean detached, float offset) {
		preview.setOffset(dir.opposite(), offset);
		placeAt(neighbor, dir, preview, detached);
	}

}
