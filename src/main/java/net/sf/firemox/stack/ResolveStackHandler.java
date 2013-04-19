/*
 * Created on Jul 27, 2004 
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
package net.sf.firemox.stack;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public interface ResolveStackHandler {

	/**
	 * called when the stack is resolving
	 * 
	 * @see StackManager#resolveStack()
	 */
	void resolveStack();

	/**
	 * Indicates if this ability is immediately after it has been added to the
	 * stack. Note it's not says immediately it has been triggered or playable,
	 * but says it has been activated - so added directly to the stack -, or has
	 * been triggered - so added to the triggered buffer zone - and then has been
	 * selected to be moved to the stack.
	 * 
	 * @return true if this ability is immediately after it has been added to the
	 *         stack.
	 */
	boolean isAutoResolve();

	/**
	 * Indicates if this ability is immediately after it has been added to the
	 * stack (like isAutoResolve), and if no information is displayed or prompted
	 * to the users. Users would not see this ability played.
	 * 
	 * @return true if this ability is immediately after it has been added to the
	 *         stack (like isAutoResolve), and if no information is displayed or
	 *         prompted to the users. Users would not see this ability played.
	 * @see #isAutoResolve()
	 */
	boolean isHidden();
}