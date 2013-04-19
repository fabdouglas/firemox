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
package net.sf.firemox.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents all atomic actions necessary to implement all possible abilities
 * of the Magic play.
 * 
 * @author Fabrice Daugan
 * @since 0.1a
 */
public enum Actiontype {

	/**
	 * @see net.sf.firemox.action.PayMana
	 */
	PAY_MANA,

	/**
	 */
	MOVE_PLAYER_CARD,

	/**
	 * @see net.sf.firemox.action.GiveMana
	 */
	GIVE_MANA_BASIC,

	/**
	 * @see net.sf.firemox.action.Target
	 */
	TARGET,

	/**
	 */
	ASSIGN_DAMAGE_TARGET,

	/**
	 */
	ASSIGN_DAMAGE_FROM_TO,

	/**
	 */
	TAP,

	/**
	 */
	SHUFFLE,

	/**
	 * @see net.sf.firemox.action.InputChoice
	 */
	CHOICE,

	/**
	 * @see net.sf.firemox.action.Input
	 */
	MSG,

	/**
	 */
	CREATE_CARD,

	/**
	 */
	SKIP_PHASE,

	/**
	 */
	FORCE_PLAY,

	/**
	 */
	LOSE_GAME,

	/**
	 * @see net.sf.firemox.action.Hop
	 */
	HOP,

	/**
	 */
	MOVE_CARD,

	/**
	 */
	ASSIGN_DAMAGE_SOURCE_DEST,

	/**
	 * @see net.sf.firemox.action.WaitActivatedChoice
	 */
	WAIT_ACTIVATED_CHOICE,

	/**
	 * @see net.sf.firemox.action.WaitTriggeredBufferChoice
	 */
	WAIT_TRIGGERED_BUFFER_CHOICE,

	/**
	 */
	UPDATE_LIFE,

	/**
	 */
	UPDATE_TOUGHNESS,

	/**
	 */
	LETHAL_DAMAGE,

	/**
	 */
	UNREGISTER_THIS,

	/**
	 */
	ATTACH_LIST,

	/**
	 */
	ATTACH,

	/**
	 */
	ABORT,

	/**
	 */
	ADD_OBJECT,

	/**
	 * @see net.sf.firemox.action.MoveObject
	 */
	MOVE_OBJECT,

	/**
	 * @see net.sf.firemox.action.RemoveObject
	 */
	REMOVE_OBJECT,

	/**
	 * @see net.sf.firemox.action.Repeat
	 */
	REPEAT_ACTION,

	/**
	 */
	IF_THEN_ELSE,

	/**
	 * @see net.sf.firemox.action.ModifyTargetableRegister
	 */
	MODIFY_TARGETABLE_REGISTER,

	/**
	 */
	ZONE_VISIBILITY,

	/**
	 */
	MODIFY_STACK_REGISTER,

	/**
	 */
	MODIFY_STATIC_REGISTER,

	/**
	 * @see net.sf.firemox.action.RemoveMe
	 */
	REMOVE_ME,

	/**
	 */
	ADD_MODIFIER,

	/**
	 */
	SET_ID_CARD,

	/**
	 * @see net.sf.firemox.action.RefreshModifier
	 */
	REFRESH_MODIFIER,

	/**
	 * @see net.sf.firemox.action.AddModifierFromStaticModifier
	 */
	REFRESH_STATIC_MODIFIER,

	/**
	 * @see net.sf.firemox.action.DetachMe
	 */
	DETACH_ME,

	/**
	 */
	NEXT_CURRENT_PLAYER,

	/**
	 */
	NEXT_PHASE,

	/**
	 */
	CREATE_ABILITY,

	/**
	 */
	CREATE_MODIFIER,

	/**
	 * @see net.sf.firemox.action.Sound
	 */
	SOUND,

	/**
	 */
	MODIFY_ABILITY_REGISTER,

	/**
	 */
	MODIFY_TARGET_LIST_REGISTER,

	/**
	 * @see net.sf.firemox.action.ActionFactory
	 */
	ADD_ABILITY,

	/**
	 * @see net.sf.firemox.action.ActionFactory
	 */
	RESOLVE_HIDDEN,

	/**
	 * @see net.sf.firemox.action.ActionFactory
	 */
	INPUT_NUMBER,

	/**
	 * @see net.sf.firemox.action.ActionFactory
	 */
	INPUT_COLOR,

	/**
	 * @see net.sf.firemox.action.ActionFactory
	 */
	INPUT_ZONE,

	/**
	 * @see net.sf.firemox.action.ActionFactory
	 */
	INPUT_PROPERTY,

	/**
	 */
	TARGET_LIST,

	/**
	 */
	INT_LIST,

	/**
	 */
	GENERATE_EVENT,

	/**
	 * @see net.sf.firemox.action.GiveMana
	 */
	GIVE_MANA_MULTI,

	/**
	 * 
	 */
	TARGET_ALL,

	/**
	 */
	FACE,

	/**
	 * 
	 */
	OBJECT_MAP,

	/**
	 * 
	 */
	TARGET_RANDOM,

	/**
	 * 
	 */
	MODIFY_REQUIRED_MANA,

	/**
	 * 
	 */
	COPY_CARD,

	/**
	 * 
	 */
	RESTORE_CARD,

	/**
	 * 
	 */
	PILE;

	/**
	 * Write this enumeration to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void write(OutputStream out) throws IOException {
		out.write(ordinal());
	}

	/**
	 * Read and return the enumeration from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given input stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	static Actiontype valueOf(InputStream input) throws IOException {
		return values()[input.read()];
	}
}