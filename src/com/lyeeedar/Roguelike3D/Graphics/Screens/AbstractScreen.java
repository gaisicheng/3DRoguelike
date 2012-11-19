package com.lyeeedar.Roguelike3D.Graphics.Screens;

import java.awt.Font;
import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ArrayMap;
import com.lyeeedar.Roguelike3D.Roguelike3DGame;
import com.lyeeedar.Roguelike3D.Game.GameData;
import com.lyeeedar.Roguelike3D.Game.GameObject;
import com.lyeeedar.Roguelike3D.Graphics.Models.FullscreenQuad;
import com.lyeeedar.Roguelike3D.Graphics.Renderers.PrototypeRendererGL20;
 

public abstract class AbstractScreen implements Screen{
	
	int screen_width;
	int screen_height;

	protected final Roguelike3DGame game;

	protected final SpriteBatch spriteBatch;
	protected BitmapFont font;
	protected final Stage stage;

	protected PrototypeRendererGL20 protoRenderer;
	protected FrameBuffer frameBuffer;
	protected FullscreenQuad fullscreenQuad;
	protected ShaderProgram shader;
	
	PerspectiveCamera cam;
	
	FPSLogger fps = new FPSLogger();

	public AbstractScreen(Roguelike3DGame game)
	{
		this.game = game;
		
		font = new BitmapFont(Gdx.files.internal("data/skins/default.fnt"), false);
		spriteBatch = new SpriteBatch();
		stage = new Stage(0, 0, true, spriteBatch);
		
		protoRenderer = new PrototypeRendererGL20(GameData.lightManager);
		//frameBuffer = new FrameBuffer(Format.RGBA4444, 800, 600, true);
		frameBuffer = new FrameBuffer(Format.RGB888, 800, 600, true);
		
		fullscreenQuad = new FullscreenQuad();
		shader = new ShaderProgram(
				Gdx.files.internal("data/shaders/fullscreen/texture.vertex.glsl"),
				Gdx.files.internal("data/shaders/fullscreen/texture.fragment.glsl")
				);
		if (!shader.isCompiled()) Gdx.app.log("Problem loading shader:", shader.getLog());
	}

	@Override
	public void render(float delta) {
		
		update(delta);
	
		frameBuffer.begin();
		Gdx.gl20.glViewport(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());
		
		Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		Gdx.gl20.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl20.glCullFace(GL20.GL_BACK);
		
		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glDepthMask(true);

		draw(delta);
		frameBuffer.end();

		Gdx.graphics.getGL20().glViewport(0, 0, 800, 600);
		
		Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glDepthMask(true);
		
		shader.begin();
		frameBuffer.getColorBufferTexture().bind();
		//shader.setUniformMatrix("u_mvp", cam.combined.cpy().mul());
		fullscreenQuad.render(shader);
		shader.end();
		
		Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glDepthMask(false);
		
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
		stage.draw();
		
        fps.log();
		
	}

	@Override
	public void resize(int width, int height) {
		screen_width = width;
		screen_height = height;

		float aspectRatio = (float) width / (float) height;
        //cam = new PerspectiveCamera(90, 2f * aspectRatio, 2f);
        cam = new PerspectiveCamera(90, width, height);
        frameBuffer = new FrameBuffer(Format.RGB888, width, height, true);
        cam.near = 0.01f;
        cam.far = 200;
        protoRenderer.cam = cam;
		
		stage.setViewport( width, height, true );
	}

	@Override
	public void dispose() {
		protoRenderer.dispose();

		spriteBatch.dispose();
		font.dispose();
		stage.dispose();

	}
	
	@Override
	public void show()
	{
		
	}
	
	public abstract void create();
	public abstract void draw(float delta);
	public abstract void update(float delta);

}
