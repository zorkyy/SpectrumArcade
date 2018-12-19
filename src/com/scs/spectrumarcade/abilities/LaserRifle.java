package com.scs.spectrumarcade.abilities;

import com.jme3.math.Vector3f;
import com.scs.spectrumarcade.Settings;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.entities.AbstractPhysicalEntity;
import com.scs.spectrumarcade.entities.LaserBolt;

public class LaserRifle extends AbstractAbility implements IAbility {

	private static final long SHOT_INTERVAL = 1000;

	private long nextShotTime = 0;
	private AbstractPhysicalEntity shooter; 

	public LaserRifle(SpectrumArcade game, AbstractPhysicalEntity _shooter) {
		super(game);
		
		shooter = _shooter;
	}


	@Override
	public void activate() {
		if (nextShotTime < System.currentTimeMillis()) {
			nextShotTime = System.currentTimeMillis() + SHOT_INTERVAL;

			Vector3f pos = game.player.getMainNode().getWorldTranslation().clone();
			pos.y += Settings.PLAYER_HEIGHT;
			pos.addLocal(game.getCamera().getDirection().mult(2));

			LaserBolt laser = new LaserBolt(game, shooter, pos.x, pos.y, pos.z, game.getCamera().getDirection());
			game.addEntity(laser);

		}
	}


}
