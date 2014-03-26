/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.rnb.internal.dnd;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.view.opengl.canvas.ITransferSerializer;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;

/**
 * @author Samuel Gratzl
 *
 */
public class PerspectiveDragInfo implements IDragInfo {
	private final Perspective perspective;
	private final Integer referenceID;
	private final EDimension dim;

	public PerspectiveDragInfo(Perspective perspective, Integer referenceID, EDimension dim) {
		this.perspective = perspective;
		this.referenceID = referenceID;
		this.dim = dim;
	}

	/**
	 * @return the referenceID, see {@link #referenceID}
	 */
	public Integer getReferenceID() {
		return referenceID;
	}

	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	/**
	 * @return the perspective, see {@link #perspective}
	 */
	public Perspective getPerspective() {
		return perspective;
	}

	@Override
	public String getLabel() {
		return perspective.getLabel();
	}

	public static final class Serializer implements ITransferSerializer {

		@Override
		public String getId() {
			return "PerspectiveDragInfo";
		}

		@Override
		public void write(Object data, ObjectOutputStream o) throws IOException {
			assert data instanceof PerspectiveDragInfo;
			PerspectiveDragInfo info = (PerspectiveDragInfo) data;
			Perspective t = info.getPerspective();
			o.writeObject(t.getDataDomain().getDataDomainID());
			o.writeObject(t.getPerspectiveID());
			o.writeInt(info.getReferenceID() == null ? -1 : info.getReferenceID().intValue());
			o.writeObject(info.getDim());
		}

		@Override
		public Object read(ObjectInputStream in) throws ClassNotFoundException, IOException {
			String dataDomainId = (String) in.readObject();
			String perspectiveId = (String) in.readObject();
			int readInt = in.readInt();
			Integer referenceId = readInt < 0 ? null : Integer.valueOf(readInt);
			EDimension dim = (EDimension)in.readObject();
			IDataDomain dataDomain = DataDomainManager.get().getDataDomainByID(dataDomainId);
			assert dataDomain instanceof ATableBasedDataDomain;

			Table d = ((ATableBasedDataDomain) dataDomain).getTable();

			Perspective p = dim.isRecord() ? d.getRecordPerspective(perspectiveId) : d
					.getDimensionPerspective(perspectiveId);
			return new PerspectiveDragInfo(p, referenceId, dim);
		}

		@Override
		public boolean apply(Class<?> clazz) {
			return PerspectiveDragInfo.class.isAssignableFrom(clazz);
		}

	}

}
