/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.dnd;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.canvas.ITransferSerializer;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class TablePerspectiveDragInfo implements IDragInfo {
	private final TablePerspective tablePerspective;

	public TablePerspectiveDragInfo(TablePerspective tablePerspective) {
		this.tablePerspective = tablePerspective;
	}

	/**
	 * @return the tablePerspective, see {@link #tablePerspective}
	 */
	public TablePerspective getTablePerspective() {
		return tablePerspective;
	}

	@Override
	public String getLabel() {
		return tablePerspective.getLabel();
	}

	public static final class Serializer implements ITransferSerializer {

		@Override
		public String getId() {
			return "TablePerspectiveDragInfo";
		}

		@Override
		public void write(Object data, ObjectOutputStream o) throws IOException {
			assert data instanceof TablePerspectiveDragInfo;
			TablePerspectiveDragInfo info = (TablePerspectiveDragInfo) data;
			TablePerspective t = info.getTablePerspective();
			o.writeObject(t.getDataDomain().getDataDomainID());
			o.writeObject(t.getTablePerspectiveKey());
		}

		@Override
		public Object read(ObjectInputStream in) throws ClassNotFoundException, IOException {
			String dataDomainId = (String) in.readObject();
			String tablePerspectiveKey = (String) in.readObject();

			IDataDomain dataDomain = DataDomainManager.get().getDataDomainByID(dataDomainId);
			assert dataDomain instanceof ATableBasedDataDomain;
			TablePerspective t = ((ATableBasedDataDomain) dataDomain).getTablePerspective(tablePerspectiveKey);
			return new TablePerspectiveDragInfo(t);
		}

		@Override
		public boolean apply(Class<?> clazz) {
			return TablePerspectiveDragInfo.class.isAssignableFrom(clazz);
		}

	}
}
