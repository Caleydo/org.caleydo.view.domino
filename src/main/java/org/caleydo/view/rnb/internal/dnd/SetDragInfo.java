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
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.canvas.ITransferSerializer;
import org.caleydo.core.view.opengl.layout2.dnd.IDragInfo;
import org.caleydo.view.rnb.api.model.typed.TypedSet;

import com.google.common.collect.ImmutableSet;

/**
 * @author Samuel Gratzl
 *
 */
public class SetDragInfo implements IDragInfo {
	private final TypedSet set;
	private final EDimension dim;
	private final String label;

	public SetDragInfo(String label, TypedSet set, EDimension dim) {
		this.label = label;
		this.set = set;
		this.dim = dim;
	}


	/**
	 * @return the dim, see {@link #dim}
	 */
	public EDimension getDim() {
		return dim;
	}

	/**
	 * @return the set, see {@link #set}
	 */
	public TypedSet getSet() {
		return set;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public static final class Serializer implements ITransferSerializer {

		@Override
		public String getId() {
			return "SetDragInfo";
		}

		@Override
		public void write(Object data, ObjectOutputStream o) throws IOException {
			assert data instanceof SetDragInfo;
			SetDragInfo info = (SetDragInfo) data;
			o.writeObject(info.getLabel());
			o.writeObject(info.getSet().getIdType().getTypeName());
			o.writeObject(info.getDim());
			TypedSet s = info.getSet();
			o.writeInt(s.size());
			for (Integer si : s)
				o.writeInt(si.intValue());
		}

		@Override
		public Object read(ObjectInputStream in) throws ClassNotFoundException, IOException {
			String label = (String) in.readObject();
			IDType idType = IDType.getIDType((String) in.readObject());
			EDimension dim = (EDimension)in.readObject();
			int size = in.readInt();
			ImmutableSet.Builder<Integer> b = ImmutableSet.builder();
			for (int i = 0; i < size; ++i)
				b.add(in.readInt());
			return new SetDragInfo(label, new TypedSet(b.build(), idType), dim);
		}

		@Override
		public boolean apply(Class<?> clazz) {
			return SetDragInfo.class.isAssignableFrom(clazz);
		}

	}

}
