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
package net.sf.firemox.action.target;

import java.util.HashMap;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.TestOn;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.93
 */
public class SingletonTarget extends AbstractTarget {

	/**
	 * New instance of this class providing the action name and the selection.
	 * 
	 * @param actionName
	 *          the action name.
	 * @param on
	 *          the selection.
	 */
	private SingletonTarget(String actionName, TestOn on) {
		super(actionName);
		this.on = on;
	}

	/**
	 * Return the instance of this class corresponding to the given selecttion.
	 * 
	 * @param on
	 *          the selection.
	 * @return the SingletonTarget instance
	 */
	public static SingletonTarget getInstance(TestOn on) {
		SingletonTarget instance = instances.get(on);
		if (instance != null)
			return instance;
		instance = new SingletonTarget("", on);
		instances.put(on, instance);
		return instance;
	}

	private TestOn on;

	private static Map<TestOn, SingletonTarget> instances = new HashMap<TestOn, SingletonTarget>();

	@Override
	public Target getAbstractTarget(ContextEventListener context, Ability ability) {
		return on.getTargetable(ability, context, null);
	}

	@Override
	public String toString(Ability ability) {
		if (actionName == null)
			return on.toString();
		return actionName;
	}

	@Override
	public String toHtmlString(Ability ability, ContextEventListener context) {
		if (actionName == null)
			return on.toHtmlString(ability, context);
		return actionName;
	}
}
