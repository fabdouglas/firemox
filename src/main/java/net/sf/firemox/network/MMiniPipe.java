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
package net.sf.firemox.network;

import net.sf.firemox.tools.Log;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.3
 */
public class MMiniPipe {

	/**
	 * Create an instance of MMiniPipe initializing the token to free. Means that
	 * the next call to method <code>take()</code> would'nt block the calling
	 * thread.
	 * 
	 * @see MMiniPipe#take()
	 * @see #MMiniPipe(boolean)
	 */
	public MMiniPipe() {
		this(false);
	}

	/**
	 * Create an instance of MMiniPipe initializing the token to the specified
	 * boolean. Means that the next call to method <code>take()</code> would'nt
	 * block the calling thread only if the specified <code>taken</code> is
	 * false.
	 * 
	 * @param taken
	 *          one token critical resource
	 * @see MMiniPipe#take()
	 */
	public MMiniPipe(boolean taken) {
		this.taken = taken;
	}

	/**
	 * Try to take this token. If the token is already token, the current thread
	 * would be blocked until a threadd release the requested token.
	 */
	public synchronized void take() {
		try {
			while (taken) {
				wait(200);
			}
			taken = true;
		} catch (InterruptedException e) {
			Log.error("mini pipe error");
		}
	}

	/**
	 * Try to take this token. If the token is already token, the current thread
	 * would be blocked until a threadd release the requested token.
	 * 
	 * @return true if has been token.
	 */
	public synchronized boolean takeNoBlock() {
		if (!taken) {
			taken = true;
			return false;
		}
		return false;
	}

	/**
	 * Try to take this token. If the token is already token, the current thread
	 * would be blocked until a threadd release the requested token.
	 */
	public synchronized void release() {
		taken = false;
		notify();
	}

	/**
	 * This boolean represents the token. When it's value is <code>false</code>
	 * that's mean the token is free, so any thread can take it without being
	 * blocked. When it' value is <code>true</code> the next thread would take
	 * this token would be blocked until the current owner release it.
	 */
	protected boolean taken;

}