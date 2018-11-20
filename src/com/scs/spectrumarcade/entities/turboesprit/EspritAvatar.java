package com.scs.spectrumarcade.entities.turboesprit;

import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.scs.spectrumarcade.Globals;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.entities.PlayerCar;
import com.scs.spectrumarcade.models.EspritModel;

public class EspritAvatar extends PlayerCar {
	
	private Node camNode;

	public EspritAvatar(SpectrumArcade _game, float x, float y, float z) {
		super(_game, x, y, z);
		
		camNode = new Node("CameraNode");
		camNode.setLocalTranslation(0f, 1.2f, -4);
		this.mainNode.attachChild(camNode);
	}


	@Override
	public void process(float tpfSecs) {
		super.process(tpfSecs);
		Globals.p("Esprit pos: " + this.getMainNode().getWorldTranslation());
	}


	@Override
	protected Node getModel() {
		return new EspritModel(game.getAssetManager());
	}

	
	@Override
	public void setCameraLocation(Camera cam) {
		cam.setLocation(camNode.getWorldTranslation()); //this.mainNode;
		
	}


}
