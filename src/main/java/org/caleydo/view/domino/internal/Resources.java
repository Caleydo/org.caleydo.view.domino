/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal;

import java.net.URL;

import org.caleydo.data.loader.ResourceLoader;
import org.caleydo.data.loader.ResourceLocators;
import org.caleydo.data.loader.ResourceLocators.IResourceLocator;
import org.caleydo.view.domino.internal.plugin.Activator;

/**
 * @author Samuel Gratzl
 *
 */
public class Resources {
	/**
	 * the resource locator of this plugin to find icons,...
	 */
	private static IResourceLocator resourceLocator = ResourceLocators.chain(
			ResourceLocators.classLoader(Activator.class.getClassLoader()), ResourceLocators.DATA_CLASSLOADER,
			ResourceLocators.FILE);

	/**
	 * @return
	 */
	public static ResourceLoader getResourceLoader() {
		return new ResourceLoader(resourceLocator);
	}

	/**
	 * @return the resourceLocator, see {@link #resourceLocator}
	 */
	public static IResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public static final URL ICON = icon("icon.png");

	public static final URL ICON_SORT_DIM = icon("sort_columns_dim.png");
	public static final URL ICON_SORT_REC = icon("sort_columns.png");
	public static final URL ICON_STRATIFY_DIM = icon("category_dim.png");
	public static final URL ICON_STRATIFY_REC = icon("category.png");
	public static final URL ICON_DELETE = icon("cross.png");
	public static final URL ICON_DELETE_ALL = icon("cross_all.png");
	public static final URL ICON_MERGE_REC = icon("category_group_select.png");
	public static final URL ICON_MERGE_DIM = icon("category_group_select_dim.png");
	public static final URL ICON_LIMIT_DATA_DIM = icon("award_star_gold_1.png");
	public static final URL ICON_LIMIT_DATA_REC = icon("award_star_gold_1.png");

	public static final URL ICON_STATE_MOVE = icon("transform_move.png");
	public static final URL ICON_STATE_SELECT = icon("select.png");

	public static final URL ICON_SELECT_REC = icon("layer_resize_replicate_h.png");
	public static final URL ICON_SELECT_DIM = icon("layer_resize_replicate_v.png");

	public static final URL ICON_SELECT_ALL = icon("layer_resize.png");

	public static final URL ICON_TRANSPOSE = icon("transform_rotate.png");

	/**
	 * @param string
	 * @return
	 */
	private static URL icon(String icon) {
		return Resources.class.getResource("icons/" + icon);
	}
}
