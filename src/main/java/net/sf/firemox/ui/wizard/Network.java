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
package net.sf.firemox.ui.wizard;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import net.sf.firemox.DeckBuilder;
import net.sf.firemox.deckbuilder.Deck;
import net.sf.firemox.deckbuilder.DeckConstraint;
import net.sf.firemox.deckbuilder.DeckConstraints;
import net.sf.firemox.deckbuilder.DeckReader;
import net.sf.firemox.tools.Configuration;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.component.PasswordChecker;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.jvnet.lafwidget.LafWidget;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.81
 */
public class Network extends YesNo implements ItemListener {

	/**
	 * Minimal port number.
	 */
	protected static final int MINIMAL_PORT = 1000;

	/**
	 * Maximal port number
	 */
	protected static final int MAXIMAL_PORT = 65535;

	/**
	 * Creates a new instance of Network <br>
	 * 
	 * @param title
	 *          the title of this wizard.
	 * @param description
	 *          the description appended to the title of this wizard. This content
	 *          will be displayed as Html.
	 * @param okButton
	 *          the 'ok button' label.
	 * @param width
	 *          the preferred width.
	 * @param height
	 *          the preferred height.
	 */
	public Network(String title, String description, String okButton, int width,
			int height) {
		super(title, description, "wiz_run.gif", okButton, LanguageManager
				.getString("cancel"), width, height);

		// client deck file
		final JPanel deckPanel = setSizes(new JLabel(LanguageManager
				.getString("wiz_network.deck")
				+ " : "));
		deckList = new JComboBox(Configuration.getArray("decks.deck"));
		deckList.setEditable(true);
		deckList.setSelectedIndex(0);
		deckList.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_network.deck.tooltip"));
		deckList
				.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE, MAXIMAL_TXT_SIZE));
		deckList.setPrototypeDisplayValue(new Object());
		deckList.addItemListener(this);
		addCheckValidity(deckList);
		deckPanel.add(deckList);
		deckBtn = new JButton("...");
		deckBtn.setMnemonic('.');
		deckBtn.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_network.deck.browse.tooltip"));
		deckBtn.setPreferredSize(new Dimension(MAXIMAL_TXT_SIZE, MAXIMAL_TXT_SIZE));
		deckBtn.setMaximumSize(deckBtn.getPreferredSize());
		deckBtn.addActionListener(this);
		deckPanel.add(deckBtn);

		// JDeckBuilder button
		jdeckBuilderBtn = new JButton(UIHelper.getIcon("menu_tools_jdb.gif"));
		jdeckBuilderBtn.setToolTipText("<html>"
				+ LanguageManager.getString("menu_tools_jdb.tooltip"));
		jdeckBuilderBtn.setPreferredSize(deckBtn.getPreferredSize());
		jdeckBuilderBtn.setMaximumSize(deckBtn.getPreferredSize());
		jdeckBuilderBtn.addActionListener(this);
		deckPanel.add(jdeckBuilderBtn);
		gameParamPanel.add(deckPanel);

		// Deck constraints
		final JPanel deckConstraint = setSizes(new JLabel(LanguageManager
				.getString("wiz_network.deck.constraint")
				+ " : "));
		constraintList = new JComboBox();
		for (DeckConstraint constraint : DeckConstraints.getDeckConstraints()) {
			constraintList.addItem(constraint);
		}
		String constraint = Configuration.getString("decks.constraint",
				DeckConstraints.DECK_CONSTRAINT_NAME_NONE);
		constraintList.setEditable(false);
		constraintList.setSelectedItem(DeckConstraints
				.getDeckConstraint(constraint));
		constraintList.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_network.deck.constraint.tooltip"));
		constraintList.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE,
				MAXIMAL_TXT_SIZE));
		deckConstraint.add(constraintList);

		validator = new JButton(UIHelper.getIcon("wiz_network.deck.validator.gif"));
		validator.setToolTipText("<html>"
				+ LanguageManager.getString("wiz_network.deck.validator.tooltip"));
		validator.setPreferredSize(deckBtn.getPreferredSize());
		validator.setMaximumSize(deckBtn.getPreferredSize());
		validator.addActionListener(this);
		deckConstraint.add(validator);

		gameParamPanel.add(deckConstraint);

		// port of connection
		final JPanel gamePortPanelClient = setSizes(new JLabel(LanguageManager
				.getString("wiz_network.port")
				+ " : "));
		final NumberFormatter format = new NumberFormatter();
		format.setMinimum(MINIMAL_PORT);
		format.setMaximum(Short.MAX_VALUE);
		portTxt = new JFormattedTextField(format);
		portTxt.setValue(Configuration.getInt("port", 1299));
		portTxt.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE, MAXIMAL_TXT_SIZE));
		addCheckValidity(portTxt);
		gamePortPanelClient.add(portTxt);
		gameParamPanel.add(gamePortPanelClient);

		// IP of server
		JPanel gameIPPanelClient = setSizes(new JLabel(LanguageManager
				.getString("wiz_network.ip")
				+ " : "));
		ipTxt = new JTextField(Configuration.getString("ip", "127.0.0.1"));
		ipTxt.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE, MAXIMAL_TXT_SIZE));
		gameIPPanelClient.add(ipTxt);
		gameParamPanel.add(gameIPPanelClient);

		// password of game to join
		JPanel clientPasswordPanel = setSizes(new JLabel(UIHelper
				.getIcon("pswd.gif")));
		passwordTxt = new JPasswordField();
		passwordTxt.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE,
				MAXIMAL_TXT_SIZE));
		passwordTxt.putClientProperty(LafWidget.PASSWORD_STRENGTH_CHECKER,
				new PasswordChecker());
		clientPasswordPanel.add(passwordTxt);
		gameParamPanel.add(clientPasswordPanel);
	}

	@Override
	protected boolean checkValidity() {
		try {
			if (getSelectedDeck() == null || getSelectedDeck().length() == 0) {
				wizardInfo.resetError(LanguageManager
						.getString("wiz_network.deck.missed"));
			} else if (MToolKit.getFile(getSelectedDeck()) == null
					|| !MToolKit.getFile(getSelectedDeck()).exists()
					|| !MToolKit.getFile(getSelectedDeck()).isFile()) {
				wizardInfo.resetError(LanguageManager
						.getString("wiz_network.deck.notfound"));
			} else if (portTxt.getText().length() == 0) {
				wizardInfo.resetError(LanguageManager
						.getString("wiz_network.port.missed"));
			} else if ((Integer) portTxt.getValue() < 0
					|| (Integer) portTxt.getValue() > Short.MAX_VALUE) {
				wizardInfo.resetError(LanguageManager
						.getString("wiz_network.port.outofrange"));
			} else {
				return true;
			}
		} catch (NumberFormatException e) {
			// Ignore this error
		}
		return false;
	}

	/**
	 * Return the selected deck file.
	 * 
	 * @return the selected deck file.
	 */
	private String getSelectedDeck() {
		return (String) deckList.getSelectedItem();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final Object source = event.getSource();
		if (source == deckBtn) {
			/*
			 * invoked when server user chooses a deck file
			 */
			final String dekFile = MToolKit.getDeckFile();
			if (dekFile != null) {
				deckList.removeAllItems();
				for (String item : Configuration.getArray("decks.deck"))
					deckList.addItem(item);

				deckList.setSelectedItem(dekFile);
				if (checkValidity()) {
					wizardInfo.reset();
				}
			}
		} else if (source == jdeckBuilderBtn) {
			// open the deck builder
			dispose();
			DeckBuilder.loadFromMagic();
		} else if (source == validator) {
			// validate the deck
			try {
				if (validateDeck()) {
					JOptionPane.showMessageDialog(this, LanguageManager.getString("ok"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			super.actionPerformed(event);
		}
	}

	/**
	 * Initialize the preferred size of given label in order to obtain an right
	 * aligned label text.
	 * 
	 * @param label
	 *          the label to add to the returned JPanel component.
	 * @return the created JPanel component containing the given label. The
	 *         returned panel has it's maximum size set in order to have a correct
	 *         gap between each created panel this way.
	 */
	protected JPanel setSizes(JLabel label) {
		JPanel res = new JPanel();
		res.setLayout(new BoxLayout(res, BoxLayout.X_AXIS));
		res
				.setMaximumSize(new Dimension(UNBOUNDED_TXT_SIZE, MAXIMAL_TXT_SIZE + 10));
		label.setPreferredSize(new Dimension(MARGIN_SIZE, MAXIMAL_TXT_SIZE));
		label.setMinimumSize(label.getPreferredSize());
		label.setMaximumSize(label.getPreferredSize());
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		res.add(label);
		return res;
	}

	/**
	 * Validate the current deck. When the validate success, the deck is updated.
	 * 
	 * @return <code>true</code> if the selected deck exists and is valid.
	 * @throws Exception
	 *           if the deck could not be validated.
	 */
	protected boolean validateDeck() throws Exception {
		String deckName = getSelectedDeck();
		this.deck = DeckReader.getDeck(this, deckName);
		if (this.deck == null) {
			return false;
		}
		return DeckReader.validateDeck(this, deck, (DeckConstraint) constraintList
				.getSelectedItem());
	}

	/**
	 * The text field containing the IP address
	 */
	protected final JTextField ipTxt;

	/**
	 * The recent deck lists
	 */
	protected final JComboBox deckList;

	/**
	 * The available constraints
	 */
	protected final JComboBox constraintList;

	/**
	 * The button used to open your file browser
	 */
	private final JButton deckBtn;

	/**
	 * The text filed containing the password to use for connection.
	 */
	protected final JPasswordField passwordTxt;

	/**
	 * The button used to open the deck builder
	 */
	private final JButton jdeckBuilderBtn;

	/**
	 * The button used to validate the deck.
	 */
	private final JButton validator;

	/**
	 * The validated deck.
	 */
	protected Deck deck;

	/**
	 * The text field containing the port number used for connection.
	 */
	protected final JFormattedTextField portTxt;

	public void itemStateChanged(ItemEvent e) {
		if (checkValidity())
			wizardInfo.reset();

	}

}
