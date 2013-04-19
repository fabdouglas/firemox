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
package net.sf.firemox.event;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.AbstractCard;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.MSocketListener;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.test.Test;
import net.sf.firemox.tools.IntegerWrapper;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;
import net.sf.firemox.ui.wizard.Replacement;

/**
 * An event triggered on a game or player event.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.54
 */
public abstract class TriggeredEvent extends MEventListener {

	/**
	 * Create an instance of MEventTriggered by reading a file Offset's file must
	 * pointing on the first byte of this event <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idZone [1]</li>
	 * <li>test [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          is the file containing this event
	 * @param card
	 *          is the card owning this event
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public TriggeredEvent(InputStream inputFile, MCard card) throws IOException {
		super(inputFile, card);
	}

	/**
	 * Creates a new instance of MEventTriggered specifying all attributes of this
	 * class. All parameters are copied, not cloned. So this new object shares the
	 * card and the specified codes
	 * 
	 * @param idZone
	 *          the place constraint to activate this event
	 * @param test
	 *          the additional test of this event
	 * @param card
	 *          is the card owning this card
	 */
	protected TriggeredEvent(int idZone, Test test, MCard card) {
		super(idZone, test, card);
	}

	@Override
	public abstract MEventListener clone(MCard card);

	@Override
	public abstract Event getIdEvent();

	@Override
	public void registerToManager(Ability ability) {
		if (TRIGGRED_ABILITIES.get(getIdEvent()) == null) {
			TRIGGRED_ABILITIES.put(getIdEvent(), new ArrayList<Ability>(20));
			// add this event listener
			TRIGGRED_ABILITIES.get(getIdEvent()).add(ability);
		} else {
			ability.optimizeRegisterToManager();
		}
	}

	/*
	 * Create and returns an union of this event and the specified one. Both event
	 * must have the same type. Test(s) and events attributes may be grouped
	 * depending instance of this event. If no possible append is possible <code>null</code>
	 * is returned. @param other the event to append with 'or' operator. @return a
	 * new event representing 'this' or 'other'
	 */
	// TODO public abstract MEventListener appendOr(MEventListener other);
	@Override
	public void removeFromManager(Ability ability) {
		if (TRIGGRED_ABILITIES.get(getIdEvent()) != null) {
			// remove this event listener
			TRIGGRED_ABILITIES.get(getIdEvent()).remove(ability);
		}
	}

	@Override
	public final boolean isActivated() {
		return false;
	}

	@Override
	public final boolean isTriggered() {
		return true;
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return LanguageManagerMDB.getString("event-"
				+ getClass().getSimpleName().toLowerCase());
	}

	/**
	 * Return true if the current event has not been replaced. If only one
	 * replacement ability has been found, it should be used. If several
	 * replacement abilities have been found a form containing choice would be
	 * displaced.
	 * 
	 * @param source
	 *          the event source.
	 * @param result
	 *          the found replacement abilities.
	 * @param eventName
	 *          the current event name.
	 * @return true if the current event has not been replaced. False otherwise.
	 */
	protected static final boolean manageReplacement(MCard source,
			List<AbstractCard> result, String eventName) {
		if (result != null) {
			if (result.size() > 1) {
				final Player controller = source.getController();
				replacement = new IntegerWrapper(-1);
				controller.setHandedPlayer();
				if (controller.isYou()) {
					Log.debug("You choose a replacement ability to use");
					new Replacement(eventName, result).setVisible(true);
					ConnectionManager.send(CoreMessageType.REPLACEMENT_ANSWER,
							(byte) (Replacement.replacement / 256),
							(byte) (Replacement.replacement % 256));
				} else {
					MagicUIComponents.logListing.append(1,
							"choosing a replacement ability to use");
					StackManager.noReplayToken.release();
					Log.debug("Opponent chooses a replacement ability to use");
					// Currently a simple "telnet-like" minimal code treating :
					Replacement.replacement = MSocketListener.getInstance()
							.readReplacementAnswer();
					Log.debug("Replacement : virtual mouseClick");
					MagicUIComponents.logListing.append(1, "Chosen replacement : "
							+ result.get(Replacement.replacement).toString());
				}
			} else {
				Replacement.replacement = 0;
			}

			if (StackManager.newSpell((TriggeredCard) result
					.get(Replacement.replacement))) {
				StackManager.resolveStack();
			}
			return false;
		}
		return true;
	}

	/**
	 * The selected replacement ability to use
	 */
	public static IntegerWrapper replacement;
}