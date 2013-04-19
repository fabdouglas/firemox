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
package net.sf.firemox.clickable.ability;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import net.sf.firemox.action.Actiontype;
import net.sf.firemox.action.InputChoice;
import net.sf.firemox.action.MAction;
import net.sf.firemox.action.RemoveObject;
import net.sf.firemox.action.Repeat;
import net.sf.firemox.action.target.AbstractTarget;
import net.sf.firemox.action.target.ChosenTarget;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.CardCopy;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.clickable.target.card.TriggeredCard;
import net.sf.firemox.clickable.target.card.TriggeredCardChoice;
import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.modifier.Unregisterable;
import net.sf.firemox.stack.ResolveStackHandler;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdZones;
import net.sf.firemox.token.TrueFalseAuto;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.apache.commons.lang.StringUtils;

/**
 * An ability contains a cost part and an effect part. Each ability is
 * associated to an event conditioning it's activation.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.1
 * @since 0.60 name attribute added
 * @since 0.86 Object to be removed from a component are checked be enabling an
 *        ability.
 * @since 0.86 The controller of this ability may be any player.
 * @since 0.90 linked abilities added.
 */
public abstract class Ability implements ResolveStackHandler, Unregisterable {

	/**
	 * Create an instance of Ability
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>name name [String]</li>
	 * <li>priority [Priority]</li>
	 * <li>optimization [Optimization]</li>
	 * <li>play-as-spell [TrueFalseAuto]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this ability
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	protected Ability(InputStream inputFile) throws IOException {
		// name of this ability
		this.name = StringUtils.trimToNull(MToolKit.readString(inputFile).intern());

		// To enable recursive ability dependencies
		AbilityFactory.lastInstance = this;

		/**
		 * We read the ability tag. If this ability has 'isHidden' tag, it would be
		 * considered as abstract and no picture would be used to represent it, so
		 * it would be played immediately without player intervention. If this
		 * ability has this tag and requires player intervention the play would
		 * crash.
		 */
		priority = Priority.valueOf(inputFile);
		optimizer = Optimization.valueOf(inputFile);
		if (isHidden()) {
			pictureName = null;
		} else {
			pictureName = StringUtils.trimToNull(MToolKit.readString(inputFile));
		}
		playAsSpell = TrueFalseAuto.deserialize(inputFile);
	}

	/**
	 * Create an instance of Ability
	 * 
	 * @param name
	 *          Name of card used to display this ability in a stack
	 * @param optimizer
	 *          the optimizer to use.
	 * @param priority
	 *          the resolution type.
	 * @param pictureName
	 *          the picture name of this ability. If <code>null</code> the card
	 *          picture will be used instead.
	 */
	protected Ability(String name, Optimization optimizer, Priority priority,
			String pictureName) {
		// name of this ability
		this.name = StringUtils.trimToNull(name);
		this.optimizer = optimizer;
		this.priority = priority;
		this.pictureName = pictureName;
	}

	/**
	 * Register this ability to manager trying to append test on existing ability
	 * with same effects.
	 * 
	 * @since 0.82
	 */
	public void optimizeRegisterToManager() {
		if (!MEventListener.TRIGGRED_ABILITIES.get(eventComing.getIdEvent())
				.contains(this)) {
			MEventListener.TRIGGRED_ABILITIES.get(eventComing.getIdEvent()).add(this);
		}
	}

	/**
	 * Return the name of this ability
	 * 
	 * @return the new name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Verify in the 'cost' part there is no target action may cause abortion of
	 * this ability.
	 * 
	 * @return true if all actions in the 'cost' part can be played.
	 */
	public boolean checkTargetActions() {
		for (int i = 0; i < actionList().length; i++) {
			if (actionList()[i] instanceof ChosenTarget) {
				if (i != 0
						&& actionList()[i - 1].getIdAction() == Actiontype.REPEAT_ACTION
						&& !((ChosenTarget) actionList()[i]).checkTarget(this,
								((Repeat) actionList()[i - 1]).getPreemptionTimes(this, this
										.getCard()))) {
					return false;
				}
				if (!((ChosenTarget) actionList()[i]).checkTarget(this, 1)) {
					return false;
				}
			} else if (actionList()[i] instanceof InputChoice) {
				if (!((InputChoice) actionList()[i]).checkTarget(this, i)) {
					return false;
				}
				i += ((InputChoice) actionList()[i]).getSkipHop();
			}
		}
		return true;
	}

	/**
	 * Checks too the other actions requiring a particular state, such as the
	 * presence of an object.
	 * 
	 * @return true if the other actions requiring a particular state, such as the
	 *         presence of an object are OK.
	 */
	public boolean checkObjectActions() {
		for (int i = 0; i < actionList().length; i++) {
			if (actionList()[i] instanceof RemoveObject && i != 0) {
				if (actionList()[i - 1] instanceof Repeat) {
					if (i > 1
							&& actionList()[i - 2] instanceof AbstractTarget
							&& !((RemoveObject) actionList()[i])
									.checkObject(((AbstractTarget) actionList()[i - 2])
											.getAbstractTarget(StackManager.getInstance()
													.getAbilityContext(), this),
											((Repeat) actionList()[i - 1]).getPreemptionTimes(this,
													null))) {
						return false;
					}
				} else if (actionList()[i - 1] instanceof AbstractTarget
						&& !((RemoveObject) actionList()[i]).checkObject(
								((AbstractTarget) actionList()[i - 1]).getAbstractTarget(
										StackManager.getInstance().getAbilityContext(), this), 1)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Is this ability contains targeting action.
	 * 
	 * @return true if this ability contains targeting action.
	 */
	public boolean recheckTargets() {
		return StackManager.getInstance().getTargetedList().recheckList(this) > 0;
	}

	/**
	 * Return card where is this ability. As default, it return null.
	 * 
	 * @return true card where is this ability
	 */
	public abstract MCard getCard();

	/**
	 * Return card where is this ability
	 * 
	 * @return true card where is this ability
	 */
	public Target getTargetable() {
		return getCard();
	}

	public boolean isAutoResolve() {
		return priority.isAutoResolve();
	}

	public boolean isHidden() {
		return priority.isHidden();
	}

	/**
	 * Indicates whether this ability is chosen in priority to the others without
	 * this tag.
	 * 
	 * @return true if this ability is chosen in priority to the others without
	 *         this tag.
	 */
	public boolean hasHighPriority() {
		return priority.hasHighPriority();
	}

	/**
	 * compare the current event to the event activating this ability. If
	 * matching, verify that there enough mana in the player's mana pool
	 * 
	 * @return true if this ability can be played responding the current event
	 */
	public abstract boolean isMatching();

	/**
	 * Return a card representing this ability.
	 * 
	 * @return a card representing this ability
	 */
	public CardCopy getCardCopy() {
		return new CardCopy(pictureName, getCard());
	}

	/**
	 * Return the picture name associated to this ability. Is <code>null</code>
	 * if no picture is used with this ability.
	 * 
	 * @return the picture name associated to this ability.
	 */
	public String getPictureName() {
		return pictureName;
	}

	/**
	 * Return list of actions to play to cast this ability
	 * 
	 * @return list of actions to play to cast this ability
	 */
	public abstract MAction[] actionList();

	/**
	 * Return list of actions effects of this ability
	 * 
	 * @return list of actions effects of this ability
	 */
	public abstract MAction[] effectList();

	/**
	 * Return matched to activate this ability matched to activate this ability.
	 * As default, return null.
	 * 
	 * @return event matched to activate this ability
	 */
	public MEventListener eventComing() {
		return eventComing;
	}

	/**
	 * Set the new event for this ability.
	 * 
	 * @param event
	 *          the new event for this ability.
	 */
	public void setEvent(MEventListener event) {
		this.eventComing = event;
	}

	/**
	 * return a copy of this ability <br>
	 * TODO remove parameter container since it is not used in this constructor As
	 * default, return null
	 * 
	 * @param container
	 *          is not used here
	 * @return copy of this ability
	 */
	public Ability clone(MCard container) {
		return null;
	}

	/**
	 * Return a MTriggeredCard representing this ability. This clone should used
	 * to be added into the triggered buffer zone of player controlling this
	 * ability.
	 * 
	 * @param context
	 *          the attached context of this ability
	 * @return a TriggeredCard object built from this and the specified context
	 */
	public TriggeredCard getTriggeredClone(ContextEventListener context) {
		return new TriggeredCard(this, context, StackManager.abilityID);
	}

	/**
	 * Return a MTriggeredCard representing this ability. This clone should used
	 * to be added into the triggered buffer zone of player controlling this
	 * ability.
	 * 
	 * @param context
	 *          the attached context of this ability
	 * @return a TriggeredCardChoice object built from this and the specified
	 *         context
	 */
	public TriggeredCardChoice getTriggeredCloneChoice(
			ContextEventListener context) {
		return new TriggeredCardChoice(this, context, StackManager.abilityID);
	}

	public void resolveStack() {
		if (StackManager.isEmpty()) {
			// the stack is empty, we resolve the stack as normal
			StackManager.idActivePlayer = StackManager.idCurrentPlayer;
			StackManager.resolveStack();
		} else {
			// re check waiting triggered abilities
			StackManager.activePlayer().waitTriggeredBufferChoice(true);
		}
	}

	/**
	 * called when this ability is going to be triggered This method would add
	 * this ability to the triggered zone, or perform another play action
	 * 
	 * @param context
	 *          the context needed by event activated
	 * @return true if this ability has been added to the triggered buffer zone,
	 *         return false otherwise
	 */
	public boolean triggerIt(ContextEventListener context) {
		return true;
	}

	@Override
	public String toString() {
		return name == null ? this.getClass().getName() + "-- name = ??"
				: getName();
	}

	/**
	 * Return the HTML code representing this ability.
	 * 
	 * @param context
	 *          the context needed by event activated
	 * @return the HTML code representing this ability.
	 * @since 0.85 Event is displayed
	 */
	public String toHtmlString(ContextEventListener context) {
		return toString();
	}

	/**
	 * Return ability html title. Type of ability and a few other information
	 * 
	 * @return ability html title. Type of ability and a few other information
	 */
	public String getAbilityTitle() {
		return "<br>" + LanguageManager.getString("card.name") + " : " + getCard();
	}

	public void removeFromManager() {
		eventComing().removeFromManager(this);
		if (linkedAbilities != null) {
			for (Ability ability : linkedAbilities) {
				ability.removeFromManager();
			}
		}
	}

	/**
	 * Add this ability to the looked for events. Linked abilities are also
	 * registered.
	 */
	public void registerToManager() {
		eventComing().registerToManager(this);
		if (linkedAbilities != null) {
			for (Ability ability : linkedAbilities) {
				ability.registerToManager();
			}
		}
	}

	/**
	 * Return the controller of this ability
	 * 
	 * @return the controller of this ability
	 */
	public Player getController() {
		return getCard().getController();
	}

	/**
	 * Add a linked ability.
	 * 
	 * @param ability
	 *          a linked ability to add.
	 */
	public final void addLinkedAbility(Ability ability) {
		if (linkedAbilities == null) {
			linkedAbilities = new ArrayList<Ability>();
		}
		linkedAbilities.add(ability);
	}

	/**
	 * Compare two abilities the specified ability to the TBZ.
	 * 
	 * @param thisContext
	 *          the attached context of this ability
	 * @param ability
	 *          the ability to add
	 * @param context
	 *          the attached context of given ability
	 * @return true if the abilities are functionally equal in this context.
	 */
	public boolean equals(ContextEventListener thisContext, Ability ability,
			ContextEventListener context) {
		return context == ability;
	}

	@Override
	public final boolean equals(Object object) {
		return object == this;
	}

	@Override
	public int hashCode() {
		if (name == null)
			return super.hashCode();
		return name.hashCode();
	}

	/**
	 * Is this ability is played as a spell.
	 * 
	 * @return <code>true</code> if this ability is played as a spell.
	 */
	public boolean isPlayAsSpell() {
		if (playAsSpell == TrueFalseAuto.AUTO)
			return !getCard().isSameIdZone(IdZones.PLAY);

		return playAsSpell.getValue();
	}

	/**
	 * Return <code>true</code> if this ability is a system ability.
	 * 
	 * @return <code>true</code> if this ability is a system ability.
	 */
	public boolean isSystemAbility() {
		return false;
	}

	/**
	 * Return a String identifying this ability with the name and/or card name.
	 * 
	 * @param context
	 *          the current context of this ability.
	 * @return a String identifying this ability with the name and/or card name.
	 */
	public abstract String getLog(ContextEventListener context);

	/**
	 * The attached activation event.
	 */
	protected MEventListener eventComing;

	/**
	 * The optimizer to use to manage the 'add' method to the TBZ
	 */
	public Optimization optimizer;

	/**
	 * The resolution selector choose the right abstract zone where an hidden
	 * ability would be added.
	 */
	public Priority priority;

	/**
	 * Ability name
	 */
	protected final String name;

	/**
	 * The ability picture to use. Only if the ability is not hidden.
	 */
	protected final String pictureName;

	/**
	 * The linked abilities to this ability. Registering/Unregistering this
	 * ability causes the same to these linked abilities.
	 */
	protected Collection<Ability> linkedAbilities;

	/**
	 * If this ability is played as a copy of card or added to stack
	 */
	protected TrueFalseAuto playAsSpell;
}