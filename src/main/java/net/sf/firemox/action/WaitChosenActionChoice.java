/* 
 * WaitChosenActionChoice.java
 * Created on 26 févr. 2004
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import net.sf.firemox.action.listener.WaitingAbility;
import net.sf.firemox.action.listener.WaitingAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.action.JChosenAction;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.CanICast;
import net.sf.firemox.stack.ActivatedChoiceList;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.zone.MZone;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public final class WaitChosenActionChoice extends MAction implements
		WaitingAction, WaitingAbility {

	/**
	 * Create a new instance of WaitTriggeredBufferChoice
	 */
	private WaitChosenActionChoice() {
		super();
	}

	/**
	 * This action activate the panel containing the ChosenAction, highlights the
	 * playable mana-source abilities, and the the spell's controller as the
	 * handed player.
	 * 
	 * @param controller
	 *          is the player waiting the action choice.
	 * @TODO enable advaned abilities to be played
	 */
	protected void play(Player controller) {
		final List<Ability> res = new ArrayList<Ability>();
		final List<Ability> advRes = new ArrayList<Ability>();
		CanICast.dispatchManaAbilityEvent(controller.idPlayer, res);
		finished();
		if (!res.isEmpty()) { // TODO Enable advanced too : || !advRes.isEmpty()){
			activChoiceList = new ActivatedChoiceList(res, advRes);
			res.clear();
			advRes.clear();
			zoneList = activChoiceList.highLight();

			// bring to front the ChosenAction panel
			((JTabbedPane) MagicUIComponents.chosenCostPanel.getParent())
					.setSelectedComponent(MagicUIComponents.chosenCostPanel);
		}
		StackManager.enableAbort();
		controller.setActivePlayer();
	}

	public boolean clickOn(JChosenAction action) {
		// TODO Modifying chosenAction order is not yet implemented
		// return true;
		return false;
	}

	public boolean clickOn(Ability ability) {
		return StackManager.idHandedPlayer == 0;
	}

	public boolean succeedClickOn(JChosenAction action) {
		finished();
		return action.setSelected();
	}

	public boolean succeedClickOn(Ability ability) {
		finished();
		StackManager.newSpell(ability, ability.isMatching());
		return true;
	}

	@Override
	public Actiontype getIdAction() {
		return Actiontype.WAIT_ACTIVATED_CHOICE;
	}

	public boolean manualSkip() {
		finished();
		// activate automatically the first uncompleted ChosenAction
		return MagicUIComponents.chosenCostPanel.playFirstUncompleted();
	}

	public void finished() {
		if (activChoiceList != null) {
			activChoiceList.disHighLight();
			activChoiceList.clear();
			activChoiceList = null;
		}
		if (zoneList != null) {
			for (MZone zone : zoneList) {
				zone.disHighLight();
			}
			zoneList.clear();
			zoneList = null;
			StackManager.PLAYERS[0].disHighLight();
			StackManager.PLAYERS[1].disHighLight();
		}
		StackManager.disableAbort();
	}

	public List<Ability> abilitiesOf(MCard card) {
		return activChoiceList == null ? null : activChoiceList.abilitiesOf(card);
	}

	public List<Ability> advancedAbilitiesOf(MCard card) {
		return activChoiceList == null ? null : activChoiceList
				.advancedAbilitiesOf(card);
	}

	@Override
	public String toString(Ability ability) {
		return "Wait mana source choice";
	}

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static WaitChosenActionChoice getInstance() {
		if (instance == null)
			instance = new WaitChosenActionChoice();
		return instance;
	}

	/**
	 * This is the list of zone concerned by highlighted cards
	 */
	private List<MZone> zoneList;

	/**
	 * the current list of choices of active player
	 */
	private ActivatedChoiceList activChoiceList;

	/**
	 * The unique instance of this class
	 */
	private static WaitChosenActionChoice instance;

}