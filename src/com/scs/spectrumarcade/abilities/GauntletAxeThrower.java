package com.scs.spectrumarcade.abilities;

import com.jme3.math.Vector3f;
import com.scs.spectrumarcade.Globals;
import com.scs.spectrumarcade.Settings;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.entities.gauntlet.ThrowingAxe;

public class GauntletAxeThrower extends AbstractAbility implements IAbility {

	private static final long SHOT_INTERVAL = 1000;

	private long nextShotTime = 0;

	public GauntletAxeThrower(SpectrumArcade game) {
		super(game);
	}


	@Override
	public void activate() {
		if (nextShotTime < System.currentTimeMillis()) {
			//Globals.p("Throwing axe");
			nextShotTime = System.currentTimeMillis() + SHOT_INTERVAL;

			Vector3f pos = game.player.getMainNode().getWorldTranslation().clone();
			pos.y += Settings.PLAYER_HEIGHT;
			pos.addLocal(game.getCamera().getDirection().mult(2));
			ThrowingAxe bomb = new ThrowingAxe(game, pos.x, pos.y, pos.z);
			game.addEntity(bomb);

			Vector3f force = game.getCamera().getDirection().mult(10);
			//force.y = 0;
			//srb.applyCentralForce(force);
			bomb.srb.setLinearVelocity(force);
			Globals.p("Force=" + force);


		}
	}

}