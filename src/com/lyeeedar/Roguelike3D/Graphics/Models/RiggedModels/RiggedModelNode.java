package com.lyeeedar.Roguelike3D.Graphics.Models.RiggedModels;

import java.io.Serializable;
import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Pools;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEffect;
import com.lyeeedar.Graphics.ParticleEffects.ParticleEmitter;
import com.lyeeedar.Roguelike3D.Game.GameData;
import com.lyeeedar.Roguelike3D.Game.Actor.GameActor;
import com.lyeeedar.Roguelike3D.Game.LevelObjects.LevelObject;
import com.lyeeedar.Roguelike3D.Graphics.Lights.LightManager;
import com.lyeeedar.Roguelike3D.Graphics.Renderers.Renderer;

public class RiggedModelNode implements Serializable
{
	public static final Matrix4 tmpMat = new Matrix4();
	public static final Vector3 tmpVec = new Vector3();
	
	private static final long serialVersionUID = -3949208107618544807L;
	public final RiggedSubMesh[] submeshes;
	public final int[] submeshMaterials;
	public transient Matrix4[] meshMatrixes;
	public RiggedModelNode[] childNodes;
	public transient RiggedModelNode parent;
	public RiggedModelBehaviour behaviour;
	public ParticleEffect particleEffect;
	public final String ID;
	
	public float radius;
	public float renderRadius;	
	public final float rigidity;
	public final boolean collidable;

	public final Matrix4 position;
	public final Matrix4 rotation;
	public final Matrix4 offsetPosition = new Matrix4();
	public final Matrix4 offsetRotation = new Matrix4();
	
	public transient Matrix4 composedMatrix;

	public boolean collideMode = false;
	
	public RiggedModelNode(String ID, RiggedSubMesh[] submeshes, int[] submeshMaterials, Matrix4 position, Matrix4 rotation, int rigidity, boolean collidable)
	{
		this.ID = ID;
		if (submeshes.length != submeshMaterials.length)
		{
			throw new RuntimeException("Invalid number of materials to submeshes in RiggedModelNode!");
		}
		
		this.submeshes = submeshes;
		this.submeshMaterials = submeshMaterials;
		this.position = position;
		this.rotation = rotation;
		this.rigidity = rigidity;
		this.collidable = collidable;
	}
	
	public void equip(GameActor holder, int side)
	{
		if (behaviour != null) behaviour.equip(holder, side);
		
		for (RiggedModelNode rmn : childNodes) rmn.equip(holder, side);
	}
	
	public void setParticleEffect(ParticleEffect effect)
	{
		if (particleEffect != null) {
			particleEffect.dispose();
			particleEffect.delete();
		}
		this.particleEffect = effect;
	}
	
	public void setBehaviour(RiggedModelBehaviour behaviour)
	{
		this.behaviour = behaviour;
	}
	
	public void setParent(RiggedModelNode parent)
	{
		this.parent = parent;
	}
	
	public void setChilden(RiggedModelNode... children)
	{
		this.childNodes = children;
	}
	
	public void composeMatrixes(Matrix4 composed)
	{
		composedMatrix.set(composed).mul(position).mul(rotation).mul(offsetPosition).mul(offsetRotation);
		
		for (int i = 0; i < submeshes.length; i++)
		{
			meshMatrixes[i].set(composed).scale(submeshes[i].scale, submeshes[i].scale, submeshes[i].scale);
		}
		
		for (RiggedModelNode rgn : childNodes)
		{
			rgn.composeMatrixes(composedMatrix);
		}
		
		if (particleEffect != null) {
			Vector3 tmp = Pools.obtain(Vector3.class);
			particleEffect.setPosition(tmp.set(0, 0, 0).mul(composedMatrix));
			Pools.free(tmp);
		}
	}
	
	public void render(RiggedModel model, Renderer renderer, ArrayList<ParticleEmitter> emitters, Camera cam)
	{
		if (particleEffect != null) {
			particleEffect.getVisibleEmitters(emitters, cam);
		}
		
		for (int i = 0; i < submeshes.length; i++)
		{
			renderer.draw(submeshes[i], meshMatrixes[i], model.materials[submeshMaterials[i]], renderRadius);
		}
		
		for (RiggedModelNode rgn : childNodes)
		{
			rgn.render(model, renderer, emitters, cam);
		}
	}
	
	public GameActor checkCollision(GameActor holder)
	{
		tmpVec.set(0, 0, 0).mul(composedMatrix);
		
		if (collidable && collideMode) {
			GameActor ga = GameData.level.collideSphereActorsAll(tmpVec.x, tmpVec.y, tmpVec.z, radius, holder.UID);

			LevelObject lo = GameData.level.collideSphereLevelObjectsAll(tmpVec.x, tmpVec.y, tmpVec.z, radius);
			
			if (lo != null) ga = holder;
			
			if (GameData.level.collideSphereAll(tmpVec.x, tmpVec.y, tmpVec.z, radius, holder.UID)) ga = holder;
			
			if (ga != null) {
				rotation.rotate(0, 1, 0, (100-rigidity)/50);
				return ga;
			}
		}
		
		for (RiggedModelNode rmn : childNodes)
		{
			GameActor ga = rmn.checkCollision(holder);
			if (ga != null) return ga;
		}
		
		return null;
	}
	
	public void update(float delta, Camera cam)
	{
		if (particleEffect != null)
		{
			particleEffect.update(delta, cam);
		}
		if (behaviour != null) behaviour.update(delta);
		
		for (RiggedModelNode rmn : childNodes)
		{
			rmn.update(delta, cam);
		}
	}
	
	public void held()
	{
		if (behaviour != null) behaviour.held();
		
		for (RiggedModelNode rmn : childNodes)
		{
			rmn.held();
		}
	}
	
	public void released()
	{
		if (behaviour != null) behaviour.released();
		
		for (RiggedModelNode rmn : childNodes)
		{
			rmn.released();
		}
	}
	
	public void cancel()
	{
		if (behaviour != null) behaviour.cancel();
		
		for (RiggedModelNode rmn : childNodes)
		{
			rmn.cancel();
		}
	}

	public void setCollideMode(boolean mode, boolean propogateUp)
	{
		if (propogateUp)
		{
			if (parent != null)
			{
				parent.setCollideMode(mode, true);
			}
			else
			{
				for (RiggedModelNode rmn : childNodes)
				{
					rmn.setCollideMode(mode, false);
				}
			}
		}
		else
		{
			collideMode = mode;
			if (behaviour != null) behaviour.proccessCollideMode(mode);
			
			for (RiggedModelNode rmn : childNodes)
			{
				rmn.setCollideMode(mode, false);
			}
		}
	}
	
	public void create()
	{
		composedMatrix = Pools.obtain(Matrix4.class).idt();
		meshMatrixes = new Matrix4[submeshes.length];
		
		for (int i = 0; i < meshMatrixes.length; i++)
		{
			meshMatrixes[i] = Pools.obtain(Matrix4.class).idt();
		}
		
		for (RiggedSubMesh rsm : submeshes)
		{
			rsm.create();
		}
		
		BoundingBox box = new BoundingBox();
		
		for (RiggedSubMesh sm : submeshes)
		{
			box.ext(sm.getBoundingBox());
		}
		
		float longest = (box.getDimensions().x > box.getDimensions().z) ? box.getDimensions().x : box.getDimensions().z;
		longest = (box.getDimensions().y > longest) ? box.getDimensions().y : longest;
		this.radius = (longest / 2.0f);
		
		this.renderRadius = (radius > 1) ? radius : 1;
		
		for (RiggedModelNode rmn : childNodes)
		{
			rmn.create();
		}
		
		if (particleEffect != null) {
			particleEffect.create();
		}
		
	}
	
	public void fixReferences()
	{
		for (RiggedModelNode rmn : childNodes)
		{
			rmn.setParent(this);
			rmn.fixReferences();
		}
	}
	
	public void dispose()
	{
		Pools.free(composedMatrix);
		composedMatrix = null;
		for (int i = 0; i < meshMatrixes.length; i++)
		{
			Pools.free(meshMatrixes[i]);
			meshMatrixes[i] = null;
		}
		
		for (RiggedSubMesh rsm : submeshes)
		{
			rsm.dispose();
		}

		for (RiggedModelNode rmn : childNodes)
		{
			rmn.dispose();
		}
		
		if (particleEffect!= null) particleEffect.dispose();
	}
	
	public void getLight(LightManager lightManager)
	{
		if (particleEffect != null)
		{
			particleEffect.getLight(lightManager);
		}
		
		for (RiggedModelNode rmn : childNodes)
		{
			rmn.getLight(lightManager);
		}
	}
	
	public void bakeLight(LightManager lights, boolean bakeStatics)
	{
		for (int i = 0; i < submeshes.length; i++)
		{
			submeshes[i].bakeLight(lights, bakeStatics, meshMatrixes[i]);
		}

		for (RiggedModelNode rmn : childNodes)
		{
			rmn.bakeLight(lights, bakeStatics);
		}
	}
	
	public RiggedModelNode getNode(String ID)
	{
		if (this.ID.equalsIgnoreCase(ID))
		{
			return this;
		}
		
		for (RiggedModelNode rmn : childNodes)
		{
			RiggedModelNode returned = rmn.getNode(ID);
			if (returned != null) return returned;
		}
		
		return null;
	}
}
