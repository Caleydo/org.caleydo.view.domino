/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.tourguide.ui;

import java.util.Comparator;
import java.util.Set;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.id.IDCategory;
import org.caleydo.core.util.base.ICallback;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.RadioController;
import org.caleydo.core.view.opengl.layout2.layout.GLLayoutDatas;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

/**
 * @author Samuel Gratzl
 *
 */
public class EntityTypeSelector extends GLElementContainer implements ISelectionCallback {

	private final RadioController controller = new RadioController(this);
	private final ICallback<IDCategory> onCategorySelected;
	/**
	 *
	 */
	public EntityTypeSelector(ICallback<IDCategory> onCategorySelected) {
		super(GLLayouts.flowVertical(3));
		setSize(80, -1);
		this.onCategorySelected = onCategorySelected;

		createEntityNodes();
	}

	/**
	 *
	 */
	private void createEntityNodes() {
		Set<IDCategory> categories = findAllUsedIDCategories();
		for (IDCategory cat : categories) {
			GLButton b = new GLButton(EButtonMode.CHECKBOX);
			controller.add(b);
			b.setLayoutData(cat);
			b.setRenderer(GLButton.createRadioRenderer(cat.getCategoryName()));
			b.setSize(-1, 18);
			this.add(b);
		}
		controller.setSelected(0);
	}

	/**
	 * @return
	 */
	public static Set<IDCategory> findAllUsedIDCategories() {
		ImmutableSet.Builder<IDCategory> b = ImmutableSortedSet.orderedBy(new Comparator<IDCategory>() {
			@Override
			public int compare(IDCategory o1, IDCategory o2) {
				return String.CASE_INSENSITIVE_ORDER.compare(o1.getCategoryName(), o2.getCategoryName());
			}
		});
		IDataSupportDefinition inhomogenous = DataSupportDefinitions.inhomogenousTables;
		for (ATableBasedDataDomain d : DataDomainManager.get().getDataDomainsByType(ATableBasedDataDomain.class)) {
			b.add(d.getRecordIDCategory());
			if (!inhomogenous.apply(d)) // just in case of uniform tables
				b.add(d.getDimensionIDCategory());
		}
		return b.build();
	}

	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {
		IDCategory cat = getIDCategory(button);
		onCategorySelected.on(cat);
	}

	private IDCategory getIDCategory(GLButton button) {
		return button.getLayoutDataAs(IDCategory.class, GLLayoutDatas.<IDCategory> throwInvalidException());
	}

	/**
	 * @return
	 */
	public IDCategory getActive() {
		return getIDCategory(controller.getSelectedItem());
	}
}
