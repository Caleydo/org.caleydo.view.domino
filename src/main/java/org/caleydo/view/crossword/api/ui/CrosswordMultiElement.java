/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.crossword.api.ui;

import static org.caleydo.core.data.collection.EDimension.DIMENSION;
import static org.caleydo.core.data.collection.EDimension.RECORD;
import static org.caleydo.view.crossword.api.ui.layout.EEdgeType.PARENT_CHILD;
import static org.caleydo.view.crossword.api.ui.layout.EEdgeType.SHARED;
import static org.caleydo.view.crossword.api.ui.layout.EEdgeType.SIBLING;
import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAccessor;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IGLElementParent;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.crossword.api.model.CenterRadius;
import org.caleydo.view.crossword.api.model.ConnectorStrategies;
import org.caleydo.view.crossword.api.model.PerspectiveMetaData;
import org.caleydo.view.crossword.api.model.TablePerspectiveMetaData;
import org.caleydo.view.crossword.api.model.TypedSet;
import org.caleydo.view.crossword.api.ui.layout.EEdgeType;
import org.caleydo.view.crossword.api.ui.layout.IGraphEdge;
import org.caleydo.view.crossword.api.ui.layout.IGraphVertex;
import org.caleydo.view.crossword.api.ui.layout.IVertexConnector;
import org.caleydo.view.crossword.internal.CrosswordView;
import org.caleydo.view.crossword.internal.ui.CrosswordBandLayer;
import org.caleydo.view.crossword.internal.ui.CrosswordElement;
import org.caleydo.view.crossword.internal.ui.CrosswordLayoutInfo;
import org.caleydo.view.crossword.internal.ui.layout.DefaultGraphLayout;
import org.caleydo.view.crossword.spi.config.ElementConfig;
import org.caleydo.view.crossword.spi.config.MultiConfig;
import org.caleydo.view.crossword.spi.model.IBandRenderer;
import org.caleydo.view.crossword.spi.model.IConnectorStrategy;
import org.caleydo.view.crossword.spi.ui.layout.IGraphLayout;
import org.caleydo.view.crossword.spi.ui.layout.IGraphLayout.GraphLayoutModel;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * crossword root element
 *
 * @author Samuel Gratzl
 *
 */
public class CrosswordMultiElement extends GLElement implements IHasMinSize, IGLElementParent,
		Iterable<CrosswordElement> {

	/**
	 * data structure for managing the elements
	 */
	private final UndirectedGraph<GraphVertex, GraphEdge> graph = new Pseudograph<>(GraphEdge.class);
	/**
	 * dedicated element/layer for the bands for better caching behavior
	 */
	private final CrosswordBandLayer bands = new CrosswordBandLayer();

	private final MultiConfig config;

	private final IGraphLayout layout;

	/**
	 * whether the bars of all element should be shown all the time or just on demand (hover)
	 */
	private boolean alwaysShowHeader;

	/**
	 * result of the layout algorithm
	 */
	private GraphLayoutModel layoutInstance;

	/**
	 *
	 */
	public CrosswordMultiElement(MultiConfig config) {
		this(new DefaultGraphLayout(), config);
	}

	/**
	 * @param layout
	 */
	public CrosswordMultiElement(IGraphLayout layout, MultiConfig config) {
		this.layout = layout;
		this.config = config;
		GLElementAccessor.setParent(bands, this);
	}


	@Override
	public Vec2f getMinSize() {
		float x = 0;
		float y = 0;
		for (GLElement child : this) {
			Vec4f bounds = child.getBounds();
			x = Math.max(x, bounds.x() + bounds.z());
			y = Math.max(y, bounds.y() + bounds.w());
		}
		return new Vec2f(x, y);
	}

	/**
	 * @return the alwaysShowHeader, see {@link #alwaysShowHeader}
	 */
	public boolean isAlwaysShowHeader() {
		return alwaysShowHeader;
	}

	public void toggleAlwaysShowHeader() {
		this.alwaysShowHeader = !this.alwaysShowHeader;
		for (CrosswordElement elem : Iterables.filter(this, CrosswordElement.class))
			elem.relayout();
	}

	@Override
	public void layout(int deltaTimeMs) {
		super.layout(deltaTimeMs);
		bands.layout(deltaTimeMs);
		for (GraphVertex vertex : graph.vertexSet())
			vertex.asElement().layout(deltaTimeMs);
	}

	@Override
	public void relayout() {
		super.relayout();
		GLElementAccessor.relayoutDown(bands);
	}

	@Override
	protected final void layoutImpl(int deltaTimeMs) {
		super.layoutImpl(deltaTimeMs);
		Vec2f size = getSize();
		GLElementAccessor.asLayoutElement(bands).setBounds(0, 0, size.x(), size.y());

		this.layoutInstance = layout.doLayout(graph.vertexSet(), graph.edgeSet());
		relayoutParent(); // trigger update of the parent for min size changes
	}

	/**
	 * @return
	 */
	public List<? extends IBandRenderer> getBandRoutes() {
		return (layoutInstance.getRoutes());
	}

	@Override
	protected void takeDown() {
		GLElementAccessor.takeDown(bands);
		for (GraphVertex vertex : graph.vertexSet())
			GLElementAccessor.takeDown(vertex.asElement());
		super.takeDown();
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		GLElementAccessor.init(bands, context);
		for (GraphVertex vertex : graph.vertexSet())
			GLElementAccessor.init(vertex.asElement(), context);
	}

	private void setup(CrosswordElement child) {
		IGLElementParent ex = child.getParent();
		boolean doInit = ex == null;
		if (ex == this) {
			// internal move
			removeVertex(child);
		} else if (ex != null) {
			doInit = !ex.moved(child);
		}
		GLElementAccessor.setParent(child, this);
		if (doInit && context != null)
			GLElementAccessor.init(child, context);
	}

	/**
	 * @param child
	 */
	private boolean removeVertex(GLElement child) {
		GraphVertex vertex = toVertex(child);
		if (vertex == null)
			return false;
		return graph.removeVertex(vertex);
	}

	/**
	 * @param crosswordElement
	 */
	private void add(CrosswordElement child) {
		GraphVertex vertex = addImpl(child);
		createBands(vertex, ImmutableSet.of(child));
	}

	private GraphVertex addImpl(CrosswordElement child) {
		setup(child);
		final GraphVertex vertex = new GraphVertex(child);
		graph.addVertex(vertex);
		relayout();
		return vertex;
	}

	private void createBands(GraphVertex child, Set<CrosswordElement> ignores) {
		final TablePerspective tablePerspective = child.getTablePerspective();
		final IDType recIDType = tablePerspective.getRecordPerspective().getIdType();
		final IDType dimIDType = tablePerspective.getDimensionPerspective().getIdType();

		for (GraphVertex other : graph.vertexSet()) {
			if (ignores.contains(other.asElement()))
				continue;
			final TablePerspective tablePerspective2 = child.getTablePerspective();
			final IDType recIDType2 = tablePerspective2.getRecordPerspective().getIdType();
			final IDType dimIDType2 = tablePerspective2.getDimensionPerspective().getIdType();
			if (recIDType.resolvesTo(recIDType2))
				addEdge(child, other, SHARED, connect(RECORD), connect(RECORD));
			if (dimIDType.resolvesTo(recIDType2))
				addEdge(child, other, SHARED, connect(DIMENSION), connect(RECORD));
			if (recIDType.resolvesTo(dimIDType2))
				addEdge(child, other, SHARED, connect(RECORD), connect(DIMENSION));
			if (dimIDType.resolvesTo(dimIDType2))
				addEdge(child, other, SHARED, connect(DIMENSION), connect(DIMENSION));
		}
	}

	private static VertexConnector connect(EDimension type) {
		return connect(type, ConnectorStrategies.SHARED);
	}

	private static VertexConnector connect(EDimension type, IConnectorStrategy model) {
		return new VertexConnector(type, model);
	}

	private void addEdge(GraphVertex child, GraphVertex other, EEdgeType type, VertexConnector sourceConnector,
			VertexConnector targetConnector) {
		final GraphEdge edge = new GraphEdge(type, sourceConnector, targetConnector);
		graph.addEdge(child, other, edge);
		edge.update();
	}

	/**
	 * @param crosswordElement
	 * @param dimensionSubTablePerspectives
	 */
	private void split(CrosswordElement base, boolean inDim) {
		TablePerspective table = base.getTablePerspective();
		final EDimension type = EDimension.get(inDim);
		final GraphVertex baseVertex = toVertex(base);
		final GroupList groups = (inDim ? table.getDimensionPerspective() : table.getRecordPerspective())
				.getVirtualArray().getGroupList();
		assert groups.size() > 1;

		List<CrosswordElement> children = toElements(base, inDim, baseVertex, table);

		// combine the elements that should be ignored
		final ImmutableSet<CrosswordElement> ignore = ImmutableSet.<CrosswordElement> builder().addAll(children)
				.add(base)
				.build();

		int total = inDim ? table.getNrDimensions() : table.getNrRecords();

		List<GraphVertex> vertices = new ArrayList<>(ignore.size());

		for (int i = 0; i < children.size(); ++i) {
			CrosswordElement child = children.get(i);
			Group group = groups.get(i);
			GraphVertex vertex = addImpl(child);
			vertices.add(vertex);
			createBands(vertex, ignore);
			int startIndex = group.getStartIndex();
			float offset = startIndex / (float) total;
			addEdge(baseVertex, vertex, PARENT_CHILD, connect(type, ConnectorStrategies.createParent(offset)),
					connect(type));
			// add parent edge
			for (int j = 0; j < i; ++j) {
				GraphVertex child2 = vertices.get(j);
				addEdge(vertex, child2, SIBLING, connect(type.opposite()), connect(type.opposite())); // add sibling
																										// edge
			}
		}

		// update metadata flags
		TablePerspectiveMetaData metaData = base.getMetaData();
		(inDim ? metaData.getDimension() : metaData.getRecord()).setSplitted();
	}

	private List<CrosswordElement> toElements(CrosswordElement base, boolean inDim, final GraphVertex baseVertex,
			TablePerspective table) {
		final List<TablePerspective> datas = inDim ? table.getDimensionSubTablePerspectives() : table
				.getRecordSubTablePerspectives();

		List<CrosswordElement> children = new ArrayList<>(datas.size());

		final ElementConfig econfig = config.getSplittedElementConfig(baseVertex);

		TablePerspectiveMetaData metaData = new TablePerspectiveMetaData(
					inDim ? 0 : PerspectiveMetaData.FLAG_CHILD, inDim ? PerspectiveMetaData.FLAG_CHILD : 0);
		for (TablePerspective t : datas) {
			final CrosswordElement new_ = new CrosswordElement(t, metaData, econfig, base);
			children.add(new_);
		}
		return children;
	}

	/**
	 * @param crosswordElement
	 */
	public void splitDim(CrosswordElement child) {
		split(child, true);
	}

	public void splitRec(CrosswordElement child) {
		split(child, false);
	}

	@Override
	public boolean moved(GLElement child) {
		if (child instanceof CrosswordElement)
			removeVertex(child);
		relayout();
		return context != null;
	}

	private final Function<GraphVertex, CrosswordElement> toCrosswordElement = new Function<GraphVertex, CrosswordElement>() {
		@Override
		public CrosswordElement apply(GraphVertex input) {
			return input.asElement();
		}
	};

	@Override
	public Iterator<CrosswordElement> iterator() {
		return Iterators.transform(graph.vertexSet().iterator(), toCrosswordElement);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
		bands.render(g);
		g.incZ();
		for (GraphVertex vertex : graph.vertexSet())
			vertex.asElement().render(g);
		g.decZ();
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
		bands.renderPick(g);
		g.incZ();
		for (GraphVertex vertex : graph.vertexSet())
			vertex.asElement().renderPick(g);
		g.decZ();
	}

	@Override
	protected boolean hasPickAbles() {
		return super.hasPickAbles() || !graph.vertexSet().isEmpty();
	}

	/**
	 * @param r
	 */
	public void remove(GLElement child) {
		if (removeVertex(child)) {
			GLElementAccessor.takeDown(child);
			relayout();
		}
	}

	/**
	 * @param tablePerspective
	 */
	public void add(TablePerspective tablePerspective) {
		add(new CrosswordElement(tablePerspective, new TablePerspectiveMetaData(0, 0), config.getDefaultElementConfig()));
	}

	public void addAll(Iterable<TablePerspective> tablePerspectives) {
		for (TablePerspective tablePerspective : tablePerspectives)
			add(tablePerspective);
	}

	/**
	 * @param removed
	 */
	public void removeAll(Collection<TablePerspective> removed) {
		if (removed.isEmpty())
			return;
		List<CrosswordElement> toRemove = new ArrayList<>();
		for (CrosswordElement elem : Iterables.filter(this, CrosswordElement.class)) {
			if (removed.contains(elem.getTablePerspective())
					|| removed.contains(elem.getTablePerspective().getParentTablePerspective()))
				toRemove.add(elem);
		}
		for (CrosswordElement r : toRemove)
			remove(r);
	}

	public Iterable<GraphEdge> getBands() {
		return graph.edgeSet();
	}

	/**
	 * @param crosswordElement
	 */
	public void onConnectionsChanged(CrosswordElement child) {
		for (GraphEdge edge : graph.edgesOf(toVertex(child)))
			edge.update();
	}

	private GraphVertex toVertex(GLElement child) {
		for (GraphVertex vertex : graph.vertexSet())
			if (vertex.asElement() == child)
				return vertex;
		return null;
	}

	public void changePerspective(CrosswordElement child, boolean isDim, Perspective new_) {
		TablePerspective old = child.getTablePerspective();
		ATableBasedDataDomain dataDomain = old.getDataDomain();

		new_ = dataDomain.convertForeignPerspective(new_);
		Table table = dataDomain.getTable();

		Perspective record = old.getRecordPerspective();
		Perspective dimension = old.getDimensionPerspective();
		if (isDim)
			dimension = new_;
		else
			record = new_;
		TablePerspective newT;
		if (!table.containsDimensionPerspective(dimension.getPerspectiveID())
				|| !table.containsRecordPerspective(record.getPerspectiveID())) {
			newT = new TablePerspective(dataDomain, record, dimension);
			newT.setPrivate(true);
		} else
			newT = dataDomain.getTablePerspective(record.getPerspectiveID(), dimension.getPerspectiveID());
		child.setTablePerspective(newT);
		if (context instanceof CrosswordView) {
			((CrosswordView) context).replaceTablePerspectiveInternally(old, newT);
		}
	}

	private class GraphVertex implements IGraphVertex {
		private final CrosswordElement element;

		public GraphVertex(CrosswordElement element) {
			super();
			this.element = element;
		}

		public CrosswordElement asElement() {
			return element;
		}

		@Override
		public TypedSet getIDs(EDimension type) {
			return element.getIDs(type);
		}

		public TablePerspective getTablePerspective() {
			return element.getTablePerspective();
		}

		private IGLLayoutElement asLayoutElement() {
			return GLElementAccessor.asLayoutElement(element);
		}

		private CrosswordLayoutInfo asLayoutInfo() {
			return element.getLayoutDataAs(CrosswordLayoutInfo.class, null);
		}

		@Override
		public Vec2f getLocation() {
			return asLayoutElement().getLocation();
		}

		@Override
		public Rect getBounds() {
			return asLayoutElement().getRectBounds();
		}

		@Override
		public Vec2f getSize() {
			CrosswordLayoutInfo info = asLayoutInfo();
			return info.getSize();
		}

		@Override
		public void setBounds(Vec2f location, Vec2f size) {
			asLayoutElement().setBounds(location.x(), location.y(), size.x(), size.y());
		}

		@Override
		public void move(float x, float y) {
			Vec2f l = getLocation();
			asLayoutElement().setLocation(l.x() + x, l.y() + y);
		}

		@Override
		public Set<GraphEdge> getEdges() {
			return graph.edgesOf(this);
		}

		@Override
		public boolean hasEdge(EEdgeType type) {
			return Iterables.any(getEdges(), type);
		}
	}

	private static class VertexConnector implements IVertexConnector {
		private final EDimension type;
		private final IConnectorStrategy strategy;
		private CenterRadius values;

		public VertexConnector(EDimension type, IConnectorStrategy strategy) {
			this.strategy = strategy;
			this.type = type;
		}

		@Override
		public EDimension getDimension() {
			return type;
		}

		@Override
		public float getCenter() {
			return values.getCenter();
		}

		@Override
		public float getRadius() {
			return values.getRadius();
		}

		public void update(TypedSet ids, TypedSet intersection) {
			values = strategy.update(ids, intersection);
		}
	}

	private static class GraphEdge extends DefaultEdge implements IGraphEdge {
		private static final long serialVersionUID = -92295569780679555L;
		private final EEdgeType type;
		private TypedSet intersection;
		private final VertexConnector sourceConnector;
		private final VertexConnector targetConnector;

		public GraphEdge(EEdgeType type, VertexConnector sourceConnector, VertexConnector targetConnector) {
			this.type = type;
			this.sourceConnector = sourceConnector;
			this.targetConnector = targetConnector;

		}
		@Override
		public GraphVertex getSource() {
			return (GraphVertex) super.getSource();
		}

		@Override
		public GraphVertex getTarget() {
			return (GraphVertex) super.getTarget();
		}

		@Override
		public TypedSet getIntersection() {
			return intersection;
		}

		@Override
		public VertexConnector getSourceConnector() {
			return sourceConnector;
		}

		@Override
		public VertexConnector getTargetConnector() {
			return targetConnector;
		}

		@Override
		public EEdgeType getType() {
			return type;
		}

		/**
		 *
		 */
		public void update() {
			GraphVertex s = getSource();
			GraphVertex t = getTarget();
			TypedSet sourceIDs = s.getIDs(sourceConnector.getDimension());
			TypedSet targetIDs = t.getIDs(targetConnector.getDimension());
			intersection = sourceIDs.intersect(targetIDs);
			sourceConnector.update(sourceIDs, intersection);
			targetConnector.update(targetIDs, intersection);
		}
	}
}
