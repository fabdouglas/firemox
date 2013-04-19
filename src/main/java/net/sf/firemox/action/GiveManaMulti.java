/* 
 * GiveManaMulti.java
 * Created on 2005/08/25
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
 * 
 */
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.expression.Expression;
import net.sf.firemox.expression.ExpressionFactory;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.TestOn;
import net.sf.firemox.token.IdCommonToken;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * It's the mana source action, modify directly the mana pool like : Land, other
 * mana sources. Add several colors at the same time. This is the difference
 * between GiveManaBasic
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.72 support counter ability
 * @since 0.86
 */
class GiveManaMulti extends GiveMana implements LoopAction {

	/**
	 * Create an instance of GiveManaMulti by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>restriction usage test [...]</li>
	 * <li>idPlayer receiving this mana [2]</li>
	 * <li>COLORLESS : expression [...] or (IdTokens#REGISTERS)[2]</li>
	 * <li>BLACK : expression[...]</li>
	 * <li>BLUE : expression[...]</li>
	 * <li>GREEN : expression[...]</li>
	 * <li>RED : expression[...]</li>
	 * <li>WHITE : expression[...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	GiveManaMulti(InputStream inputFile) throws IOException {
		super(inputFile);
		final int code0 = MToolKit.readInt16(inputFile);
		if (code0 == IdTokens.MANA_POOL) {
			on = TestOn.deserialize(inputFile);
		} else {
			codeExpr = new Expression[IdCommonToken.COLOR_NAMES.length];
			for (int i = 0; i < codeExpr.length; i++) {
				codeExpr[i] = ExpressionFactory.readNextExpression(inputFile);
			}
		}
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.GIVE_MANA_MULTI;
	}

	public boolean continueLoop(ContextEventListener context, int loopingIndex,
			Ability ability) {
		if (!PayMana.useMana) {
			return true;
		}
		final int realCode = on != null ? on.getCard(StackManager.currentAbility,
				null).cachedRegisters[loopingIndex] : codeExpr[loopingIndex].getValue(
				ability, null, context);
		giveMana(ability, context, loopingIndex, realCode);
		return false;
	}

	public int getStartIndex() {
		return codeExpr.length - 1;
	}

	@Override
	public boolean play(ContextEventListener context, Ability ability) {
		throw new InternalError("should not been called");
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		String res = null;
		int[] realCode = null;
		if (on != null) {
			realCode = on.getCard(StackManager.currentAbility, null).cachedRegisters;
		} else {
			realCode = new int[codeExpr.length];
			for (int i = realCode.length; i-- > 0;) {
				try {
					realCode[i] = codeExpr[i].getValue(ability, null, context);
				} catch (Throwable t) {
					realCode[i] = -1;
				}
			}
		}
		if (realCode[0] != 0) {
			res = LanguageManagerMDB.getString("add-mana")
					+ MToolKit.getHtmlMana(0, realCode[0]);
		}

		for (int j = IdCommonToken.COLOR_NAMES.length; j-- > 1;) {
			if (realCode[j] > 0) {
				if (res == null) {
					res = LanguageManagerMDB.getString("add-mana");
				}
				if (realCode[j] > PayMana.thresholdColored) {
					res += MToolKit.getHtmlMana(j, 1) + "x" + realCode[j];
				} else {
					res += MToolKit.getHtmlMana(j, realCode[j]);
				}
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
		int[] realCode = null;
		if (on != null) {
			realCode = on.getCard(StackManager.currentAbility, null).cachedRegisters;
		} else {
			realCode = new int[codeExpr.length];
			for (int i = realCode.length; i-- > 0;) {
				try {
					realCode[i] = codeExpr[i].getValue(ability, null, null);
				} catch (Throwable t) {
					realCode[i] = -1;
				}
			}
		}
		if (realCode[0] != 0) {
			res = "Add " + realCode[0];
		}

		for (int j = IdCommonToken.COLOR_NAMES.length; j-- > 1;) {
			if (realCode[j] > 0) {
				if (res == null) {
					res = "Add ";
				}
				res += IdCommonToken.COLOR_NAMES[j] + "x" + realCode[j] + ",";
			}
		}
		if (res == null) {
			return "Add 0";
		}
		return res;
	}

	/**
	 * The test manager
	 */
	private TestOn on;

	/**
	 * represent the amount of mana to add to the mana pool. The complex
	 * expression to use for the right value. Is null if the IdToken number is not
	 * a complex expression.
	 */
	private Expression[] codeExpr = null;

}