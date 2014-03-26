/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.undo;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.caleydo.core.util.base.Labels;
import org.caleydo.view.rnb.internal.RnB;

import com.google.common.collect.Collections2;

/**
 * @author Samuel Gratzl
 *
 */
public class CmdComposite implements ICmd {
	private final ICmd[] cmds;

	public CmdComposite(ICmd[] cmds) {
		this.cmds = cmds;
	}

	public static ICmd chain(ICmd... cmds) {
		return new CmdComposite(cmds);
	}

	@Override
	public String getLabel() {
		return StringUtils.join(Collections2.transform(Arrays.asList(cmds), Labels.TO_LABEL), ", ");
	}

	@Override
	public ICmd run(RnB domino) {
		ICmd[] u = new ICmd[cmds.length];
		for (int i = 0; i < cmds.length; ++i) {
			u[u.length - 1 - i] = cmds[i].run(domino);
		}
		return chain(u);
	}
}
