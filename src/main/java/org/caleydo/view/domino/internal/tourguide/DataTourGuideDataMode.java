/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide;

import java.util.Arrays;
import java.util.Collections;

import org.caleydo.core.data.collection.EDataClass;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.view.tourguide.api.adapter.ATourGuideDataMode;
import org.caleydo.view.tourguide.api.model.ADataDomainQuery;
import org.caleydo.view.tourguide.api.model.AScoreRow;
import org.caleydo.view.tourguide.api.model.InhomogenousDataDomainQuery;
import org.caleydo.vis.lineup.model.CategoricalRankColumnModel;
import org.caleydo.vis.lineup.model.IRow;
import org.caleydo.vis.lineup.model.RankTableModel;
import org.caleydo.vis.lineup.model.StringRankColumnModel;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * @author Samuel Gratzl
 *
 */
public class DataTourGuideDataMode extends ATourGuideDataMode {

	@Override
	public Iterable<? extends ADataDomainQuery> createDataDomainQuery(IDataDomain dd) {
		return Collections.singleton(createFor(dd));
	}

	protected ADataDomainQuery createFor(IDataDomain dd) {
		if (!DataSupportDefinitions.homogenousTables.apply(dd))
			return new InhomogenousDataDomainQuery((ATableBasedDataDomain) dd,
 Sets.immutableEnumSet(
					EDataClass.CATEGORICAL, EDataClass.NATURAL_NUMBER, EDataClass.REAL_NUMBER));
		return new DataDomainQuery((ATableBasedDataDomain) dd);
	}

	@Override
	public void addDefaultColumns(RankTableModel table) {
		addDataDomainRankColumn(table);
		final StringRankColumnModel base = new StringRankColumnModel(GLRenderers.drawText("Data Set"),
				StringRankColumnModel.DEFAULT);
		table.add(base);
		base.setWidth(150);
		base.orderByMe();
		table.add(CategoricalRankColumnModel.createSimple(GLRenderers.drawText("Type"), new Function<IRow, String>() {
			@Override
			public String apply(IRow input) {
				return input instanceof DataDomainScoreRow ? "2D" : "1D";
			}

		}, Arrays.asList("1D", "2D")));
		table.add(new SizeRankColumnModel("#Records", new Function<IRow, Integer>() {
			@Override
			public Integer apply(IRow in) {
				return ((AScoreRow) in).size();
			}
		}).setWidth(75));
		table.add(new SizeRankColumnModel("#Dimensions", new Function<IRow, Integer>() {
			@Override
			public Integer apply(IRow in) {
				return Iterables.size(((AScoreRow) in).getDimensionIDs());
			}
		}).setWidth(75));
	}

	@Override
	public boolean apply(IDataDomain input) {
		return input instanceof ATableBasedDataDomain;
	}

}
