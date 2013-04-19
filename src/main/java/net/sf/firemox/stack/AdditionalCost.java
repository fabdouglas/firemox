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
package net.sf.firemox.stack;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.sf.firemox.action.ActionFactory;
import net.sf.firemox.action.MAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.action.ToStringHelper;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;

/**
 * An additional cost definition : constraint and added actions.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public class AdditionalCost {

	/**
	 * The constraint to apply this additional cost
	 */
	public final Test constraint;

	/**
	 * The additional cost
	 */
	public final MAction[] cost;

	/**
	 * Creates a new instance of this class <br>
	 * 
	 * @param inputFile
	 *          is the file containing this definition.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public AdditionalCost(InputStream inputFile) throws IOException {
		this.constraint = TestFactory.readNextTest(inputFile);
		this.cost = ActionFactory.readActionList(inputFile, null);
	}

	@Override
	public String toString() {
		return "{" + constraint + ":" + Arrays.toString(cost) + "}";
	}

	/**
	 * Return the HTML code representing this additional cost. If the given
	 * ability is named, it's name will be returned. If not, if existing, the
	 * picture associated to this ability is returned. Otherwise, toHtmlString is
	 * called for each owned actions.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param context
	 *          the context needed by event activated
	 * @return the HTML code representing this additional cost.
	 */
	public String toHtmlString(Ability ability, ContextEventListener context) {
		return ToStringHelper.toHtmlString(ability, cost, context);
	}
}
