package com.scs.spectrumarcade.entities;

import com.jme3.asset.TextureKey;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.Settings;

public class Wall extends AbstractPhysicalEntity {

	public Wall(SpectrumArcade _game, float x, float yBottom, float z, float w, float h, float d, String tex, boolean tile) {
		super(_game, "Wall");

		Box box1 = new Box(w/2, h/2, d/2);

		if (tile) {
			box1.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(new float[]{
					0, h, w, h, w, 0, 0, 0, // back
					0, h, d, h, d, 0, 0, 0, // right
					0, h, w, h, w, 0, 0, 0, // front
					0, h, d, h, d, 0, 0, 0, // left
					w, 0, w, d, 0, d, 0, 0, // top
					w, 0, w, d, 0, d, 0, 0  // bottom
			}));
		}

		Geometry geometry = new Geometry("WallGeom", box1);
		geometry.setShadowMode(ShadowMode.Receive);

		TextureKey key3 = new TextureKey(tex);
		key3.setGenerateMips(true);
		Texture tex3 = game.getAssetManager().loadTexture(key3);
		tex3.setWrap(WrapMode.Repeat);

		Material material = new Material(game.getAssetManager(),"Common/MatDefs/Light/Lighting.j3md");
		material.setTexture("DiffuseMap", tex3);
		geometry.setMaterial(material);

		geometry.setLocalTranslation(w/2, h/2, d/2);

		this.mainNode.attachChild(geometry);

		mainNode.setLocalTranslation(x, yBottom, z); // Move it into position

		srb = new RigidBodyControl(0);
		mainNode.addControl(srb);

		geometry.setUserData(Settings.ENTITY, this);

	}

}
