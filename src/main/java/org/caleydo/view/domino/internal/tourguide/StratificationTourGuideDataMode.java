/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import java.util.Arrays;
import java.util.Collections;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.datadomain.pathway.PathwayDataDomain;
import org.caleydo.view.tourguide.api.adapter.ATourGuideDataMode;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.CategoricalDataDomainQuery;
import org.caleydo.view.tourguide.api.model.InhomogenousDataDomainQuery;
import org.caleydo.view.tourguide.api.model.StratificationDataDomainQuery;
import org.caleydo.vis.lineup.model.GroupRankColumnModel;
import org.caleydo.vis.lineup.model.IRow;
import org.caleydo.vis.lineup.model.RankTableModel;
import org.caleydo.vis.lineup.model.StringRankColumnModel;

import com.google.common.base.Function;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class StratificationTourGuideDataMode extends ATourGuideDataMode {

	@Override
	public Iterable<? extends ADataDomainQuery> createDataDomainQuery(IDataDomain dd) {
		if (dd instanceof PathwayDataDomain)
			return Arrays.asList(new PathwaySetDataDomainQuery());
		if (!DataSupportDefinitions.homogenousTables.apply(dd))
			// inhomogenous categorical just them
			return Collections.singleton(new InhomogenousDataDomainQuery((ATableBasedDataDomain) dd, Sets
					.immutableEnumSet(EDataClass.CATEGORICAL)));
		if (DataSupportDefinitions.categoricalTables.apply(dd)) {
			return Arrays.asList(new CategoricalDataDomainQuery((ATableBasedDataDomain) dd, EDimension.DIMENSION),new CategoricalDataDomainQuery((ATableBasedDataDomain) dd, EDimension.RECORD));
		}
		return Arrays.asList(new StratificationDataDomainQuery((ATableBasedDataDomain) dd, EDimension.DIMENSION),
				new StratificationDataDomainQuery((ATableBasedDataDomain) dd, EDimension.RECORD));
	}

	@Override
	public void addDefaultColumns(RankTableModel table) {
		addDataDomainRankColumn(table);
		final StringRankColumnModel base = new StringRankColumnModel(GLRenderers.drawText("Data Set"),
				StringRankColumnModel.DEFAULT);
		table.add(base);
		base.setWidth(150);
		base.orderByMe();

		GroupRankColumnModel group = new GroupRankColumnModel("Metrics", Color.GRAY, new Color(0.95f, .95f, .95f));
		table.add(group);
		group.add(new SizeRankColumnModel("#Elements", new Function<IRow, Integer>() {
			@Override
			public Integer apply(IRow in) {
				return ((AScoreRow) in).size();
			}
		}).setWidth(75));

		group.add(new SizeRankColumnModel("#Groups", new Function<IRow, Integer>() {
			@Override
			public Integer apply(IRow in) {
				return ((AScoreRow) in).getGroupSize();
			}
		}).setWidth(75).setCollapsed(false));

		addGroupDistributionRankColumn(table);
	}

	@Override
	public boolean apply(IDataDomain input) {
		return input instanceof ATableBasedDataDomain || input instanceof PathwayDataDomain;
	}

}
