package com.lyeeedar.Roguelike3D.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.lyeeedar.Roguelike3D.Game.GameData.Damage_Type;
import com.lyeeedar.Roguelike3D.Game.GameData.Element;
import com.lyeeedar.Roguelike3D.Game.Actor.Player;
import com.lyeeedar.Roguelike3D.Game.Item.Component;
import com.lyeeedar.Roguelike3D.Game.Item.Equipment_BODY;
import com.lyeeedar.Roguelike3D.Game.Item.Equipment_BOOTS;
import com.lyeeedar.Roguelike3D.Game.Item.Equipment_HAND;
import com.lyeeedar.Roguelike3D.Game.Item.Equipment_HEAD;
import com.lyeeedar.Roguelike3D.Game.Item.Equipment_LEGS;
import com.lyeeedar.Roguelike3D.Game.Item.Item;
import com.lyeeedar.Roguelike3D.Game.Item.Recipe;

public class GameStats {
	
	public static int MAX_HEALTH;
	public static int HEALTH;
	public static int WEIGHT;
	public static int STRENGTH;
	public static HashMap<Element, Integer> ELE_DEF = new HashMap<Element, Integer>();
	public static HashMap<Damage_Type, Integer> DAM_DEF = new HashMap<Damage_Type, Integer>();
	public static ArrayList<String> FACTIONS;
	
	public static Equipment_HEAD head;
	public static Equipment_BODY body;
	public static Equipment_LEGS legs;
	public static Equipment_BOOTS boots;
	public static Equipment_HAND l_hand;
	public static Equipment_HAND r_hand;
	
	public static TreeMultimap<Integer, Recipe> recipes = TreeMultimap.create();
	public static TreeMultimap<Integer, Component> components = TreeMultimap.create();
	
	public static void init()
	{
		MAX_HEALTH = HEALTH = 100;
		WEIGHT = 1;
		STRENGTH = 1;
		ELE_DEF = GameData.getElementMap();
		DAM_DEF = GameData.getDamageMap();
		FACTIONS = new ArrayList<String>();
		FACTIONS.add("PLAYER");
	}
	
	public static void setPlayerStats(Player player)
	{
		player.setStats(HEALTH, WEIGHT, STRENGTH, ELE_DEF, DAM_DEF, FACTIONS);
	}
	
	public static void addRecipe(Recipe recipe)
	{
		recipes.put(recipe.rarity, recipe);
	}
	
	public static void addComponent(Component c)
	{
		if (components.containsEntry(c.rarity, c)) {
			System.out.println("Component already added!");
			Component cc = components.get(c.rarity).floor(c);
			cc.amount += c.amount;
		}
		else components.put(c.rarity, c);
	}
	
	public static void removeComponent(Component c, int amount)
	{
		c.amount -= amount;
		
		if (c.amount <= 0)
		{
			components.remove(c.rarity, c);
		}
	}
}