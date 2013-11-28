/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.domino.internal.ui.prototype.ui;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.function.IDoubleSizedIterator;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ATextured1DUI extends GLElement {
	private Texture texture;

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		texture = new Texture(GL.GL_TEXTURE_2D);
	}
	@Override
	protected void takeDown() {
		texture.destroy(GLContext.getCurrentGL());
		super.takeDown();
	}

	protected abstract EDimension getDimension();

	protected void update(IDoubleSizedIterator it) {
		int width = it.size();
		FloatBuffer buffer = FloatBuffer.allocate(width * 3); // w*rgb*float

		while(it.hasNext()) {
			float v = (float) it.nextPrimitive();
			buffer.put(v).put(v).put(v);
		}

		updateTexture(width, buffer);
	}

	protected final void updateTexture(int width, FloatBuffer buffer) {
		buffer.rewind();
		TextureData texData = new TextureData(GLProfile.getDefault(), GL.GL_RGB /* internalFormat */, width, 1,
				0 /* border */, GL.GL_RGB /* pixelFormat */, GL.GL_FLOAT /* pixelType */, false /* mipmap */,
				false /* dataIsCompressed */, false /* mustFlipVertically */, buffer, null);
		texture.updateImage(GLContext.getCurrentGL(), texData);
		texData.destroy();

		repaint();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		GL2 gl = g.gl;

		EDimension dim = getDimension();

		g.checkError();
		texture.enable(gl);
		texture.bind(gl);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		g.color(Color.WHITE);
		g.save();
		gl.glTranslatef(1, 1, g.z());

		final float size = dim.select(w, h);
		final float op_size = -2 + (dim.select(h, w));

		g.checkError();
		gl.glBegin(GL2GL3.GL_QUADS);
		rect(0, 1, 0, size, op_size, gl, dim);
		gl.glEnd();
		g.checkError();
		texture.disable(gl);
		g.checkError();
		g.restore();
	}

	private void rect(float s1, float s2, float x, float x2, float y, GL2 gl, EDimension dim) {
		if (dim.isHorizontal()) {
			gl.glTexCoord2f(s1, 1f);
			gl.glVertex2f(x, 0);
			gl.glTexCoord2f(s2, 1f);
			gl.glVertex2f(x2, 0);
			gl.glTexCoord2f(s2, 0f);
			gl.glVertex2f(x2, y);
			gl.glTexCoord2f(s1, 0f);
			gl.glVertex2f(x, y);
		} else {
			gl.glTexCoord2f(s1, 0);
			gl.glVertex2f(0, x);
			gl.glTexCoord2f(s1, 1);
			gl.glVertex2f(y, x);
			gl.glTexCoord2f(s2, 1);
			gl.glVertex2f(y, x2);
			gl.glTexCoord2f(s2, 0);
			gl.glVertex2f(0, x2);
		}
	}
}
