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
package net.sf.firemox.operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public enum IdOperation {

	/**
	 * The addition : To get the sum value of a list or add two values.
	 * 
	 * @see net.sf.firemox.operation.Add
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	ADD("add"),

	/**
	 * The affectation
	 * 
	 * @see net.sf.firemox.operation.Set
	 */
	SET("set"),

	/**
	 * The reset <br>
	 * For target-list : 'clear' operation clear the current target list. If
	 * 'list-index' is specified, the target list saved at this index is cleared.
	 * 
	 * @see net.sf.firemox.operation.Clear
	 */
	CLEAR("clear"),

	/**
	 * The multiplication
	 * 
	 * @see net.sf.firemox.operation.Mult
	 */
	MULT("mult"),

	/**
	 * added abstract operation to represent final value. <br>
	 * This value does not appear in the available operators.
	 * 
	 * @see net.sf.firemox.operation.IntValue
	 */
	INT_VALUE("int"),

	/**
	 * The binary and operation
	 * 
	 * @see net.sf.firemox.operation.Add
	 */
	AND("and"),

	/**
	 * The binary or operation
	 * 
	 * @see net.sf.firemox.operation.Or
	 */
	OR("or"),

	/**
	 * The reference value. <br>
	 * There is no associated Operation.
	 * 
	 * @see net.sf.firemox.expression.ReferenceValue
	 */
	REF_VALUE("ref"),

	/**
	 * Indentify a couple : register name + index withoutusing register modifiers.
	 * <br>
	 * There is no associated Operation.
	 * 
	 * @see net.sf.firemox.expression.BaseRegisterIntValue
	 */
	BASE_REGISTER_INT_VALUE("base"),

	/**
	 * The xor operation
	 * 
	 * @see net.sf.firemox.operation.Xor
	 */
	XOR("xor"),

	/**
	 * The truncated division
	 * 
	 * @see net.sf.firemox.operation.DivTruncated
	 */
	DIV_TRUNCATED("div-truncated"),

	/**
	 * The rounded division
	 * 
	 * @see net.sf.firemox.operation.DivRounded
	 */
	DIV_ROUNDED("div-rounded"),

	/**
	 * The add the rounded division/2
	 * 
	 * @see net.sf.firemox.operation.AddHalfRounded
	 */
	ADD_ROUNDED("add-half-rounded"),

	/**
	 * The truncated division/2
	 * 
	 * @see net.sf.firemox.operation.AddHalfTruncated
	 */
	ADD_TRUNCATED("add-half-truncated"),

	/**
	 * added abstract operation to represent final value. This value does not
	 * appear in the available operators. <br>
	 * There is no associated Operation.
	 * 
	 * @see net.sf.firemox.expression.Counter
	 */
	COUNTER("counter"),

	/**
	 * This operation is only there to enable operation comparaison.
	 * 
	 * @see net.sf.firemox.operation.Any
	 */
	ANY("any"),

	/**
	 * <br>
	 * For target-list : 'remove-last' operation remove the last target from the
	 * current list. If 'list-index' is specified, the last target is removed from
	 * target list saved at this index. <br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	REMOVE_LAST("remove-last"),

	/**
	 * <br>
	 * For target-list : 'remove-first' operation remove the first target from the
	 * current list. If 'list-index' is specified, the first target is removed
	 * from target list saved at this index.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	REMOVE_FIRST("remove-first"),

	/**
	 * <br>
	 * For target-list : 'remove-tail' operation remove all targets but the last
	 * one from the current list. If 'list-index' is specified, all targets but
	 * the last are removed from target list saved at this index.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	REMOVE_TAIL("remove-tail"),

	/**
	 * <br>
	 * For target-list : 'remove-queue' operation remove all targets but the first
	 * one from the current list. If 'list-index' is specified, all targets but
	 * the first are removed from target list saved at this index.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	REMOVE_QUEUE("remove-queue"),

	/**
	 * <br>
	 * For target-list : 'collapse-combat' operation is called to group natively
	 * defending/attacking card. Would be removed later. The 'list-index' is
	 * required.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.TargetList
	 */
	COLLAPSE_COMBAT("collapse-combat"),

	/**
	 * <br>
	 * For target-list : 'save' operation ajoute la liste de cibles courante dans
	 * la liste des listes de cibles sauvegardees. Si 'index' et 'target' sont
	 * specifies, cette cible sera inseree a l'index specifie. Si 'list-index',
	 * 'target' et 'index' sont specifies, la cible est inseree a cet index dans
	 * la liste sauvegardee a 'list-index'.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	SAVE("save"),

	/**
	 * <br>
	 * For target-list : 'load' operation load a target list saved at the
	 * specified list-index.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	LOAD("load"),

	/**
	 * <br>
	 * For target-list :'filter' operation remove all targets making true the
	 * specified test. If 'list-index' is specified, this operation is applied on
	 * the saved list at 'list-index'.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	FILTER("filter"),

	/**
	 * Indentify a couple : register name + index.
	 */
	REGISTER_ACCESS("register-access"),

	/**
	 * The soustraction. <br>
	 * For target-list : 'remove' operation remove from the current target list,
	 * the one placed at the specified index. If a target is specified, it would
	 * be removed from the current list of target. If 'list-index' is specified
	 * this operation is applied on the list saved at this index. If 'list-index'
	 * is specified but neither 'index' neither 'target', the saved list at this
	 * index is removed from the saved lists.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	REMOVE("remove"),

	/**
	 * The soustraction. <br>
	 * For target-list : 'remove' operation remove from the current target list,
	 * the one placed at the specified index. If a target is specified, it would
	 * be removed from the current list of target. If 'list-index' is specified
	 * this operation is applied on the list saved at this index. If 'list-index'
	 * is specified but neither 'index' neither 'target', the saved list at this
	 * index is removed from the saved lists.<br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	MINUS("minus"),

	/**
	 * To get the last index of an element. <br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	LAST_INDEX_OF("last-index-of"),

	/**
	 * To get the minimum value of values within a list. <br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 * @see net.sf.firemox.operation.Minimum
	 */
	MINIMUM("minimum"),

	/**
	 * To get the maximum value of values within a list. <br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 * @see net.sf.firemox.operation.Maximum
	 */
	MAXIMUM("maximum"),

	/**
	 * To get the size of a list. <br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	SIZE("size"),

	/**
	 * To get the first element of a list <br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	FIRST("first"),

	/**
	 * To get the last element of a list <br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	LAST("last"),

	/**
	 * @see net.sf.firemox.operation.IntList
	 */
	INT_LIST("int-list"),

	/**
	 * @see net.sf.firemox.operation.TargetList
	 */
	TARGET_LIST("target-list"),

	/**
	 * @see net.sf.firemox.expression.targetlist.IndexOfSavedList
	 */
	INDEX_OF_SAVED_LIST("index-of-saved-list"),

	/**
	 * To get the first index of an element <br>
	 * There is no associated Operation, target-list and int-list use this
	 * operation as id only.
	 * 
	 * @see net.sf.firemox.operation.IntList
	 * @see net.sf.firemox.operation.TargetList
	 */
	INDEX_OF("index-of"),

	/**
	 * There is no associated Operation.
	 * 
	 * @see net.sf.firemox.expression.IfThenElse
	 */
	IF_THEN_ELSE("if-then-else"),

	/**
	 * @see net.sf.firemox.operation.AndNot
	 */
	AND_NOT("and-not"),

	/**
	 * @see net.sf.firemox.expression.Position
	 */
	POSITION("position"),

	/**
	 * @see net.sf.firemox.expression.BitCount
	 */
	BIT_COUNT("bit-count"),

	/**
	 * @see net.sf.firemox.expression.CardColors
	 */
	CARD_COLORS("card-colors"),

	/**
	 * @see net.sf.firemox.expression.HighestAmong
	 */
	HIGHEST_AMONG("highest-among"),

	/**
	 * @see net.sf.firemox.expression.LowestAmong
	 */
	LOWEST_AMONG("lowest-among"),

	/**
	 * @see net.sf.firemox.expression.StringMethod
	 */
	STRING_METHOD("method"),

	/**
	 * @see net.sf.firemox.expression.ObjectValue
	 */
	OBJECT_VALUE("object-value"),

	/**
	 * @see net.sf.firemox.expression.CardTypes
	 */
	CARD_TYPES("card-types"),

	/**
	 * @see net.sf.firemox.expression.CardProperties
	 */
	CARD_PROPERTIES("card-properties"),

	/**
	 * 
	 */
	MANA_PAID("manapaid"),

	/**
	 * Indentify a couple : register name + index.
	 */
	ABSTRACT_VALUE("abstract-value"),

	/**
	 * 
	 */
	TEST_ON("test-on"),

	/**
	 * 
	 */
	TO_INDEX("to-index"),

	/**
	 * 
	 */
	TO_CODE("to-code"),

	/**
	 * 
	 */
	NEGATIVE("negative"),

	/**
	 * 
	 */
	DECREMENT("decrement"),

	/**
	 * 
	 */
	INCREMENT("increment"),

	/**
	 * 
	 */
	DECK_COUNTER("deck-counter"),

	/**
	 * Remove all elements in a given saved list from the current target list.
	 */
	REMOVE_ALL("remove-all"),

	/**
	 * Add all elements from a given saved list to the current target list.
	 */
	ADD_ALL("add-all");

	/**
	 * Human readable name.
	 */
	private final String xsdName;

	/**
	 * Private constructor with real name. Create a new instance of this class.
	 * 
	 * @param xsdName
	 *          Human readable name.
	 */
	private IdOperation(String xsdName) {
		this.xsdName = xsdName;
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

	/**
	 * Read and return the enumeration from the given input stream.
	 * 
	 * @param input
	 *          the stream containing the enumeration to read.
	 * @return the enumeration from the given input stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static IdOperation deserialize(InputStream input) throws IOException {
		return values()[input.read()];
	}

	/**
	 * Return null of enumeration value corresponding to the given XSD name.
	 * 
	 * @param xsdName
	 *          the XSD name of this abstract value.
	 * @return null of enumeration value corresponding to the given XSD name.
	 */
	public static IdOperation valueOfXsd(String xsdName) {
		for (IdOperation value : values()) {
			if (value.xsdName.equals(xsdName)) {
				return value;
			}
		}
		return null;
	}

}