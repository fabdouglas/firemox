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

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * It's the mana source action, modify directly the mana pool like : Land, other
 * mana sources. Only one color is added.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
class GiveManaBasic extends GiveMana {

	/**
	 * Create an instance of GiveManaBasic by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>restriction usage test [...]</li>
	 * <li>idPlayer receiving this mana [2]</li>
	 * <li>mana color to give : expression [...]</li>
	 * <li>mana amount to give : expression [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	GiveManaBasic(InputStream inputFile) throws IOException {
		super(inputFile);
		this.manaColor = ExpressionFactory.readNextExpression(inputFile);
		this.valueExpr = ExpressionFactory.readNextExpression(inputFile);
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		giveMana(ability, context, manaColor.getValue(ability, null, context),
				valueExpr.getValue(ability, null, context));
		return false;
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		String res = null;
		final int realCode = valueExpr.getValue(ability, null, context);
		final int color = manaColor.getValue(ability, null, context);
		if (color <= 0) {
			if (realCode > 0) {
				res = LanguageManagerMDB.getString("add-mana")
						+ MToolKit.getHtmlMana(0, realCode);
			}
		} else if (realCode > 0) {
			res = LanguageManagerMDB.getString("add-mana");
			if (realCode > PayMana.thresholdColored) {
				res += MToolKit.getHtmlMana(color, 1) + "x" + realCode;
			} else {
				res += MToolKit.getHtmlMana(color, realCode);
			}
		}
		if (res == null) {
			return LanguageManagerMDB.getString("add-mana")
					+ MToolKit.getHtmlMana(0, 0);
		}
		return res;
	}

	@Override
	public String toString(Ability ability) {
		String res = null;
		final int realCode = valueExpr.getValue(ability, null, null);
		final int color = manaColor.getValue(ability, null, null);
		if (color == 0) {
			if (realCode > 0) {
				res = "Add " + realCode;
			}
		} else if (color < 0) {
			res = "Add ?? x" + realCode + ",";
		} else if (realCode > 0) {
			res = "Add color" + color + "x" + realCode + ",";
		}
		if (res == null) {
			return "Add 0";
		}
		return res;
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.GIVE_MANA_BASIC;
	}

	/**
	 * The mana's color
	 */
	private Expression manaColor;

	/**
	 * represent the amount of mana to add to the mana pool.
	 */
	private Expression valueExpr = null;

}