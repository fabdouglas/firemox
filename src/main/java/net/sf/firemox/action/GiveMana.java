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
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.GivenMana;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextCardCardIntIntTest;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.ContextTest;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.test.TestOn;

/**
 * It's the mana source action, modify directly the mana pool like : Land, other
 * mana sources
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 * @since 0.72 support counter ability
 */
public abstract class GiveMana extends UserAction implements StandardAction {

	/**
	 * Create an instance of GiveMana by reading a file Offset's file must
	 * pointing on the first byte of this action <br>
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>restriction usage test [...]</li>
	 * <li>Player receiving this mana [TestOn]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected GiveMana(InputStream inputFile) throws IOException {
		super(inputFile);
		restriction = TestFactory.readNextTest(inputFile);
		player = TestOn.deserialize(inputFile);
	}

	/**
	 * Add mana to the current player
	 * 
	 * @param ability
	 *          is the ability owning this action. The card component of this
	 *          ability may correspond to the card owning this action too.
	 * @param context
	 *          is the context attached to this action.
	 * @param color
	 *          the mana color to add
	 * @param value
	 *          the amount of mana to add
	 */
	protected void giveMana(Ability ability, ContextEventListener context,
			int color, int value) {
		if (value <= 0) {
			StackManager.resolveStack();
		} else {
			final Player destinationPlayer = (Player) player.getTargetable(ability,
					context, null);
			if (GivenMana.tryAction(ability.getCard(), destinationPlayer, color,
					value, getRestrictionTest())) {
				destinationPlayer.mana.addMana(color, value, getRestrictionTest());
				GivenMana.dispatchEvent(ability.getCard(), destinationPlayer, color,
						value, getRestrictionTest());
				StackManager.resolveStack();
			}
		}
	}

	private Test getRestrictionTest() {
		return restriction instanceof ContextTest ? ((MContextCardCardIntIntTest) StackManager
				.getInstance().getAbilityContext()).test
				: restriction;
	}

	public abstract boolean play(ContextEventListener context, Ability ability);

	@Override
	public abstract String toHtmlString(Ability ability,
			ContextEventListener context);

	@Override
	public abstract String toString(Ability ability);

	/**
	 * The restriction mana usage test
	 */
	protected Test restriction;

	/**
	 * Player receiving this mana
	 */
	protected TestOn player;

}