package com.scs.spectrumarcade.entities.antattack;

import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.scs.spectrumarcade.Globals;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.components.IProcessable;
import com.scs.spectrumarcade.entities.AbstractPhysicalEntity;
import com.scs.spectrumarcade.entities.HUDTextEntity;
import com.scs.spectrumarcade.jme.JMEAngleFunctions;
import com.scs.spectrumarcade.levels.ArcadeRoom;
import com.scs.spectrumarcade.models.GenericWalkingAvatar;

import ssmith.util.RealtimeInterval;

public class Damsel extends AbstractPhysicalEntity implements IProcessable { // INotifiedOfCollision

	private static final float TURN_SPEED = 1f;

	private GenericWalkingAvatar model;
	public boolean followingPlayer;
	private BetterCharacterControl playerControl;

	private RealtimeInterval checkPosInterval = new RealtimeInterval(500);
	private Vector3f prevPos = new Vector3f();
	private long dontWalkUntil = 0;

	public Damsel(SpectrumArcade _game, float x, float z) {
		super(_game, "Damsel");

		model = new GenericWalkingAvatar(game.getAssetManager(), "Textures/antattack/avatar_black.png");
		model.setLocalTranslation(0, 0, 0);
		this.mainNode.attachChild(model);

		mainNode.setLocalTranslation(x, 11, z);
		mainNode.updateModelBound();

		BoundingBox bb = (BoundingBox)model.getWorldBound();
		playerControl = new BetterCharacterControl(bb.getZExtent(), bb.getYExtent()*2, 10f);
		playerControl.setJumpForce(new Vector3f(0, 5f, 0)); 
		playerControl.setGravity(new Vector3f(0, 1f, 0));
		this.getMainNode().addControl(playerControl);

		this.model.idleAnim();

	}


	@Override
	public void process(float tpfSecs) {
		this.turnTowardsPlayer();
		if (followingPlayer) {
			if (this.distance(game.player) > 10f) {
				this.followingPlayer = false;
				this.model.idleAnim();
			} else if (this.distance(game.player) > 3f) {
				if (dontWalkUntil < System.currentTimeMillis()) {
					this.model.walkAnim();
					moveFwds();
					if (checkPosInterval.hitInterval()) {
						if (this.mainNode.getWorldTranslation().distance(this.prevPos) < .1f) {
							Globals.p("Damsel stuck; jumping");

							Vector3f walkDirection = this.playerControl.getViewDirection();
							playerControl.setWalkDirection(walkDirection.mult(-2.4f));

							playerControl.setJumpForce(new Vector3f(0, 50f, 0)); 
							this.playerControl.jump();
							this.model.jumpAnim();
						} 
						prevPos.set(this.mainNode.getWorldTranslation());
					}
				}
				// Check if reached the exit
				Vector3f pos = this.getMainNode().getWorldTranslation();
				if (pos.z > 127) {
					// Game complete
					Globals.p("Damsel has reached the end!");

					HUDTextEntity be = new HUDTextEntity(game, "WELL DONE!", 72, ColorRGBA.Black, 10, game.getCamera().getHeight()-50, 5);
					game.addEntity(be);

					game.setNextLevel(ArcadeRoom.class, -1);
				}
			} else {
				playerControl.setWalkDirection(new Vector3f());
				this.model.idleAnim();
			}
		} else {
			if (this.distance(game.player) < 5f) {
				followingPlayer = true;
			}
		}
	}


	private void turnTowardsPlayer() {
		float leftDist = this.leftNode.getWorldTranslation().distance(game.player.getMainNode().getWorldTranslation()); 
		float rightDist = this.rightNode.getWorldTranslation().distance(game.player.getMainNode().getWorldTranslation()); 
		if (leftDist > rightDist) {
			JMEAngleFunctions.turnSpatialLeft(this.mainNode, TURN_SPEED);
		} else {
			JMEAngleFunctions.turnSpatialLeft(this.mainNode, -TURN_SPEED);
		}
		this.playerControl.setViewDirection(mainNode.getWorldRotation().getRotationColumn(2));
	}


	private void moveFwds() {
		Vector3f walkDirection = this.playerControl.getViewDirection();
		playerControl.setWalkDirection(walkDirection.mult(4.1f));

	}


	@Override
	public void actuallyRemove() {
		super.actuallyRemove();
		if (playerControl != null) {
			this.game.bulletAppState.getPhysicsSpace().remove(this.playerControl);
		}

	}


}
