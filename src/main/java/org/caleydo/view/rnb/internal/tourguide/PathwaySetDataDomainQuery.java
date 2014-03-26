/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.tourguide;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.id.IDType;
import org.caleydo.core.id.IIDTypeMapper;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.datadomain.pathway.PathwayDataDomain;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.PathwayPerspectiveRow;
import org.caleydo.vis.lineup.model.ARankColumnModel;
import org.caleydo.vis.lineup.model.CategoricalRankColumnModel;
import org.caleydo.vis.lineup.model.IRow;
import org.caleydo.vis.lineup.model.RankTableModel;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author Samuel Gratzl
 *
 */
public class PathwaySetDataDomainQuery extends ADataDomainQuery {
	/**
	 * @param dataDomain
	 */
	public PathwaySetDataDomainQuery() {
		super(DataDomainManager.get().getDataDomainsByType(PathwayDataDomain.class).get(0));
	}

	@Override
	public PathwayDataDomain getDataDomain() {
		return (PathwayDataDomain) super.getDataDomain();
	}

	@Override
	public boolean apply(AScoreRow input) {
		return true;
	}

	@Override
	protected boolean hasFilterImpl() {
		return false;
	}

	@Override
	protected List<AScoreRow> getAll() {
		List<AScoreRow> r = Lists.newArrayList();
		IDType david = getIDType();
		IIDTypeMapper<Integer, Integer> mapper = getDataDomain().getGeneIDMappingManager().getIDTypeMapper(
				PathwayVertexRep.getIdType(), david);
		Set<Integer> idsInPathway = new HashSet<>();
		for (PathwayGraph per : PathwayManager.get().getAllItems()) {
			idsInPathway.clear();
			for (PathwayVertexRep vertexRep : per.vertexSet()) {
				idsInPathway.add(vertexRep.getID());
			}
			Set<Integer> davids = mapper.apply(idsInPathway);
			if (davids.isEmpty())
				continue;
			r.add(new PathwayPerspectiveRow(per, david, davids));
		}
		return r;
	}

	public IDType getIDType() {
		return getDataDomain().getDavidIDType();
	}

	@Override
	public void createSpecificColumns(RankTableModel table) {
		Map<EPathwayDatabaseType, String> metaData = new EnumMap<>(EPathwayDatabaseType.class);
		for (EPathwayDatabaseType type : EPathwayDatabaseType.values()) {
			metaData.put(type, type.getName());
		}
		table.add(new CategoricalRankColumnModel<>(GLRenderers.drawText("Database"),
				new Function<IRow, EPathwayDatabaseType>() {
					@Override
					public EPathwayDatabaseType apply(IRow in) {
						if (!(in instanceof PathwayPerspectiveRow))
							return null;
						PathwayPerspectiveRow r = (PathwayPerspectiveRow) in;
						return r.getType();
					}
				}, metaData));
	}

	@Override
	public void removeSpecificColumns(RankTableModel table) {
		for (ARankColumnModel c : table.getFlatColumns()) {
			if (c instanceof CategoricalRankColumnModel && "Database".equals(c.getLabel())) {
				c.hide();
				c.destroy();
				break;
			}
		}
	}

	@Override
	public void updateSpecificColumns(RankTableModel table) {

	}

	@Override
	public List<AScoreRow> onDataDomainUpdated() {
		return null;
	}

}
