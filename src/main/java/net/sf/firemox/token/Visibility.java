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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.firemox.clickable.target.player.Player;
import net.sf.firemox.stack.StackManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public enum Visibility {

	/**
	 * 
	 */
	PUBLIC,

	/**
	 * 
	 */
	PRIVATE,

	/**
	 * 
	 */
	HIDDEN;

	/**
	 * Is the component is visible for the specified player.
	 * 
	 * @param controller
	 *          the controller.
	 * @param lookingPlayer
	 *          the player looking the component.
	 * @return true if the component is visible for the specified player.
	 */
	public final boolean isVisibleFor(Player controller, Player lookingPlayer) {
		if (lookingPlayer.isYou()) {
			return isVisibleForYou(controller);
		}
		return isVisibleForOpponent(controller);
	}

	/**
	 * Is the component is visible for the specified player.
	 * 
	 * @param controller
	 *          the controller.
	 * @return true if the component is visible for you.
	 */
	public boolean isVisibleForYou(Player controller) {
		switch (this) {
		case PUBLIC:
			return true;
		case PRIVATE:
			return controller.isYou();
		case HIDDEN:
		default:
			return false;
		}
	}

	/**
	 * Is the component is visible for the specified player.
	 * 
	 * @param controller
	 *          the controller.
	 * @return true if the component is visible for you.
	 */
	public boolean isVisibleForOpponent(Player controller) {
		switch (this) {
		case PUBLIC:
			return true;
		case PRIVATE:
			return !controller.isYou();
		case HIDDEN:
		default:
			return false;
		}
	}

	/**
	 * Return the increased visibility for this player
	 * 
	 * @param controller
	 *          The controller of component to modify.
	 * @param visibilityChange
	 *          The visibility change.
	 * @return the increased visibility for this player
	 */
	public Visibility increaseFor(Player controller,
			VisibilityChange visibilityChange) {
		switch (this) {
		case PUBLIC:
			return PUBLIC;
		case PRIVATE:
		case HIDDEN:
		default:
			switch (visibilityChange) {
			case controller:
				return PRIVATE;
			case everyone:
				return PUBLIC;
			case opponent:
				// TODO Add a pattern for opponent-private visibility
				return PUBLIC;
			case you:
			default:
				if (StackManager.getSpellController() == controller)
					return PRIVATE;
				// TODO Add a pattern for opponent-private visibility
				return PUBLIC;
			}
		}
	}

	/**
	 * Return the increased visibility for this player
	 * 
	 * @param controller
	 *          The controller of component to modify.
	 * @param visibilityChange
	 *          The visibility change.
	 * @return the increased visibility for this player
	 */
	public Visibility decreaseFor(Player controller,
			VisibilityChange visibilityChange) {
		switch (this) {
		case PUBLIC:
			switch (visibilityChange) {
			case controller:
				// TODO Add a pattern for opponent-private visibility
				return HIDDEN;
			case everyone:
				return HIDDEN;
			case opponent:
				return PRIVATE;
			case you:
			default:
				if (StackManager.getSpellController() == controller)
					// TODO Add a pattern for opponent-private visibility
					return HIDDEN;
				return PRIVATE;
			}
		case PRIVATE:
			switch (visibilityChange) {
			case controller:
			case everyone:
				return HIDDEN;
			case opponent:
				return PRIVATE;
			case you:
			default:
				if (StackManager.getSpellController() == controller)
					return HIDDEN;
				// TODO Add a pattern for opponent-private visibility
				return PRIVATE;
			}
		case HIDDEN:
		default:
			return HIDDEN;
		}
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
	public static Visibility deserialize(InputStream input) throws IOException {
		return values()[input.read()];
	}

	/**
	 * Write this enumeration to the given output stream.
	 * 
	 * @param out
	 *          the stream this enumeration would be written.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public void serialize(OutputStream out) throws IOException {
		out.write(ordinal());
	}

}
