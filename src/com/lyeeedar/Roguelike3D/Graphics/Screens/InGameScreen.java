/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package com.lyeeedar.Roguelike3D.Graphics.Screens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.esotericsoftware.tablelayout.Cell;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEffect;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEmitter;
import com.lyeeedar.Roguelike3D.Roguelike3DGame;
import com.lyeeedar.Roguelike3D.Roguelike3DGame.GameScreen;
import com.lyeeedar.Roguelike3D.Game.GameData;
import com.lyeeedar.Roguelike3D.Game.GameObject;
import com.lyeeedar.Roguelike3D.Game.Actor.GameActor;
import com.lyeeedar.Roguelike3D.Game.Level.LevelGraphics;
import com.lyeeedar.Roguelike3D.Game.LevelObjects.LevelObject;
import com.lyeeedar.Roguelike3D.Game.Spell.Spell;
import com.lyeeedar.Roguelike3D.Graphics.Lights.LightManager.LightQuality;
import com.lyeeedar.Roguelike3D.Graphics.Models.VisibleObject;
import com.lyeeedar.Roguelike3D.Graphics.Renderers.DeferredRenderer;
import com.lyeeedar.Roguelike3D.Graphics.Renderers.ForwardRenderer;

public class InGameScreen extends AbstractScreen {
	
	public boolean paused = false;
	Texture pausedTint;
	
	public static final int VIEW_DISTANCE = 100;
	public static final int ACTIVATE_DISTANCE = 100;
	public static final boolean SHOW_COLLISION_BOX = false;
	public static final int MAP_WIDTH = 100;
	public static final int MAP_HEIGHT = 100;
	public static final int MAP_X = 10;
	public static final int MAP_Y = 10;

	Texture crosshairs;
	Sprite arrow;
	
	float time = 0;
	int particleNum = 0;
	int drawnParticleNum = 0;
	ArrayList<ParticleEmitter> visibleEmitters = new ArrayList<ParticleEmitter>();
	
	public InGameScreen(Roguelike3DGame game) {
		super(game);
	}

	@Override
	public void drawModels(float delta) {

		if (GameData.skyBox != null) GameData.skyBox.render(cam);
		
		renderer.begin();

		for (VisibleObject vo : GameData.levelGraphics.graphics)
		{
			vo.render(renderer);
		}
		
		GameData.level.render(renderer, cam, visibleEmitters);
		
		renderer.end(GameData.lightManager);
	}

	@Override
	public void drawTransparent(float delta) {
		particleNum = 0;
		
		for (Spell s : GameData.spells)
		{
			s.particleEffect.getVisibleEmitters(visibleEmitters, cam);
		}
		
		Collections.sort(visibleEmitters, ParticleEmitter.getComparator());
		
		ParticleEmitter.begin(cam);
		for (ParticleEmitter p : visibleEmitters)
		{
			particleNum += p.getActiveParticles();
			p.render();
		}
		ParticleEmitter.end();
		visibleEmitters.clear();
		
		time -= delta;
		if (time < 0)
		{
			System.out.println("Java Heap Size: "+Gdx.app.getJavaHeap()/1000000+"mb");
			System.out.println("Visible Particles: "+particleNum);
			System.out.println("Frame Time: "+Gdx.graphics.getRawDeltaTime());
			time = 1;
			drawnParticleNum = particleNum;
		}
	}

	@Override
	public void drawOrthogonals(float delta) {
		
		spriteBatch.begin();
		//Table.drawDebug(stage);
		if (paused)
		{
			spriteBatch.draw(pausedTint, 0, 0, screen_width, screen_height);	
			
			int x = (int)( ( (GameData.player.position.x / 10f) + 0.5f) * LevelGraphics.STEP );
			int y = (int)( ( (GameData.player.position.z / 10f) + 0.5f) * LevelGraphics.STEP );
			
			spriteBatch.draw(GameData.levelGraphics.map, MAP_X, MAP_Y, MAP_WIDTH*2, MAP_HEIGHT*2,
					x-MAP_WIDTH, y-MAP_HEIGHT, MAP_WIDTH*2, MAP_HEIGHT*2,
					false, false);
			
			// Work out angle
			float angle = 90 * GameData.player.rotation.x;
			
			if (GameData.player.rotation.z > 0)
			{
				angle = 180+angle;
			}
			else
			{
				angle = 0-angle;
			}
			
			arrow.setRotation(angle);
			arrow.setPosition(MAP_WIDTH+MAP_X, MAP_HEIGHT+MAP_Y);
			arrow.draw(spriteBatch);
		}
		else
		{			
			spriteBatch.draw(crosshairs, screen_width/2f, screen_height/2f);
			if (activatePrompt != null) font.draw(spriteBatch, activatePrompt, screen_width/2f, (screen_height/2f)-40);
		}
		
		font.draw(spriteBatch, desc, 300, 20);
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, screen_height-20);
		font.draw(spriteBatch, "Render Type: " + getRenderType(), 20, screen_height-40);
		font.draw(spriteBatch, "Visible Particles: " + drawnParticleNum, 20, screen_height-60);
		
		spriteBatch.end();
		stage.draw();
	}
	
	public String getRenderType()
	{
		if (GameData.lightQuality == LightQuality.FORWARD_VERTEX)
		{
			return "Forward";
		}
		else if (DeferredRenderer.BUFFER == 0)
		{
			return "Deferred - Final";
		}
		else if (DeferredRenderer.BUFFER == 1)
		{
			return "Deferred - Geometry";
		}
		else if (DeferredRenderer.BUFFER == 2)
		{
			return "Deferred - Normals";
		}
		else if (DeferredRenderer.BUFFER == 3)
		{
			return "Deferred - Depth";
		}
		else if (DeferredRenderer.BUFFER == 4)
		{
			return "Deferred - Lighting";
		}
		else
		{
			return "";
		}
	}
	
	int count = 1;
	float dist = VIEW_DISTANCE;
	float tempdist = 0;
	//GameObject lookedAtObject = null;
	StringBuilder desc = new StringBuilder();
	String activatePrompt = null;
	
	Ray ray = new Ray(new Vector3(), new Vector3());
	boolean tabCD = false;
	boolean cd1 = false;
	boolean cd2 = false;
	boolean cdPlus = false;
	boolean cdMinus = false;
	
	float activateCD = 0;
	@Override
	public void update(float delta) {
		activateCD -= delta;
		if (!paused)
		{
			GameData.level.update(delta, cam);
			
			Iterator<Spell> spells = GameData.spells.iterator();
			while (spells.hasNext())
			{
				Spell s = spells.next();
				boolean dispose = s.update(delta, cam);
				
				if (dispose) spells.remove();
			}
			
			if (GameData.player == null) return;
			cam.position.set(GameData.player.position).add(GameData.player.offsetPos);
			cam.direction.set(GameData.player.rotation).add(GameData.player.offsetRot);
			cam.update();
		}
		
		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) game.switchScreen(GameScreen.MAINMENU);
		if (Gdx.input.isKeyPressed(Keys.TAB) && !tabCD) 
		{
			if (paused)
			{
				paused = false;
				Gdx.input.setCursorCatched(true);
				tabCD = true;
			}
			else
			{
				paused = true;
				Gdx.input.setCursorCatched(false);
				Gdx.input.setCursorPosition(screen_width/2, screen_height/2);
				tabCD = true;
			}
			
		}
		else if (!Gdx.input.isKeyPressed(Keys.TAB))
		{
			tabCD = false;
		}
		
		if (Gdx.input.isKeyPressed(Keys.NUM_1) && !cd1) 
		{
			DeferredRenderer.BUFFER++;
			if (DeferredRenderer.BUFFER == 5) DeferredRenderer.BUFFER = 0;
			cd1 = true;
			
		}
		else if (!Gdx.input.isKeyPressed(Keys.NUM_1))
		{
			cd1 = false;
		}
		
		if (paused)
		{
			Ray ray2 = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
			dist = cam.far*cam.far;
			ray.set(ray2);
			
			activatePrompt = null;
		}
//		else
//		{	
//			ray.origin.set(GameData.player.position);
//			ray.direction.set(GameData.player.rotation);
//			dist = VIEW_DISTANCE;
//			
//			activatePrompt = getActivatePrompt(dist, ray);
//		}
//		
//		getDescription(dist, ray, paused);
//		
//		if (!paused && GameData.controls.getActivate() && activateCD < 0)
//		{
//			ray.origin.set(GameData.player.position);
//			ray.direction.set(GameData.player.rotation);
//			dist = ACTIVATE_DISTANCE;
//			
//			GameObject go = GameData.level.getClosestActor(ray, dist, GameData.player.UID, tmpVec);
//			
//			if (go != null)
//			{			
//				System.out.println("actor collision");
//				dist = tmpVec.dst2(ray.origin);
//			}
//
//			GameObject go2 = GameData.level.getClosestLevelObject(ray, dist, GameData.player.UID, tmpVec);
//			
//			if (go2 != null) 
//			{
//				if (go != null)	go.activate();
//				else go2.activate();
//			}
//			else
//			{
//				if (go != null)	go.activate();
//			}
//			
//			activateCD = 1;
//		}
		
		stage.act(delta);
	}
	
//	public void getDescription(float dist, Ray ray, boolean longDesc)
//	{
//		desc.delete(0, desc.length());
//		desc.append("There is nothing there but empty space.");
//		
//		GameObject go = GameData.level.getClosestActor(ray, dist, GameData.player.UID, tmpVec);
//		
//		if (go != null)
//		{
//			desc.delete(0, desc.length());
//			if (longDesc) {
//				desc.append(go.getRadius());
//			}
//			else
//			{
//				desc.append(go.getShortDesc());
//			}
//			
//			dist = tmpVec.dst2(ray.origin);
//		}
//
//		go = GameData.level.getClosestLevelObject(ray, dist, GameData.player.UID, tmpVec);
//		
//		if (go != null)
//		{
//			desc.delete(0, desc.length());
//			if (longDesc) {
//				desc.append(go.getRadius());
//			}
//			else
//			{
//				desc.append(go.getShortDesc());
//			}
//			
//			dist = tmpVec.dst2(ray.origin);
//		}
//		
//		dist = GameData.level.getDescription(ray, dist, desc, paused);
//	}
//	
//	public String getActivatePrompt(float dist, Ray ray)
//	{
//		String desc = null;
//		
//		GameObject go = GameData.level.getClosestActor(ray, dist, GameData.player.UID, tmpVec);
//		
//		if (go != null)
//		{
//
//			desc = go.getActivatePrompt();
//			
//			dist = tmpVec.dst2(ray.origin);
//		}
//
//		go = GameData.level.getClosestLevelObject(ray, dist, GameData.player.UID, tmpVec);
//		
//		if (go != null)
//		{
//			desc = go.getActivatePrompt();
//			
//			dist = tmpVec.dst2(ray.origin);
//		}
//		
//		return desc;
//	}

	Table table;
	@Override
	public void create() {
		
		GameData.init(game);

		crosshairs = new Texture(Gdx.files.internal("data/textures/crosshairs.png"));
		arrow = new Sprite(new Texture(Gdx.files.internal("data/textures/arrow.png")));
		
		pausedTint = new Texture(Gdx.files.internal("data/textures/pausedScreenTint.png"));

		if (GameData.isAndroid) {
			
			stage.addActor(GameData.controls.getMovePad());
			stage.addActor(GameData.controls.getLookPad());
		}
	}

	@Override
	public void hide() {
		Gdx.input.setCursorCatched(false);
		Gdx.input.setInputProcessor(null);
	}
	
	@Override
	public void show()
	{
		Gdx.input.setCursorCatched(true);
		if (renderer != null) renderer.dispose();
		if (GameData.lightQuality == LightQuality.FORWARD_VERTEX)
		{
			renderer = new ForwardRenderer();
		}
		else if (GameData.lightQuality == LightQuality.DEFERRED)
		{
			renderer = new DeferredRenderer();
		}
		else
		{
			System.err.println("ARRRRRRRGGGGGHHHHHHH!");
		}
		renderer.createShader(GameData.lightManager);
		renderer.updateResolution();
		renderer.cam = cam;
		
		stage.setViewport(GameData.resolution[0], GameData.resolution[1], false);
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void superDispose() {
		pausedTint.dispose();
		crosshairs.dispose();
	}

}
