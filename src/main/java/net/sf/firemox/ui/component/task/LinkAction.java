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
package net.sf.firemox.ui.component.task;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.Action;

import net.sf.firemox.tools.MToolKit;
import net.sf.firemox.tools.WebBrowser;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public class LinkAction extends TaskAction {

	/**
	 * Create a new instance of this class.
	 * 
	 * @param dbStream
	 *          the stream containing the action definition.
	 * @throws IOException
	 *           if error occurred during the reading process from the specified
	 *           input stream
	 */
	public LinkAction(final InputStream dbStream) throws IOException {
		super(dbStream);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Display a popup : open this URL
		try {
			if (url.startsWith("file:")) {
				WebBrowser.launchBrowser(url);
			} else {
				WebBrowser.launchBrowser("file:///" + MToolKit.getRelativePath() + "/"
						+ url);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected void setValue(String htmlValue) {
		putValue(Action.NAME, "<html><u>" + title + "<u>");
		url = htmlValue;
	}

	/**
	 * The url of this link
	 */
	private String url;

}
