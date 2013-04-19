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
package net.sf.firemox.stack;

import java.util.ArrayList;
import java.util.List;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.Targeted;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdTargets;
import net.sf.firemox.tools.Log;

/**
 * Represents list of targets : player(s) and/or card(s).
 * 
 * @see net.sf.firemox.xml.tbs.Card
 * @see net.sf.firemox.clickable.target.player.Player
 * @since 0.2
 * @since 0.53 manage the abortion when resolving a spell
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public class TargetedList {

	/**
	 * Creates a new instance of MTargetedList
	 * 
	 * @param source
	 *          the source of this list of targets
	 */
	public TargetedList(Ability source) {
		super();
		// TODO allAbortion = true;
		this.source = source;
	}

	/**
	 * Add a player/card to the targeted list <br>
	 * If the current target options indicates that an event has to be generated
	 * when a target is chosen, the event Targeted is dispatched.
	 * 
	 * @param eventOption
	 *          is the event mode : Force, default, none. Depending this value,
	 *          the added target will be rechecked before resolution process
	 * @param targeted
	 *          the player to add to the list
	 * @param test
	 *          is the used filter. All objects making false this test are removed
	 *          from the specified list and from the constraint really targeted
	 *          list.
	 * @param raiseEvent
	 *          if false, is any case, no event can be triggered.
	 */
	public void addTarget(int eventOption, Target targeted, Test test,
			boolean raiseEvent) {
		if (list.contains(targeted)) {
			Log.warn("The target " + targeted + " is already in the target list");
		}
		list.add(targeted);
		if (eventOption != IdTargets.RAISE_EVENT_NOT) {
			// we mark this target as really targeted
			if (reallyTargeted == null) {
				reallyTargeted = new ArrayList<Target>();
				reallyTargetedTest = new ArrayList<Test>();
			}
			reallyTargeted.add(targeted);
			reallyTargetedTest.add(test);
			if (raiseEvent) {
				Targeted.dispatchEvent(source.getCard(), targeted);
			}
		}
	}

	/**
	 * Return the target placed at index
	 * 
	 * @param index
	 *          the target register to extract from the list
	 * @return the target placed at index
	 */
	public Target get(int index) {
		return list.get(index);
	}

	/**
	 * Return the last target
	 * 
	 * @return the last target
	 */
	public Target getLast() {
		return get(size() - 1);
	}

	/**
	 * Return the first target
	 * 
	 * @return the first target
	 */
	public Target getFirst() {
		return get(0);
	}

	/**
	 * Remove the last target
	 */
	public void removeLast() {
		remove(size() - 1);
	}

	/**
	 * Return a target from the list
	 * 
	 * @param index
	 *          the index of the element to be removed.
	 */
	public void remove(int index) {
		if (reallyTargeted != null) {
			Target removed = list.remove(index);
			final int indexOf = reallyTargeted.indexOf(removed);
			if (indexOf != -1) {
				reallyTargeted.remove(indexOf);
				reallyTargetedTest.remove(indexOf);
			}
		} else {
			list.remove(index);
		}
	}

	/**
	 * Return the specified target from the list
	 * 
	 * @param target
	 *          the target to be removed.
	 */
	public void remove(Target target) {
		list.remove(target);
		if (reallyTargeted != null) {
			final int indexOf = reallyTargeted.indexOf(target);
			if (indexOf != -1) {
				reallyTargeted.remove(indexOf);
				reallyTargetedTest.remove(indexOf);
			}
		}
	}

	/**
	 * Return the specified target from the list
	 * 
	 * @param target
	 *          the target to be removed.
	 */
	public void removeTargeted(Target target) {
		list.remove(target);
		if (reallyTargeted != null) {
			final int indexOf = reallyTargeted.lastIndexOf(target);
			if (indexOf != -1) {
				reallyTargeted.remove(indexOf);
				reallyTargetedTest.remove(indexOf);
			}
		}
	}

	/**
	 * Remove all component but the first.
	 */
	public void removeQueue() {
		for (int i = list.size(); i-- > 1;) {
			remove(i);
		}
	}

	/**
	 * Remove all component but the last.
	 */
	public void removeTail() {
		for (int i = list.size() - 1; i-- > 0;) {
			remove(i);
		}
	}

	/**
	 * Indicates if there are targets
	 * 
	 * @return true if there are targets
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * Retourne la taille de la liste
	 * 
	 * @return number of targets
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Indicates that we're no longer looking for the targets
	 */
	public void clear() {
		list.clear();
		if (reallyTargeted != null) {
			reallyTargeted.clear();
			reallyTargetedTest.clear();
		}
	}

	/**
	 * Recheck the whole list of targets added with raised event.
	 * 
	 * @param ability
	 *          is the ability owning this request. The card component of this
	 *          ability should correspond to the card owning this request too.
	 * @return the amount of remaining object making true the specified test.
	 */
	public int recheckList(Ability ability) {
		if (reallyTargeted == null || reallyTargeted.size() == 0) {
			return 1;
		}
		for (int i = reallyTargeted.size(); i-- > 0;) {
			final Target tested = reallyTargeted.get(i);
			if (!reallyTargetedTest.get(i).test(ability, tested)) {
				// invalid target
				list.remove(tested);
				reallyTargeted.remove(i);
				reallyTargetedTest.remove(i);
			}
		}
		return reallyTargeted.size();
	}

	/**
	 * Return a clone of this list.
	 * 
	 * @return a clone of this list.
	 */
	public TargetedList cloneList() {
		TargetedList clone = new TargetedList(source);
		clone.list = new ArrayList<Target>(list);
		clone.reallyTargeted = reallyTargeted == null ? null
				: new ArrayList<Target>(reallyTargeted);
		clone.reallyTargetedTest = reallyTargetedTest == null ? null
				: new ArrayList<Test>(reallyTargetedTest);
		return clone;

	}

	@Override
	public String toString() {
		return list.toString();
	}

	/**
	 * Returns <code>true</code> if this target list contains the specified
	 * target.
	 * 
	 * @param target
	 *          the searched target.
	 * @param raiseEvent
	 *          when specified, the search target <code>true</code> must match
	 *          with the way the target has been added. May be <code>null</code>.
	 * @return <code>true</code> if this target list contains the specified
	 *         target.
	 */
	public boolean contains(Target target, Boolean raiseEvent) {
		if (raiseEvent != null && raiseEvent.booleanValue()) {
			return reallyTargeted.contains(target);
		}
		if (raiseEvent != null && !raiseEvent.booleanValue()) {
			return !reallyTargeted.contains(target);
		}
		return list.contains(target);
	}

	/**
	 * represents all targeted card(s) and/or player(s)
	 */
	public List<Target> list = new ArrayList<Target>();

	/**
	 * indicates if currently, all targets have been aborted.
	 */
	// TODO private boolean allAbortion;
	/**
	 * Indicates if target was really targeted. Item is null is was not really
	 * targeted, non-null otherwise
	 */
	private List<Target> reallyTargeted = null;

	/**
	 * Indicates if target was really targeted. Item is null is was not really
	 * targeted, non-null otherwise
	 */
	private List<Test> reallyTargetedTest = null;

	/**
	 * Source of this target spell/ability
	 */
	public Ability source;
}