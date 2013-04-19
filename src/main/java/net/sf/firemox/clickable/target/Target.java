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
package net.sf.firemox.clickable.target;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import net.sf.firemox.action.listener.WaitingAbility;
import net.sf.firemox.clickable.Clickable;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.ability.UserAbility;
import net.sf.firemox.clickable.target.card.Damage;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.modifier.ControllerModifier;
import net.sf.firemox.modifier.RegisterIndirection;
import net.sf.firemox.modifier.RegisterModifier;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.token.IdCommonToken;

/**
 * Represents a target : player or card.
 * 
 * @see net.sf.firemox.xml.tbs.Card
 * @see net.sf.firemox.clickable.target.player.Player
 * @version 0.91
 * @since 0.3
 * @since 0.90 a map of objects {String, Target} is added.
 * @since 0.91 removed properties instance variable to CardModel
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @author <a href="mailto:kismet-sl@users.sourceforge.net">Stefano "Kismet"
 *         Lenzi</a>
 */
public abstract class Target extends Clickable implements ActionListener {

	/**
	 * Private property key "creator"
	 */
	static final String KEY_CREATOR = "creator";

	/**
	 * Creates a new instance of MTargetable <br>
	 */
	public Target() {
		super();
		isHighLighted = false;
		setBackground(Color.black);
	}

	/**
	 * remove all damages on this card
	 */
	public void clearDamages() {
		for (int i = getComponentCount(); i-- > 0;) {
			if (getComponent(i) instanceof Damage) {
				remove(getComponent(i));
			}
			registers[IdCommonToken.DAMAGE] = 0;
		}
	}

	@Override
	public abstract String toString();

	/**
	 * return true if this target is a player
	 * 
	 * @return true if this target is a player
	 */
	public final boolean isPlayer() {
		return !isCard();
	}

	/**
	 * indicates if this target is a card
	 * 
	 * @return true if this target is a card
	 */
	public abstract boolean isCard();

	/**
	 * An ActionListener that listens to the ability choice
	 * 
	 * @param evt
	 *          the event
	 */
	public void actionPerformed(ActionEvent evt) {
		// Only trigger on context menu items
		if (evt.getSource() instanceof JMenuItem) {
			StackManager.noReplayToken.take();
			try {
				if (StackManager.idHandedPlayer == 0) {
					for (int j = TargetFactory.abilitiesMenu.getComponentCount(); j-- > 0;) {
						if (TargetFactory.abilitiesMenu.getComponent(j) == evt.getSource()) {
							// is the clicked ability an advanced one ?
							boolean advancedMode = false;
							for (int i = j; i-- > 0;) {
								if (TargetFactory.abilitiesMenu.getComponent(i) instanceof JSeparator) {
									// this is an advanced ability
									advancedMode = true;
									((UserAbility) ((WaitingAbility) StackManager.actionManager.currentAction)
											.advancedAbilitiesOf(TargetFactory.triggerTargetable)
											.get(j - i - 1)).mouseClicked(j - i + 127);
									break;
								}
							}
							if (!advancedMode) {
								// this is not an advanced ability
								((UserAbility) ((WaitingAbility) StackManager.actionManager.currentAction)
										.abilitiesOf(TargetFactory.triggerTargetable).get(j))
										.mouseClicked(j);
							}
							break;
						}
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				StackManager.noReplayToken.release();
			}
		}
	}

	/**
	 * The border will be highligthed to a color identifying it easily as a token
	 * component.
	 */
	public final void tokenize() {
		highLight(TargetFactory.TOKENIZE_COLOR);
	}

	/**
	 * The border will be highligthed to a color identifying it easily as a target
	 * component.
	 * 
	 * @param highlightedZones
	 *          the set of highlighted zone.
	 */
	public void targetize(boolean... highlightedZones) {
		highLight(TargetFactory.TARGET_COLOR);
	}

	/**
	 * Highlight as "target" the list of target
	 * 
	 * @param list
	 *          the list of target to highlight.
	 * @param highlightedZones
	 *          the list of highlighted zones
	 */
	public static void targetize(List<Target> list, boolean[] highlightedZones) {
		for (Target target : list) {
			target.targetize(highlightedZones);
		}
	}

	/**
	 * Return the value corresponding to the true register index.
	 * 
	 * @param index
	 *          the register index
	 * @return the value corresponding to the true register index.
	 */
	public abstract int getValue(int index);

	/**
	 * Return the value corresponding to the true register index.
	 * 
	 * @param index
	 *          the register index
	 * @return the value corresponding to the true register index.
	 */
	public int getValueIndirection(int index) {
		return getValue(index);
	}

	/**
	 * Add a modifier to this object
	 * 
	 * @param modifier
	 *          the modifier to add to this object
	 * @param index
	 *          is the modifier register index
	 */
	public void addModifier(RegisterModifier modifier, int index) {
		if (registerModifiers[index] == null) {
			registerModifiers[index] = modifier;
		} else {
			registerModifiers[index].addModifier(modifier);
		}
	}

	/**
	 * Add a modifier to this object
	 * 
	 * @param modifier
	 *          the indirection modifier to add to this object
	 * @param index
	 *          is the modifier register index
	 */
	public void addModifier(RegisterIndirection modifier, int index) {
		if (indirections[index] == null) {
			indirections[index] = modifier;
		} else {
			indirections[index].addModifier(modifier);
		}
	}

	/**
	 * Is this target an ability of the given type? if the given type is "any" it
	 * tests if it is an ability
	 * 
	 * @param abilityType
	 *          type of ability to test
	 * @return true if this target is an ability of the given type
	 */
	public abstract boolean isAbility(int abilityType);

	/**
	 * Is this target a spell
	 * 
	 * @return true if this target is a spell
	 */
	public abstract boolean isSpell();

	/**
	 * Remove a register modifier from this component.
	 * 
	 * @param modifier
	 *          the register modifier to remove.
	 * @param index
	 *          index of register to remove.
	 */
	public abstract void removeModifier(RegisterModifier modifier, int index);

	/**
	 * Remove a register-indirection modifier from this component.
	 * 
	 * @param indirection
	 *          the register-indirection modifier to remove.
	 * @param index
	 *          index of register indirection to remove.
	 */
	public abstract void removeModifier(RegisterIndirection indirection, int index);

	/**
	 * Return this target as it was at the given timestamp.
	 * 
	 * @param timeStamp
	 *          the timestamp number.
	 * @return this target as it was at the given timestamp.
	 */
	public abstract Target getLastKnownTargetable(int timeStamp);

	/**
	 * Return the original target without looking for the timestamp number.
	 * 
	 * @return the original target without looking for the timestamp number.
	 */
	public Target getOriginalTargetable() {
		return this;
	}

	/**
	 * Return the named object. If the object has not been found,
	 * <code>null</code> value is returned.
	 * 
	 * @param objectName
	 *          the searched object's name
	 * @return the named object.
	 */
	public Target getPrivateNamedObject(String objectName) {
		if (KEY_CREATOR.equals(objectName)) {
			return getCreator();
		}
		if (privateNamedObjects == null) {
			return null;
		}
		return privateNamedObjects.get(objectName);
	}

	/**
	 * Return the creator of this target component. May be null if has not been
	 * set.
	 * 
	 * @return the creator of this target component.
	 */
	public MCard getCreator() {
		return creator;
	}

	/**
	 * Set the creator of this target component.
	 * 
	 * @param creator
	 *          the card creating this card. May be null.
	 */
	public void setCreator(MCard creator) {
		this.creator = creator;
	}

	/**
	 * Remove the named object.
	 * 
	 * @param objectName
	 *          the searched object's name
	 */
	public void removePrivateNamedObject(String objectName) {
		if (privateNamedObjects != null) {
			privateNamedObjects.remove(objectName);
		}
	}

	/**
	 * Add an object to this target. This object will be accessible with the
	 * specified name.
	 * 
	 * @param objectName
	 *          the object's name to save.
	 * @param target
	 *          the target to save.
	 */
	public void addPrivateNamedObject(String objectName, Target target) {
		if (privateNamedObjects == null) {
			privateNamedObjects = new HashMap<String, Target>();
		}
		privateNamedObjects.put(objectName, target);
	}

	/**
	 * Remove all stored objects.
	 */
	public void clearPrivateNamedObject() {
		if (privateNamedObjects != null) {
			privateNamedObjects.clear();
			privateNamedObjects = null;
		}
	}

	/**
	 * Return a cloned instance of map of objects of this target.
	 * 
	 * @return a cloned instance of map of objects of this target.
	 */
	public Map<String, Target> getPrivateNamedObjects() {
		if (privateNamedObjects == null) {
			return null;
		}
		return new HashMap<String, Target>(privateNamedObjects);
	}

	/**
	 * Return the id of this component. For instance, this id is only relevant for
	 * players.
	 * 
	 * @return the id of this component.
	 */
	public int getId() {
		return 0;
	}

	/**
	 * Return the timestamp value of this target.
	 * 
	 * @return the timestamp value of this target.
	 */
	public abstract int getTimestamp();

	/**
	 * Add a reference to this target.
	 */
	public abstract void addTimestampReference();

	/**
	 * Decrement the reference counter for the current timestamp of this card.
	 * 
	 * @param timestamp
	 *          is the reference to decrement.
	 */
	public abstract void decrementTimestampReference(int timestamp);

	/**
	 * Current regiters of this target
	 */
	public int[] registers;

	/**
	 * The registerModifiers on this object
	 */
	public RegisterModifier[] registerModifiers;

	/**
	 * The ControllerModifier on this object
	 */
	public ControllerModifier controllerModifier;

	/**
	 * The registerModifiers on this object
	 */
	public RegisterIndirection[] indirections;

	/**
	 * Accessible objects of this component. Is <code>null</code> while no
	 * object has been saved. This map is destroyed when this component move.
	 */
	private Map<String, Target> privateNamedObjects;

	/**
	 * The creator of this target component.
	 */
	private MCard creator;

	/**
	 * The cached abilities of this component.
	 */
	public List<Ability> cachedAbilities;
}
