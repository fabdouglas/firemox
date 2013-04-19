/*
 *   Firemox is a turn based strategy simulator
 *   Copyright (C) 2003-2007 Fabrice Daugan
 *
 *   This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 *   This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more 
 * details.
 *
 *   You should have received a copy of the GNU General Public License along  
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sf.firemox.modifier;

import java.awt.Graphics;
import java.awt.Image;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.modifier.model.ObjectFactory;
import net.sf.firemox.operation.Operation;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class ObjectAbilityModifier extends AbilityModifier {

	/**
	 * Creates a new instance of ObjectColorModifier <br>
	 * 
	 * @param context
	 *          the modifier context.
	 * @param abilitiesToAdd
	 *          the abilities to add/remove/...
	 * @param op
	 *          the operation applied to previous value with the value of this
	 *          modifier.
	 * @param main
	 *          is this object modifier is the main modifier ?.
	 */
	public ObjectAbilityModifier(ModifierContext context, Operation op,
			Ability[] abilitiesToAdd, boolean main) {
		super(context, op, abilitiesToAdd);
		objectPicture = ObjectFactory.getObjectPicture(name);
		this.main = main;
	}

	@Override
	public Modifier removeObject(String objectName, Test objectTest) {
		if (name.equals(objectName) && objectTest.test(ability, to)) {
			StackManager.postRefreshAbilities(to);
			return next;
		}
		return super.removeObject(objectName, objectTest);
	}

	@Override
	public int paintObject(Graphics g, int startX, int startY) {
		if (main) {
			if (startX + 13 > CardFactory.cardWidth) {
				return paintObject(g, 3, startY - 16);
			}
			g.drawImage(objectPicture, startX, startY, 13, 15, null);
			return super.paintObject(g, startX + 3, startY);
		}
		return super.paintObject(g, startX, startY);
	}

	@Override
	public int getNbObjects(String objectName, Test objectTest) {
		if (main && objectName.equals(name) && objectTest.test(ability, to)) {
			if (next == null) {
				return 1;
			}
			return 1 + next.getNbObjects(objectName, objectTest);
		}
		return super.getNbObjects(objectName, objectTest);
	}

	/**
	 * Picture representing this object
	 */
	private Image objectPicture;

	/**
	 * is this object modifier is the main modifier
	 */
	private boolean main;
}
