package com.scs.spectrumarcade.misc;

import java.io.IOException;

import com.scs.spectrumarcade.Settings;

import mygame.texturesheet.TextureSheetGenerator;

public class GenerateSATextureSheet {

	public static void main(String[] args) {
		try {
			new GenerateSATextureSheet();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public GenerateSATextureSheet() throws IOException {
		String[][] tiles = {
				{"mm_bricks.png", "redfloor_pxl.png", "conveyor_top.png", "exit.png", "androids_wall.png", "sand.jpg", "conveyor_side.png", "black.png", "gauntlet_wall1.png", "gauntlet_door.png", "grass.jpg", "cutgrass.jpg"}, 
				{"antattack.png", "splat_wall.png", "road2.png", "ericwall2.png", "minedout_cyan.png", "redfloor_udg.png", "ericouterwall.png", "white.png", "lavarock.jpg", "rock.png"},
				{"motos_cyan.png", "motos_magenta.png", "motos_white.png", "motos_yellow.png", "stockcarwall_cyan.png", "startfinish.png", "stockcarwall_cyan_transp.png"},
				{"trailblazer_cyan.png", "trailblazer_magenta.png", "trailblazer_red.png", "trailblazer_white.png", "trailblazer_yellow.png", "trailblazer_green.png", "trailblazer_blue.png"}
		}; // Keep these in the same order!

		TextureSheetGenerator gen = new TextureSheetGenerator();
		gen.generateTextureSheet("assets/Textures/blocks", tiles, Settings.TEX_PER_SHEET, 32, "sa_tiles", 4, true);

	}

}
