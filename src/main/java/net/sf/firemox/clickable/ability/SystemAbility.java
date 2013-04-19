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
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.ActionFactory;
import net.sf.firemox.action.MAction;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.EventFactory;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.TrueFalseAuto;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.60
 */
public class SystemAbility extends Ability {

	/**
	 * Create an instance of SystemAbility <br>
	 */
	private SystemAbility() {
		super("system-instance", Optimization.none, Priority.hidden_high, null);
		playAsSpell = TrueFalseAuto.AUTO;
		card = SystemCard.instance;
	}

	/**
	 * Create an instance of SystemAbility <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>name name + '\0' [...]</li>
	 * <li>ability tags [1]</li>
	 * <li>event [...]</li>
	 * <li>nb of actions for effect part [1]</li>
	 * <li>effect action i [...]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this ability
	 * @throws IOException
	 */
	public SystemAbility(InputStream inputFile) throws IOException {
		super(inputFile);

		try {
			// Read triggered event
			eventComing = EventFactory.readNextEvent(inputFile, SystemCard.instance);

			// Read list of effect associated to
			effectList = ActionFactory.readActionList(inputFile, null);

			if (isHidden()) {
				card = SystemCard.instance;
			} else {
				card = new MCard(pictureName, SystemCard.instance);
			}
		} catch (IOException e) {
			if (getName() == null) {
				Log.debug(">> I/O ERROR in SystemAbility : " + getClass().getName());
			} else {
				Log.debug(">> I/O ERROR in SystemAbility : " + getName());
			}
			Log.error(e);
			throw e;
		} catch (Throwable e) {
			e.printStackTrace();
			if (getName() == null) {
				throw new InternalError(">> TBS ERROR in SystemAbility : "
						+ getClass().getName());
			}
			throw new InternalError(">> TBS ERROR in SystemAbility : " + getName());
		}
	}

	@Override
	public boolean triggerIt(ContextEventListener context) {
		super.triggerIt(context);
		if (isHidden()) {
			/**
			 * This ability is considered as an abstract one : the player won't see
			 * any picture representing this ability, and this ability won't require
			 * any player intervention. Instead of resolving immediately this ability,
			 * we put it in a special location of the triggered buffer zone which
			 * would be threated before the standard triggered abilities
			 */
			StackManager.activePlayer().zoneManager.triggeredBuffer.addHidden(this,
					context);
		} else {
			StackManager.activePlayer().zoneManager.triggeredBuffer
					.add(getTriggeredClone(context));
		}
		return true;
	}

	@Override
	public boolean isMatching() {
		return true;
	}

	@Override
	public final MAction[] actionList() {
		return EMPTY_ACTIONS;
	}

	@Override
	public MCard getCard() {
		return card;
	}

	@Override
	public MAction[] effectList() {
		return effectList;
	}

	@Override
	public boolean recheckTargets() {
		return true;
	}

	@Override
	public String getLog(ContextEventListener context) {
		return getName();
	}

	@Override
	public String getAbilityTitle() {
		return LanguageManager.getString("systemability")
				+ (getName() != null ? ": " + getName() : "");
	}

	@Override
	public boolean isSystemAbility() {
		return true;
	}

	/**
	 * will contains MAction objects of cost part
	 */
	protected static final MAction[] EMPTY_ACTIONS = {};

	/**
	 * will contains MAction objects of effect part
	 */
	protected MAction[] effectList;

	/**
	 * The card identifying this ability
	 */
	protected MCard card;

	/**
	 * An instance used to represent the game.
	 */
	public static SystemAbility instance = new SystemAbility();
}