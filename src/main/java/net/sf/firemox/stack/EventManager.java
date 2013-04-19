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
 * 
 */
package net.sf.firemox.stack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import net.sf.firemox.clickable.ability.AbilityFactory;
import net.sf.firemox.clickable.ability.SystemAbility;
import net.sf.firemox.clickable.target.card.SystemCard;
import net.sf.firemox.event.MEventListener;
import net.sf.firemox.event.phase.BeforePhase;
import net.sf.firemox.event.phase.BeginningPhase;
import net.sf.firemox.event.phase.EndOfPhase;
import net.sf.firemox.modifier.model.ModifierFactory;
import net.sf.firemox.stack.phasetype.PhaseType;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.token.MCommonVars;
import net.sf.firemox.tools.Log;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.zone.ZoneManager;

import org.apache.commons.io.IOUtils;

/**
 * This class manage the turn structure : phase order, loop and phase's UI
 * manager(highlight, breakpoints, pass)
 * 
 * @since 0.21 a graphical representation of phase
 * @since 0.30 an option "auto play single "YOU MUST" ability is supported
 * @since 0.30 an option "skip all" is supported
 * @since 0.31 an option "skip all even opponent's spell" is supported
 * @since 0.31 graphical representation of phases for both players
 * @since 0.31 attack phase is supported
 * @since 0.52 support option PLAYED_ONCE_BY_PHASE and AUTOMATICALLY_PLAYED
 * @since 0.53 turns are counted
 * @since 0.80 BEFORE_PHASE and END_OF_PHASE_... event can be replaced.
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 */
public final class EventManager {

	private static final String TURN_STR = LanguageManager.getString("turnid")
			+ " : ";

	/**
	 * Create a new instance of this class.
	 */
	private EventManager() {
		super();
	}

	/**
	 * Create an instance of MEventManager by reading a file
	 * 
	 * @since 0.31 graphical representation of phases for both players
	 */
	public static void init() {
		MPhase.optionsMenu = new JPopupMenu();
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(LanguageManager
				.getString("breakpoint"), new javax.swing.ImageIcon(IdConst.IMAGES_DIR
				+ "breakpoint.gif"));
		item.setFont(MToolKit.defaultFont);
		item.setToolTipText("<html>"
				+ LanguageManager.getString("breakpoint.tooltip"));
		item.setMnemonic('b');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MPhase.triggerPhase.setBreakpoint(((JCheckBoxMenuItem) evt.getSource())
						.isSelected());
				MPhase.triggerPhase.repaint();
			}
		});
		MPhase.optionsMenu.add(item);
		item = new JCheckBoxMenuItem(LanguageManager.getString("skipPhase"),
				UIHelper.getIcon("skipall1.gif"));
		item.setFont(MToolKit.defaultFont);
		item.setToolTipText("<html>"
				+ LanguageManager.getString("skipPhase.tooltip"));
		item.setMnemonic('l');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MPhase.triggerPhase.setSkipAll(((JCheckBoxMenuItem) evt.getSource())
						.isSelected());
				MPhase.triggerPhase.repaint();
			}
		});
		MPhase.optionsMenu.add(item);
		item = new JCheckBoxMenuItem(LanguageManager.getString("skipPhaseOnce"),
				UIHelper.getIcon("skipall2.gif"));
		item.setFont(MToolKit.defaultFont);
		item.setToolTipText("<html>"
				+ LanguageManager.getString("skipPhaseOnce.tooltip") + "<br><br>"
				+ MagicUIComponents.HTML_ICON_TIP
				+ LanguageManager.getString("skipPhaseOnceTTtip"));
		item.setMnemonic('o');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MPhase.triggerPhase.setSkipAllTmp(((JCheckBoxMenuItem) evt.getSource())
						.isSelected());
				MPhase.triggerPhase.repaint();
			}
		});

		MPhase.optionsMenu.add(item);
		item = new JCheckBoxMenuItem(LanguageManager.getString("skipPhaseMedium"),
				UIHelper.getIcon("skipallm2.gif"));
		item.setFont(MToolKit.defaultFont);
		item.setToolTipText("<html>"
				+ LanguageManager.getString("skipPhaseMedium.tooltip") + "<br>"
				+ MagicUIComponents.HTML_ICON_WARNING
				+ LanguageManager.getString("skipPhaseAllTTwarn"));
		item.setMnemonic('m');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MPhase.triggerPhase.setSkipMedium(((JCheckBoxMenuItem) evt.getSource())
						.isSelected());
				MPhase.triggerPhase.repaint();
			}
		});
		MPhase.optionsMenu.add(item);
		item = new JCheckBoxMenuItem(LanguageManager
				.getString("skipPhaseMediumOnce"), UIHelper.getIcon("skipallm.gif"));
		item.setFont(MToolKit.defaultFont);
		item.setToolTipText("<html>"
				+ LanguageManager.getString("skipPhaseMediumOnce.tooltip") + "<br>"
				+ MagicUIComponents.HTML_ICON_WARNING
				+ LanguageManager.getString("skipPhaseAllTTwarn"));
		item.setMnemonic('p');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MPhase.triggerPhase.setSkipMediumTmp(((JCheckBoxMenuItem) evt
						.getSource()).isSelected());
				MPhase.triggerPhase.repaint();
			}
		});

		MPhase.optionsMenu.add(item);
		item = new JCheckBoxMenuItem(LanguageManager.getString("skipPhaseAll"),
				UIHelper.getIcon("skipall3.gif"));
		item.setFont(MToolKit.defaultFont);
		item.setToolTipText("<html>"
				+ LanguageManager.getString("skipPhaseAll.tooltip") + "<br>"
				+ MagicUIComponents.HTML_ICON_WARNING
				+ LanguageManager.getString("skipPhaseAllTTwarn"));
		item.setMnemonic('u');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MPhase.triggerPhase
						.setSkipAllVery(((JCheckBoxMenuItem) evt.getSource()).isSelected());
				MPhase.triggerPhase.repaint();
			}
		});
		MPhase.optionsMenu.add(item);
		item = new JCheckBoxMenuItem(LanguageManager.getString("skipPhaseAllOnce"),
				UIHelper.getIcon("skipall4.gif"));
		item.setFont(MToolKit.defaultFont);
		item.setToolTipText("<html>"
				+ LanguageManager.getString("skipPhaseAllOnce.tooltip") + "<br>"
				+ MagicUIComponents.HTML_ICON_WARNING
				+ LanguageManager.getString("skipPhaseAllTTwarn"));
		item.setMnemonic('t');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				MPhase.triggerPhase.setSkipAllVeryTmp(((JCheckBoxMenuItem) evt
						.getSource()).isSelected());
				MPhase.triggerPhase.repaint();
			}
		});
		MPhase.optionsMenu.add(item);
	}

	/**
	 * Initialize the UI depending on the current TBS
	 */
	public static void initTbsUI() {
		MagicUIComponents.magicForm.turnsLbl.setText(TURN_STR
				+ MCommonVars.registers[IdTokens.TURN_ID]);
		MagicUIComponents.logListing.setText("");

		// update the phases GUI
		StackManager.PLAYERS[StackManager.idCurrentPlayer]
				.resetPhases(MPhase.phases[StackManager.idCurrentPlayer]);
		StackManager.PLAYERS[1 - StackManager.idCurrentPlayer]
				.resetPhases(MPhase.phases[1 - StackManager.idCurrentPlayer]);
	}

	/**
	 * remove all events in the stack of this phase, read new system abilities,
	 * turn structure and set the current phase.
	 * <ul>
	 * Structure of InputStream : Data[size]
	 * <li>number of phases type [1]</li>
	 * <li>phases type i [...]</li>
	 * <li>number of phases in one turn [1]</li>
	 * <li>phase identifier i [1]</li>
	 * <li>phase index (not identifier) for first turn [1]</li>
	 * <li>number of state based ability of play [1]</li>
	 * <li>state based ability i [...]</li>
	 * <li>number of static modifier of game [1]</li>
	 * <li>static modifier of game i [...]</li>
	 * </ul>
	 * 
	 * @param dbStream
	 *          the MDB file containing rules
	 * @param settingFile
	 *          setting file attached to this MDB
	 * @throws IOException
	 */
	public static void init(FileInputStream dbStream, String settingFile)
			throws IOException {
		nextCurrentPlayer = -1;
		nextPhaseIndex = -1;

		// remove all event listener
		MEventListener.reset();

		// read the different phase types
		int nbPhases = dbStream.read();
		PhaseType[] phaseTypes = new PhaseType[nbPhases];
		while (nbPhases-- > 0) {
			PhaseType phaseType = new PhaseType(dbStream);
			phaseTypes[phaseType.id] = phaseType;
		}

		// read the turn structure
		int nbPhasesPerTurn = dbStream.read();
		turnStructure = new PhaseType[nbPhasesPerTurn];
		Log.debug("Turn Structure :");
		for (int i = 0; i < nbPhasesPerTurn; i++) {
			turnStructure[i] = phaseTypes[dbStream.read()];
			Log.debug("\t" + i + ":" + turnStructure[i].phaseName);
		}

		// first phase index
		int startIdPhase = dbStream.read();
		Log.debug("First phase of first turn is "
				+ turnStructure[startIdPhase].phaseName + "(" + startIdPhase + ")");

		// read phases GUI
		try {
			final InputStream in = MToolKit.getResourceAsStream(settingFile.replace(
					".mdb", ".pref"));
			MPhase.phases = new MPhase[2][turnStructure.length];
			for (int i = 0; i < turnStructure.length; i++) {
				MPhase.phases[0][i] = new MPhase(turnStructure[i], 0, in);
			}
			for (int i = 0; i < EventManager.turnStructure.length; i++) {
				MPhase.phases[1][i] = new MPhase(turnStructure[i], 1, in);
			}
			IOUtils.closeQuietly(in);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(MagicUIComponents.magicForm,
					LanguageManager.getString("loadtbssettingspb") + " : "
							+ e.getMessage() + e.getStackTrace()[0], LanguageManager
							.getString("error"), JOptionPane.WARNING_MESSAGE);
		}

		// read triggered abilities of play
		int nbTriggered = dbStream.read();
		Log.debug("System abilities (" + nbTriggered + "):");
		while (nbTriggered-- > 0) {
			// read the ability and register it
			AbilityFactory.readAbility(dbStream, SystemCard.instance)
					.registerToManager();
		}

		// reset the breakpoints, options and initialize all phases graphics
		for (int i = MPhase.phases[0].length; i-- > 1;) {
			MPhase.phases[0][i].reset();
			MPhase.phases[1][i].reset();
		}

		// set the current phase to ID__PHASE_MAIN
		phaseIndex = startIdPhase - 1;
		currentIdPhase = turnStructure[phaseIndex].id;
		parsingBeforePhaseEvent = false;
		parsingEndPhaseEvent = false;
		replacingBefore = false;

		// read static-modifiers of game
		int nbStaticModifiers = dbStream.read();
		Log.debug("Static-modifiers (" + nbStaticModifiers + "):");
		while (nbStaticModifiers-- > 0) {
			// read the static-modifiers and register them
			ModifierFactory.readModifier(dbStream).addModifierFromModel(
					SystemAbility.instance, SystemCard.instance);
		}

	}

	/**
	 * Go to the first phase of first turn
	 */
	public static void start() {
		MagicUIComponents.timer.start();
		gotoNextPhase();
	}

	/**
	 * Goto the next phase.For each player (current first), if mana pool isn't
	 * empty -> mana burn. phase will be ID__BEFORE_PHASE_UNTAP
	 */
	public static void gotoNextPhase() {
		StackManager.idActivePlayer = StackManager.idCurrentPlayer;
		Log.debug("Ending phase " + turnStructure[phaseIndex].phaseName + "("
				+ phaseIndex + ")");

		/**
		 * Since we are currently parsing the BEFORE_PHASE_... event, we do not go
		 * to the next pahse, but only return to the last instruction where the
		 * markup <code>parsingBeforePhaseEvent</code> has been previously set.
		 */
		if (parsingBeforePhaseEvent) {
			Log
					.debug("parsingBeforePhaseEvent is true, gotoNextPhase has been canceled");
		} else {

			if (parsingEndPhaseEvent) {
				Log.debug("\tEnding AGAIN " + turnStructure[phaseIndex].phaseName + "("
						+ phaseIndex + ")");
			} else {
				if (!EndOfPhase.tryAction(currentIdPhase)) {
					// this end of phase has been replaced
					Log.debug("\tEnd of Phase " + turnStructure[phaseIndex].phaseName
							+ "(" + phaseIndex + ") has been replaced");
					return;
				}
				parsingEndPhaseEvent = true;
				EndOfPhase.dispatchEvent();
				if (!StackManager.activePlayer().waitTriggeredBufferChoice(false)) {
					// Log.debug("parsingPhaseEvent is broken, gotoNextPhase has been
					// canceled");
					return;
				}
				if (!StackManager.isEmpty()) {
					throw new IllegalStateException(
							"The stack must be empty before going to the next phase, stack="
									+ Arrays.toString(ZoneManager.stack.getComponents()));
				}
			}
		}
		parsingEndPhaseEvent = false;
		replacingBefore = false;

		while (true) {

			if (StackManager.gameLostProceed) {
				return;
			}

			// change the current player?
			if (nextCurrentPlayer != -1) {
				StackManager.idCurrentPlayer = nextCurrentPlayer;
				nextCurrentPlayer = -1;
				/**
				 * changing current player make restart the turn even if
				 * <code>nextPhaseIndex</code> has been specified
				 */
				phaseIndex = -1;

				/*
				 * clear the stored target list of the previous turn, and clear damages
				 * of players, all damages should have been resolved, but we remove all
				 * MDamage object in the damage list to free memory.
				 */
				StackManager.SAVED_TARGET_LISTS.clear();
				StackManager.PLAYERS[StackManager.idCurrentPlayer].clearDamages();
				StackManager.PLAYERS[1 - StackManager.idCurrentPlayer].clearDamages();
			}

			// set the new phase index
			if (!parsingBeforePhaseEvent) {
				updatePhase();
			}

			Log.debug("Current phase is : " + turnStructure[phaseIndex].phaseName
					+ "(index=" + phaseIndex + ", id=" + currentIdPhase + ")");

			StackManager.idActivePlayer = StackManager.idCurrentPlayer;
			MagicUIComponents.magicForm.turnsLbl.setText(TURN_STR
					+ MCommonVars.registers[IdTokens.TURN_ID]);

			/**
			 * Even if the current phase does not really come (due to a 'skip token'),
			 * we raise the 'MEventBeforePhase' event. All abilities triggering this
			 * way should have the 'isHidden' tag since no player action is allowed
			 * since this is a very special event. After this call, the stack should
			 * be empty since abilities are immediately resolved (triggered -> stacked ->
			 * resolved).
			 */
			if (replacingBefore) {
				Log.debug("\t... BeforePhase can no more be replaced");
			} else {
				replacingBefore = true;
				if (!BeforePhase.tryAction(currentIdPhase)) {
					return;
					// Log.debug("\t... phase has not been replaced");
				}
			}

			if (parsingBeforePhaseEvent) {
				Log.debug("\t... BeforePhase event is not raised again");
			} else {
				parsingBeforePhaseEvent = true;
				BeforePhase.dispatchEvent();
				if (!StackManager.activePlayer().waitTriggeredBufferChoice(false)) {
					return;
					// Log.debug("\t... no triggered BeforePhase ability");
				}
			}
			replacingBefore = false;
			parsingBeforePhaseEvent = false;

			if (nextPhaseIndex != -1 || nextCurrentPlayer != -1) {
				if (nextPhaseIndex != -1) {
					phaseIndex = nextPhaseIndex - 1;
					currentIdPhase = turnStructure[phaseIndex].id;
					nextPhaseIndex = -1;
				}
				if (nextCurrentPlayer != -1) {
					StackManager.idCurrentPlayer = nextCurrentPlayer;
					StackManager.idActivePlayer = StackManager.idCurrentPlayer;
					nextCurrentPlayer = -1;
				}
			} else {
				// The turn structure has not been replaced.
				break;
			}

			updatePhasesGUI();
		}

		// beginning of phase ... triggered abilities
		if (currentPhase().skipThisPhase) {
			/*
			 * The current phase is marked with a 'skip token', we remove this one,
			 * and call the gotoNextPhase() function.
			 */
			Log.debug("\t-> this phase is skipped");
			currentPhase().skipThisPhase = false;
			gotoNextPhase();
		} else {
			/**
			 * This phase can really start, and 'Beginning event' is raised. The
			 * associated triggerred abilities can be added to the TBZ (triggered
			 * buffer zone). So after this call the TBZ may contains many abilities
			 * and would be managed later.
			 */
			BeginningPhase.dispatchEvent();
			StackManager.activePlayer().waitTriggeredBufferChoice(true);
		}
	}

	/**
	 * 
	 */
	private static void updatePhase() {
		if (nextPhaseIndex != -1) {
			phaseIndex = nextPhaseIndex;
			currentIdPhase = turnStructure[phaseIndex].id;
			nextPhaseIndex = -1;
		} else {
			phaseIndex = ++phaseIndex % turnStructure.length;
			currentIdPhase = turnStructure[phaseIndex].id;
		}
		updatePhasesGUI();
	}

	/**
	 * Update the phases GUI : colors indicating the current phases, and the
	 * handed player
	 */
	public static void updatePhasesGUI() {
		for (int i = MPhase.phases[StackManager.idActivePlayer].length; i-- > 0;) {
			MPhase.phases[StackManager.idCurrentPlayer][i].setActive(i == phaseIndex,
					StackManager.idCurrentPlayer == StackManager.idActivePlayer);
			MPhase.phases[1 - StackManager.idCurrentPlayer][i].setActive(false,
					StackManager.idCurrentPlayer != StackManager.idActivePlayer);
		}
	}

	/**
	 * return the phase type associate to the current phase
	 * 
	 * @return the phase type associate to the current phase
	 */
	public static PhaseType currentPhaseType() {
		return turnStructure[phaseIndex];
	}

	/**
	 * return the phase type associate to the current phase
	 * 
	 * @return the phase type associate to the current phase
	 */
	public static MPhase currentPhase() {
		return MPhase.phases[StackManager.idCurrentPlayer][phaseIndex];
	}

	/**
	 * Return true if we are currently parsing the "before phase" event.
	 * 
	 * @return true if we are currently parsing the "before phase" event.
	 */
	public static boolean parsinfBeforeEnd() {
		return parsingBeforePhaseEvent;
	}

	/**
	 * This markup indicates we are currently parsing the BEFORE_PHASE_... event.
	 * This token is used by the stack manager to know if the next time the
	 * gotoNextPhase() method is called would effectively make going to the next
	 * phase or simply skip the already parsing step of the BEFORE_PHASE_...
	 * event. Also, when this token is set to true just before the parse begin,
	 * and set to false the next time the gotoNextPhase() method would be called
	 * by the stack manager. When this markup is set to true, instead of giving
	 * priority to player, we resolve the stack as if that player has chosen to
	 * decline to response.
	 */
	private static boolean parsingBeforePhaseEvent;

	/**
	 * This markup indicates we are currently parsing the END_PHASE_... event.
	 * This token is used by the stack manager to release a maximum of stack
	 * frames. During the stack resolution, instead of calling the "gotoNextPhase"
	 * method, if this markup is true, the process simply return.
	 */
	public static boolean parsingEndPhaseEvent;

	/**
	 * is the current idPhase (not the index of phase)
	 */
	public static int currentIdPhase = 0;

	/**
	 * List of successive phase of any turn
	 */
	public static PhaseType[] turnStructure;

	/**
	 * The next 'current-player' for the next phase. If -1, the next
	 * 'current-player' would not change.
	 */
	public static int nextCurrentPlayer;

	/**
	 * The next 'currentIdPhase'. If -1, the next phase will follow the turn
	 * structure.
	 */
	public static int nextPhaseIndex;

	/**
	 * represents the current index of phase
	 */
	public static int phaseIndex;

	/**
	 * This markup is used to prevent multiple replacement of "BEFORE_PHASE_..."
	 * event since this event can be replaced only once per phase.
	 */
	private static boolean replacingBefore;

}