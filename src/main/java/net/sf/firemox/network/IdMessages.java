/*
 * IdMessages.java 
 * Created on 31 oct. 2003
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
package net.sf.firemox.network;

/**
 * @author Fabrice Daugan
 * @since 0.2c
 */
public interface IdMessages {
	/**
	 * Comment for <code>STR_PASSWD</code>
	 */
	String STR_PASSWD = "?PASSWD?";

	/**
	 * Comment for <code>STR_OK</code>
	 */
	String STR_OK = "OK";

	/**
	 * Comment for <code>STR_NOPASSWD</code>
	 */
	String STR_NOPASSWD = "";

	/**
	 * Comment for <code>STR_WRONGPASSWD</code>
	 */
	String STR_WRONGPASSWD = "WRONGPASSWD";

	/**
	 * The wrong version message.
	 */
	String STR_WRONGVERSION = "WRONGVERSION";

	/**
	 * Comment for <code>STR_DISCONNECT</code>
	 */
	String STR_DISCONNECT = "?DISCONNECT?";

	/**
	 * Comment for <code>STR_NEED_ACK</code>
	 */
	String STR_NEED_ACK = "?";

	/**
	 * Comment for <code>STR_DONE</code>
	 */
	String STR_DONE = "DONE";

	/**
	 * Comment for <code>MAX_PLAYERS</code>
	 */
	int MAX_PLAYERS = 4;

	/**
	 * Comment for <code>TIME_OUT</code>
	 */
	int TIME_OUT = 2000;
}