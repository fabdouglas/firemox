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
package net.sf.firemox.clickable.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.sf.firemox.action.WaitChosenActionChoice;
import net.sf.firemox.action.context.ActionContextWrapper;
import net.sf.firemox.action.handler.ChosenAction;
import net.sf.firemox.clickable.ability.Ability;
import net.sf.firemox.clickable.target.card.CardFactory;
import net.sf.firemox.clickable.target.card.MCard;
import net.sf.firemox.database.DatabaseFactory;
import net.sf.firemox.event.context.ContextEventListener;
import net.sf.firemox.network.ConnectionManager;
import net.sf.firemox.network.message.CoreMessageType;
import net.sf.firemox.stack.StackManager;
import net.sf.firemox.tools.Log;
import net.sf.firemox.ui.MagicUIComponents;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.i18n.LanguageManager;
import net.sf.firemox.ui.wizard.WizardImageIcon;

/**
 * The panel containing all playable action.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class ChosenCostPanel extends JPanel implements ActionListener {

	/**
	 * Create a new instance of this class.
	 */
	public ChosenCostPanel() {
		super(new BorderLayout());
		final JPanel controlsPanel = new JPanel(null);
		final BoxLayout layout0 = new BoxLayout(controlsPanel, BoxLayout.X_AXIS);
		controlsPanel.setLayout(layout0);
		actionsPanel = new JPanel();
		final BoxLayout layout = new BoxLayout(actionsPanel, BoxLayout.Y_AXIS);
		actionsPanel.setLayout(layout);
		cancelButton = new JButton(LanguageManager.getString("cancel"), UIHelper
				.getIcon("cancel.gif"));
		cancelButton.setEnabled(false);
		final JScrollPane scrollActions = new JScrollPane();
		scrollActions
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollActions.setAutoscrolls(true);
		scrollActions.setViewportView(actionsPanel);
		add(controlsPanel, BorderLayout.SOUTH);
		add(scrollActions, BorderLayout.CENTER);

		controlsPanel.add(cancelButton);
		controlsPanel.add(new JPanel());
		controlsPanel.setPreferredSize(new Dimension(1, 30));
		miniCard = new JLabel("  ");
		miniCard.setHorizontalAlignment(SwingConstants.RIGHT);
		controlsPanel.add(miniCard);
		cancelButton.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelButton) {
			StackManager.cancel();
		}
	}

	/**
	 * 
	 * 
	 */
	private void cleanPriv() {
		// clean old actions
		cancelButton.setEnabled(false);
		actionsPanel.removeAll();
		currentAction = null;
		recordIndex = 0;
	}

	/**
	 * Disable multiple way pay
	 */
	public void clean() {
		cleanPriv();
		recordIndex = 1;
		((WizardImageIcon) miniCard.getIcon())
				.setImage(DatabaseFactory.scaledBackImage);

		// disable the ChosenAction panel
		((JTabbedPane) getParent()).setSelectedIndex(0);
		// miniCard.repaint();
	}

	/**
	 * <ul>
	 * Initialize UI
	 * <li>Remove all existing JChosenAction components
	 * <li>Set the card's picture with the appropriate tooltip
	 * <li>Fill the panel with the JChosenAction components associated to the
	 * given <code>contexts</code>
	 * <li>Bring to front this panel
	 * </ul>
	 * 
	 * @param card
	 *          the card owning the played ability
	 * @param contexts
	 *          the Action contexts of the played ability
	 */
	public void initUI(MCard card, ActionContextWrapper... contexts) {
		((WizardImageIcon) miniCard.getIcon()).setCard(card);
		cleanPriv();
		miniCard.setToolTipText(StackManager.tokenCard.getTooltipString());
		for (ActionContextWrapper context : contexts) {
			if (context != null && context.action instanceof ChosenAction) {
				actionsPanel.add(new JChosenAction(context));
			}
		}
		if (actionsPanel.getComponentCount() == 0) {
			throw new InternalError("No chosenAction");
		}

		// active the ChosenAction panel
		((JTabbedPane) getParent())
				.setSelectedComponent(MagicUIComponents.chosenCostPanel);
	}

	/**
	 * Reset the UI and execute the first ChosenAction.
	 * 
	 * @param card
	 *          the card owning the played ability
	 * @param contexts
	 *          the Action contexts of the played ability
	 * @return Always false.TODO Modifying chosenAction order is not yet
	 *         implemented
	 */
	public boolean reset(MCard card, ActionContextWrapper[] contexts) {
		initUI(card, contexts);

		StackManager.actionManager.currentAction = WaitChosenActionChoice
				.getInstance();

		// automatically play the first ChosenAction
		StackManager.actionManager.completeChosenAction(WaitChosenActionChoice
				.getInstance().succeedClickOn(
						(JChosenAction) actionsPanel.getComponent(0)));

		/*
		 * if (actionsPanel.getComponentCount() == 1) { // play automatically this
		 * action StackManager.actionManager.currentAction =
		 * WaitChosenActionChoice.instance; StackManager.actionManager
		 * .completeChosenAction(WaitChosenActionChoice.instance
		 * .succeedClickOn(((JCostAction) actionsPanel.getComponent(0)))); return
		 * false; // return reActivate(((JCostAction) //
		 * actionsPanel.getComponent(0)).context.contextID); }
		 * StackManager.actionManager.waitActionChoice();
		 */
		return false;
	}

	/**
	 * Reactivate this manager giving the choice to highlight.
	 * 
	 * @param contextID
	 *          the action context id
	 * @return true if there is no more ChosenAction to activate. Othervise false
	 *         value indicate a ChosenAction has been activated. public boolean
	 *         reActivate(int contextID) { if
	 *         (!StackManager.actionManager.getActionContext(contextID).isCompleted()) { //
	 *         the action to activate is not completed if (currentAction != null) { //
	 *         first disactivate the current action currentAction.disHighLight();
	 *         currentAction = null; } for (int index = 0; index <
	 *         actionsPanel.getComponentCount(); index++) { if
	 *         (actionsPanel.getComponent(index) instanceof JChosenAction) {
	 *         currentAction = (JChosenAction) actionsPanel.getComponent(index);
	 *         if (currentAction.context.contextID == contextID) { return
	 *         currentAction.setSelected(); } } } throw new InternalError( "could
	 *         not find the ChosenAction with contextID=" + contextID); } // the
	 *         action to is already completed return true; }
	 */

	/**
	 * Initialize
	 */
	public void initialize() {
		miniCard.setToolTipText(CardFactory.ttHeader + "??");
		miniCard
				.setIcon(new WizardImageIcon(DatabaseFactory.scaledBackImage, null));
		ttClick = "<br><br><b>"
				+ LanguageManager.getString("clickchosenaction.tooltip") + "</b>";
	}

	/**
	 * Set the current ChosenAction as completed. Incrementing the internal
	 * execution count and setting the record index.
	 * 
	 * @param ability
	 *          the current ability.
	 * @param context
	 *          the context of current ability.
	 * @param actionContext
	 *          the context of current action.
	 * @return true if the current ChosenAction is completed and if this action
	 *         has not to be repeated.
	 */
	public boolean completeAction(Ability ability, ContextEventListener context,
			ActionContextWrapper actionContext) {
		// increment counter
		actionContext.done++;
		// notify refresh description text
		if (actionContext.done >= actionContext.repeat) {
			// completely done
			actionContext.recordIndex = recordIndex++;
			actionContext.refreshText(ability, context);
			return true;
		}
		actionContext.refreshText(ability, context);
		return false;
	}

	/**
	 * Find the first uncompleted action, and execute it.
	 * 
	 * @return true if an uncompleted has been found.
	 */
	public boolean playFirstUncompleted() {
		for (int index = 0; index < actionsPanel.getComponentCount(); index++) {
			if (actionsPanel.getComponent(index) instanceof JChosenAction) {
				currentAction = (JChosenAction) actionsPanel.getComponent(index);
				if (!currentAction.context.isCompleted()) {
					return currentAction.setSelected();
				}
			}
		}
		// no more uncompleted action
		return true;
	}

	/**
	 * This method is invoked when opponent has clicked on this object.
	 * 
	 * @param data
	 *          data sent by opponent.
	 */
	public void clickOn(byte[] data) {
		// waiting for player information
		JChosenAction action = (JChosenAction) actionsPanel.getComponent(data[0]);
		Log.debug("clickOn(VirtualInput):" + action.context.action);
		StackManager.actionManager.succeedClickOn(action);
	}

	/**
	 * Send to opponent the message indicating that we've clicked on this card
	 * 
	 * @param action
	 *          the chosen action.
	 */
	public void sendClickToOpponent(JChosenAction action) {
		for (int index = 0; index < actionsPanel.getComponentCount(); index++) {
			if (actionsPanel.getComponent(index) instanceof JChosenAction
					&& actionsPanel.getComponent(index) == action) {
				ConnectionManager.send(CoreMessageType.CLICK_ACTION, (byte) index);
				return;
			}
		}
		Log.fatal("could not find the ChosenAction to send");
	}

	/**
	 * The current record index the completed action would obtain.
	 */
	private int recordIndex;

	/**
	 * The selected action. Is null if no action is selected.
	 */
	private JChosenAction currentAction;

	private JLabel miniCard;

	/**
	 * Panel containing card of ability to pay
	 */
	private JPanel actionsPanel;

	/**
	 * The cancel button allowing to cancel the current spell.
	 */
	public JButton cancelButton;

	static String ttClick;
}
