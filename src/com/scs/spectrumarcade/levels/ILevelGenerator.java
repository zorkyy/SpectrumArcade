package com.scs.spectrumarcade.levels;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.scs.spectrumarcade.CameraSystem;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.components.IAvatar;

public interface ILevelGenerator {

	void setGame(SpectrumArcade game);
	
	void generateLevel(SpectrumArcade game, int levelNum) throws FileNotFoundException, IOException, URISyntaxException;
	
	Vector3f getAvatarStartPos();
	
	IAvatar createAndPositionAvatar();
	
	ColorRGBA getBackgroundColour();
	
	void process(float tpfSecs);
	
	String getHUDText();
	
	void setupCameraSystem(CameraSystem sys);
	
	void remove();
	
}
