package com.scs.spectrumarcade.entities.gauntlet;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.scs.spectrumarcade.Globals;
import com.scs.spectrumarcade.IProcessable;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.components.ICausesHarmOnContact;
import com.scs.spectrumarcade.components.INotifiedOfCollision;
import com.scs.spectrumarcade.entities.AbstractPhysicalEntity;
import com.scs.spectrumarcade.entities.FloorOrCeiling;
import com.scs.spectrumarcade.entities.VoxelTerrainEntity;
import com.scs.spectrumarcade.jme.AbstractBillboard;

public class Ghost_Gauntlet extends AbstractPhysicalEntity implements ICausesHarmOnContact, INotifiedOfCollision, IProcessable {

	private static final float WIDTH = 0.5f;
	private static final float HEIGHT = 1f;
	
	private Vector3f turnDir = new Vector3f();
	private AbstractBillboard ab;
	
	public Ghost_Gauntlet(SpectrumArcade _game, float x, float z) {
		super(_game, "Ghost");

		Box box = new Box(WIDTH/2, HEIGHT/2, WIDTH/2);
		Geometry geometry = new Geometry("GhostBox", box);
		//geometry.setShadowMode(ShadowMode.CastAndReceive);
		//JMEModelFunctions.setTextureOnSpatial(game.getAssetManager(), geometry, "Textures/floater.png");
		geometry.setCullHint(CullHint.Always);
		geometry.setLocalTranslation(0, .5f, 0);
		this.mainNode.attachChild(geometry);
		
		ab = new AbstractBillboard(game.getAssetManager(), "Textures/skeleton-ghost.png", WIDTH, HEIGHT);
		ab.setLocalTranslation(0, .5f, 0);
		this.mainNode.attachChild(ab);

		mainNode.setLocalTranslation(x, 0, z);
		mainNode.updateModelBound();

		srb = new RigidBodyControl(1f);
		mainNode.addControl(srb);
	}


	@Override
	public float getDamageCaused() {
		return 1;
	}


	@Override
	public void process(float tpfSecs) {
		//Globals.p("Ant pos: " + this.getMainNode().getWorldTranslation());
		if (this.getMainNode().getWorldTranslation().y < -5) {
			Globals.pe("GHOST OFF EDGE");
			this.markForRemoval();
		}

		turnTowardsPlayer();
		moveFwds();
		ab.lookAt(game.player.getMainNode().getWorldTranslation(), Vector3f.UNIT_Y);
	}


	private void moveFwds() {
		Vector3f dir = this.getMainNode().getLocalRotation().getRotationColumn(2);
		//dir.y = -.1f;
		Vector3f force = dir.mult(1);
		//Globals.p("Ant force: " + dir);
		this.srb.setLinearVelocity(force); // todo - need this every frame?
	}


	private void turnTowardsPlayer() {
		float leftDist = this.leftNode.getWorldTranslation().distance(game.player.getMainNode().getWorldTranslation()); 
		float rightDist = this.rightNode.getWorldTranslation().distance(game.player.getMainNode().getWorldTranslation()); 
		if (leftDist > rightDist) {
			turnDir.set(0, 1, 0).multLocal(0.01f);
		} else {
			turnDir.set(0, -1, 0).multLocal(0.01f);
		}
		this.srb.applyTorqueImpulse(turnDir);
	}


	@Override
	public void notifiedOfCollision(AbstractPhysicalEntity collidedWith) {
		if (collidedWith instanceof FloorOrCeiling) {
			// Do nothing
		} else if (collidedWith instanceof VoxelTerrainEntity || collidedWith instanceof Ghost_Gauntlet) {
		}
	}


}
