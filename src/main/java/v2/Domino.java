/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package v2;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.view.opengl.canvas.IGLMouseListener.IMouseEvent;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLSandBox;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator;
import org.caleydo.core.view.opengl.layout2.dnd.EDnDType;
import org.caleydo.core.view.opengl.layout2.dnd.IDnDItem;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.core.view.opengl.layout2.dnd.IDropGLTarget;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.mock.MockDataDomain;
import org.caleydo.view.domino.api.model.graph.EDirection;
import org.caleydo.view.domino.internal.dnd.DragElement;
import org.caleydo.view.domino.internal.dnd.TablePerspectiveRemoveDragCreator;

import v2.data.Categorical2DDataDomainValues;
import v2.data.StratificationDataValue;
import v2.toolbar.LeftToolBar;
import v2.toolbar.ToolBar;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;

/**
 * @author Samuel Gratzl
 *
 */
public class Domino extends GLElementContainer implements IDropGLTarget, IPickingListener, IGLLayout2 {
	private GLElementContainer placeholders;
	private final Bands bands;
	private final Blocks blocks;
	private final ToolBar toolBar;
	private final LeftToolBar leftToolBar;
	private final SetMultimap<SelectionType, NodeGroup> selections = HashMultimap.create();
	private final GLElementContainer content;
	private SelectLayer select;
	private DragElement currentlyDraggedVis;

	/**
	 *
	 */
	public Domino() {
		setLayout(this);

		this.toolBar = new ToolBar();
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
		blocks.setzDelta(0.1f);
		content.add(blocks);

		this.bands = new Bands();
		this.bands.setVisibility(EVisibility.PICKABLE);
		this.bands.setzDelta(0.01f);
		this.bands.onPick(this);
		content.add(this.bands);
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

	private void fakeData() {
		MockDataDomain d = MockDataDomain.createCategorical(200, 200, MockDataDomain.RANDOM, "a", "b");
		Node node = new Node(new Categorical2DDataDomainValues(d.getDefaultTablePerspective()));
		blocks.add(new Block(node));

		MockDataDomain d2 = MockDataDomain.createCategorical(200, 200, MockDataDomain.RANDOM, "c", "d", "e");
		node = new Node(new Categorical2DDataDomainValues(d2.getDefaultTablePerspective()));
		blocks.add(new Block(node).setLocation(250, 250));

		TablePerspective grouping = MockDataDomain.addRecGrouping(d2, 100, 50);
		node = new Node(new StratificationDataValue(grouping.getRecordPerspective(), EDimension.RECORD, null));
		blocks.add(new Block(node).setLocation(20, 250));
	}

	@Override
	protected void init(IGLElementContext context) {
		context.getMouseLayer().addRemoteDragInfoUICreator(new TablePerspectiveRemoveDragCreator(this));
		super.init(context);
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
				this.select.dragTo(pick.getDx(), pick.getDy(), ((IMouseEvent)pick).isCtrlDown());
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
		Vec2f pos = blocks.toRelative(item.getMousePos());
		if (currentlyDraggedVis != null)
			pos.add(currentlyDraggedVis.getLocation());
		b.setLocation(pos.x(), pos.y());
		blocks.add(b);
		removePlaceholder();
		bands.relayout();
		content.getParent().relayout();
		return b;
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
		cleanupNode(node);
	}

	public void cleanupNode(Node node) {
		for (SelectionType type : selections.keySet()) {
			Set<NodeGroup> c = selections.get(type);
			boolean changed = false;
			for (Iterator<NodeGroup> it = c.iterator(); it.hasNext();) {
				NodeGroup g = it.next();
				if (g.getNode() == node) {
					it.remove();
					changed = true;
				}
			}
			if (changed)
				toolBar.update(type);
		}
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
			if (item.getInfo() instanceof ADragInfo)
				addPlaceholdersFor(((ADragInfo) item.getInfo()).getBaseNode());
			else {
				Node node = Nodes.extract(item);
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
		return EDnDType.MOVE;
	}

	/**
	 * @param neighbor
	 * @param dir
	 * @param node
	 * @param transpose
	 * @param detached
	 */
	public void placeAt(Node neighbor, EDirection dir, Node node, boolean transpose, boolean detached) {
		removeNode(node);
		Block block = getBlock(neighbor);
		if (transpose) {
			node.transposeMe();
		}
		block.addNode(neighbor, dir, node, detached);
		removePlaceholder();
		bands.relayout();
	}

	private void removePlaceholder() {
		content.remove(placeholders);
		placeholders = null;
	}



	public boolean isSelected(SelectionType type, NodeGroup group) {
		return selections.get(type).contains(group);
	}

	public void select(SelectionType type, NodeGroup group, boolean additional) {
		Set<NodeGroup> c = selections.get(type);
		if (!additional)
			c.clear();
		c.add(group);
		toolBar.update(type);
	}

	public Set<NodeGroup> getSelection(SelectionType type) {
		return selections.get(type);
	}

	public void clear(SelectionType type, NodeGroup group) {
		Set<NodeGroup> c = selections.get(type);
		boolean changed = !c.isEmpty();
		if (group != null)
			changed = c.remove(group);
		else
			c.clear();
		if (changed)
			toolBar.update(type);
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
			clear(SelectionType.SELECTION, null);

		Rectangle2D r = rect.asRectangle2D();
		for (Block block : blocks.getBlocks()) {
			if (block.getRectangleBounds().intersects(r)) {
				block.selectByBounds(r);
			}
		}
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
}
