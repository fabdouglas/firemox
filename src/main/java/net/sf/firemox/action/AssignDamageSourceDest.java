/*
 * Created on 2 mars 2005
 * 
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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.AssignedDamage;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
class AssignDamageSourceDest extends UserAction implements LoopAction {

	/**
	 * Create an instance of AssignDamageTarget by reading a file Offset's file
	 * must pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>super [Action]</li>
	 * <li>amount [Expression]</li>
	 * <li>type [Expression]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 * @see net.sf.firemox.token.IdDamageTypes
	 */
	AssignDamageSourceDest(InputStream inputFile) throws IOException {
		super(inputFile);
		valueExpr = ExpressionFactory.readNextExpression(inputFile);
		type = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.ASSIGN_DAMAGE_SOURCE_DEST;
	}

	public int getStartIndex() {
		// TODO Implement completely this action
		// return StackManager.getInstance().getTargetedList().size() - 1;
		return 0;
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		final int type = this.type.getValue(ability, null, context);
		final int value = valueExpr.getValue(ability, ability.getCard(), context);
		final MCard source = (MCard) StackManager.getInstance().getTargetedList()
				.get(0);
		final Target target = StackManager.getInstance().getTargetedList().get(1);
		if (!target.isCard() || checkTimeStamp(context, (MCard) target)
				&& ((MCard) target).getIdZone() == IdZones.PLAY) {
			if (!AssignedDamage.tryAction(source, target, value, type)) {
				// this action has been replaced
				return false;
			}
			if (value > 0) {
				AssignedDamage.dispatchEvent(source, target, value, type);
			}
		}
		return true;
	}

	@Override
	public String toString(Ability ability) {
		try {
			if (valueExpr.getValue(ability, null, null) == IdConst.ALL) {
				return LanguageManagerMDB.getString("assign-damage-target-all");
			}
			return LanguageManagerMDB.getString("assign-damage-target", valueExpr
					.getValue(ability, null, null));
		} catch (Exception e) {
			return LanguageManagerMDB.getString("assign-damage-target", "?");
		}
	}

	/**
	 * The complex expression to use for the right value. Is null if the IdToken
	 * number is not a complex expression.
	 */
	private final Expression valueExpr;

	/**
	 * represent the type of damage
	 */
	private final Expression type;

}
