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
	public static final URL ICON_MISSING = icon("missing.png");

	public static final URL ICON_SORT_DIM = icon("sort_columns_dim.png");
	public static final URL ICON_SORT_REC = icon("sort_columns.png");
	public static final URL ICON_STRATIFY_DIM = icon("category_dim.png");
	public static final URL ICON_STRATIFY_REC = icon("category.png");
	public static final URL ICON_DELETE = icon("cross.png");
	public static final URL ICON_DELETE_ALL = icon("cross_all.png");
	public static final URL ICON_MERGE_REC = icon("merge.png");
	public static final URL ICON_MERGE_DIM = icon("merge_dim.png");
	public static final URL ICON_LIMIT_DATA_DIM = icon("limit_dim.png");
	public static final URL ICON_LIMIT_DATA_REC = icon("limit.png");

	public static final URL ICON_STATE_MOVE = icon("transform_move.png");
	public static final URL ICON_STATE_SELECT = icon("select.png");
	public static final URL ICON_STATE_BANDS = ICON_MISSING;

	public static final URL ICON_SELECT_REC = icon("select_rec.png");
	public static final URL ICON_SELECT_DIM = icon("select_dim.png");

	public static final URL ICON_SELECT_ALL = icon("selectAll.png");

	public static final URL ICON_TRANSPOSE = icon("transform_rotate.png");

	public static final URL ICON_SET_UNION = icon("setop_union.png");
	public static final URL ICON_SET_INTERSECT = icon("setop_intersection.png");
	public static final URL ICON_SET_DIFFERENCE = icon("setop_diff.png");

	public static final URL ICON_FOCUS = icon("focus.png");
	public static final URL ICON_HATCHING = icon("hatching.png");

	public static final URL ICON_UNDO = icon("arrow_undo.png");
	public static final URL ICON_REDO = icon("arrow_redo.png");
	public static final URL ICON_UNDO_DISABLED = icon("arrow_undo_disabled.png");
	public static final URL ICON_REDO_DISABLED = icon("arrow_redo_disabled.png");


	/**
	 * @param string
	 * @return
	 */
	private static URL icon(String icon) {
		return Resources.class.getResource("icons/" + icon);
	}
}
