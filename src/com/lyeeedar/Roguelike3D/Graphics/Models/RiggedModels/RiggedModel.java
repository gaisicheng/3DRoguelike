package com.lyeeedar.Roguelike3D.Graphics.Models.RiggedModels;

import java.io.Serializable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.lyeeedar.Roguelike3D.Game.GameData;
import com.lyeeedar.Roguelike3D.Game.Actor.GameActor;
import com.lyeeedar.Roguelike3D.Graphics.Colour;
import com.lyeeedar.Roguelike3D.Graphics.Lights.LightManager;
import com.lyeeedar.Roguelike3D.Graphics.Materials.Material;
import com.lyeeedar.Roguelike3D.Graphics.Materials.MaterialAttribute;
import com.lyeeedar.Roguelike3D.Graphics.Materials.TextureAttribute;
import com.lyeeedar.Roguelike3D.Graphics.Models.Shapes;
import com.lyeeedar.Roguelike3D.Graphics.Models.StillSubMesh;
import com.lyeeedar.Roguelike3D.Graphics.Models.SubMesh;
import com.lyeeedar.Roguelike3D.Graphics.ParticleEffects.ParticleEmitter;
import com.lyeeedar.Roguelike3D.Graphics.Renderers.ForwardRenderer;
import com.lyeeedar.Roguelike3D.Graphics.Renderers.Renderer;

public class RiggedModel implements Serializable {

	private static final long serialVersionUID = -3089869808778076973L;
	public Material[] materials;
	public RiggedModelNode rootNode;
	
	static ShaderProgram shader;
	
	public RiggedModel(RiggedModelNode node, Material[] materials) {
		this.rootNode = node;
		this.materials = materials;
		
		if (shader == null) {
			final String vertexShader = Gdx.files.internal("data/shaders/model/rigged_model.vertex.glsl").readString();
			final String fragmentShader = Gdx.files.internal("data/shaders/model/rigged_model.fragment.glsl").readString();
			
			shader = new ShaderProgram(vertexShader, fragmentShader);
			if (!shader.isCompiled())
			{
				Gdx.app.error("Problem loading shader:", shader.getLog());
			}
		}
	}
	
	boolean held = false;
	public void held()
	{
		if (!held) rootNode.held();
		held = true;
	}
	
	public void released()
	{
		if (held) rootNode.released();
		
		held = false;
	}
	
	public void update(float delta, GameActor holder)
	{
		rootNode.update(delta);
	}

	public void composeMatrixes(Matrix4 composed)
	{
		rootNode.composeMatrixes(composed);
	}
	
	public void draw(Renderer renderer)
	{
		rootNode.render(this, renderer);
	}
	
	public void create()
	{
		for (Material m : materials)
		{
			m.create();
		}
		
		rootNode.create();
	}
	
	public void fixReferences()
	{
		rootNode.fixReferences();
	}
	
	public void dispose()
	{
		rootNode.dispose();
		for (Material m : materials)
		{
			m.dispose();
		}
	}
	
	public void bakeLight(LightManager lights, boolean bakeStatics)
	{
		rootNode.bakeLight(lights, bakeStatics);
	}
	
	/**
	 * rootnode
	 *    |
	 *    hilt
	 *    |
	 *    blade
	 *    .
	 *    .
	 *    .
	 *    n
	 *    .
	 *    .
	 *    .
	 *    nodeblade
	 *    |
	 *    nodeTip
	 * @param length
	 * @return
	 */
	public static RiggedModel getSword(int length)
	{
		RiggedModelNode rootnode = new RiggedModelNode(new RiggedSubMesh[]{}, new int[]{}, new Matrix4(), new Matrix4(), 0, false);
		rootnode.setParent(null);
		
		RiggedSubMesh[] meshes = {new RiggedSubMesh("Hilt", GL20.GL_TRIANGLES, 1.0f, "file", "model!"), new RiggedSubMesh("Guard", GL20.GL_TRIANGLES, 1.0f, "file", "model(", "0", "0", "0.4")};	
		
		RiggedModelNode hilt = new RiggedModelNode(meshes, new int[]{0, 0}, new Matrix4().setToTranslation(0, 0, 0.7f), new Matrix4(), 0, false);
		
		hilt.setParent(rootnode);
		rootnode.setChilden(hilt);
		
		RiggedSubMesh[] meshesblade = {new RiggedSubMesh("Blade", GL20.GL_TRIANGLES, 0.5f, "file", "modelHBlade")};
		
		RiggedModelNode prevNode = hilt;
		for (int i = 0; i < length-1; i++)
		{
			RiggedModelNode node = new RiggedModelNode(meshesblade, new int[]{0}, new Matrix4().setToTranslation(0, 0, 0.5f), new Matrix4(), 100, true);
			node.setParent(prevNode);
			prevNode.setChilden(node);
			
			prevNode = node;
		}
		
		RiggedModelNode nodeblade = new RiggedModelNode(meshesblade, new int[]{0}, new Matrix4().setToTranslation(0, 0, 0.34f), new Matrix4(), 100, true);
		nodeblade.setParent(prevNode);
		prevNode.setChilden(nodeblade);

		RiggedSubMesh[] meshestip = {new RiggedSubMesh("Tip", GL20.GL_TRIANGLES, -0.5f, "file", "modelABlade")};
		
		RiggedModelNode nodeTip = new RiggedModelNode(meshestip, new int[]{0}, new Matrix4().setToTranslation(0, 0, 0.2f), new Matrix4(), 100, true);
		nodeTip.setParent(nodeblade);
		nodeblade.setChilden(nodeTip);
		nodeTip.setChilden();
		
		Material material = new Material("basic");
		material.setTexture("blank");
		
		return new RiggedModel(rootnode, new Material[]{material});
	}
	
	public static RiggedModel getTorch()
	{
		RiggedModelNode rootnode = new RiggedModelNode(new RiggedSubMesh[]{}, new int[]{}, new Matrix4(), new Matrix4(), 0, false);
		rootnode.setParent(null);
		
		RiggedSubMesh[] meshes = {new RiggedSubMesh("Torch", GL20.GL_TRIANGLES, 1, "cube", "0.1", "0.1", "3")};
		
		RiggedModelNode node = new RiggedModelNode(meshes, new int[]{0}, new Matrix4().setToTranslation(0, 0, 1.5f), new Matrix4(), 100, true);
		
		rootnode.setChilden(node);
		node.setParent(rootnode);
		
		RiggedSubMesh[] meshes1 = {new RiggedSubMesh("Flame", GL20.GL_TRIANGLES, 1, "cube", "0.01", "0.01", "0.01")};
		
		RiggedModelNode node1 = new RiggedModelNode(meshes1, new int[]{0}, new Matrix4().setToTranslation(0, -0.5f, -0.5f), new Matrix4(), 100, true);
		
		ParticleEmitter p = new ParticleEmitter(0, 0, 0, 0.1f, 0.1f, 0.1f, 0.02f, 350);
		p.setTexture("texf", new Vector3(0.0f, -0.7f, 0.0f), 4.0f, new Colour(0.6f, 0.4f, 1.0f, 1.0f), new Colour(0.0f, 0.0f, 0.6f, 1.0f), true, 0.03f);
		p.create();
		
		node.setChilden(node1);
		
		node1.setParticleEmitter(p);
		node1.setParent(node);
		node1.setChilden();
		
		GameData.level.particleEmitters.add(p);
		
		Material material = new Material("basic");
		material.setTexture("wood");
		
		return new RiggedModel(rootnode, new Material[]{material});
		
	}
}