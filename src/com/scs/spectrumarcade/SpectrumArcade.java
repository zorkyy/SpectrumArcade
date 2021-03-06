package com.scs.spectrumarcade;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.prefs.BackingStoreException;

import javax.swing.JOptionPane;

import com.atr.jme.font.asset.TrueTypeLoader;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.LightList;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.scene.Spatial;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.scs.spectrumarcade.CameraSystem.View;
import com.scs.spectrumarcade.abilities.IAbility;
import com.scs.spectrumarcade.components.IAvatar;
import com.scs.spectrumarcade.components.IEntity;
import com.scs.spectrumarcade.components.IProcessable;
import com.scs.spectrumarcade.entities.AbstractPhysicalEntity;
import com.scs.spectrumarcade.levels.AntAttackLevel;
import com.scs.spectrumarcade.levels.ArcadeRoom;
import com.scs.spectrumarcade.levels.ILevelGenerator;

import ssmith.util.FixedLoopTime;

public class SpectrumArcade extends SimpleApplication implements ActionListener, PhysicsCollisionListener {

	private static final int MODE_GAME = 0;
	private static final int MODE_RETURNING_TO_ARCADE = 1;

	// Key codes
	public static final String KEY_RECORD = "record";
	public static final String KEY_RETURN_TO_ARCADE = "return";

	public static final Random rnd = new Random();

	public List<IEntity> entities = new ArrayList<IEntity>();
	public List<IProcessable> entitiesForProcessing = new ArrayList<IProcessable>();
	private List<IEntity> entitiesToAdd = new LinkedList<>();
	private List<IEntity> entitiesToRemove = new LinkedList<>();
	public BulletAppState bulletAppState;

	public AbstractPhysicalEntity player;

	private SpotLight spotlight;
	private HUD hud;
	private boolean game_over = false;
	private boolean player_won = false;
	private VideoRecorderAppState video_recorder;
	private int mode = MODE_GAME;

	public DirectionalLight sun;
	public GameData gameData;

	private HashMap<Integer, IAbility> abilities = new HashMap<>();
	private boolean[] abilityActivated = new boolean[3];

	private ILevelGenerator currentLevel;
	private Class<? extends ILevelGenerator> nextLevel;
	private Thread loadingLevelThread = null;

	private int nextLevelNum;
	public CameraSystem camSys;
	protected FixedLoopTime loopTimer = new FixedLoopTime(5);

	public boolean playerDead = false;
	private long restartPlayerAt;

	public static void main(String[] args) {
		try {
			if (Settings.RELEASE_MODE) {
				String msg = "Please note this game is very early in development!  It will probably frequently crash,\nand the games are unfinished and poorly balanced with few features.  But apart from that, have fun!";
				JOptionPane.showMessageDialog(null, msg);
			}

			AppSettings settings = new AppSettings(true);
			try {
				settings.load(Settings.NAME);
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
			settings.setTitle(Settings.NAME + " (v" + Settings.VERSION + ")");
			if (Settings.SHOW_LOGO) {
				settings.setSettingsDialogImage("todo");
			} else {
				//settings.setSettingsDialogImage(null);
			}

			SpectrumArcade app = new SpectrumArcade();
			app.setSettings(settings);
			app.start();

			/*if (Settings.RECORD_VID) {
				System.out.println("Video saved at " + video.getCanonicalPath());
				System.out.println("Audio saved at " + audio.getCanonicalPath());
			}*/

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e);
		}

	}


	public void simpleInitApp() {
		//if (Settings.LOAD_FROM_JAR) {
		//assetManager.registerLocator("assets/", ClasspathLocator.class);
		/*} else {
			assetManager.registerLocator("assets/", FileLocator.class);
		}*/

		/*
		assetManager.registerLocator("./", FileLocator.class); // default
		assetManager.registerLocator("../", FileLocator.class); // default
		assetManager.registerLocator("../assets/", FileLocator.class); // default
		assetManager.registerLocator("./assets/", FileLocator.class); // default
		assetManager.registerLocator("assets/Textures/", FileLocator.class);
		 */
		getAssetManager().registerLoader(TrueTypeLoader.class, "ttf");

		cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, Settings.CAM_DIST);

		// Set up Physics
		bulletAppState = new BulletAppState();//PhysicsSpace.BroadphaseType.DBVT);
		//bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);
		//bulletAppState.getPhysicsSpace().enableDebug(assetManager);

		if (Settings.FREE_CAM) {
			Globals.p("FREE CAM ENABLED");
			this.flyCam.setMoveSpeed(12f);
		} else {
			setUpKeys();
		}
		setUpLight();

		bulletAppState.getPhysicsSpace().addCollisionListener(this);

		final int SHADOWMAP_SIZE = 1024*2;
		DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(getAssetManager(), SHADOWMAP_SIZE, 2);
		dlsr.setLight(sun);
		this.viewPort.addProcessor(dlsr);

		gameData = new GameData();

		hud = new HUD(this, cam, ColorRGBA.Green);
		this.guiNode.attachChild(hud);

		stateManager.getState(StatsAppState.class).toggleStats(); // Turn off stats
/*
		{
			// DepthOfFieldFilter
			FilterPostProcessor fpp2 = new FilterPostProcessor(getAssetManager());
			DepthOfFieldFilter dff = new DepthOfFieldFilter();
			dff.setFocusDistance(2f);
			dff.setFocusRange(20f);
			fpp2.addFilter(dff);
			viewPort.addProcessor(fpp2);
		}
*/
		if (Settings.RELEASE_MODE) {
			this.setNextLevel(ArcadeRoom.class, -1);
		} else {
			this.setNextLevel(AntAttackLevel.class, 1); // TrailblazerLevel // AntAttackLevel // ManicMinerCentralCavern // AndroidsLevel
			// StockCarChamp3DLevel();//GauntletLevel();//ArcadeRoom();//MotosLevel();//MinedOutLevel(); //TurboEspritLevel();//SplatLevel();//EricAndTheFloatersLevel();//(); //
			// AndroidsLevel // KrakatoaLevel // TomahawkLevel
		}

		//File video, audio;
		if (Settings.RECORD_VID) {
			/*app.setTimer(new IsoTimer(60));
			video = File.createTempFile("JME-video", ".avi");
			audio = File.createTempFile("JME-audio", ".wav");
			Capture.captureVideo(app, video);
			Capture.captureAudio(app, audio);*/
			VideoRecorderAppState video_recorder = new VideoRecorderAppState();
			this.getStateManager().attach(video_recorder);

		}

		loopTimer.start();
	}


	private void setUpLight() {
		// Remove existing lights
		this.rootNode.getWorldLightList().clear(); //this.rootNode.getWorldLightList().size();
		LightList list = this.rootNode.getWorldLightList();
		for (Light it : list) {
			this.rootNode.removeLight(it);
		}

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White);
		rootNode.addLight(al);

		sun = new DirectionalLight();
		sun.setColor(ColorRGBA.White);
		sun.setDirection(new Vector3f(-.5f, -1f, -.5f).normalizeLocal());
		rootNode.addLight(sun);

		/*this.spotlight = new SpotLight();
			spotlight.setColor(ColorRGBA.White.mult(3f));
			spotlight.setSpotRange(10f);
			spotlight.setSpotInnerAngle(FastMath.QUARTER_PI / 8);
			spotlight.setSpotOuterAngle(FastMath.QUARTER_PI / 2);
			rootNode.addLight(spotlight);
		 */
	}


	private void setUpKeys() {
		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("Fwd", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("Backwards", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_E));
		inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("Test", new KeyTrigger(KeyInput.KEY_T));
		inputManager.addMapping("Cam1", new KeyTrigger(KeyInput.KEY_1));
		inputManager.addMapping("Cam2", new KeyTrigger(KeyInput.KEY_2));
		inputManager.addMapping("Cam3", new KeyTrigger(KeyInput.KEY_3));
		inputManager.addMapping("Cam4", new KeyTrigger(KeyInput.KEY_4));
		//inputManager.addMapping(KEY_RECORD, new KeyTrigger(KeyInput.KEY_R));
		inputManager.addMapping(KEY_RETURN_TO_ARCADE, new KeyTrigger(KeyInput.KEY_X));
		inputManager.addMapping("R", new KeyTrigger(KeyInput.KEY_R));

		inputManager.addListener(this, "Left");
		inputManager.addListener(this, "Right");
		inputManager.addListener(this, "Fwd");
		inputManager.addListener(this, "Backwards");
		inputManager.addListener(this, "Up");
		inputManager.addListener(this, "Down");
		inputManager.addListener(this, "Jump");
		inputManager.addListener(this, "Test");
		inputManager.addListener(this, "Cam1");
		inputManager.addListener(this, "Cam2");
		inputManager.addListener(this, "Cam3");
		inputManager.addListener(this, "Cam4");
		inputManager.addListener(this, "R");
		inputManager.addListener(this, KEY_RETURN_TO_ARCADE);

		inputManager.addMapping("Ability1", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
		inputManager.addListener(this, "Ability1");

	}


	@Override
	public void simpleUpdate(float tpfSecs) {
		if (tpfSecs > 1f) {
			tpfSecs = 1f;
		}

		if (this.loadingLevelThread != null) {
			if (!this.loadingLevelThread.isAlive()) {
				loadingLevelThread = null;
				beginLevel();
			}
			return;
		}

		addAndRemoveEntities();

		if (mode == MODE_RETURNING_TO_ARCADE) {
			this.cam.setLocation(cam.getLocation().add(0, tpfSecs*20, 0));
			this.cam.lookAt(this.player.getMainNode().getWorldTranslation(), Vector3f.UNIT_Y);
			if (cam.getLocation().y > 30) {
				mode = MODE_GAME;
			}
		} else {
			// Start a new level?
			if (this.nextLevel != null) {
				try {
					if (currentLevel != null) {
						currentLevel.remove();
					}
					currentLevel = nextLevel.newInstance();
					currentLevel.setGame(this);
					this.startNewLevel(this.nextLevelNum);
				} catch (Exception e) {
					throw new RuntimeException("Error", e);
				}
				this.nextLevel = null;
				return;
			}

			if (this.playerDead) {
				if (this.restartPlayerAt < System.currentTimeMillis()) {
					Globals.p("Restarting player");
					this.playerDead = false;
					IAvatar a = (IAvatar)player;
					a.warp(currentLevel.getAvatarStartPos());
					a.clearForces();
				}
			} else {
				for (int i=1 ; i<=2 ; i++) {
					if (this.abilityActivated[i]) {
						activateAbility(i);
					}
				}
			}
			currentLevel.process(tpfSecs);

			for(IProcessable ip : this.entitiesForProcessing) {
				ip.process(tpfSecs);
			}

			hud.processByClient(tpfSecs);

			if (!Settings.FREE_CAM) {
				if (player != null) {
					camSys.process(cam, player);
				}
			}

			loopTimer.waitForFinish();
			loopTimer.start();

		}

		if (spotlight != null) {
			this.spotlight.setPosition(cam.getLocation());
			this.spotlight.setDirection(cam.getDirection());
		}
	}


	private void startNewLevel(int levelNum) throws FileNotFoundException, IOException, URISyntaxException {
		// Clear previous level
		this.getBulletAppState().getPhysicsSpace().removeAll(this.getRootNode());
		this.rootNode.detachAllChildren();
		this.guiNode.detachAllChildren();
		this.entities.clear();
		this.entitiesToAdd.clear();
		this.entitiesToRemove.clear();
		entitiesForProcessing.clear();

		this.guiNode.attachChild(hud);

		//loadingLevel = true;
		final SpectrumArcade ths = this; 
		this.loadingLevelThread = new Thread("LevelLoader") {

			@Override
			public void run() {
				try {
					Globals.p("Loading level...");
					currentLevel.generateLevel(ths, levelNum);
					Globals.p("Finished Loading level");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		loadingLevelThread.start();
	}


	private void beginLevel() {
		player = (AbstractPhysicalEntity)currentLevel.createAndPositionAvatar();
		this.addEntity((AbstractPhysicalEntity)player);
		//loadingLevel = false;
		this.getViewPort().setBackgroundColor(currentLevel.getBackgroundColour());

		if (!Settings.FREE_CAM) {
			camSys = new CameraSystem(this);
			currentLevel.setupCameraSystem(camSys);
		} else {
			this.getCamera().setLocation(this.player.getMainNode().getWorldTranslation());
		}

		IAvatar a = (IAvatar)player;

		// Default to 3rd person
		if (!Settings.FREE_CAM) {
			this.camSys.setView(View.Third);
			a.setAvatarVisible(true);
		}
	}


	public void addEntity(IEntity e) {
		//if (!loadingLevel) {
		this.entitiesToAdd.add(e);
		/*} else {
			this.actuallyAddEntity(e); 
		}*/
	}


	private void addAndRemoveEntities() {
		while (this.entitiesToRemove.size() > 0) {
			IEntity e = this.entitiesToRemove.remove(0);
			this.actuallyRemoveEntity(e);
		}

		while (this.entitiesToAdd.size() > 0) {
			IEntity e = this.entitiesToAdd.remove(0);
			//this.entities.add(e);
			actuallyAddEntity(e);
		}
	}


	public void onAction(String binding, boolean isPressed, float tpf) {
		if (player != null) {
			IAvatar a = (IAvatar)player;
			// DO NOT DO ANY MAJOR ACTIONS IN THIS, DO THEM IN THE MAIN THREAD!
			a.onAction(binding, isPressed, tpf);

			if (binding.equals("Ability1")) {
				if (this.game_over == false) {
					abilityActivated[1] = isPressed;
				}
			} else if (binding.equals("Cam1")) {
				this.camSys.setView(View.First);
				a.setAvatarVisible(false);
				Globals.p("Setting view to 1");
			} else if (binding.equals("Cam2")) {
				this.camSys.setView(View.Third);
				a.setAvatarVisible(true);
				Globals.p("Setting view to 2");
			} else if (binding.equals("Cam3")) {
				this.camSys.setView(View.TopDown);
				a.setAvatarVisible(true);
				Globals.p("Setting view to 3");
			} else if (binding.equals("Cam4")) {
				this.camSys.setView(View.Cinema);
				a.setAvatarVisible(true);
				Globals.p("Setting view to 4");
			}
		}

		if (binding.equals(KEY_RECORD)) {
			if (isPressed) {
				if (video_recorder == null) {
					//log("RECORDING VIDEO");
					video_recorder = new VideoRecorderAppState();
					stateManager.attach(video_recorder);
					/*if (Statics.MUTE) {
						log("Warning: sounds are muted");
					}*/
				} else {
					//log("STOPPED RECORDING");
					stateManager.detach(video_recorder);
					video_recorder = null;
				}
			}
		} else if (binding.equals(KEY_RETURN_TO_ARCADE)) {
			if (mode != MODE_RETURNING_TO_ARCADE && this.currentLevel instanceof ArcadeRoom == false) {
				mode = MODE_RETURNING_TO_ARCADE;
				Vector3f pos = this.getCamera().getLocation().clone();
				if (pos.y < 0) {
					pos.y = 0; // In case we've fallen off edge
				}
				this.getCamera().setLocation(pos);
				this.setNextLevel(ArcadeRoom.class, -1);
			}
		}

	}


	public void setNextLevel(Class<? extends ILevelGenerator> clazz, int levelNum) {
		this.saveLevel(clazz, levelNum);

		this.nextLevel = clazz;
		this.nextLevelNum = levelNum;
	}


	public void saveLevel(Class<? extends ILevelGenerator> clazz, int levelNum) {
		gameData.setLevel(clazz, levelNum);
	}


	/**
	 * Get whether an entity is within our field of view.
	 * @param entity
	 * @return
	 */
	public FrustumIntersect getInsideOutside(AbstractPhysicalEntity entity) {
		FrustumIntersect insideoutside = cam.contains(entity.getMainNode().getWorldBound());
		return insideoutside;
	}


	@Override
	public void collision(PhysicsCollisionEvent event) {
		if (mode == MODE_GAME) {
			//System.out.println(event.getObjectA().getUserObject().toString() + " collided with " + event.getObjectB().getUserObject().toString());

			Spatial ga = (Spatial)event.getObjectA().getUserObject(); 
			AbstractPhysicalEntity a = ga.getUserData(Settings.ENTITY);
			while (a == null && ga.getParent() != null) {
				//Globals.p("Getting parent of " + ga);
				ga = ga.getParent();
				a = ga.getUserData(Settings.ENTITY);
			}
			if (a == null) {
				//throw new RuntimeException("Geometry " + ga.getName() + " has no entity");
				return;
			}

			Spatial gb = (Spatial)event.getObjectB().getUserObject(); 
			AbstractPhysicalEntity b = gb.getUserData(Settings.ENTITY);
			while (b == null && gb.getParent() != null) {
				//Globals.p("Getting parent of " + gb);
				gb = gb.getParent();
				b = gb.getUserData(Settings.ENTITY);
			}
			if (b == null) {
				//throw new RuntimeException("Geometry " + ga.getName() + " has no entity");
				return;
			}

			CollisionLogic.collision(this, a, b);
		}
	}


	public boolean isGameOver() {
		return this.game_over;
	}


	public boolean hasPlayerWon() {
		return this.player_won;
	}


	public static void p(String s) {
		System.out.println(System.currentTimeMillis() + ": " + s);
	}


	public static void pe(String s) {
		System.err.println(System.currentTimeMillis() + ": " + s);
	}


	public BulletAppState getBulletAppState() {
		return bulletAppState;
	}


	private void actuallyAddEntity(IEntity e) {
		this.entities.add(e);
		e.actuallyAdd();
		if (e instanceof IProcessable) {
			this.entitiesForProcessing.add((IProcessable)e);
		}
	}


	public void markEntityForRemoval(IEntity e) {
		this.entitiesToRemove.add(e);
	}



	public void actuallyRemoveEntity(IEntity e) {
		e.actuallyRemove();
		this.entities.remove(e);
		if (e instanceof IProcessable) {
			this.entitiesForProcessing.remove((IProcessable)e);
		}
		if (e instanceof PhysicsTickListener) {
			this.getBulletAppState().getPhysicsSpace().removeTickListener((PhysicsTickListener)e);
		}

	}


	public String getHUDText() {
		if (this.loadingLevelThread != null) {
			return "LOAD \"\"";
		} else if (mode == MODE_GAME) {
			return currentLevel.getHUDText();
		} else {
			return "C NONSENCE IN BASIC";
		}
	}


	public void playerKilled(String name) {
		if (!playerDead) {
			Globals.p("Player killed by " + name);
			playerDead = true;
			this.restartPlayerAt = System.currentTimeMillis() + 3000;

			hud.showDamageBox();

			IAvatar a = (IAvatar)player;
			a.showKilledAnim();

			this.camSys.setView(View.Third);
			a.setAvatarVisible(true);

		}

	}


	public void setAbility(int num, IAbility a) {
		this.abilities.put(num, a);
	}


	public void activateAbility(int num) {
		IAbility a = abilities.get(1);
		if (a != null) {
			a.activate();
		}
	}


}
