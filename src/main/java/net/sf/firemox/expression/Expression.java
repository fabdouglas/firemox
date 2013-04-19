/*
 * Created on Nov 8, 2004 
 * Original filename was Expression.java
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
 * 
 */
package net.sf.firemox.expression;

import java.util.List;
import java.util.Map;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.test.Test;

/**
 * This class represents an expression. Value type is <code>int</code> by
 * default but it can also be an object. If the method <code>getObject()</code>
 * is not overridden, the given object value will be the <code>Integer</code>
 * representation of the <code>int</code> value.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.80
 */
public abstract class Expression {

	/**
	 * Creates a new instance of <code>Expression</code>.
	 */
	protected Expression() {
		super();
	}

	/**
	 * Returns the object value of this expression.
	 * 
	 * @param ability
	 *          the ability owning this test. The card component of this ability
	 *          should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @param context
	 *          the context event listener
	 * @return the object value of this expression
	 */
	public Object getObject(Ability ability, Target tested,
			ContextEventListener context) {
		return Integer.valueOf(getValue(ability, tested, context));
	}

	/**
	 * Returns the class of the object value of this expression.
	 * 
	 * @return the class of the object value of this expression
	 */
	public Class<?> getObjectClass() {
		return int.class;
	}

	/**
	 * Returns the integer value of this expression
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @param context
	 *          is the context attached to this test.
	 * @return the integer value of this expression
	 */
	public abstract int getValue(Ability ability, Target tested,
			ContextEventListener context);

	/**
	 * Returns the integer value of this expression exactly as it will be when the
	 * ability will be executed.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param tested
	 *          the tested card
	 * @param context
	 *          is the context attached to this test.
	 * @return the integer value of this expression. Returns -1 if is not
	 *         preemptable.
	 */
	public int getPreemptionValue(Ability ability, Target tested,
			ContextEventListener context) {
		return getValue(ability, tested, context);
	}

	/**
	 * Returns this expression where values depending on values of this action
	 * have been replaced.
	 * 
	 * @param values
	 *          are reference values.
	 * @return a parsed test.
	 * @since 0.85
	 */
	public Expression getConstraintExpression(Map<String, Expression> values) {
		return this;
	}

	/**
	 * Adds to the specified list, the events modifying the result of this test.
	 * 
	 * @param res
	 *          is the list of events to fill
	 * @param source
	 *          is the card source of event
	 * @param globalTest
	 *          the optional global test to include in the event test.
	 */
	public void extractTriggeredEvents(List<MEventListener> res, MCard source,
			Test globalTest) {
		// Nothing to do
	}

	/**
	 * Is this expression is a constant.
	 * 
	 * @return true if this expression is a constant.
	 */
	public boolean isConstant() {
		return false;
	}

	/**
	 * Return true if the associated value can be evaluated without ability
	 * context.
	 * 
	 * @return true if the associated value can be evaluated without ability
	 *         context.
	 */
	public boolean canBePreempted() {
		return true;
	}

}
