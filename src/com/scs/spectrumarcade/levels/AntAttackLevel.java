package com.scs.spectrumarcade.levels;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.scs.spectrumarcade.BlockCodes;
import com.scs.spectrumarcade.SpectrumArcade;
import com.scs.spectrumarcade.abilities.BombGun_AA;
import com.scs.spectrumarcade.components.IAvatar;
import com.scs.spectrumarcade.entities.FloorOrCeiling;
import com.scs.spectrumarcade.entities.HUDTextEntity;
import com.scs.spectrumarcade.entities.VoxelTerrainEntity;
import com.scs.spectrumarcade.entities.WalkingPlayer;
import com.scs.spectrumarcade.entities.antattack.AAScanner;
import com.scs.spectrumarcade.entities.antattack.Ant;
import com.scs.spectrumarcade.entities.antattack.Damsel;
import com.scs.spectrumarcade.models.GenericWalkingAvatar;

import mygame.util.Vector3Int;
import ssmith.lang.Functions;
import ssmith.lang.NumberFunctions;

public class AntAttackLevel extends AbstractLevel implements ILevelGenerator {

	public static final Vector3f exitPos = new Vector3f(54, 0, 124);

	private static int MAP_BORDER = 10;
	private static int MAP_SIZE = 128;

	private Damsel damsel;

	public AntAttackLevel() {
		super();
	}


	@Override
	public void generateLevel(SpectrumArcade game, int levelNum) throws FileNotFoundException, IOException, URISyntaxException {
		FloorOrCeiling floor = new FloorOrCeiling(game, -MAP_BORDER, 0, -MAP_BORDER, MAP_SIZE+(MAP_BORDER*2), 2, MAP_SIZE+(MAP_BORDER*2), "Textures/blocks/white.png");
		game.addEntity(floor);

		VoxelTerrainEntity terrainUDG = new VoxelTerrainEntity(game, 0f, 0f, 0f, new Vector3Int(MAP_SIZE+2, 9, MAP_SIZE+2), 16, 1f, .1f);
		game.addEntity(terrainUDG);

		String text = Functions.readAllTextFileFromJar("maps/antattack_map.txt");
		String[] lines = text.split("\n");

		for (String line : lines) {
			String[] parts = line.split(",");
			int x = Integer.parseInt(parts[0]);
			int y = Integer.parseInt(parts[1]);
			int z = Integer.parseInt(parts[2]);
			terrainUDG.addBlock_Block(new Vector3Int(x, y, z), BlockCodes.ANT_ATTACK);
		}


		// Add ants
		for (int i=0 ; i<5 ; i++) {
			int x = NumberFunctions.rnd(10, MAP_SIZE-20);
			int z = NumberFunctions.rnd(10, MAP_SIZE-20);
			Ant ant = new Ant(game, x, 9, z); // Make height unique to stop collisions at start
			game.addEntity(ant);
		}

		// Add damsel
		int x = NumberFunctions.rnd(3, MAP_SIZE-4);
		int z = NumberFunctions.rnd(3, MAP_SIZE-4);
		damsel = new Damsel(game, x, z);//54, 110);
		game.addEntity(damsel);

		HUDTextEntity be = new HUDTextEntity(game, "WELCOME TO ANTCHESTER", 72, ColorRGBA.Black, 10, game.getCamera().getHeight()-50, 5);
		game.addEntity(be);

		AAScanner scanner = new AAScanner(game, damsel);
		game.addEntity(scanner);
	}


	@Override
	public Vector3f getAvatarStartPos() {
		return new Vector3f(exitPos.x, 2f, exitPos.z);
	}


	@Override
	public IAvatar createAndPositionAvatar() {
		WalkingPlayer wp = new WalkingPlayer(game, getAvatarStartPos(), 4f, 4f, new GenericWalkingAvatar(game.getAssetManager(), "Textures/antattack/avatar_black.png"));
		game.setAbility(1, new BombGun_AA(game));
		return wp;
	}


	@Override
	public ColorRGBA getBackgroundColour() {
		return ColorRGBA.Cyan;
	}


	@Override
	public void process(float tpfSecs) {
		//Globals.p("Avatar Pos: " + game.player.getMainNode().getWorldTranslation());

	}


	@Override
	public String getHUDText() {
		if (damsel.followingPlayer) {
			return "Get back to the exit!";
		} else {
			return "Find the damsel!";
		}
	}

}
