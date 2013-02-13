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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.lyeeedar.Roguelike3D.Roguelike3DGame;
import com.lyeeedar.Roguelike3D.Game.GameData;
import com.lyeeedar.Roguelike3D.Game.GameStats;
import com.lyeeedar.Roguelike3D.Game.Item.Component;
import com.lyeeedar.Roguelike3D.Game.Item.Component.Component_Type;
import com.lyeeedar.Roguelike3D.Game.Item.Recipe;
import com.lyeeedar.Roguelike3D.Game.Level.XML.RecipeReader;
import com.lyeeedar.Roguelike3D.Roguelike3DGame.GameScreen;

public class MainMenuScreen extends UIScreen {
	
	Table table;

	public MainMenuScreen(Roguelike3DGame game) {
		super(game);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void create() {

		Label lblTitle = new Label("EtDotR", skin);
		
		TextButton btnContinue = new TextButton("Continue", skin);
		btnContinue.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				GameData.load();
				return false;
			}
		});
		
		TextButton btnNewGame = new TextButton("New Game", skin);
		btnNewGame.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				GameData.init(game);
				return false;
			}
		});

		
		TextButton btnOptions = new TextButton("Options", skin);
		btnOptions.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				game.switchScreen(GameScreen.OPTIONS);
				return false;
			}
		});
		
		TextButton btnCredits = new TextButton("Credits", skin);
		btnCredits.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				game.switchScreen(GameScreen.CREDITS);
				return false;
			}
		});
		
		TextButton btnExit = new TextButton("Exit", skin);
		btnExit.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				game.ANNIHALATE();
				return false;
			}
		});
		
		TextButton btnTest = new TextButton("Test", skin);
		btnTest.addListener(new InputListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				RecipeReader reader = new RecipeReader("sword");
				Recipe recipe;
				
				for (int i = 0; i < 10; i++)
				{
					recipe = new Recipe(reader);
					recipe.rarity += i;
					GameStats.addRecipe(recipe);
					recipe = new Recipe(reader);
					recipe.rarity += i;
					GameStats.addRecipe(recipe);
					recipe = new Recipe(reader);
					recipe.rarity += i;
					GameStats.addRecipe(recipe);
					recipe = new Recipe(reader);
					recipe.rarity += i;
					GameStats.addRecipe(recipe);
					
					recipe = new Recipe(reader);
					recipe.rarity += i;
					GameStats.addRecipe(recipe);
				}
				
				for (int i = 1; i < 11; i++)
				{
					Component c = new Component(Component_Type.CLAW, "Claw thingy 1"+i, i, i, "desc", i, 1, i, i, GameData.getElementMap(), "grasping-claws");
					GameStats.addComponent(c);
					c = new Component(Component_Type.CLAW, "Claw thingy 2"+i, i, i, "desc", i, 2, i, i, GameData.getElementMap(), "grasping-claws");
					GameStats.addComponent(c);
					c = new Component(Component_Type.CLAW, "Claw thingy 3"+i, i, i, "desc", i, 3, i, i, GameData.getElementMap(), "grasping-claws");
					GameStats.addComponent(c);
				}
				
				Component c = new Component(Component_Type.TOOTH, "Tooth thingy", 1, 1, "desc", 1, 50, 10, 10, GameData.getElementMap(), "grasping-claws");
				GameStats.addComponent(c);
				
				game.switchScreen(GameScreen.RECIPES);
				return false;
			}
		});

		table = new Table();
		table.debug();
		table.add(lblTitle).center().padBottom(50);
		table.row();
		table.add(btnContinue).width(300).height(50).padBottom(25);
		table.row();
		table.add(btnNewGame).width(300).height(50).padBottom(25);
		table.row();
		table.add(btnOptions).width(300).height(50).padBottom(25);
		table.row();
		table.add(btnCredits).width(300).height(50).padBottom(25);
		table.row();
		table.add(btnExit).width(300).height(50).padBottom(25);
		table.row();
		table.add(btnTest).width(300).height(50).padBottom(25);


		table.setFillParent(true);
		stage.addActor(table);	
	}

	@Override
	public void drawModels(float delta) {
	}

	@Override
	public void drawDecals(float delta) {
	}

	@Override
	public void drawOrthogonals(float delta) {
		
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void superDispose() {
	}

	@Override
	protected void createSuper() {
	}

	@Override
	protected void superSuperDispose() {
	}

}
