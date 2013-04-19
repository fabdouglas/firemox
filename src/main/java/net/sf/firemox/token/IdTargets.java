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
package net.sf.firemox.token;

/**
 * The possible modes for target
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82 manage "targeted" event generation for target action
 */
public interface IdTargets {

	/**
	 * Indicates that the active player (you) chooses each target.
	 */
	int CHOOSE = 0x00;

	/**
	 * Indicates that the previously targeted player event chooses the targets.
	 */
	int TARGET_CHOOSE = 0x01;

	/**
	 * Indicates that the opponent chooses the targets, not you.
	 */
	int OPPONENT_CHOOSE = 0x02;

	/**
	 * Indicates that the player concerned by the current event chooses the
	 * targets.
	 */
	int CONTEXT_CHOOSE = 0x03;

	/**
	 * Indicates that the player identifier saved into the current ability
	 * register chooses the targets.
	 */
	int STACK0_CHOOSE = 0x04;

	/**
	 * 
	 */
	int ATTACHED_TO_CONTROLLER_CHOOSE = 0x05;

	/**
	 * 
	 */
	int RESERVED_0 = 0x06;

	/**
	 * 
	 */
	int RESERVED_1 = 0x07;

	/**
	 * Indicates that the target list is filled randomly.
	 */
	int RANDOM = 0x0E;

	/**
	 * Indicates that there is no choice, all valid target are added to the list
	 */
	int ALL = 0x0F;

	/**
	 * Indicates that there is no choice, all valid target are added to the list
	 */
	int ALLOW_CANCEL = 0x08;

	/**
	 * The "targeted" event is generated only if a player has to choose a target.
	 */
	int RAISE_EVENT_AUTO = 0x00;

	/**
	 * The "targeted" event is always generated
	 */
	int RAISE_EVENT_FORCE = 0x10;

	/**
	 * The "targeted" event is never generated
	 */
	int RAISE_EVENT_NOT = 0x20;

	/**
	 * The target(s) would be added into the private target list of current
	 * ability.
	 */
	int UNUSED_0 = 0x40;

	/**
	 * The target(s) would be added into the first ability found in the stack
	 * having the same name than the current ability's name.
	 */
	int UNUSED_1 = 0x80;

	/**
	 * The avaliable mode names for target
	 */
	String[] MODE_NAMES = { "choose", "random", "opponentchoose",
			"contextchoose", "all", "stack0-choose", "target-choose",
			"attachedto.controller-choose" };

	/**
	 * The avaliable mode values for target
	 */
	int[] MODE_VALUES = { CHOOSE, RANDOM, OPPONENT_CHOOSE, CONTEXT_CHOOSE, ALL,
			STACK0_CHOOSE, TARGET_CHOOSE, ATTACHED_TO_CONTROLLER_CHOOSE };
}