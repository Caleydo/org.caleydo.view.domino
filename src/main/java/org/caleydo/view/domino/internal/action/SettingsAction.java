/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.domino.internal.action;

import org.caleydo.core.gui.SimpleAction;
import org.caleydo.core.gui.SimpleEventAction;
import org.caleydo.view.domino.internal.Resources;
import org.caleydo.view.domino.internal.event.ToggleHeaderAlwaysEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * different settings regarding the presentation of crossword
 *
 * @author Samuel Gratzl
 *
 */
public class SettingsAction extends SimpleAction implements IMenuCreator {
	private final Object receiver;
	private Menu menu;

	public SettingsAction(Object receiver) {
		super("Domino Settings", "resources/icons/setting_tools.png", Resources.getResourceLoader());
		this.receiver = receiver;
		setMenuCreator(this);
	}

	@Override
	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}

	@Override
	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}

		menu= new Menu(parent);

		addActionToMenu(menu, new SimpleEventAction("Toggle always show header bar",
 "resources/icons/text_large_cap.png", Resources
						.getResourceLoader(), new ToggleHeaderAlwaysEvent()
						.to(receiver).from(this)));

		return menu;
	}

	private void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}
}
