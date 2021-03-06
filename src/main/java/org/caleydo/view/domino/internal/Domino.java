/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionCommands;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.DeepScan;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.view.opengl.canvas.IGLKeyListener;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragGLSource.IDragEvent;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.view.domino.api.model.EDirection;
import org.caleydo.view.domino.internal.dnd.ADragInfo;
import org.caleydo.view.domino.internal.dnd.BlockDragInfo;
import org.caleydo.view.domino.internal.dnd.DragElement;
import org.caleydo.view.domino.internal.dnd.ItemDragInfo;
import org.caleydo.view.domino.internal.dnd.MultiNodeGroupDragInfo;
import org.caleydo.view.domino.internal.dnd.NodeDragInfo;
import org.caleydo.view.domino.internal.dnd.NodeGroupDragInfo;
import org.caleydo.view.domino.internal.dnd.RulerDragInfo;
import org.caleydo.view.domino.internal.dnd.TablePerspectiveRemoveDragCreator;
import org.caleydo.view.domino.internal.event.HideNodeEvent;
import org.caleydo.view.domino.internal.toolbar.DynamicToolBar;
import org.caleydo.view.domino.internal.toolbar.LeftToolBar;
import org.caleydo.view.domino.internal.toolbar.ToolBar;
import org.caleydo.view.domino.internal.ui.AItem;
import org.caleydo.view.domino.internal.ui.Ruler;
import org.caleydo.view.domino.internal.ui.SelectionInfo;
import org.caleydo.view.domino.internal.undo.AddItemCmd;
import org.caleydo.view.domino.internal.undo.AddLazyBlockCmd;
import org.caleydo.view.domino.internal.undo.AddLazyMultiBlockCmd;
import org.caleydo.view.domino.internal.undo.AddRulerCmd;
import org.caleydo.view.domino.internal.undo.CmdComposite;
import org.caleydo.view.domino.internal.undo.MoveBlockCmd;
import org.caleydo.view.domino.internal.undo.MoveItemCmd;
import org.caleydo.view.domino.internal.undo.MoveRulerCmd;
import org.caleydo.view.domino.internal.undo.RemoveNodeCmd;
import org.caleydo.view.domino.internal.undo.RemoveNodeGroupCmd;
import org.caleydo.view.domino.internal.undo.ZoomCmd;
import org.eclipse.swt.SWT;

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
	private final MiniMapCanvas content;
	private SelectLayer select;
	private DragElement currentlyDraggedVis;
	private boolean showDebugInfos = false;
	private boolean showBlockLabels = true;
	private boolean showGroupLabels = true;

	private EToolState tool = EToolState.MOVE;

	private final UndoStack undo = new UndoStack(this);

	@DeepScan
	private final NodeSelections selections = new NodeSelections();

	/**
	 *
	 */
	public Domino() {
		setLayout(this);

		this.toolBar = new ToolBar(undo, selections);
		this.toolBar.setSize(-1, 24);
		this.add(toolBar);

		this.leftToolBar = new LeftToolBar(undo);
		this.leftToolBar.setSize(24, -1);
		this.add(leftToolBar);

		this.content = new MiniMapCanvas();
		content.setVisibility(EVisibility.PICKABLE);
		content.onPick(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				if (pick.getPickingMode() == PickingMode.MOUSE_OUT)
					removePlaceholder();
			}
		});
		this.add(content);

		this.blocks = new Blocks(selections);
		content.add(blocks);
		blocks.setzDelta(0.1f);

		this.bands = new Bands(selections);
		this.bands.setzDelta(0.01f);
		this.bands.setVisibility(EVisibility.PICKABLE);
		this.bands.setPicker(GLRenderers.RECT);
		this.bands.onPick(this);
		content.add(this.bands);

		selections.onNodeGroupSelectionChanges(new ICallback<SelectionType>() {
			@Override
			public void on(SelectionType data) {
				NodeDataItem.update(selections.getSelection(SelectionType.MOUSE_OVER),
						selections.getSelection(SelectionType.SELECTION));
			}
		});

		DynamicToolBar dynToolBar = new DynamicToolBar(undo, selections);
		content.add(dynToolBar);
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
	 * @return the bands, see {@link #bands}
	 */
	public Bands getBands() {
		return bands;
	}

	/**
	 * @return the showDebugInfos, see {@link #showDebugInfos}
	 */
	public boolean isShowDebugInfos() {
		return showDebugInfos;
	}

	/**
	 * @return
	 */
	public boolean isShowBlockLabels() {
		return showBlockLabels;
	}

	public boolean isShowGroupLabels() {
		return showGroupLabels;
	}

	/**
	 * @param showBlockLabels
	 *            setter, see {@link showBlockLabels}
	 */
	public void setShowBlockLabels(boolean showBlockLabels) {
		this.showBlockLabels = showBlockLabels;
	}

	/**
	 * @param showBlockLabels
	 *            setter, see {@link showBlockLabels}
	 */
	public void setShowGroupLabels(boolean showGroupLabels) {
		this.showGroupLabels = showGroupLabels;
	}

	/**
	 * @param showDebugInfos
	 *            setter, see {@link showDebugInfos}
	 */
	public void setShowDebugInfos(boolean showDebugInfos) {
		this.showDebugInfos = showDebugInfos;
	}

	@Override
	protected boolean hasPickAbles() {
		return true;
	}

	@Override
	public void pick(Pick pick) {
		IMouseEvent event = ((IMouseEvent) pick);
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			context.getMouseLayer().addDropTarget(this);
			break;
		case MOUSE_OUT:
			context.getMouseLayer().removeDropTarget(this);
			content.stopAutoShift();
			break;
		case MOUSE_WHEEL:
			if (event.getWheelRotation() != 0)
				undo.push(new ZoomCmd(ScaleLogic.shiftLogic(event, new Vec2f(100, 100)), blocks.toRelative(pick
						.getPickedPoint())));
			break;
		case DRAG_DETECTED:
			pick.setDoDragging(true);
			if (isPanning(event)) {
				context.getSWTLayer().setCursor(SWT.CURSOR_HAND);
			} else {
				if (this.select != null)
					content.remove(this.select);
				this.select = new SelectLayer(blocks.toRelative(pick.getPickedPoint()));
				content.add(this.select);
			}
			break;
		case DRAGGED:
			if (pick.isDoDragging() || !pick.isAnyDragging()) {
				if (!pick.isDoDragging()) {
					pick.setDoDragging(true);
					if (isPanning(event))
						context.getSWTLayer().setCursor(SWT.CURSOR_HAND);
				}
				if (pick.isDoDragging() && this.select != null) {
					this.select.dragTo(pick.getDx(), pick.getDy(), event.isCtrlDown());
				} else if (isPanning(event)) {
					content.shiftViewport(-pick.getDx(), -pick.getDy());
				}
			}
			break;
		case CLICKED:
			if (!event.isCtrlDown() && !event.isAltDown() && !event.isShiftDown()) {
				// clear selection
				SelectionCommands.clearSelections();
			}
			break;
		case MOUSE_RELEASED:
			if (pick.isDoDragging() && this.select != null) {
				this.select.dragTo(pick.getDx(), pick.getDy(), event.isCtrlDown());
				content.remove(this.select);
				this.select = null;
			}
			context.getSWTLayer().setCursor(-1);
			break;
		default:
			break;
		}
	}

	private static boolean isPanning(IMouseEvent event) {
		return event.isAltDown() || event.isButtonDown(2);
	}

	public void zoom(Vec2f shift, Vec2f mousePos) {
		blocks.zoom(shift, mousePos);
		bands.relayout();
	}

	public void zoom(IDCategory category, float scale) {
		blocks.zoomRuler(category, scale);
		bands.relayout();
	}

	@Override
	public boolean canSWTDrop(IDnDItem item) {
		return item.getInfo() instanceof ADragInfo || Nodes.canExtract(item);
	}

	@Override
	public void onDrop(IDnDItem item) {
		IDragInfo info = item.getInfo();
		final Vec2f pos = toDropPosition(item);
		if (info instanceof RulerDragInfo) {
			dropRuler(pos, ((RulerDragInfo) info).getRuler());
		} else if (info instanceof ItemDragInfo) {
			dropItem(pos, ((ItemDragInfo) info).getItem());
		} else if (info instanceof NodeGroupDragInfo) {
			NodeGroupDragInfo g = (NodeGroupDragInfo) info;
			dropNode(pos, g.getGroup().toNode(), item.getType() == EDnDType.MOVE ? g.getGroup() : null);
		} else if (info instanceof NodeDragInfo) {
			NodeDragInfo g = (NodeDragInfo) info;
			dropNode(pos, item.getType() == EDnDType.COPY ? new Node(g.getNode()) : g.getNode(), null);
		} else if (info instanceof MultiNodeGroupDragInfo) {
			MultiNodeGroupDragInfo g = (MultiNodeGroupDragInfo) info;
			Node start = g.getPrimary().toNode();
			undo.push(new AddLazyMultiBlockCmd(start, pos, g.getPrimary(), g.getGroups()));
		} else if (info instanceof BlockDragInfo) {
			final Block start = ((BlockDragInfo) info).getStart();
			undo.push(new MoveBlockCmd(((BlockDragInfo) info).getBlocks(), pos.minus(start.getLocation())));
		} else {
			Node node = Nodes.extract(item);
			dropNode(pos, node, null);
		}
		content.stopAutoShift();
	}

	@Override
	public void onDropLeave() {

	}

	/**
	 * @param pos
	 * @param ruler
	 */
	private void dropRuler(Vec2f pos, Ruler ruler) {
		Vec2f shift = pos.minus(ruler.getLocation());
		if (!blocks.hasRuler(ruler.getIDCategory())) {
			ruler.setLocation(pos.x(), pos.y());
			undo.push(new AddRulerCmd(ruler));
		} else
			undo.push(new MoveRulerCmd(ruler.getIDCategory(), shift));
	}

	private void dropItem(Vec2f pos, AItem item) {
		Vec2f shift = pos.minus(item.getLocation());
		if (!blocks.hasItem(item)) {
			item.setLocation(pos.x(), pos.y());
			undo.push(new AddItemCmd(item));
		} else
			undo.push(new MoveItemCmd(item, shift));
	}

	public void moveRuler(IDCategory category, Vec2f shift) {
		blocks.moveRuler(category, shift);
		bands.relayout();
	}

	private Block dropNode(Vec2f pos, Node node, NodeGroup groupToRemove) {
		Block block = node.getBlock();
		if (block != null && block.nodeCount() == 1) {
			Vec2f shift = pos.minus(block.getLocation());
			undo.push(new MoveBlockCmd(Collections.singleton(block), shift));
			return block;
		}
		final AddLazyBlockCmd cmd = new AddLazyBlockCmd(node, pos);
		if (block != null)
			undo.push(CmdComposite.chain(new RemoveNodeCmd(node), cmd));
		else if (groupToRemove != null)
			undo.push(CmdComposite.chain(cmd, new RemoveNodeGroupCmd(groupToRemove)));
		else {
			undo.push(cmd);
		}
		removePlaceholder();
		bands.relayout();
		content.getParent().relayout();
		return null; // FIXME
	}

	/**
	 * @return the undo, see {@link #undo}
	 */
	public UndoStack getUndo() {
		return undo;
	}

	public void moveBlocks(Set<Block> blocks, Vec2f shift) {
		for (Block b : blocks) {
			Vec2f loc = b.getLocation();
			b.setLocation(loc.x() + shift.x(), loc.y() + shift.y());
		}
		bands.relayout();
		content.relayout();
	}

	private Vec2f toDropPosition(IDnDItem item) {
		Vec2f pos = blocks.toRelative(item.getMousePos());
		if (currentlyDraggedVis != null)
			pos.add(currentlyDraggedVis.getLocation());
		return pos;
	}

	public boolean containsNode(Node node) {
		return node.getBlock() != null;
	}
	/**
	 * @param node
	 */
	public void removeNode(Node node) {
		Block block = node.getBlock();
		if (block != null && block.removeNode(node)) {
			blocks.remove(block);
			bands.relayout();
		}
		cleanup(node);
	}

	public void cleanup(Node node) {
		selections.cleanup(node);
	}

	public void addPlaceholdersFor(Node node) {
		if (placeholders != null)
			return;
		if (tool == EToolState.BANDS)
			return;

		placeholders = new GLElementContainer(new ToRelativeLayout());
		content.add(placeholders);

		final List<GLElement> l = placeholders.asList();
		for (Block block : blocks.getBlocks()) {
			l.addAll(block.addPlaceholdersFor(node));
		}

		if (l.isEmpty()) {
			Vec2f size = getSize();
			Rect r = new Rect();
			r.x(50);
			r.y(size.y() * 0.25f);
			r.width(Block.DETACHED_OFFSET);
			r.height(size.y() * 0.5f);
			l.add(new FreePlaceholder(EDirection.EAST, r));
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

		content.autoShift(item.getMousePos());
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
	public void placeAt(Node neighbor, EDirection dir, Node node) {
		removeNode(node);
		Block block = neighbor.getBlock();
		block.addNode(neighbor, dir, node);
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
		final Rectangle2D r = rect.asRectangle2D();
		switch (this.tool) {
		case MOVE:
			selectNodesByBounds(clear, r);
			break;
		case SELECT:
			selectBandsByBounds(clear, r);
			break;
		case BANDS:
			selectNodesByBounds(clear, r);
			break;
		}

	}

	/**
	 * @param clear
	 * @param r
	 */
	private void selectBandsByBounds(boolean clear, Rectangle2D r) {
		bands.selectBandsByBounds(clear, r);
	}

	private void selectNodesByBounds(boolean clear, final Rectangle2D r) {
		if (clear) {
			if (tool == EToolState.BANDS)
				selections.clear(SelectionType.SELECTION, (Block) null);
			else
				selections.clear(SelectionType.SELECTION, (NodeGroup) null);
		}

		for (Block block : blocks.getBlocks()) {
			if (block.getRectangleBounds().intersects(r)) {
				block.selectByBounds(r, tool);
			}
		}
	}

	public void toggleShowMiniMap() {
		this.content.toggleShowMiniMap();
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

	public Blocks getOutlerBlocks() {
		return blocks;
	}

	/**
	 * @param tool
	 *            setter, see {@link tool}
	 */
	public void setTool(EToolState tool) {
		if (this.tool == tool)
			return;
		this.tool = tool;
		blocks.setTool(tool);
		bands.relayout();
		if (tool == EToolState.BANDS) {
			selections.clear(SelectionType.MOUSE_OVER, (NodeGroup) null);
			selections.clear(SelectionType.SELECTION, (NodeGroup) null);
		} else {
			selections.clear(SelectionType.MOUSE_OVER, (Block) null);
			selections.clear(SelectionType.SELECTION, (Block) null);
		}
	}

	/**
	 * @return the tool, see {@link #tool}
	 */
	public EToolState getTool() {
		return tool;
	}

	/**
	 * @param block
	 */
	public void removeBlock(Block block) {
		blocks.remove(block);
		selections.cleanup(block);
		updateBands();
	}

	public void addBlock(Block block) {
		block.setTool(tool);
		blocks.addBlock(block);
		updateBands();
	}

	public IDragInfo startSWTDrag(IDragEvent event, Block block) {
		Set<Block> selected = selections.getBlockSelection(SelectionType.SELECTION);
		selected = new HashSet<>(selected);
		selected.add(block);
		for (Block b : selected)
			EventPublisher.trigger(new HideNodeEvent().to(b));
		return new BlockDragInfo(event.getMousePos(), selected, block);
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
		undo.push(new MoveBlockCmd(blocks, change));
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
		else if (e.isControlDown() && (e.isKey('a') || e.isKey('A')))
			selectAll();
		this.leftToolBar.keyPressed(e);
		this.toolBar.keyPressed(e);
	}

	/**
	 *
	 */
	private void selectAll() {
		for (Block b : blocks.getBlocks())
			b.selectAll();
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
	public void persistPreview(Node preview) {
		preview.setPreviewing(false);
		removePlaceholder();
		bands.relayout();
	}

	public void addPreview(Node neighbor, EDirection dir, Node preview) {
		preview.setPreviewing(true);
		placeAt(neighbor, dir, preview);
	}

	/**
	 * @param ruler
	 */
	public void addRuler(Ruler ruler) {
		blocks.addRuler(ruler);
		leftToolBar.onShowHideRuler(ruler.getIDCategory(), true);
	}

	/**
	 * @param ruler
	 */
	public void removeRuler(Ruler ruler) {
		blocks.removeRuler(ruler);
		leftToolBar.onShowHideRuler(ruler.getIDCategory(), false);
	}

	/**
	 * @param idCategory
	 * @return
	 */
	public int getVisibleItemCount(IDCategory category) {
		return blocks.getVisibleItemCount(category);
	}

	/**
	 * @param block
	 * @param dim
	 * @return
	 */
	public List<Block> explode(Block block, EDimension dim) {
		List<Block> r = blocks.explode(block, dim);
		bands.relayout();
		return r;
	}

	/**
	 * @param blocks2
	 * @param dim
	 * @return
	 */
	public Block combine(List<Block> blocks, EDimension dim) {
		Block r = this.blocks.combine(blocks, dim);
		bands.relayout();
		return r;
	}

	/**
	 * @param idCategory
	 */
	public void scrollRulerIntoView(IDCategory category) {
		Ruler r = this.blocks.getRuler(category);
		if (r == null)
			return;
		this.content.scrollInfoView(r.getRectBounds());
	}

	/**
	 * @param item
	 */
	public void addItem(AItem item) {
		this.blocks.addItem(item);
		if (item instanceof SelectionInfo) {
			leftToolBar.onShowHideSelectionInfo(((SelectionInfo) item).getIDCategory(), true);
			// update scaling
			float f = ((SelectionInfo) item).getScaleFactor();
			for (SelectionInfo info : this.blocks.selectionInfos()) {
				float f2 = info.getScaleFactor();
				if (f2 < f) {
					f = f2;
				}
			}
			for (SelectionInfo info : this.blocks.selectionInfos()) {
				info.setScaleFactor(f);
			}
		}
	}

	/**
	 * @param item
	 */
	public void removeItem(AItem item) {
		this.blocks.removeItem(item);
		if (item instanceof SelectionInfo) {
			leftToolBar.onShowHideSelectionInfo(((SelectionInfo) item).getIDCategory(), false);
		}
	}
}
