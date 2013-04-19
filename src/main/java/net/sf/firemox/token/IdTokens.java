/*
 *   Firemox is a turn based strategy simulator 
 *   Copyright (C) 2003-2007 Fabrice Daugan
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package net.sf.firemox.token;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public interface IdTokens {

	/**
	 * Stack register size
	 */
	int STACK_REGISTER_SIZE = 2;

	/**
	 * Size of card's registers
	 */
	int CARD_REGISTER_SIZE = 18;

	/**
	 * Size of delayed's registers
	 */
	int ABILITY_REGISTER_SIZE = 2;

	/**
	 * Size of player registers
	 */
	int PLAYER_REGISTER_SIZE = 32;

	/**
	 * Target is a card
	 */
	int CARD = 0x1000;

	/**
	 * Target is a player
	 */
	int PLAYER = 0x2000;

	/**
	 * Correspond to the cards/players targetted. They are present in the target
	 * list.
	 */
	int TARGET = CARD | PLAYER; // = 0x3000;

	/**
	 * Access to stack registers
	 */
	int STACK = 0x7000;

	/**
	 * Special access to the static value correponding to the turn number. The
	 * first turn should be 0.
	 */
	int TURN_ID = 0x001F;

	/**
	 * Access to the saved card.
	 */
	int DELAYED_REGISTERS = 0xF400;

	/**
	 * Special access to the static values. Actions may access or modify a list of
	 * registers. These registers do not interact with the play, and are not
	 * modified by the MP engine. It's to the responsability of abilities to
	 * manage entirely these registers. There are 256 available registers.
	 */
	int STATIC_REGISTER = 0x8100;

	/**
	 * Target is saved object
	 */
	int PRIVATE_NAMED_TARGETABLE = 0x1FFA;

	/**
	 * access to the card containing the current spell/ability
	 */
	int MYSELF = 0xFFFC;

	/**
	 * Target is a player or a card
	 */
	int DEALTABLE = 0x1FFD;

	/**
	 * The register index of stack registers where the answer of the last message
	 * box. The answer values can be IdAnswer#YES or IdAnswer#NO
	 * 
	 * @see IdAnswer#yes
	 * @see IdAnswer#no
	 */
	int MSG_ANSWER_INDEX = 0;

	/**
	 * Special access to the whole mana pool player and manacost of card.
	 */
	int MANA_POOL = 0xFF;

	/**
	 * Access the required mana of current spell.
	 */
	int REQUIRED_MANA = 0x1FF9;

	/**
	 * Access to identifier of player
	 */
	int ID = 0x4400;

	/**
	 * Index of card power information
	 */
	int POWER = 0x0B;

	/**
	 * Index of card toughness information
	 */
	int TOUGHNESS = 0x0C;

	/**
	 * First index of card where any modification does not generate any event
	 */
	int FIRST_FREE_CARD_INDEX = 0x0D;

	/**
	 * Index of player poison information
	 */
	int POISON = 0x0B;

	/**
	 * Index of player life information
	 */
	int LIFE = 0x0C;

	/**
	 * Value for playable idCard for MeventCanICast.
	 */
	int MANA_ABILITY = 0;

	/**
	 * Comment for <code>registerIndexName</code>
	 */
	String[] REGISTER_INDEX_NAMES = { "mana-pool", "id", "yes", "no", "?" };

	/**
	 * Comment for <code>registerIndexValue</code>
	 */
	int[] REGISTER_INDEX_VALUES = { MANA_POOL, ID, 1, 0, IdConst.ALL };

}