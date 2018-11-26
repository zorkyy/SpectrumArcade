package com.scs.spectrumarcade.entities;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Node;
import com.scs.spectrumarcade.IEntity;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.components.INotifiedOfCollision;
import com.scs.spectrumarcade.levels.ILevelGenerator;
import com.scs.spectrumarcade.models.ArcadeMachineModel;

public class ArcadeMachine extends AbstractPhysicalEntity implements INotifiedOfCollision {

	private Class<? extends ILevelGenerator> level;
	
	public ArcadeMachine(SpectrumArcade _game, float x, float y, float z, String folder, Class<? extends ILevelGenerator> _level) {
		super(_game, "ArcadeMachine");

		level = _level;
		
		Node geometry = new ArcadeMachineModel(_game.getAssetManager(), folder);
		this.mainNode.attachChild(geometry);
		mainNode.setLocalTranslation(x, y, z);
		mainNode.updateModelBound();

		srb = new RigidBodyControl(0);
		mainNode.addControl(srb);
		//srb.setKinematic(true);

	}

	
	@Override
	public void notifiedOfCollision(AbstractPhysicalEntity collidedWith) {
		if (collidedWith == game.player) {
			try {
				ILevelGenerator object = level.newInstance();
				object.setGame(game);
				this.game.startNewLevel(object);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	
}
	