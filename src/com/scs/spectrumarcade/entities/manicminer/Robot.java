package com.scs.spectrumarcade.entities.manicminer;

import com.scs.spectrumarcade.IProcessable;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.entities.AbstractPhysicalEntity;

public class Robot extends AbstractPhysicalEntity implements IProcessable  {

	public static final float START_HEALTH = 5f;

	public Robot(SpectrumArcade _game, float x, float y, float z, int sX, int sY) {
		super(_game, "Robot");
		
			//this.ai = new RobotAI(this, sX, sY);
		
	}

	@Override
	public void process(float tpfSecs) {
	}
	

}
