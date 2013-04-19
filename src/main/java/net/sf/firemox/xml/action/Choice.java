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
package net.sf.firemox.xml.action;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import net.sf.firemox.action.Actiontype;
import net.sf.firemox.xml.XmlAction;
import net.sf.firemox.xml.XmlTbs;
import net.sf.firemox.xml.XmlToMDB;
import net.sf.firemox.xml.XmlTools;
import net.sf.firemox.xml.XmlParser.Node;

/**
 * Propose to choose within several valid actions list. If 'hop' attribute is
 * specified and is 'true', cancel option would be available during the choice.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.85
 */
public class Choice implements XmlToMDB {

	/**
	 * <ul>
	 * Structure of stream : Data[size]
	 * <li>message [Msg]</li>
	 * <li>allow cancel [boolean]</li>
	 * <li>hop on cancel [Expression]</li>
	 * <li>action index [1]</li>
	 * <li>action is in effects [boolean]</li>
	 * <li>nb choices [1]</li>
	 * <li>size of choice #1 [int]</li>
	 * <li>size of choice #n [int]</li>
	 * <li>actions of choice #1 [Action[]]</li>
	 * <li>hop action value= SIGMA(i={2..n}, nb actions of choice i) [Expression]</li>
	 * <li>actions of choice #2 [Action[]]</li>
	 * <li>hop action value= SIGMA(i={m..n}, nb actions of choice i) [Expression]</li>
	 * </ul>
	 * 
	 * @param node
	 *          the XML action structure
	 * @param out
	 *          output stream where the card structure will be saved.
	 * @return the amount of written action in the output.
	 * @throws IOException
	 */
	public int buildMdb(Node node, OutputStream out) throws IOException {
		Msg.buildMdbMsg(Actiontype.CHOICE, node, out);

		out.write("true".equals(node.getAttribute("cancel")) ? 1 : 0);
		XmlTools.writeAttrOptionsDefault(node, "hop", out, "0");
		out.write(XmlTbs.currentActionIndex);
		out.write(XmlTbs.currentInEffect ? 1 : 0);
		final List<Node> choices = node.getNodes("either");
		final int nbChoices = choices.size();
		final byte[] nbActions = new byte[nbChoices];
		final long[] offsets = new long[nbChoices];

		// prepare the actions counts
		out.write(nbChoices);
		final long startOffset = ((FileOutputStream) out).getChannel().position();
		out.write(nbActions);

		// write choices
		for (int idChoice = 0; idChoice < nbChoices; idChoice++) {
			final Iterator<?> it = choices.get(idChoice).iterator();
			while (it.hasNext()) {
				final Object obj = it.next();
				if (obj instanceof Node) {
					nbActions[idChoice] += XmlAction.getAction(((Node) obj).getTag())
							.buildMdb((Node) obj, out);
				}
			}
			if (idChoice < nbChoices - 1) {
				Actiontype.HOP.write(out);
				out.write(0); // no name
				XmlAction.writeDebug("implicit-choice-hop", out); // no debug
				offsets[idChoice] = ((FileOutputStream) out).getChannel().position();
				XmlTools.writeConstant(out, 0);
			}
		}
		final long endOffset = ((FileOutputStream) out).getChannel().position();

		// write the actions counts
		((FileOutputStream) out).getChannel().position(startOffset);
		out.write(nbActions);

		// write hop values
		int totalSum = 1;
		for (int idChoice = 0; idChoice < nbChoices - 1; idChoice++) {
			((FileOutputStream) out).getChannel().position(offsets[idChoice]);
			int sum = 0;
			totalSum += nbActions[idChoice] + 1;
			for (int i = idChoice + 1; i < nbChoices; i++) {
				sum += nbActions[i] + (i < nbChoices - 1 ? 2 : 1);
			}
			XmlTools.writeConstant(out, sum);
		}
		totalSum += nbActions[nbChoices - 1];
		((FileOutputStream) out).getChannel().position(endOffset);
		return totalSum;
	}

}