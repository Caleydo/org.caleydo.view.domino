/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.api.model;

import org.caleydo.view.domino.spi.model.IConnectorStrategy;

/**
 * @author Samuel Gratzl
 *
 */
public class ConnectorStrategies {
	public static final IConnectorStrategy SHARED = new IConnectorStrategy() {
		@Override
		public CenterRadius update(TypedSet ids, TypedSet intersection) {
			int max = ids.size();
			int have = intersection.size();
			float size = have / (float) max;
			float offset = (1 - size) * 0.5f;
			float radius = size * 0.5f;
			return new CenterRadius(offset + radius, radius);
		}
	};

	public static IConnectorStrategy createParent(float offset) {
		return new ParentConnectorModel(offset);
	}

	private static final class ParentConnectorModel implements IConnectorStrategy {
		private final float offset;

		/**
		 * @param offset
		 */
		public ParentConnectorModel(float offset) {
			this.offset = offset;
		}

		@Override
		public CenterRadius update(TypedSet ids, TypedSet intersection) {
			int max = ids.size();
			int have = intersection.size();
			float size = have / (float) max;
			float radius = size * 0.5f;
			return new CenterRadius(offset + radius, radius);
		}
	}
}
