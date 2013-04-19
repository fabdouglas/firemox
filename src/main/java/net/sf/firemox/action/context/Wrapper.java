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
package net.sf.firemox.action.context;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @param <T>
 *          The type of wrapped object.
 * @since 0.86
 */
public class Wrapper<T> implements ActionContext {

	/**
	 * Create a new instance of this class.
	 */
	public Wrapper() {
		super();
	}

	/**
	 * The wrapped object.
	 */
	private T object;

	/**
	 * Return the wrapped object.
	 * 
	 * @return the wrapped object.
	 */
	public T getObject() {
		return object;
	}

	/**
	 * Set the wrapped object.
	 * 
	 * @param object
	 *          the wrapped object.
	 */
	public void setObject(T object) {
		this.object = object;
	}
}
