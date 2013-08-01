/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.crossword.internal;

import java.net.URL;

import org.caleydo.data.loader.ResourceLoader;
import org.caleydo.data.loader.ResourceLocators;
import org.caleydo.data.loader.ResourceLocators.IResourceLocator;

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

	public static final URL deleteIcon() {
		return icon("cross.png");
	}

	public static final URL cutRecord() {
		return icon("cut_red.png");
	}

	public static final URL cutDimension() {
		return icon("cut.png");
	}

	/**
	 * @param string
	 * @return
	 */
	private static URL icon(String icon) {
		return Resources.class.getResource("icons/" + icon);
	}
}
