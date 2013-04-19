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
package net.sf.firemox.stack;

import java.awt.Image;
import java.awt.Toolkit;

import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.Target;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.event.context.MContextCardCardIntInt;
import net.sf.firemox.event.context.MContextTarget;
import net.sf.firemox.test.Test;
import net.sf.firemox.token.IdConst;
import net.sf.firemox.token.IdTargets;
import net.sf.firemox.token.IdTokens;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.TargetGlassPane;
import net.sf.firemox.ui.i18n.LanguageManager;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class TargetHelper {

	/**
	 * Code to represent the source of target action.
	 */
	public static final String STR_SOURCE = "S";

	/**
	 * Code to represent the context of a spell.
	 */
	public static final String STR_CONTEXT1 = "C1";

	/**
	 * Code to represent the context2 of a spell.
	 */
	public static final String STR_CONTEXT2 = "C2";

	/**
	 * Picture used to draw a targeted component over the Glass pane.
	 */
	// private Image targetPicture;
	/**
	 * Picture used to draw a targeted component when it is added to the current
	 * target list..
	 */
	private Image targetPictureSml;

	/**
	 * Url picture used to draw a targeted component when it is added to the
	 * current target list. The returned picture is a 12 x 12 one.
	 */
	private String targetPictureUrl;

	/**
	 * Glasspane instance
	 */
	private final TargetGlassPane targetGlassPane;

	/**
	 * Create a new instance of this class.
	 */
	private TargetHelper() {
		targetGlassPane = new TargetGlassPane();
		targetPictureSml = Toolkit.getDefaultToolkit().getImage(
				IdConst.IMAGES_DIR + "target_sml.gif");
		targetPictureUrl = "<br><img src='file:///"
				+ MToolKit.getIconPath("target_sml.gif") + "'>&nbsp;"
				+ LanguageManager.getString("target.istargeted");
	}

	/**
	 * Unique instance of this class.
	 */
	private static TargetHelper instance;

	/**
	 * Return the unique instance of this class.
	 * 
	 * @return the unique instance of this class.
	 */
	public static TargetHelper getInstance() {
		if (instance == null) {
			instance = new TargetHelper();
		}
		return instance;
	}

	/**
	 * Return <code>true</code> when there is the required amount of valid
	 * targets.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param source
	 *          the card source of the target. Often is the card owning the
	 *          current ability.
	 * @param test
	 *          the constraint applied on the target. This test must be true to
	 *          make valid a target.
	 * @param type
	 *          the target type (player, card, both)
	 * @param choiceMode
	 *          the choice mode like random, all, chosen by playerX,...
	 * @param context
	 *          the associated context of current ability.
	 * @param restrictionZone
	 *          the restriction zone. If is <code>-1</code> the scan would be
	 *          processed on all zones.
	 * @param hopCounter
	 *          the minimum amount of target looked for. For optimisation purpose.
	 * @param canBePreempted
	 *          <code>true</code> if the valid targets can be derterminated
	 *          before runtime.
	 * @return <code>true</code> when there is the required amount of valid
	 *         targets.
	 */
	public boolean checkTarget(Ability ability, MCard source, Test test,
			int type, int choiceMode, ContextEventListener context,
			int restrictionZone, int hopCounter, boolean canBePreempted) {
		switch (type) {
		case IdTokens.DEALTABLE:
			// Target is a player or card
			if (choiceMode == IdTargets.ALL) {
				return true;
			}
			if (test(ability, StackManager.PLAYERS[0], test, canBePreempted)) {
				return true;
			}
			if (test(ability, StackManager.PLAYERS[1], test, canBePreempted)) {
				return true;
			}
			if (restrictionZone != -1) {
				if (StackManager.PLAYERS[0].zoneManager.getContainer(restrictionZone)
						.countAllCardsOf(test, ability, hopCounter, canBePreempted) >= hopCounter) {
					return true;
				}
				if (StackManager.PLAYERS[1].zoneManager.getContainer(restrictionZone)
						.countAllCardsOf(test, ability, hopCounter, canBePreempted) >= hopCounter) {
					return true;
				}
			} else {
				if (StackManager.PLAYERS[0].zoneManager.countAllCardsOf(test, ability,
						hopCounter, canBePreempted) >= hopCounter) {
					return true;
				}
				if (StackManager.PLAYERS[1].zoneManager.countAllCardsOf(test, ability,
						hopCounter, canBePreempted) >= hopCounter) {
					return true;
				}
			}
			// no valid target
			return false;
		case IdTokens.PLAYER:
			// Target is a player
			if (choiceMode == IdTargets.ALL || choiceMode == IdTargets.RANDOM) {
				return true;
			}
			if (test(ability, StackManager.PLAYERS[0], test, canBePreempted)) {
				return true;
			}
			if (test(ability, StackManager.PLAYERS[1], test, canBePreempted)) {
				return true;
			}
			// no valid player
			return false;
		case IdTokens.CARD:
			// Target is a card
			if (choiceMode == IdTargets.ALL) {
				return true;
			}
			if (restrictionZone != -1) {
				if (StackManager.PLAYERS[0].zoneManager.getContainer(restrictionZone)
						.countAllCardsOf(test, ability, hopCounter, canBePreempted) >= hopCounter) {
					return true;
				}
				if (StackManager.PLAYERS[1].zoneManager.getContainer(restrictionZone)
						.countAllCardsOf(test, ability, hopCounter, canBePreempted) >= hopCounter) {
					return true;
				}
			} else {
				if (StackManager.PLAYERS[0].zoneManager.countAllCardsOf(test, ability,
						hopCounter, canBePreempted) >= hopCounter) {
					return true;
				}
				if (StackManager.PLAYERS[1].zoneManager.countAllCardsOf(test, ability,
						hopCounter, canBePreempted) >= hopCounter) {
					return true;
				}
			}
			// no valid card
			return false;
		default:
			return true;
		}
	}

	/**
	 * Return <code>true</code> when there is the required amount of valid
	 * targets.
	 * 
	 * @param ability
	 *          is the ability owning this test. The card component of this
	 *          ability should correspond to the card owning this test too.
	 * @param target
	 *          component to test.
	 * @param test
	 *          the constraint applied on the target. This test must be true to
	 *          make valid a target.
	 * @param canBePreempted
	 *          <code>true</code> if the valid targets can be derterminated
	 *          before runtime.
	 * @return <code>true</code> when there is the required amount of valid
	 *         targets.
	 */
	private boolean test(Ability ability, Target target, Test test,
			boolean canBePreempted) {
		if (canBePreempted)
			return test.test(ability, target);
		return test.testPreemption(ability, target);
	}

	/**
	 * Add a target Id displayed on each targeted components.
	 * 
	 * @param stackContext
	 *          the stack context of source.
	 */
	public synchronized void addTargetedBy(StackContext stackContext) {
		this.stackContext = stackContext;
		targetGlassPane.setStackContext(stackContext);
	}

	/**
	 * Remove any target Id displayed on targeted components.
	 */
	public synchronized void removeTargetedBy() {
		this.stackContext = null;
		targetGlassPane.setVisible(false);
		MagicUIComponents.magicForm.updateTimerPanel();
	}

	/**
	 * Return picture used to draw a targeted component when it is added to the
	 * current target list. The returned picture is a 12 x 12 one.
	 * 
	 * @return Picture used to draw a targeted component when it is added to the
	 *         current target list.
	 */
	public Image getTargetPictureSml() {
		return targetPictureSml;
	}

	/**
	 * Return URL picture used to draw a targeted component when it is added to
	 * the current target list. The returned picture is a 12 x 12 one.
	 * 
	 * @return URL picture used to draw a targeted component when it is added to
	 *         the current target list.
	 */
	public String getTargetPictureAsUrl() {
		return targetPictureUrl;
	}

	/**
	 * Return the target id of given component.
	 * 
	 * @param target
	 *          the component requesting it's target id.
	 * @return the target id (base 1) of given component. Return 'S' value if is
	 *         the source. Return a null value if is neither the source, neither a
	 *         target.
	 */
	public synchronized String getMyId(Target target) {
		// Is there a spell under the cursor?
		StackContext stackContext = this.stackContext;
		if (stackContext == null) {
			stackContext = StackManager.getInstance();
		}

		// Is it the source?
		if (stackContext.getSourceCard() == target) {
			return STR_SOURCE; // Now "Source" text is no more printed "S";
		}

		int id = -1;
		if (stackContext.getTargetedList() != null) {
			id = stackContext.getTargetedList().list.indexOf(target);
		}
		// Is it a target?
		if (id < 0) {
			// Is this component is in the context
			final ContextEventListener evListener = stackContext.getAbilityContext();
			if (evListener != null) {
				if (evListener instanceof MContextTarget
						&& ((MContextTarget) evListener).getOriginalTargetable() == target) {
					return STR_CONTEXT1;
				}
				if (evListener instanceof MContextCardCardIntInt
						&& ((MContextCardCardIntInt) evListener).getOriginalCard2() == target) {
					return STR_CONTEXT2;
				}
			}
			return null;
		}

		// Return the target Id
		return String.valueOf(id + 1);
	}

	/**
	 * The active StackElement component. May be <code>null</code>.
	 */
	private StackContext stackContext;
}
