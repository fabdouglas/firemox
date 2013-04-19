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
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.sf.firemox.action.intlist.IntList;
import net.sf.firemox.action.objectmap.ObjectMapFactory;
import net.sf.firemox.action.target.TargetFactory;
import net.sf.firemox.action.targetlist.TargetListFactory;
import net.sf.firemox.modifier.Modifier;
import net.sf.firemox.operation.IdOperation;
import net.sf.firemox.test.And;
import net.sf.firemox.test.Or;
import net.sf.firemox.test.Test;
import net.sf.firemox.test.TestFactory;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.Picture;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public final class ActionFactory {

	/**
	 * Create a new instance of this class.
	 */
	private ActionFactory() {
		super();
	}

	/**
	 * Initialize pictures and constraints associted to actions.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>baseUrl of action pictures [String]
	 * <li>nb associated pictures [int]</li>
	 * <li>action i [MAction]</li>
	 * <li>picture i [String]</li>
	 * <li>nb associated constraints [1]</li>
	 * <li>action i [MAction]</li>
	 * <li>operation for test i [Operation]</li>
	 * <li>constraint i [Test]</li>
	 * </ul>
	 * 
	 * @param inputFile
	 *          file containing this action
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static void init(InputStream inputFile) throws IOException {
		// action pictures
		final String picturesBaseUrl = MToolKit.readString(inputFile);
		PICTURES.clear();
		for (int i = inputFile.read(); i-- > 0;) {
			final String actionName = MToolKit.readString(inputFile);
			final String pictureName = MToolKit.readString(inputFile);
			try {
				Picture.download(MToolKit
						.getTbsPicture("actions/" + pictureName, false), new URL(
						picturesBaseUrl + pictureName));
				PICTURES
						.put(actionName, "<img src='file:///"
								+ MToolKit.getTbsHtmlPicture("actions/" + pictureName)
								+ "'>&nbsp;");
			} catch (Exception e) {
				Log.info(e.getMessage());
			}
		}

		// action constraints
		CONSTRAINTS.clear();
		for (int i = inputFile.read(); i-- > 0;) {
			final Object[] constraint = new Object[3];

			// read action
			final MAction action = ActionFactory.readAction(inputFile, null);
			constraint[0] = action;

			// read operation id
			constraint[1] = IdOperation.deserialize(inputFile);

			// read constraint test
			constraint[2] = TestFactory.readNextTest(inputFile);

			// associate action to the constraint
			CONSTRAINTS.put(action.getIdAction(), constraint);
		}
	}

	/**
	 * Read a list a actions from the given input stream.
	 * 
	 * @param inputFile
	 *          the stream used to create list of actions.
	 * @param modifier
	 *          the optional modifier containing the action list.
	 * @return list of actions read from the given stream.
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static MAction[] readActionList(InputStream inputFile,
			Modifier modifier) throws IOException {
		final MAction[] res = new MAction[inputFile.read()];
		try {
			for (int index = 0; index < res.length; index++) {
				res[index] = ActionFactory.readAction(inputFile, null);
			}
			return res;
		} catch (Throwable e) {
			throw new RuntimeException(
					">> TBS ERROR reading action. Last known action is : "
							+ currentAction + ", context : " + Arrays.toString(res), e);
		}
	}

	/**
	 * Return the constraints to make usable the specified actions list.
	 * 
	 * @param cost
	 *          is the actions list
	 * @param constraintTest
	 *          is the existing constrains
	 * @return the constraints to make usable the specified actions list.
	 */
	public static Test getConstraints(MAction[] cost, Test constraintTest) {
		Test res = constraintTest;
		for (int index = 0; index < cost.length; index++) {
			// constraints of action
			final Object[] constraint = CONSTRAINTS.get(cost[index].getIdAction());
			if (constraint != null && cost[index].equal((MAction) constraint[0])) {
				// there is a constraint with this action
				switch ((IdOperation) constraint[1]) {
				case AND:
					res = And.append(constraintTest, cost[index]
							.parseTest((Test) constraint[2]));
					break;
				case OR:
					res = Or.append(constraintTest, (Test) constraint[2]);
					break;
				default:
					throw new InternalError("Unknown constraint operation : "
							+ ((Integer) constraint[1]).intValue());
				}
			}
		}
		return res;
	}

	/**
	 * Read and return the next action from specified inputFile
	 * 
	 * @param inputFile
	 *          the input stream where action will be read.
	 * @param modifier
	 *          the modifier that would be used with actions that need it. Can be
	 *          null.
	 * @return the next action read into of specified inputFile
	 * @throws IOException
	 *           If some other I/O error occurs
	 */
	public static MAction readAction(InputStream inputFile, Modifier modifier)
			throws IOException {
		Actiontype idAction = Actiontype.valueOf(inputFile);
		switch (idAction) {
		case ABORT:
			return Abort.instance;
		case ADD_ABILITY:
			return new AddAbility(inputFile);
		case ADD_MODIFIER:
			return new AddModifier(inputFile);
		case ADD_OBJECT:
			return new AddObject(inputFile);
		case ASSIGN_DAMAGE_FROM_TO:
			return new AssignDamageFromTo(inputFile);
		case ASSIGN_DAMAGE_TARGET:
			return new AssignDamageTarget(inputFile);
		case ASSIGN_DAMAGE_SOURCE_DEST:
			return new AssignDamageSourceDest(inputFile);
		case ATTACH:
			return new Attach(inputFile);
		case ATTACH_LIST:
			return new AttachList(inputFile);
		case CHOICE:
			return new InputChoice(inputFile);
		case COPY_CARD:
			return new CopyCard(inputFile);
		case CREATE_ABILITY:
			return new CreateAbility(inputFile);
		case CREATE_CARD:
			return new CreateCard(inputFile);
		case CREATE_MODIFIER:
			return new CreateModifier(inputFile);
		case DETACH_ME:
			return DetachMe.getInstance();
		case FACE:
			return new Face(inputFile);
		case FORCE_PLAY:
			return new ForcePlay(inputFile);
		case GENERATE_EVENT:
			return new GenerateEvent(inputFile);
		case GIVE_MANA_BASIC:
			return new GiveManaBasic(inputFile);
		case GIVE_MANA_MULTI:
			return new GiveManaMulti(inputFile);
		case HOP:
			return new Hop(inputFile);
		case IF_THEN_ELSE:
			return new IfThenHop(inputFile);
		case INPUT_COLOR:
			return new InputColor(inputFile);
		case INPUT_NUMBER:
			return new InputNumber(inputFile);
		case INPUT_ZONE:
			return new InputZone(inputFile);
		case INPUT_PROPERTY:
			return new InputProperty(inputFile);
		case INT_LIST:
			return IntList.readNextIntList(inputFile);
		case LETHAL_DAMAGE:
			return LethalDamage.instance;
		case LOSE_GAME:
			return new LoseGame(inputFile);
		case MODIFY_ABILITY_REGISTER:
			return new ModifyAbilityRegister(inputFile);
		case MODIFY_STACK_REGISTER:
			return new ModifyStackRegister(inputFile);
		case MODIFY_TARGET_LIST_REGISTER:
			return new ModifyTargetListRegister(inputFile);
		case MODIFY_TARGETABLE_REGISTER:
			return new ModifyTargetableRegister(inputFile);
		case MODIFY_STATIC_REGISTER:
			return new ModifyStaticRegister(inputFile);
		case MODIFY_REQUIRED_MANA:
			return new ModifyRequiredMana(inputFile);
		case MOVE_CARD:
			return new MoveCard(inputFile);
		case MOVE_OBJECT:
			return new MoveObject(inputFile);
		case MOVE_PLAYER_CARD:
			return new MovePlayerCard(inputFile);
		case MSG:
			return new Input(inputFile);
		case NEXT_CURRENT_PLAYER:
			return new NextCurrentPlayer(inputFile);
		case NEXT_PHASE:
			return new NextPhase(inputFile);
		case OBJECT_MAP:
			return ObjectMapFactory.readNextObjectMap(inputFile);
		case PAY_MANA:
			return new PayMana(inputFile);
		case PILE:
			return new Pile(inputFile);
		case REMOVE_OBJECT:
			return new RemoveObject(inputFile);
		case REPEAT_ACTION:
			return new Repeat(inputFile);
		case RESOLVE_HIDDEN:
			return ResolveHidden.instance;
		case RESTORE_CARD:
			return new RestoreCard(inputFile);
		case SET_ID_CARD:
			return new SetIdCard(inputFile);
		case SHUFFLE:
			return new Shuffle(inputFile);
		case SKIP_PHASE:
			return new SkipPhase(inputFile);
		case SOUND:
			return new Sound(inputFile);
		case TAP:
			return new Tap(inputFile);
		case TARGET:
			return TargetFactory.readNextTarget(inputFile);
		case TARGET_ALL:
			return new TargetAllNoEvent(inputFile);
		case TARGET_RANDOM:
			return new TargetRandomNoEvent(inputFile);
		case TARGET_LIST:
			return TargetListFactory.readNextTargetList(inputFile);
		case UNREGISTER_THIS:
			return UnregisterThis.instance;
		case UPDATE_TOUGHNESS:
			return UpdateToughness.getInstance();
		case UPDATE_LIFE:
			return UpdateLife.getInstance();
		case ZONE_VISIBILITY:
			return new ZoneVisibility(inputFile);
		default:
			throw new InternalError("Unknow action: " + idAction);
		}
	}

	/**
	 * Associated pictures
	 */
	protected static final Map<String, String> PICTURES = new HashMap<String, String>();

	/**
	 * Available action constraints.
	 */
	private static final Map<Actiontype, Object[]> CONSTRAINTS = new HashMap<Actiontype, Object[]>();

	/**
	 * Is the current action is being to be parsed. Used only while parsing.
	 */
	static UserAction currentAction;

}
