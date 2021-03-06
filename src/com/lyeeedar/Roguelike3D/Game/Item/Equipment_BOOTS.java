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
package com.lyeeedar.Roguelike3D.Game.Item;

import java.util.HashMap;

import com.lyeeedar.Roguelike3D.Game.GameData.Damage_Type;
import com.lyeeedar.Roguelike3D.Game.GameData.Element;
import com.lyeeedar.Roguelike3D.Game.Actor.GameActor;
import com.lyeeedar.Roguelike3D.Game.Item.Item.Item_Type;

public class Equipment_BOOTS extends Equipment_ARMOUR {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9002690235540692533L;

	public Equipment_BOOTS(float WEIGHT, int strength, HashMap<Element, Integer> ELE_DEF, HashMap<Damage_Type, Integer> DAM_DEF) {
		super(WEIGHT, Item_Type.ARMOUR_BOOTS, strength, ELE_DEF, DAM_DEF);
	}

	@Override
	public void fixReferences(GameActor actor) {
	}

}
