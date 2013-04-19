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
package net.sf.firemox.ui.wizard;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.ui.UIHelper;
import net.sf.firemox.ui.component.JLink;
import net.sf.firemox.ui.i18n.LanguageManager;

import org.apache.commons.io.IOUtils;

/**
 * @author Fabrice Daugan
 * @since 0.53
 * @since 0.54 MP logo added
 */
public class About extends Ok {

	/**
	 * Creates a new instance of About <br>
	 * 
	 * @param parent
	 */
	public About(JFrame parent) {
		super(LanguageManager.getString("wiz_about.title"), LanguageManager
				.getString("wiz_about.description"), "mp64.gif", LanguageManager
				.getString("close"), 420, 620);
		JPanel thanksPanel = new JPanel();

		thanksPanel.setLayout(new BoxLayout(thanksPanel, BoxLayout.X_AXIS));
		thanksPanel.setMaximumSize(new Dimension(32767, 26));
		thanksPanel.setOpaque(false);
		JLabel jLabel5 = new JLabel(LanguageManager.getString("version") + " : ");
		jLabel5.setFont(MToolKit.defaultFont);
		jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel5.setMaximumSize(new Dimension(100, 16));
		jLabel5.setMinimumSize(new Dimension(100, 16));
		jLabel5.setPreferredSize(new Dimension(100, 16));
		thanksPanel.add(jLabel5);
		jLabel5 = new JLabel(IdConst.VERSION);
		thanksPanel.add(jLabel5);
		gameParamPanel.add(thanksPanel);

		thanksPanel = new JPanel();
		thanksPanel.setLayout(new BoxLayout(thanksPanel, BoxLayout.X_AXIS));
		thanksPanel.setOpaque(false);
		JPanel thanksPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEADING));
		thanksPanelLeft.setLayout(new BoxLayout(thanksPanelLeft, BoxLayout.Y_AXIS));
		thanksPanelLeft.setMaximumSize(new Dimension(100, 2000));
		thanksPanelLeft.setMinimumSize(new Dimension(100, 16));
		jLabel5 = new JLabel(LanguageManager.getString("thanks") + " : ");
		jLabel5.setFont(MToolKit.defaultFont);
		jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel5.setMaximumSize(new Dimension(100, 16));
		jLabel5.setMinimumSize(new Dimension(100, 16));
		jLabel5.setPreferredSize(new Dimension(100, 16));
		thanksPanelLeft.add(jLabel5);
		thanksPanel.add(thanksPanelLeft);
		thanksPanelLeft = new JPanel();
		thanksPanelLeft.setLayout(new BoxLayout(thanksPanelLeft, BoxLayout.Y_AXIS));
		StringBuilder contributors = new StringBuilder();
		addContributor(contributors, "Fabrice Daugan", "developper", "france");
		addContributor(contributors, "Hoani Cross", "developper", "frenchpolynesia");
		addContributor(contributors, "nico100", "graphist", "france");
		addContributor(contributors, "seingalt_tm", "graphist", "france");
		addContributor(contributors, "surtur2", "tester", null);
		addContributor(contributors, "Jan Blaha", "developper", "cz");
		addContributor(contributors, "Tureba", "developper", "brazil");
		addContributor(contributors, "hakvf", "tester", "france");
		addContributor(contributors, "Stefano \"Kismet\" Lenzi", "developper",
				"italian");
		jLabel5 = new JLabel(contributors.toString());
		jLabel5.setFont(MToolKit.defaultFont);
		jLabel5.setHorizontalAlignment(SwingConstants.LEFT);
		thanksPanelLeft.add(jLabel5);
		jLabel5 = new JLink("http://obsidiurne.free.fr",
				"morgil has designed the splash screen");
		jLabel5.setFont(MToolKit.defaultFont);
		thanksPanelLeft.add(jLabel5);
		thanksPanel.add(thanksPanelLeft);

		gameParamPanel.add(thanksPanel);

		thanksPanel = new JPanel();
		thanksPanel.setLayout(new BoxLayout(thanksPanel, BoxLayout.X_AXIS));
		thanksPanel.setMaximumSize(new Dimension(32767, 26));
		thanksPanel.setOpaque(false);
		JLabel blanklbl = new JLabel();
		blanklbl.setHorizontalAlignment(SwingConstants.RIGHT);
		blanklbl.setMaximumSize(new Dimension(100, 16));
		blanklbl.setMinimumSize(new Dimension(100, 16));
		blanklbl.setPreferredSize(new Dimension(100, 16));
		thanksPanel.add(blanklbl);

		jLabel5 = new JLink("http://sourceforge.net/tracker/?func=add&group_id="
				+ IdConst.PROJECT_ID + "&atid=601043", LanguageManager
				.getString("joindev"));
		jLabel5.setFont(MToolKit.defaultFont);
		thanksPanel.add(jLabel5);
		gameParamPanel.add(thanksPanel);

		thanksPanel = new JPanel();
		thanksPanel.setLayout(new BoxLayout(thanksPanel, BoxLayout.X_AXIS));
		thanksPanel.setOpaque(false);
		jLabel5 = new JLabel(LanguageManager.getString("projecthome") + " : ");
		jLabel5.setFont(MToolKit.defaultFont);
		jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel5.setMaximumSize(new Dimension(100, 16));
		jLabel5.setMinimumSize(new Dimension(100, 16));
		jLabel5.setPreferredSize(new Dimension(100, 16));
		thanksPanel.add(jLabel5);
		jLabel5 = new JLink(IdConst.MAIN_PAGE, IdConst.MAIN_PAGE);
		jLabel5.setFont(MToolKit.defaultFont);
		thanksPanel.add(jLabel5);
		gameParamPanel.add(thanksPanel);

		// forum francais
		thanksPanel = new JPanel();
		thanksPanel.setLayout(new BoxLayout(thanksPanel, BoxLayout.X_AXIS));
		thanksPanel.setOpaque(false);
		jLabel5 = new JLabel(LanguageManager.getString("othersites") + " : ");
		jLabel5.setFont(MToolKit.defaultFont);
		jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel5.setMaximumSize(new Dimension(100, 16));
		jLabel5.setMinimumSize(new Dimension(100, 16));
		jLabel5.setPreferredSize(new Dimension(100, 16));
		thanksPanel.add(jLabel5);
		jLabel5 = new JLink("http://www.Firemox.fr.st", UIHelper
				.getIcon("mpfrsml.gif"), SwingConstants.LEFT);
		jLabel5.setToolTipText(LanguageManager.getString("frenchforum.tooltip"));
		thanksPanel.add(jLabel5);
		jLabel5 = new JLabel();
		jLabel5.setMaximumSize(new Dimension(1000, 16));
		thanksPanel.add(jLabel5);
		gameParamPanel.add(thanksPanel);

		JTextArea disclaimer = new JTextArea();
		disclaimer.setEditable(false);
		disclaimer.setLineWrap(true);
		disclaimer.setWrapStyleWord(true);
		disclaimer.setAutoscrolls(true);
		disclaimer.setTabSize(2);
		disclaimer.setFont(new Font("Arial", 0, 10));
		// Then try and read it locally
		BufferedReader inGPL = null;
		try {
			inGPL = new BufferedReader(new InputStreamReader(MToolKit
					.getResourceAsStream(IdConst.FILE_LICENSE)));
			disclaimer.read(inGPL, "");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(inGPL);
		}
		JScrollPane disclaimerSPanel = new JScrollPane();
		disclaimerSPanel
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		MToolKit.addOverlay(disclaimerSPanel);
		disclaimerSPanel.setViewportView(disclaimer);
		gameParamPanel.add(disclaimerSPanel);
	}

	/**
	 * Add a contributor to the given buffer.
	 * 
	 * @param contributors
	 *          the current buffer.
	 * @param name
	 *          the nickname/real name.
	 * @param role
	 *          the contributor role.
	 * @param country
	 *          the country key.
	 */
	private void addContributor(StringBuilder contributors, String name,
			String role, String country) {
		if (contributors.length() == 0) {
			contributors.append("<html>");
		} else {
			contributors.append("<br>");
		}
		contributors.append("<b>");
		contributors.append(name);
		contributors.append("</b> ");
		contributors.append(LanguageManager.getString(role));
		if (country != null && country.length() > 0) {
			contributors.append(" <i>(");
			contributors.append(LanguageManager.getString(country));
			contributors.append(")</i>");
		}
	}

}