/*
 * Created on 4 févr. 2005
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
package net.sf.firemox.action.intlist;

import java.io.IOException;
import java.io.InputStream;

import net.sf.firemox.action.Actiontype;
import net.sf.firemox.action.UserAction;
import net.sf.firemox.action.handler.StandardAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.ui.i18n.LanguageManagerMDB;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.82
 */
public abstract class IntList extends UserAction implements StandardAction {

	/**
	 * Create an instance of IntList by reading a file Offset's file must pointing
	 * on the first byte of this action <br>
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>idAction [1]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	protected IntList(InputStream inputFile) throws IOException {
		super(inputFile);
	}

	/**
	 * Read the next TargetList object
	 * 
	 * @param inputFile
	 *          the streamcontaining the definition of next TargetList object.
	 * @return the read TargetList object
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static IntList readNextIntList(InputStream inputFile)
			throws IOException {
		final IdOperation idOperation = IdOperation.deserialize(inputFile);
		switch (idOperation) {
		case REMOVE_FIRST:
			return new IntListRemoveFirst(inputFile);
		case REMOVE_LAST:
			return new IntListRemoveLast(inputFile);
		case REMOVE_QUEUE:
			return new IntListRemoveQueue(inputFile);
		case REMOVE_TAIL:
			return new IntListRemoveTail(inputFile);
		case REMOVE:
			return IntListRemove.readNextIntListRemove(inputFile);
		case CLEAR:
			return new IntListClear(inputFile);
		case SAVE:
			return IntListSave.readNextIntListSave(inputFile);
		case COUNTER:
			return IntListOccurence.readNextIntListOccurence(inputFile);
		case LOAD:
			return new IntListLoad(inputFile);
		case ADD:
		case MULT:
		case DIV_ROUNDED:
		case DIV_TRUNCATED:
		case OR:
		case AND:
		case XOR:
		case ADD_ROUNDED:
		case ADD_TRUNCATED:
		case SET:
			return new IntListOperation(inputFile, idOperation);
		default:
			throw new InternalError("Unknown operation for target-list : "
					+ idOperation);
		}
	}

	@Override
	public final Actiontype getIdAction() {
		return Actiontype.INT_LIST;
	}

	public abstract boolean play(ContextEventListener context, Ability ability);

	@Override
	public abstract String toString(Ability ability);

	@Override
	public final String toHtmlString(Ability ability, int times,
			ContextEventListener context) {
		if (actionName != null && actionName.charAt(0) != '%'
				&& actionName.charAt(0) != '@' && actionName.indexOf("%n") != -1) {
			if (times == 1) {
				return LanguageManagerMDB.getString(actionName.replaceAll("%n", "1"));
			}
			return LanguageManagerMDB.getString(actionName).replaceAll("%n",
					"" + times);
		}
		return "";
	}

}
