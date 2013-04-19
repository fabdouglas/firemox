/*
 * Created on Sep 3, 2004 
 * Original filename was RemoveModifier.java
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
package net.sf.firemox.clickable.ability;

import net.sf.firemox.event.TriggeredEvent;
import net.sf.firemox.modifier.Unregisterable;
import net.sf.firemox.test.Test;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.86
 */
public class ModifierRemover extends RemoveModifier {

	/**
	 * Creates a new instance of RemoveModifier <br>
	 * 
	 * @param event
	 *          the event attached to this ability.
	 * @param modifier
	 * @param refreshTest
	 */
	public ModifierRemover(TriggeredEvent event, Unregisterable modifier,
			Test refreshTest) {
		super(event, modifier);
		this.refreshTest = refreshTest;
	}

	@Override
	public boolean isMatching() {
		return !refreshTest.test(this, getCard());
	}

	/**
	 * The refreshing test
	 */
	public Test refreshTest;

}