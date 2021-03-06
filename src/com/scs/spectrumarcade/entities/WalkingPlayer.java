package com.scs.spectrumarcade.entities;

import java.util.ArrayList;
import java.util.List;

import com.jme3.audio.AudioNode;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.spectrumarcade.Settings;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.components.IAvatar;
import com.scs.spectrumarcade.components.IAvatarModel;
import com.scs.spectrumarcade.jme.JMEAngleFunctions;

public class WalkingPlayer extends AbstractPhysicalEntity implements IAvatar {

	private static final float FOOTSTEP_INTERVAL = .3f;

	// Our movement speed
	public float speed = 4;
	//private static final float strafeSpeed = 4f;

	private IAvatarModel avatarModel; // GenericWalkingAvatar 
	public BetterCharacterControl playerControl;
	private Vector3f walkDirection = new Vector3f();
	private boolean left = false, right = false, up = false, down = false;

	//Temporary vectors used on each frame.
	private Vector3f camDir = new Vector3f();
	private Vector3f camLeft = new Vector3f();
	private Vector3f tempAvatarDir = new Vector3f();

	// Footsteps
	private List<AudioNode> audio_node_footsteps = new ArrayList<>();
	private float time_until_next_footstep_sfx = 1;
	private int next_footstep_sound = 0;
	public boolean walking = false;
	private boolean canJump;

	public WalkingPlayer(SpectrumArcade _game, Vector3f pos, float _speed, float jumpPower, IAvatarModel _avatarModel) {
		super(_game, "Player");

		speed = _speed;
		canJump = jumpPower > 0;

		/** Create a box to use as our player model */
		Box box1 = new Box(Settings.PLAYER_RAD, Settings.PLAYER_HEIGHT, Settings.PLAYER_RAD);
		Geometry playerGeometry = new Geometry("Player", box1);
		playerGeometry.setCullHint(CullHint.Always);
		this.getMainNode().attachChild(playerGeometry);
		this.getMainNode().setLocalTranslation(pos);

		// create character control parameters (Radius,Height,Weight)
		// Radius and Height determine the size of the collision bubble
		// Weight determines how much gravity effects the control
		playerControl = new BetterCharacterControl(Settings.PLAYER_RAD, Settings.PLAYER_HEIGHT, 1f);
		playerControl.setJumpForce(new Vector3f(0, jumpPower, 0)); 
		//playerControl.setGravity(new Vector3f(0, 1f, 0));
		this.getMainNode().addControl(playerControl);

		avatarModel = _avatarModel;//new GenericWalkingAvatar(game.getAssetManager(), tex);
		this.getMainNode().attachChild((Node)avatarModel);
		/*
		for (int i=1 ; i<=8 ; i++) {
			AudioNode an = new AudioNode(game.getAssetManager(), "Sounds/jute-dh-steps/stepdirt_" + i + ".ogg", false);
			an.setPositional(false);
			an.setVolume(.2f);
			this.getMainNode().attachChild(an);
			this.audio_node_footsteps.add(an);
		}
		 */
	}


	@Override
	public void process(float tpf) {
		if (game.playerDead) {
			return; // Stop us changing the anim to idle or something.
		}

		if (this.avatarModel != null) {
			// Set position and direction of avatar model, which doesn't get moved automatically
			//this.container.setLocalTranslation(this.getWorldTranslation());
			tempAvatarDir.set(game.getCamera().getDirection());
			tempAvatarDir.y = 0;
			JMEAngleFunctions.rotateToWorldDirection((Spatial)this.avatarModel, tempAvatarDir);

		}

		Camera cam = game.getCamera();
		camDir.set(cam.getDirection());
		camDir.y = 0;
		camDir.normalizeLocal(); // scs new
		camDir.multLocal(speed, 0.0f, speed);

		camLeft.set(cam.getLeft()).multLocal(speed);

		walkDirection.set(0, 0, 0);
		walking = up || down || left || right;
		if (left) {
			walkDirection.addLocal(camLeft);
		}
		if (right) {
			walkDirection.addLocal(camLeft.negate());
		}
		if (up) {
			walkDirection.addLocal(camDir);
		}
		if (down) {
			walkDirection.addLocal(camDir.negate());
		}
		playerControl.setWalkDirection(walkDirection);

		if (walking) {
			if (this.avatarModel != null) {
				this.avatarModel.walkAnim();
			}
			/*
				time_until_next_footstep_sfx -= tpf;
				if (time_until_next_footstep_sfx <= 0) {
					AudioNode an = this.audio_node_footsteps.get(next_footstep_sound);
					try {
						an.playInstance();
					} catch (Exception ex) {
						// No speakers?
					}
					next_footstep_sound++;
					if (next_footstep_sound >= audio_node_footsteps.size()) {
						next_footstep_sound = 0;
					}
					time_until_next_footstep_sfx = FOOTSTEP_INTERVAL + (SpectrumArcade.rnd.nextFloat()/3);
				}
			 */
		} else {
			//time_until_next_footstep_sfx = 0;
			if (this.avatarModel != null) {
				this.avatarModel.idleAnim();
			}
		}

	}


	@Override
	public void onAction(String binding, boolean isPressed, float tpf) {
		if (binding.equals("Left")) {
			left = isPressed;
		} else if (binding.equals("Right")) {
			right = isPressed;
		} else if (binding.equals("Fwd")) {
			up = isPressed;
		} else if (binding.equals("Backwards")) {
			down = isPressed;
		} else if (binding.equals("Jump")) {
			if (canJump) {
				if (isPressed) { 
					playerControl.jump();
					this.avatarModel.jumpAnim();
				}
			}
		}

	}


	@Override
	public void warp(Vector3f v) {
		playerControl.warp(v);
	}


	@Override
	public void actuallyRemove() {
		super.actuallyRemove();
		if (playerControl != null) {
			this.game.bulletAppState.getPhysicsSpace().remove(this.playerControl);
		}

	}


	@Override
	public void clearForces() {
	}


	@Override
	public void setAvatarVisible(boolean b) {
		if (b) {
			((Node)avatarModel).setCullHint(CullHint.Never);
		} else {
			((Node)avatarModel).setCullHint(CullHint.Always);
		}

	}


	@Override
	public float getCameraHeight() {
		return 1f;
	}


	@Override
	public void showKilledAnim() {
		avatarModel.diedAnim();		
	}

}
