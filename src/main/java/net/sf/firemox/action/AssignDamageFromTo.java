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
 * 
 */
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.AssignedDamage;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * To damage from a target to another.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.94
 */
class AssignDamageFromTo extends UserAction implements StandardAction {

	/**
	 * Create an instance of AssignDamageTarget by reading a file Offset's file
	 * must pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>[super]</li>
	 * <li>amount [Expression]</li>
	 * <li>type [Expression]</li>
	 * <li>from [Target]</li>
	 * <li>to [Target]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 * @see net.sf.firemox.token.IdDamageTypes
	 */
	AssignDamageFromTo(InputStream inputFile) throws IOException {
		super(inputFile);
		valueExpr = ExpressionFactory.readNextExpression(inputFile);
		type = ExpressionFactory.readNextExpression(inputFile);
		from = TestOn.deserialize(inputFile);
		to = TestOn.deserialize(inputFile);
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.ASSIGN_DAMAGE_FROM_TO;
	}

	@Override
	public String toString(Ability ability) {
		try {
			return LanguageManagerMDB.getString("assign-damage-from-to", valueExpr
					.getValue(ability, null, null), from.toHtmlString(ability, null), to
					.toHtmlString(ability, null));
		} catch (Exception e) {
			return LanguageManagerMDB.getString("assign-damage-from-to", "?", from
					.toHtmlString(ability, null), to.toHtmlString(ability, null));
		}
	}

	/**
	 * The complex expression to use for the right value. Is null if the IdToken
	 * number is not a complex expression.
	 */
	private Expression valueExpr = null;

	/**
	 * represent the type of damage
	 */
	private Expression type;

	/**
	 * The source of damage
	 */
	private TestOn from;

	/**
	 * The target of damage
	 */
	private TestOn to;

	public boolean play(ContextEventListener context, Ability ability) {
		final int value = valueExpr.getValue(ability, ability.getCard(), context);
		final Target from = this.from.getTargetable(ability, null);
		final Target target = this.to.getTargetable(ability, null);
		final int type = this.type.getValue(ability, null, context);
		if (!from.isCard() || checkTimeStamp(context, (MCard) from)
				&& ((MCard) from).getIdZone() == IdZones.PLAY) {
			final MCard source = (MCard) from;
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
}