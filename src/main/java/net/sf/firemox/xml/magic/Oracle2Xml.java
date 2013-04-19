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
package net.sf.firemox.xml.magic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import net.sf.firemox.token.IdConst;
import net.sf.firemox.tools.MToolKit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.90
 */
public final class Oracle2Xml {

	/**
	 * May the validated cards (recycled directory) be patched by the new ones?
	 */
	private static final boolean UPDATE_CARD = false;

	/**
	 * Bounds
	 */
	private static final int MAXI = Integer.MAX_VALUE;

	private static final String TBS_NAME = IdConst.TBS_DEFAULT;

	private static Options options;

	/**
	 * Prevent this class to be instantiated.
	 */
	private Oracle2Xml() {
		super();
	}

	/**
	 * Verifies the existence of the input and the outputs for the process.
	 * 
	 * @param oracleFile
	 *          the Oracle(TM) formatted text file
	 * @param destinationDir
	 *          the directory where built card will be placed
	 * @param recycledDir
	 *          the directory where already built card are placed
	 */
	private void verifyInputsOutputs(File oracleFile, File destinationDir,
			File recycledDir) {
		if (!oracleFile.exists() || !oracleFile.isFile()) {
			System.err.println("The file '" + oracleFile + "' does not exist");
			System.exit(-1);
			return;
		}

		if (!destinationDir.exists() || !destinationDir.isDirectory()) {
			System.err.println("The destination directory '" + destinationDir
					+ "' does not exist");
			System.exit(-1);
			return;
		}
	}

	/**
	 * The main serialization method, used to serialize the given Oracle(TM) text
	 * file to as many XML cards it can parse properly. The parsed cards are
	 * stored in the directory identified by the <code>destinationDir</code>
	 * parameter while the final directory to store the validated cards are stored
	 * in the path represented by the <code>recycledDir</code> parameter.
	 * 
	 * @param oracleFile
	 *          the Oracle(TM) formatted text file
	 * @param destinationDir
	 *          the directory where built card will be placed
	 * @param recycledDir
	 *          the directory where already built card are placed
	 */
	@SuppressWarnings("null")
	public void serialize(File oracleFile, File destinationDir, File recycledDir) {
		// input/outputs existence verification
		verifyInputsOutputs(oracleFile, destinationDir, recycledDir);

		// Start parsing
		final StringBuilder cardText = new StringBuilder();
		int nbCard = 0;
		MToolKit.tbsName = TBS_NAME;

		try {
			final BufferedReader in = new BufferedReader(new FileReader(oracleFile));
			PrintWriter out = null;

			// we parse until we reach the maximum integer value for the number of
			// cards
			while (nbCard < MAXI) {
				String line = in.readLine();

				// if this is the end of the file, we stop parsing, quitting the while
				// loop
				if (line == null)
					break;

				// the card name is first read
				String cardName = line.trim();
				
				// if this is an empty line, we skip it
				if (cardName.length() == 0) {
					continue;
				}

				/*
				 * This portion of the method is dedicated to the parsing of one new
				 * card.
				 */
				
				System.out.println("Parsing [" + cardName + "]");

				// we get the XML file name for the generated card
				String fileName = MToolKit.getExactKeyName(cardName) + ".xml";

				// we test wether the card already exists in recycledDir
				if (new File(recycledDir, fileName).exists()) {

					// is already validated existing parsed card in recycledDir can be
					// updated ?
					if (UPDATE_CARD) {
						// first, we copy the card to update to a file named "temp"
						File patchFile = MToolKit.getFile(recycledDir.getAbsolutePath()
								+ File.separator + fileName);
						File tempFile = File.createTempFile(fileName, "temp");
						FileUtils.copyFile(patchFile, tempFile);
						final BufferedReader inExist = new BufferedReader(new FileReader(
								tempFile));
						final PrintWriter outExist = new PrintWriter(new FileOutputStream(
								patchFile));
						String lineExist = null;
						boolean found = false;
						while ((lineExist = inExist.readLine()) != null) {
							if (!found && lineExist.contains("name=\"")) {
								lineExist = lineExist
										.substring(0, lineExist.indexOf("name=\""))
										+ "name=\""
										+ cardName
										+ lineExist.substring(lineExist.indexOf("\"", lineExist
												.indexOf("name=\"")
												+ "name=\"".length() + 2));
								found = true;
							}
							outExist.println(lineExist);
						}
						IOUtils.closeQuietly(inExist);
						IOUtils.closeQuietly(outExist);

						if (!found) {
							System.err.println(">> Error patching card '" + cardName + "'");
							// } else {
							// patching card
						}
					}
					skipCard(in);
					continue;
				}

				out = new PrintWriter(new FileOutputStream(new File(destinationDir,
						fileName)));
				int[] manaCost = null;

				// record the card text
				cardText.setLength(0);
				cardText.append("<!--\n\t");

				List<String> properties = new ArrayList<String>();
				String power = null;
				String toughness = null;
				isNonBasicLand = false;
				isLocalEnchantCreature = false;
				isLocalEnchantLand = false;
				isLocalEnchantArtifact = false;
				isEnchantWorld = false;
				isLocalEnchantCreatureArtifact = false;
				isLocalEnchantPermanent = false;
				isLocalEnchantEnchantment = false;
				isGlobalEnchant = false;
				isInstant = false;
				isCreature = false;
				isArtifact = false;
				isSorcery = false;
				isSwamp = false;
				isIsland = false;
				isForest = false;
				isMountain = false;
				isPlains = false;
				isScheme = false;
				boolean isHybrid = false;
				boolean isTribal = false;
				boolean isPlaneswalker = false;
				boolean isEquipment = false;

				line = in.readLine().trim().toLowerCase();
				cardText.append('\t').append(line).append("\n");

				isScheme = line.indexOf("scheme") != -1;
				isPlane = line.indexOf("plane ") != -1;
				isPhenomenon = line.indexOf("phenomenon") != -1;
				if (isScheme || isPlane || isPhenomenon) {
					// schemes/planes/phenomenons not supported (yet)
					while (true) {
						line = in.readLine();
						
						if (line == null || line.length() == 0)
							break;
					}
					new File(destinationDir,
							fileName).delete();
					continue;
				}
				
				isArtifact = line.indexOf("artifact") != -1;
				if (line.startsWith("land") || line.endsWith(" land")) {
					isNonBasicLand = true;
				} else {
					manaCost = extractManaCost(line);
					isHybrid = line.contains("(");
					line = in.readLine().replaceAll("\\(.*\\)", "").toLowerCase();
					cardText.append('\t').append(line).append("\n");
					isLocalEnchantCreature = line.indexOf("enchant creature") != -1;
					isEnchantWorld = line.indexOf("world enchantment") != -1;
					isAura = line.indexOf("aura") != -1;
					isTribal = line.indexOf("tribal") != -1;
					isPlaneswalker = line.indexOf("planeswalker") != -1;
					if (line.indexOf("enchantment") != -1 && !isAura && !isEnchantWorld)
						isGlobalEnchant = true;
					if (!isGlobalEnchant && !isAura && !isEnchantWorld
							&& !isLocalEnchantCreature) {
						isInstant = line.indexOf("instant") != -1;
						isCreature = line.indexOf("creature") != -1;
						isSorcery = line.indexOf("sorcery") != -1;
						isSwamp = line.indexOf("swamp") != -1;
						isIsland = line.indexOf("island") != -1;
						isForest = line.indexOf("forest") != -1;
						isMountain = line.indexOf("mountain") != -1;
						isArtifact = isArtifact || line.indexOf("artifact") != -1;
						isPlains = line.indexOf("plains") != -1;
					}
				}
				if (line.indexOf("snow") != -1)
					properties.add("snow");

				if (line.indexOf("legendary") != -1)
					properties.add("legend");

				if (line.indexOf("-") != -1) {
					properties = extractProperties(line.substring(line.indexOf("--") + 2)
							.trim(), properties);
				}
				if (isCreature) {
					line = in.readLine();
					cardText.append('\t').append(line).append("\n");
					if (line.indexOf("/") == -1) {
						System.err.println("Error reading card '" + cardName
								+ "' : power/toughness, line=" + line);
						skipCard(in);
						continue;
					}
					power = line.substring(0, line.indexOf('/')).trim();
					toughness = line.substring(line.indexOf('/') + 1).trim();
				}
				if (isEnchantWorld) {
					properties.add("enchant-world");
				}

				if (properties.contains("equipment"))
					isEquipment = true;

				List<String> lineBuffer = new ArrayList<String>();
				boolean hasBuyBack = false;
				boolean hasFlashBack = false;
				boolean hasForecast = false;
				boolean hasAffinity = false;
				boolean hasRampage = false;
				boolean hasTransmute = false;
				boolean hasDredge = false;
				boolean hasHaunt = false;
				boolean hasBloodThirst = false;
				boolean hasBushido = false;
				String hasTransmuteCost = null;
				int hasRampageCount = 1;
				int hasDredgeCount = 1;
				int hasBloodThirstCount = 1;
				int hasBushidoCount = 1;
				boolean hasFading = false;
				boolean hasLessToPlay = false;
				boolean hasMoreToPlay = false;
				boolean hasVanishing = false;
				lowerCard = cardName.toLowerCase();
				String hasBuyBackLine = null;
				String hasFlashBackLine = null;
				String hasAffinityLine = null;
				String hasForecastLine = null;
				String hasFadingLine = null;
				String hasVanishingLine = null;
				String hasSuspendLine = null;
				boolean hasFlanking = false;
				boolean hasHaunting = false;
				boolean hasSuspend = false;
				boolean hasLifelink = false;
				boolean hasDeathtouch = false;

				String equipLine = "";

				String lastLine = null;
				while (true) {
					line = in.readLine();
					
					if (line == null || line.length() == 0)
						break;
					// we drop the last line
					if (lastLine != null && lastLine.length() > 0) {
						lastLine = updateProperties(lastLine, properties);
						lineBuffer.add(lastLine);
					}
					lastLine = null;

					//line = line.replaceAll("T ", "T :").replaceAll("\\(.*\\)", "").toLowerCase();
					line = line.replaceAll("T ", "T :").toLowerCase();
					cardText.append('\t').append(line).append('\n');

					if (isAura && !isLocalEnchantCreature && !isLocalEnchantLand
							&& !isLocalEnchantCreatureArtifact && !isLocalEnchantArtifact
							&& !isLocalEnchantPermanent && !isLocalEnchantEnchantment) {
						isLocalEnchantCreature = line.indexOf("enchant creature") != -1;
						isLocalEnchantLand = line.indexOf("enchant land") != -1;
						isLocalEnchantCreatureArtifact = line
								.indexOf("enchant artifact creature") != -1;
						isLocalEnchantArtifact = !isLocalEnchantCreatureArtifact
								&& line.indexOf("enchant artifact") != -1;
						isLocalEnchantPermanent = line.indexOf("enchant permanent") != -1;
						isLocalEnchantEnchantment = line.indexOf("enchant enchantment") != -1;
					}

					if (line.startsWith("buyback")) {
						hasBuyBack = true;
						hasBuyBackLine = line.substring("buyback".length()).replaceAll("\\(.*\\)", "").trim();
						continue;
					} else if (line.startsWith("haunt")) {
						hasHaunt = true;
						continue;
					} else if (line.startsWith("flashback")) {
						hasFlashBack = true;
						hasFlashBackLine = line.substring("flashback".length()).replaceAll("\\(.*\\)", "").trim();
						continue;
					} else if (line.startsWith("affinity for ")) {
						hasAffinity = true;
						hasAffinityLine = line.substring("affinity for ".length()).replaceAll("\\(.*\\)", "").trim();
						continue;
					} else if (line.startsWith("forecast")) {
						hasForecast = true;
						hasForecastLine = line.substring("forecast - ".length()).replaceAll("\\(.*\\)", "").trim();
						continue;
					} else if (line.startsWith("rampage ")) {
						hasRampage = true;
						hasRampageCount = Integer.parseInt(line.substring(
								"rampage ".length(), "rampage ".length() + 1).trim());
						continue;
					} else if (line.startsWith("transmute ")) {
						hasTransmute = true;
						hasTransmuteCost = line.substring("transmute ".length()).replaceAll("\\(.*\\)", "").trim();
						continue;
					} else if (line.startsWith("dredge ")) {
						hasDredge = true;
						hasDredgeCount = Integer.parseInt(line.substring(
								"dredge ".length(), "dredge ".length() + 1).trim());
						continue;
					} else if (line.startsWith("bloodthirst ")) {
						hasBloodThirst = true;
						String bloodThirst = line.substring("bloodthirst ".length(),
								"bloodthirst ".length() + 1).trim();
						if (bloodThirst.compareTo("x") != 0)
							hasBloodThirstCount = Integer.parseInt(bloodThirst);
						else
							hasBloodThirstCount = 0; // 0 must be changed to a variable that
						// counts damage opponent has taken the present turn
						continue;
					} else if (line.startsWith("bushido ")) {
						hasBushido = true;
						hasBushidoCount = Integer.parseInt(line.substring(
								"bushido ".length(), "bushido ".length() + 1).trim());
						continue;
					} else if (line.startsWith("fading")) {
						hasFading = true;
						hasFadingLine = line.substring("fading".length()).replaceAll("\\(.*\\)", "").trim();
						continue;
					} else if (line.startsWith("vanishing")) {
						hasVanishing = true;
						hasVanishingLine = line.substring("vanishing".length()).replaceAll("\\(.*\\)", "").trim();
						continue;
					} else if (line.startsWith("flanking")) {
						hasFlanking = true;
						continue;
					} else if (line.startsWith("haunt")) {
						hasHaunting = true;
						continue;
					} else if (line.startsWith("suspend")) {
						hasSuspend = true;
						hasSuspendLine = line.substring("suspend ".length()).replaceAll("\\(.*\\)", "").trim();
						continue;
					}
					if (line.contains(" less to play.")) {
						hasLessToPlay = true;
					}
					if (line.contains(" more to play.")) {
						hasMoreToPlay = true;
					}
					if (line.contains("lifelink")) {
						hasLifelink = true;
					}
					if (line.contains("deathtouch")) {
						hasDeathtouch = true;
					}

					if (line.indexOf(lowerCard + " doesn't untap during") != -1) {
						properties.add("does-not-untap");
					}
					if (line.indexOf("you may choose not to untap") != -1) {
						properties.add("may-not-untap");
					}
					if (line.indexOf(lowerCard + " can't block") != -1) {
						properties.add("cannot-block");
					}
					if (line.indexOf(lowerCard + " can't attack") != -1) {
						properties.add("cannot-attack");
					}
					if (line.indexOf(lowerCard + " is unblockable.") != -1)
						properties.add("unblockable");

					if (line.indexOf(lowerCard + " is indestructible.") != -1)
						properties.add("indestructible");

					if (line.indexOf(lowerCard + " can't be countered.") != -1)
						properties.add("cannot-be-countered");

					if (line.indexOf(lowerCard + " attacks each turn if able.") != -1)
						properties.add("attacks-if-able");

					if (line.indexOf(lowerCard + " can't be blocked by walls.") != -1)
						properties.add("cannot-be-blocked-by-walls");

					if (line.indexOf(lowerCard + " can block only creatures with flying") != -1)
						properties.add("block-only-flying");
					
					lastLine = line;
				}

				cardText.append(" -->");
				out.println("<?xml version='1.0'?>");

				out.print("<card xmlns='");
				out.println(IdConst.NAME_SPACE + "'");
				out.print("\txmlns:xsi='");
				out.print(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
				out.println("'");
				out.print("\txsi:schemaLocation='");
				out.println(IdConst.NAME_SPACE + " ../../" + IdConst.TBS_XSD + "'");
				out.println("\tname=\"" + cardName + "\">");
				out.println("<rules-author-comment>Oracle2Xml generator "
						+ IdConst.VERSION + "</rules-author-comment>\n");
				out.println(cardText.toString());
				out.println("\n\t<!-- COMPLETE THE CODE OF THIS CARD -->\n");
				out.println("\t<init>");
				out.println("\t\t<registers>");
				if (manaCost != null) {
					if(isHybrid){
						int a = 0;
						for (int i=0;i<6;i++) {
							if (manaCost[i] > 0) {
								a+=manaCost[i];
								if(i!=0)
									break;
							}
						}
						out.println("\t\t\t<register index='colorless' value='" + a + "'/>");
					}else{
						for (int i = 6; i-- > 0;) {
							if (manaCost[i] > 0) {
								out.println("\t\t\t<register index='" + extractColor(i)
										+ "' value='" + manaCost[i] + "'/>");
							}
						}
					}
				}
				// power / toughness
				if (isCreature) {
					if (power.indexOf("*") != -1) {
						// power depends on ...
						out.println("\t\t\t<register index='power'>");
						out.println("\t\t\t\t<!-- REPLACE THIS CODE -->");
						out.println("\t\t\t\t<value>");
						out.println("\t\t\t\t\t<counter restriction-zone='play'>");
						out.println("\t\t\t\t\t\t<and>");
						out.println("\t\t\t\t\t\t\t<has-idcard idcard='swamp'/>");
						out.println("\t\t\t\t\t\t\t<controller player='you'/>");
						out.println("\t\t\t\t\t\t</and>");
						out.println("\t\t\t\t\t</counter>");
						out.println("\t\t\t\t</value>");
						out.println("\t\t\t</register>");
					} else {
						out
								.println("\t\t\t<register index='power' value='" + power
										+ "'/>");
					}
					if (toughness.indexOf("*") != -1) {
						// toughness depends on ...
						out.println("\t\t\t<register index='toughness'>");
						out.println("\t\t\t\t<!-- REPLACE THIS CODE -->");
						out.println("\t\t\t\t<value>");
						out.println("\t\t\t\t\t<counter restriction-zone='play'>");
						out.println("\t\t\t\t\t\t<and>");
						out.println("\t\t\t\t\t\t\t<has-idcard idcard='swamp'/>");
						out.println("\t\t\t\t\t\t\t<controller player='you'/>");
						out.println("\t\t\t\t\t\t</and>");
						out.println("\t\t\t\t\t</counter>");
						out.println("\t\t\t\t</value>");
						out.println("\t\t\t</register>");
					} else {
						out.println("\t\t\t<register index='toughness' value='" + toughness
								+ "'/>");
					}
				}
				out.println("\t\t</registers>");

				// colors
				if (manaCost != null && manaCost.length > 0) {
					out.print("\t\t<colors>");
					for (int i = manaCost.length - 1; i-- > 1;) {
						if (manaCost[i] > 0) {
							out.print(extractColor(i));
							if (i > 0) {
								out.print(" ");
							}
						}
					}
					out.println("</colors>");
				}

				// idcards
				out.print("\t\t<idcards>");
				if (isTribal)
					out.print("tribal ");
				if (isPlaneswalker)
					out.print("planeswalker ");
				else if (isCreature && isArtifact) {
					out.print("artifact-creature ");
				} else if (isArtifact)
					out.print("artifact ");
				else if (isCreature)
					out.print("creature ");
				if (isNonBasicLand) {
					out.print("land ");
				} else if (isAura) {
					out.print("local-enchantment ");
				} else if (isEnchantWorld) {
					out.print("enchant-world ");
				} else if (isGlobalEnchant) {
					out.print("global-enchantment ");
				} else if (isInstant) {
					out.print("instant ");
				} else if (isSorcery) {
					out.print("sorcery ");
				}
				if (isSwamp) {
					out.print("swamp ");
				}
				if (isIsland) {
					out.print("island ");
				}
				if (isForest) {
					out.print("forest ");
				}
				if (isMountain) {
					out.print("mountain ");
				}
				if (isPlains) {
					out.print("plains ");
				}
				out.println("</idcards>");

				// properties
				if (!properties.isEmpty()) {
					out.print("\t\t<properties>");
					for (String property : properties) {
						out.print(property);
						out.print(" ");
					}
					out.println("</properties>");
				}
				out.println("\t</init>");

				if (!lineBuffer.isEmpty()) {
					line = lineBuffer.get(0);
				}
				if (hasFlashBack) {
					out.println("\t<actions>");
					out.println("\t\t<action reference-name='main-effects'>");
					/*
					 * if (line.indexOf(':') != -1) { writeActions(out,
					 * line.substring(line.indexOf(':') + 1), true); }
					 */
					writeActions(out, line, true, false);
					out
							.println("\t\t\t\t<!-- PUT HERE EFFECTS OF THIS SPELL. THIS WILL BE INCLUDED TO FLASHBACK TOO -->");
					out.println("\t\t</action>");
					out.println("\t</actions>");
				}

				// abilities
				out.println("\t<abilities>");

				if (hasLifelink)
					out.println("\t\t<ability ref='lifelink'/>");

				if (hasDeathtouch)
					out.println("\t\t<ability ref='deathtouch'/>");

				if (hasSuspend) {
					out.println("\t\t<ability ref='cast-suspend'>");
					out.println("\t\t\t<actions>");
					writeCost(out, hasSuspendLine
							.substring(hasSuspendLine.indexOf("-") + 1), null);
					out.println("\t\t\t</actions>");
					out.println("\t\t\t<actions>");
					out.println("\t\t\t\t<repeat value='" + hasSuspendLine.charAt(0)
							+ "'/>");
					out.println("\t\t\t\t<add-object object-name='time'/>");
					out.println("\t\t\t</actions>");
					out.println("\t\t</ability>");
				}

				if (isAura) {
					out.println("\t\t<ability ref='cast-enchant'/>");
				} else if (isCreature || isGlobalEnchant || isEnchantWorld
						|| isArtifact || isPlaneswalker) {
					if (hasAffinity) {
						out.println("\t\t<ability ref='cast-spell'>");
						out.println("\t\t\t<actions>");
						writeAffinity(out, hasAffinityLine);
						out.println("\t\t\t</actions>");
						out.println("\t\t</ability>");
					} else if(isHybrid){
						int h, i1=0, i2=0, symbols=0;
						
						for(h=1;h<6;h++){
							if(manaCost[h]>0){
								if(i1 == 0){
									i1 = h;
									symbols = manaCost[h];
								}
								else
									i2 = h;
							}
						}
						for(h=0;h<=symbols;h++){
							out.println("\t\t<activated-ability playable='this' zone='hand'>");
							out.println("\t\t\t<cost>");
							out.print("\t\t\t\t<pay-mana ");

							if(manaCost[0] > 0)
								out.print("colorless='"+ manaCost[0] +"' ");
							
							if(symbols-h > 0)
								out.print(extractColor(i1) + "='"+ (symbols-h) +"' ");
							if(h > 0 && i2 > 0)
								out.print(extractColor(i2) + "='"+ h +"'");
						
							out.println("/>");
							out.println("\t\t\t</cost>");
							out.println("\t\t\t<effects>");
							out.println("\t\t\t\t<action ref='put-in-play' />");
							out.println("\t\t\t</effects>");
							out.println("\t\t</activated-ability>");
						}
					}else{
						out.println("\t\t<ability ref='cast-spell'/>");
					}
				} else if (isNonBasicLand || isForest || isPlains || isIsland
						|| isSwamp || isMountain) {
					out.println("\t\t<ability ref='cast-land'/>");
				}

				if (hasForecast) {
					out.println("\t\t<ability ref='reset-forecast' />");
					out.println("\t\t<ability ref='forecast'>");
					out.println("\t\t\t<actions>");
					writeCost(out, hasForecastLine, null);
					out.println("\t\t\t</actions>");
					out.println("\t\t\t<actions>");
					out.println("\t\t\t\t<!-- PUT HERE THE EFFECTS OF FORECAST -->");
					out.println("\t\t\t</actions>");
					out.println("\t\t</ability>");
				}

				if (isInstant || isSorcery) {
					out.println("\t\t<activated-ability "
							+ (hasBuyBack || hasFlashBack ? "reference-name='main-ability' "
									: "") + "playable='this' name='' zone='hand'>");
					out.println("\t\t\t<cost>");
					out.println("\t\t\t\t<pay-mana value='manacost'/>");
					if (hasAffinity)
						writeAffinity(out, hasAffinityLine);
					writeXmanaCost(out, manaCost, false);
					if (hasFlashBack) {
						out
								.println("\t\t\t\t<!-- PUT HERE THE COST OF THIS SPELL. THIS WILL NOT BE INCLUDED TO FLASHBACK COST -->");
						out.println("\t\t\t</cost>");
						out.println("\t\t\t<effects>");
						out.println("\t\t\t\t<action ref='main-effects'/>");
						out.println("\t\t\t\t<action ref='finish-spell'/>");

					} else {
						if (line.indexOf("at the beginning of") != -1) {
							out.println("\t\t<create-ability>");
							out
									.println("\t\t\t<!-- UPDATE PHASE NAME, TYPE and RESOLUTION -->");
							out
									.println("\t\t\t<triggered-ability resolution='normal' zone='play'>");
							out.println("\t\t\t\t<beginning-of-phase phase='upkeep'>");
							out.println("\t\t\t\t\t<test ref='during-your-upkeep'/>");
							out.println("\t\t\t\t</beginning-of-phase>");
							out.println("\t\t\t\t<effects>");
							writeActions(out, line, true, false);
							out
									.println("\t\t\t\t\t<!-- PUT HERE EFFECTS OF THIS DELAYED CARD -->");
							out.println("\t\t\t\t\t<unregister-this/>");
							out.println("\t\t\t\t</effects>");
							out.println("\t\t\t</triggered-ability>");
							out.println("\t\t</create-ability>");
						}
						if (line.indexOf(':') != -1) {
							writeActions(out, line.substring(0, line.indexOf(':')), true,
									false);
						} else {
							line = writeCost(out, "", line);
						}
						if (line.indexOf("as an additional cost to play " + lowerCard) != -1) {
							writeActions(out, line
									.substring(("as an additional cost to play " + lowerCard)
											.length()), true, false);
							line = "";
						}
						out.println("\t\t\t\t<!-- PUT HERE THE COST OF THIS SPELL -->");
						out.println("\t\t\t</cost>");
						out.println("\t\t\t<effects>");
						for (String subLine : lineBuffer) {
							writeActions(out, subLine, false, false);
						}
						out.println("\t\t\t\t<!-- PUT HERE EFFECTS OF THIS SPELL -->");
						out.println("\t\t\t\t<action ref='finish-spell'/>");
					}
					out.println("\t\t\t</effects>");
					out.println("\t\t</activated-ability>");
				}

				// Rampage
				if (hasRampage) {
					out.println("\t\t<ability ref='rampage" + hasRampageCount + "'/>");
				}

				// Transmute
				if (hasTransmute) {
					out.println("\t\t<ability ref='transmute'>");
					out.println("\t\t\t<actions>");
					writeCost(out, hasTransmuteCost, null);
					out.println("\t\t\t</actions>");
					out.println("\t\t</ability>");
				}

				// Dredge
				if (hasDredge) {
					out.println("\t\t<ability ref='dredge" + hasDredgeCount + "'/>");
				}

				// Haunt
				if (hasHaunt) {
					out.println("\t\t<ability ref='haunting'/>");
				}

				// BloodThirst
				if (hasBloodThirst) {
					out.println("\t\t<ability ref='bloodthirst" + hasBloodThirstCount
							+ "'/>");
				}

				// Bushido
				if (hasBushido) {
					out.println("\t\t<ability ref='bushido" + hasBushidoCount
							+ "-blocked'/>");
					out.println("\t\t<ability ref='bushido" + hasBushidoCount
							+ "-blocking'/>");
				}

				// Buyback
				if (hasBuyBack) {
					line = hasBuyBackLine;
					out
							.println("\t\t<activated-ability playable='this' name='buyback%a' zone='hand'>");
					out.println("\t\t\t<cost>");
					out.println("\t\t\t\t<action ref='buyback'/>");
					writeCost(out, line, null);
					out
							.println("\t\t\t\t<!-- PUT HERE THE COST OF BUYBACK, AND COMPLETE THE MAIN ABILITY OF THE SPELL -->");
					out.println("\t\t\t\t<insert-ability ref='main-ability'/>");
					out.println("\t\t\t</cost>");
					out.println("\t\t</activated-ability>");
				}

				// Flashback
				if (hasFlashBack) {
					line = hasFlashBackLine;
					out
							.println("\t\t<activated-ability playable='this' name='flashback%a' zone='graveyard'>");
					out.println("\t\t\t<cost>");
					writeCost(out, line, null);
					out
							.println("\t\t\t\t<!-- PUT HERE THE COST OF FLASHBACK, AND COMPLETE THE MAIN ABILITY OF THE SPELL -->");
					out.println("\t\t\t</cost>");
					out.println("\t\t\t<effects>");
					out.println("\t\t\t\t<action ref='main-effects'/>");
					out.println("\t\t\t\t<action ref='flashback'/>");
					out.println("\t\t\t</effects>");
					out.println("\t\t</activated-ability>");
				}

				// Flanking
				if (hasFlanking) {
					out.println("\t\t<ability ref='flanking'/>");
				}

				// Haunting
				if (hasHaunting) {
					out.println("\t\t<ability ref='haunting'/>");
				}

				// Fading
				if (hasFading) {
					out.println("\t\t<ability ref='fading'/>");
				}

				// Vanishing
				if (hasVanishing) {
					out.println("\t\t<ability ref='vanishing'/>");
				}

				for (int k = 0; k < lineBuffer.size(); k++) {
					line = lineBuffer.get(k);

					// Equipment
					if (line.contains("equipped creature gets"))
						equipLine = line.substring(line.indexOf("gets ") + "gets ".length()
								+ 1);

					if (line.indexOf("equip ") != -1 && isEquipment) {

						out.println("\t\t<activated-ability playable='this' zone='play'>");
						out.println("\t\t\t<cost>");
						writeCost(out, line.substring("equip".length()), null);
						out.println("\t\t\t\t<action ref='target-equipable-creature' />");
						out.println("\t\t\t</cost>");
						out.println("\t\t\t<effects>");
						out.println("\t\t\t\t<action ref='equip' />");
						out.println("\t\t\t</effects>");
						out.println("\t\t</activated-ability>");
					}

					// morph
					boolean hasMorph = false;

					if (line.indexOf("morph") == 0) {
						hasMorph = true;
						out.println("\t\t<ability ref='cast-morph'/>");
						out.println("\t\t<ability ref='morph'>");
						out.println("\t\t\t<actions>");
						writeCost(out, line.substring("morph".length()), null);
						out.println("\t\t\t</actions>");
						out.println("\t\t</ability>");
					}

					if (line.indexOf(lowerCard + " comes into play tapped") != -1) {
						out.println("\t\t<ability ref='come-into-play-tapped'/>");
					}

					if (!isInstant && !isSorcery && line.length() > 0
							&& line.indexOf(':') != -1) {
						// activated abilities
						out
								.println("\t\t<activated-ability playable='instant' name='' zone='play'>");

						// test part
						if (hasMorph) {
							out.println("\t\t\t<test>");
							out.println("\t\t\t\t<is-faceup card=\"me\"/>");
							out.println("\t\t\t</test>");
						}

						// 1) cost part
						out.println("\t\t\t<cost>");
						out.println("\t\t\t\t<!-- PUT HERE THE COST OF THIS ABILITY -->");
						writeCost(out, line.substring(0, line.indexOf(':')), line
								.substring(line.indexOf(':') + 1));
						writeActions(out, line.substring(0, line.indexOf(':')), true, true);
						if (line.contains("play this ability only once each turn"))
							out.println("\t\t\t\t<action ref='use-once-each-turn'/>");
						out.println("\t\t\t</cost>");

						// 2) effect part
						out.println("\t\t\t<effects>");
						out.println("\t\t\t\t<!-- PUT HERE EFFECTS OF THIS ABILITY -->");
						if (line.indexOf(":") != -1) {
							writeActions(out, line.substring(line.indexOf(':') + 1), false,
									false);
						} else {
							writeActions(out, line, false, false);
						}
						out.println("\t\t\t</effects>");
						out.println("\t\t</activated-ability>");
					}

					if (line.indexOf("when") != -1 || line.indexOf("whenever") != -1) {
						if (line.indexOf("comes into play") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<moved-card>");
							out.println("\t\t\t\t<source-test>");
							if (line.indexOf("when " + lowerCard + " comes into play") != -1) {
								out.println("\t\t\t\t\t<and>");
								out.println("\t\t\t\t\t\t<is-this/>");
								out.println("\t\t\t\t\t\t<not>");
								out
										.println("\t\t\t\t\t\t\t<in-zone zone='play' card='tested'/>");
								out.println("\t\t\t\t\t\t</not>");
								out
										.println("\t\t\t\t\t\t<!-- PUT HERE ADDITIONAL TEST ON CARD BEFORE IT GOES TO PLAY -->");
								out.println("\t\t\t\t\t</and>");
							} else {
								out
										.println("\t\t\t\t<!-- PUT HERE ADDITIONAL TEST ON CARD BEFORE IT GOES TO PLAY -->");
							}
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t\t<destination-test>");
							out
									.println("\t\t\t\t\t<!-- PUT HERE ADDITIONAL TEST ON CARD WHEN IT GOES TO PLAY -->");
							out.println("\t\t\t\t\t<in-zone zone='play' card='tested'/>");
							out.println("\t\t\t\t</destination-test>");
							out.println("\t\t\t</moved-card>");
						} else if (line.indexOf(" is put into ") < line
								.indexOf("graveyard from ")) {
							out.println("\t\t<triggered-ability zone='graveyard'>");
							out.println("\t\t\t<moved-card>");
							out.println("\t\t\t\t<source-test>");
							out
									.println("\t\t\t\t\t<!-- UPDATE TEST ON CARD BEFORE IT GOES TO GRAVEYARD -->");
							out.println("\t\t\t\t\t<and>");
							out.println("\t\t\t\t\t\t<is-this/>");
							out.println("\t\t\t\t\t\t<in-zone zone='play' card='tested'/>");
							out.println("\t\t\t\t\t</and>");
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t\t<destination-test>");
							out
									.println("\t\t\t\t\t<!-- PUT HERE ADDITIONAL TEST ON CARD WHEN IT GOES TO GRAVEYARD -->");
							out
									.println("\t\t\t\t\t<in-zone zone='graveyard' card='tested'/>");
							out.println("\t\t\t\t</destination-test>");
							out.println("\t\t\t</moved-card>");
						} else if (line.indexOf(" leaves play") != -1) {
							out.println("\t\t<triggered-ability zone='graveyard'>");
							out.println("\t\t\t<moved-card>");
							out.println("\t\t\t\t<source-test>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE TEST ON CARD BEFORE IT LEAVE PLAY -->");
							out.println("\t\t\t\t\t\t\t<in-zone zone='play' card='tested'/>");
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t</moved-card>");
						} else if (line.indexOf("blocks") != -1
								|| line.indexOf("becomes blocked") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<declared-blocking>");
							out.println("\t\t\t\t<blocking-test>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE THE TEST APPLIED ON BLOCKING CREATURE -->");
							out.println("\t\t\t\t</blocking-test>");
							out.println("\t\t\t\t<attacking-test>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE THE TEST APPLIED ON ATTACKING CREATURE -->");
							out.println("\t\t\t\t</attacking-test>");
							out.println("\t\t\t</declared-blocking>");
						} else if (line.indexOf("attacks and isn't blocked") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out
									.println("\t\t\t<modified-register operation='or' register='card' index='state'>");
							out.println("\t\t\t\t<source-test>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE THE TEST APPLIED ON UNBLOCKED CREATURE -->");
							out.println("\t\t\t\t\t<is-this/>");
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t</modified-register>");
						} else if (line.indexOf("deals combat damage to a player") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<assigned-damage type='damage-combat'>");
							out.println("\t\t\t\t<source-test>");
							out.println("\t\t\t\t\t<is-this/>");
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t\t<destination-test>");
							out.println("\t\t\t\t\t<is-player/>");
							out.println("\t\t\t\t</destination-test>");
							out.println("\t\t\t</assigned-damage>");
						} else if (line.indexOf("deals damage to") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<assigned-damage type='damage-any'>");
							out.println("\t\t\t\t<source-test>");
							out.println("\t\t\t\t\t<is-this/>");
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t\t<destination-test>");
							out.println("\t\t\t\t\t<and>");
							out.println("\t\t\t\t\t\t<not>");
							out.println("\t\t\t\t\t\t<is-player/>");
							out.println("\t\t\t\t\t\t</not>");
							out.println("\t\t\t\t\t\t<has-idcard idcard='creature'/>");
							out.println("\t\t\t\t\t</and>");
							out.println("\t\t\t\t</destination-test>");
							out.println("\t\t\t</assigned-damage>");
						} else if (line.indexOf("deals combat damage to") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<assigned-damage type='damage-combat'>");
							out.println("\t\t\t\t<source-test>");
							out.println("\t\t\t\t\t<is-this/>");
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t\t<destination-test>");
							out.println("\t\t\t\t\t<and>");
							out.println("\t\t\t\t\t\t<not>");
							out.println("\t\t\t\t\t\t\t<is-player/>");
							out.println("\t\t\t\t\t\t</not>");
							out.println("\t\t\t\t\t\t<has-idcard idcard='creature'/>");
							out.println("\t\t\t\t\t</and>");
							out.println("\t\t\t\t</destination-test>");
							out.println("\t\t\t</assigned-damage>");
						} else if (line.indexOf("deals damage to a player") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<assigned-damage type='damage-any'>");
							out.println("\t\t\t\t<source-test>");
							out.println("\t\t\t\t\t<is-this/>");
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t\t<destination-test>");
							out.println("\t\t\t\t\t<is-player/>");
							out.println("\t\t\t\t</destination-test>");
							out.println("\t\t\t</assigned-damage>");
						} else if (line.indexOf("becomes untapped") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<become-untapped>");
							out.println("\t\t\t\t<test>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE THE TEST APPLIED ON UNTAPPING CARD -->");
							out.println("\t\t\t\t\t<is-this card='tested'/>");
							out.println("\t\t\t\t</test>");
							out.println("\t\t\t</become-untapped>");
						} else if (line.indexOf("becomes tapped") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<become-tapped>");
							out.println("\t\t\t\t<test>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE THE TEST APPLIED ON TAPPING CARD -->");
							out.println("\t\t\t\t\t<is-this card='tested'/>");
							out.println("\t\t\t\t</test>");
							out.println("\t\t\t</become-tapped>");
						} else if (line.indexOf("attacks") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<declared-blocking>");
							out.println("\t\t\t\t<blocking-test>");
							out.println("\t\t\t\t\t<is-this/>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE THE TEST APPLIED ON BLOCKING CREATURE -->");
							out.println("\t\t\t\t</blocking-test>");
							out.println("\t\t\t\t<attacking-test>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE THE TEST APPLIED ON ATTACKING CREATURE -->");
							out.println("\t\t\t\t</attacking-test>");
							out.println("\t\t\t</declared-blocking>");
						} else if (line.indexOf("is turned face up") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<faced-up>");
							out.println("\t\t\t\t<test>");
							out.println("\t\t\t\t<!-- UPDATE TEST ON FACED UP CARD -->");
							out.println("\t\t\t\t\t<is-this />");
							out.println("\t\t\t\t</test>");
							out.println("\t\t\t</faced-up>");
						} else if (line.indexOf("becomes the target of") != -1) {
							out.println("\t\t<triggered-ability zone='play'>");
							out.println("\t\t\t<targeted>");
							out.println("\t\t\t\t<source-test>");
							out.println("\t\t\t\t<!-- UPDATE TEST ON SOURCE CARD -->");
							out.println("\t\t\t\t</source-test>");
							out.println("\t\t\t\t<destination-test>");
							out.println("\t\t\t\t<!-- UPDATE TEST ON DESTINATION TARGET -->");
							out.println("\t\t\t\t\t<is-this />");
							out.println("\t\t\t\t</destination-test>");
							out.println("\t\t\t</targeted>");
							line.replaceFirst("becomes the target of", "");
						} else {
							out.println("\t\t<triggered-ability zone='play'>");
							out
									.println("\t\t\t\t\t<!-- UPDATE THE EVENT OF THIS TRIGGERED ABILITY -->");
							out.println("\t\t\t<become-tapped>");
							out.println("\t\t\t\t<test>");
							out
									.println("\t\t\t\t\t<!-- COMPLETE THE TEST APPLIED ON TAPPING CARD -->");
							out.println("\t\t\t\t\t<is-this card='tested'/>");
							out.println("\t\t\t\t</test>");
							out.println("\t\t\t</become-tapped>");
						}
						if (line.contains("target")) {
							out.println("\t\t\t<cost>");
							writeTarget(out, line, true);
							out.println("\t\t\t</cost>");
						}
						out.println("\t\t\t<effects>");
						writeActions(out, line, false, false);
						out
								.println("\t\t\t\t<!-- PUT HERE EFFECTS OF THIS TRIGGERED ABILITY -->");
						out.println("\t\t\t</effects>");
						out.println("\t\t</triggered-ability>");
					} else if (line.indexOf("cycling") != -1) {
						// Cycling
						out.println("\t\t<ability ref='cycling'>");
						out.println("\t\t\t<actions>");
						writeCost(out, line.substring(
								line.indexOf("cycling") + "cycling".length()).trim(), null);
						out.println("\t\t\t</actions>");
						out.println("\t\t</ability>");
					} else if (line.indexOf("evoke") != -1) {
						// Evoke
						out.println("\t\t<ability ref='evoke"
								+ (properties.contains("flash") ? "-flash" : "") + "'>");
						out.println("\t\t\t<actions>");
						writeCost(out, line.substring(
								line.indexOf("evoke") + "evoke".length()).trim(), null);
						out.println("\t\t\t</actions>");
						out.println("\t\t</ability>");
					} else if (line.indexOf("madness") != -1) {
						// Madness
						out.println("\t\t<ability ref='madness'>");
						out.println("\t\t\t<actions>");
						writeCost(out, line.substring(
								line.indexOf("madness") + "madness".length()).trim(), null);
						out.println("\t\t\t</actions>");
						out.println("\t\t</ability>");
					} else if (line.indexOf("soulshift") != -1) {
						// Soulshift
						out.println("\t\t<ability ref='"
								+ line.substring(line.indexOf("soulshift")).trim().replace(" ",
										"") + "'/>");
					} else if (line.indexOf("echo") != -1) {
						// Echo
						out.println("\t\t<ability ref='echo'>");
						out.println("\t\t\t<actions>");
						writeCost(out, line.substring(
								line.indexOf("echo") + "echo".length()).trim(), null);
						out.println("\t\t\t</actions>");
						out.println("\t\t</ability>");
					} else if (line.indexOf("at the beginning of") != -1) {
						// at the beginning of
						if (!isInstant && !isSorcery) {
							// a simple triggered ability
							out
									.println("\t\t\t<!-- UPDATE PHASE NAME, TYPE and RESOLUTION -->");
							out
									.println("\t\t<triggered-ability resolution='normal' zone='play'>");
							out.println("\t\t\t<beginning-of-phase phase='upkeep'>");
							out.println("\t\t\t\t<test ref='during-your-upkeep'/>");
							out.println("\t\t\t</beginning-of-phase>");
							if (line.indexOf("unless") != -1) {
								out.println("\t\t\t<cost>");
								out.println("\t\t\t\t<choice cancel='false'>");
								out.println("\t\t\t\t\t<either>");
								out.println("\t\t\t\t\t\t<pay-mana colorless='1'/>");
								out.println("\t\t\t\t\t</either>");
								out.println("\t\t\t\t\t<either>");
								out.println("\t\t\t\t\t\t<action ref='sacrifice-this'/>");
								out.println("\t\t\t\t\t</either>");
								out.println("\t\t\t\t</choice>");
								out.println("\t\t\t</cost>");
							} else {
								out.println("\t\t\t<effects>");
								writeActions(out, line, false, false);
								out
										.println("\t\t\t\t<!-- PUT HERE EFFECTS OF THIS TRIGGERED ABILITY -->");
								out.println("\t\t\t</effects>");
							}
							out.println("\t\t</triggered-ability>");
						}
					} else if (line.indexOf("cumulative upkeep") != -1) {
						// Cumulative upkeep
						out.println("\t\t<ability ref='cumulative-upkeep'/>");
						out
								.println("\t\t<triggered-ability resolution='normal' zone='play' name='cumulative-upkeep'>");
						out.println("\t\t\t<beginning-of-phase phase='upkeep'>");
						out.println("\t\t\t\t<test ref='during-your-upkeep'/>");
						out.println("\t\t\t</beginning-of-phase>");
						out.println("\t\t\t<cost>");
						out.println("\t\t\t\t<choice cancel='false'>");
						out.println("\t\t\t\t\t<either>");
						out.println("\t\t\t\t\t\t<!-- PUT HERE THE ACTION(S) TO PAY -->");
						out.println("\t\t\t\t\t\t<action ref='pay-life'>");
						out.println("\t\t\t\t\t\t\t<value>");
						out
								.println("\t\t\t\t\t\t\t\t<counter object-name='age' card='this'/>");
						out.println("\t\t\t\t\t\t\t</value>");
						out.println("\t\t\t\t\t\t</action>");
						out.println("\t\t\t\t\t</either>");
						out.println("\t\t\t\t\t<either>");
						out.println("\t\t\t\t\t\t<action ref='sacrifice-this'/>");
						out.println("\t\t\t\t\t</either>");
						out.println("\t\t\t\t</choice>");
						out.println("\t\t\t</cost>");
						out.println("\t\t</triggered-ability>");
					}
				}
				out.println("\t</abilities>");

				// Fading or vanishing counter
				if (hasFading || hasVanishing || hasLessToPlay || hasMoreToPlay) {
					out.println("\t<modifiers>");

					if (hasMoreToPlay) {
						out.println("\t\t<additional-cost-modifier linked='true'>");
						out.println("\t\t\t<test>");
						out.println("\t\t<!-- Complete the additional cost test -->");
						out.println("\t\t\t</test>");
						out.println("\t\t\t<cost>");
						out.println("\t\t<!-- Update the additional cost amount -->");
						out.println("\t\t\t\t<pay-mana colorless='1' />");
						out.println("\t\t\t</cost>");
						out.println("\t\t</additional-cost-modifier>");
					}

					if (hasLessToPlay) {
						out.println("\t\t<additional-cost-modifier linked='true'>");
						out.println("\t\t\t<test>");
						out.println("\t\t<!-- Complete the cost reduction test -->");
						out.println("\t\t\t</test>");
						out.println("\t\t\t<cost>");
						out.println("\t\t<!-- Update the cost reduction amount -->");
						out.println("\t\t\t\t<pay-mana colorless='-1' />");
						out.println("\t\t\t</cost>");
						out.println("\t\t</additional-cost-modifier>");
					}

					if (hasFading) {
						int fadingCounter = Integer.parseInt(hasFadingLine);
						if (fadingCounter == Integer.MIN_VALUE) {
							fadingCounter = 1;
						}
						for (int i = fadingCounter; i-- > 0;) {
							out.println("\t\t<object name='fade'/>");
						}
					}
					if (hasVanishing) {
						int vanishingCounter;
						if (hasVanishingLine.length() != 0)
							vanishingCounter = Integer.parseInt(hasVanishingLine);
						else {
							vanishingCounter = 1;
							out.println("\t\t<!-- NEEDS CODE FOR NUMBER OF COUNTERS -->");
						}
						if (vanishingCounter == Integer.MIN_VALUE) {
							vanishingCounter = 1;
						}
						for (int i = vanishingCounter; i-- > 0;) {
							out.println("\t\t<object name='time'/>");
						}
					}
					out.println("\t</modifiers>");
				}

				// Attachment

				if (isEquipment) {
					String pow = new String();
					String tou = new String();
					if (equipLine.length() > 0) {
						pow = equipLine.substring(0, equipLine.indexOf("/"));
						tou = equipLine.substring(equipLine.indexOf("/") + 2, equipLine
								.indexOf("/") + 3);
					} else {
						out.println("\t<!-- WRITE THE EQUIPMENT MODIFIERS -->");
					}

					out.println("\t<attachment>");
					out.println("\t\t<modifiers>");
					if (!pow.contains("0"))
						out
								.println("\t\t\t<register-modifier index='power' operation='add' linked='true' value='"
										+ pow + "' />");
					if (!tou.contains("0"))
						out
								.println("\t\t\t<register-modifier index='toughness' operation='add' linked='true' value='"
										+ tou + "' />");
					out.println("\t\t</modifiers>");
					out.println("\t\t<valid-target ref='valid-creature-to-equip' />");
					out.println("\t\t<valid-attachment ref='valid-equip-creature' />");
					out.println("\t</attachment>");
				}
				if (isAura) {
					out.print("\t<attachment");

					if (isLocalEnchantCreature) {
						if (line.indexOf("you control enchanted creature") != -1) {
							out.println(" ref='control'/>");
							out.println("\t<!-- Add the additional modifiers here-->");
						} else if (line.startsWith("enchanted creature gets")
								|| line.indexOf("enchanted creature has") != -1) {
							if (line.indexOf("gets") != -1) {
								String temp = line.substring(line.indexOf("gets")).split(" ")[1]
										.replaceFirst("[.,]", "");
								out.print(" ref='" + temp);
								if (temp.indexOf("x") != -1) {
									out
											.println("\t<!-- UPDATE X VALUE IN MODIFIERS, SEE CARD: Blanchwood Armor-->");
								}
							}
							if (line.indexOf("has") != -1) {
								String property = line.substring(line.indexOf("has"))
										.split(" ")[1].replaceFirst("[.,]", "");
								if (line.indexOf("gets") != -1) {
									out.println("'>");
									out.println("\t\t<modifiers>");
									out
											.println("\t\t\t<!-- UPDATE THE MODIFIER TYPE AND THE LINKED ATTRIBUTE -->");
									out.println("\t\t\t<property-modifier property='" + property
											+ "' linked='true'/>");
									out.println("\t\t</modifiers>");
									out.println("\t</attachment>");
								} else {
									out.println(" ref='" + property + "'/>");
								}

							} else
								out.println("'/>");
						} else {
							out.println(" ref='enchant-creature'/>");
						}
					} else if (isLocalEnchantLand) {
						out.println(" ref='enchant-land'/>");

					} else if (isLocalEnchantArtifact) {
						out.println(" ref='enchant-artifact'/>");

					} else if (isLocalEnchantEnchantment) {
						out
								.println("\t\t<valid-target ref='valid-enchantment-to-enchant' />");
						out
								.println("\t\t<valid-attachment ref='valid-enchant-enchantment' />");

					} else if (isLocalEnchantCreatureArtifact) {
						out.println(" ref='enchant-artifact-creature'/>");

					} else if (isLocalEnchantPermanent) {
						out.println(" ref='enchant'/>");
					}
				}
				out.println("</card>");
				IOUtils.closeQuietly(out);
				nbCard++;
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error openning file '" + oracleFile + "' : " + e);
		} catch (IOException e) {
			System.err.println("IOError reading file '" + oracleFile + "' : " + e);
		}
		System.out.println("Success Parsing : " + nbCard + " card(s)");
	}

	/**
	 * <ul>
	 * Argument are (in this order :
	 * <li>Oracle source file
	 * <li>destination directory
	 * </ul>
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		options = new Options();
		final CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// Display help
			if (!options.isHelp()) {
				System.out.println("Wrong parameters : " + e.getMessage());
			} else {
				System.out.println("Usage");
			}
			parser.setUsageWidth(100);
			parser.printUsage(System.out);
			System.exit(-1);
			return;
		}

		if (options.isVersion()) {
			// Display version
			System.out.println("Version is " + IdConst.VERSION);
			System.exit(-1);
			return;
		}

		if (options.isHelp()) {
			// Display help
			System.out.println("Usage");
			parser.setUsageWidth(100);
			parser.printUsage(System.out);
			System.exit(-1);
			return;
		}

		// The oracle source file
		final String oracle = options.getOracleFile();

		// the directory destination end with the file separator
		String destination = FilenameUtils.separatorsToWindows(options
				.getDestination());
		if (!destination.endsWith("/")) {
			destination += "/";
		}

		// Create the destination directories
		try {
			new File(destination).mkdirs();
		} catch (Exception e) {
			// Ignore this error and continue
		}
		new Oracle2Xml().serialize(MToolKit.getFile(oracle), MToolKit
				.getFile(destination), MToolKit.getFile("tbs/" + TBS_NAME
				+ "/recycled/"));
	}

	/**
	 * Write X expression for pay-mana action
	 * 
	 * @param out
	 *          the output were the pay-mana would be written.
	 * @param manaCost
	 *          the mana cost.
	 * @param writeFixPart
	 *          is the fixed part of mana cost is written or not.
	 * @see net.sf.firemox.xml.action.PayMana
	 */
	private void writeXmanaCost(PrintWriter out, int[] manaCost,
			boolean writeFixPart) {

		// The fixed part
		if (writeFixPart) {
			out.print("\t\t\t\t<pay-mana ");
			for (int i = 6; i-- > 0;) {
				if (manaCost[i] > 0) {
					out.print(extractColor(i) + "='" + manaCost[i] + "' ");
				}
			}
			out.println("/>");
		}

		// The X part
		if (manaCost[6] > 0) {
			/*out
					.println("\t\t\t\t<!-- UPDATE THE TEXT AND AMOUNT OF MIN/MAXI COLORLESS MANA TO PAY -->");
			out
					.println("\t\t\t\t<input-number min='0' controller='you' operation='set' register='stack' index='0' name='%'>");
			out.println("\t\t\t\t\t<text>%x-value</text>");*/
			if (manaCost[6] > 1) {
				out.println("\t\t\t\t<action ref='pay-x'/>");
				//out.println("\t\t\t\t\t<max register='you' index='manapool'/>");
			} else {
				// XX.. like
				out.println("\t\t\t\t<action ref='pay-xx'/>");
				/*out.println("\t\t\t\t\t<max>");
				out.println("\t\t\t\t\t\t<div right='" + manaCost[6] + "'>");
				out.println("\t\t\t\t\t\t\t<left register='you' index='manapool'>");
				out.println("\t\t\t\t\t\t</div>");
				out.println("\t\t\t\t\t</max>");*/
			}
			/*out.println("\t\t\t\t</input-number>");
			out.println("\t\t\t\t<pay-mana>");
			out.println("\t\t\t\t\t<colorless register='stack' index='0'/>");
			out.println("\t\t\t\t</pay-mana>");*/
		}
	}

	/**
	 * Write X expression for give-mana action
	 * 
	 * @param out
	 *          the output were the pay-mana would be written.
	 * @param actions
	 *          the mana actions.
	 * @param writeFixPart
	 *          is the fixed part of mana cost is written or not.
	 * @see net.sf.firemox.xml.action.PayMana
	 */
	private String writeXmanaGive(PrintWriter out, String actions,
			boolean writeFixPart) {

		final int index = actions.indexOf(' ', "add ".length());
		int[] manaCost = extractManaCost(actions.substring("add ".length(), index));

		// The fixed part
		if (writeFixPart) {
			out.print("\t\t\t\t<give-mana ");
			for (int i = 6; i-- > 0;) {
				if (manaCost[i] > 0) {
					out.print(extractColor(i) + "='" + manaCost[i] + "' ");
				}
			}
			out.println("/>");
		}

		// The X part
		if (manaCost[6] > 0) {
			out
					.println("\t\t\t\t<!-- UPDATE THE TEXT AND AMOUNT OF MIN/MAXI COLORLESS MANA TO GIVE -->");
			out
					.println("\t\t\t\t<input-number min='0' controller='you' operation='set' register='stack' index='0' name='%'>");
			out.println("\t\t\t\t\t<text>%x-value</text>");
			if (manaCost[6] > 1) {
				out.println("\t\t\t\t\t<max register='you' index='manapool'/>");
			} else {
				// XX.. like
				out.println("\t\t\t\t\t<max>");
				out.println("\t\t\t\t\t\t<div right='" + manaCost[6] + "'>");
				out.println("\t\t\t\t\t\t\t<left register='you' index='manapool'>");
				out.println("\t\t\t\t\t\t</div>");
				out.println("\t\t\t\t\t</max>");
			}
			out.println("\t\t\t\t</input-number>");
			out.println("\t\t\t\t<give-mana>");
			out.println("\t\t\t\t\t<colorless register='stack' index='0'/>");
			out.println("\t\t\t\t</give-mana>");
		}

		return actions.substring(index + 1);
	}

	/**
	 * Expected mana structure is [X]*[0-9]+([U][B][G][R][W])*
	 * 
	 * @param line
	 *          The string containing ONLY the manacost.
	 * @return an integer array {colorless, black, blue, green, red, white, x
	 *         occurrences}
	 */
	private int[] extractManaCost(String line) {
		final int[] res = new int[7];
		for (int i = 0; i < line.length(); i++) {
			final char mana = line.charAt(i);
			switch (mana) {
			case 'b':
				res[1]++;
				break;
			case 'u':
				res[2]++;
				break;
			case 'g':
				res[3]++;
				break;
			case 'r':
				res[4]++;
				break;
			case 'w':
				res[5]++;
				break;
			case 'x':
				res[6]++;
				break;
			default:
				// colorless mana
				int a = Character.getNumericValue(mana);
				if(a > 0 && a < 10)
					res[0] += a;
			}
		}
		return res;
	}

	private static final String[] SACRIFICE = new String[] { "land", "artifact",
			"permanent", "creature", "enchantment" };

	private static final String[] SACRIFICE_OTHER = new String[] { "plains",
			"forest", "mountain", "swamp", "island" };

	/**
	 * @param out
	 * @param action
	 */
	private void writeSacrifice(PrintWriter out, String action) {
		// Sacrifice {this}
		if (action.contains("sacrifice " + lowerCard)) {
			out.println("\t\t\t\t<action ref='sacrifice-this'/>");
			return;
		}
		if (lowerCard.contains(",")
				&& action.contains("sacrifice " + lowerCard.split(",")[0].trim())) {
			out.println("\t\t\t\t<action ref='sacrifice-this'/>");
			return;
		}
		if (action.contains("sacrifice it")) {
			out.println("\t\t\t\t\t<!-- PUT THIS ACTION IN A DELAYED ABILITY -->");
			out.println("\t\t\t\t<action ref='sacrifice-this'/>");
			return;
		}
		for (String thing : SACRIFICE) {
			if (action.contains("sacrifice a " + thing)) {
				// Sacrifice a THING
				out.println("\t\t\t\t<action ref='sacrifice-a-" + thing
						+ "' value='1'/>");
				return;
			}
			if (action.contains("sacrifice an " + thing)) {
				// Sacrifice a THING
				out.println("\t\t\t\t<action ref='sacrifice-an-" + thing
						+ "' value='1'/>");
				return;
			}
			if (action.contains("sacrifice two " + thing + "s")) {
				// Sacrifice 2 THINGS
				out.println("\t\t\t\t<action ref='sacrifice-a-" + thing
						+ "' value='2'/>");
				return;
			}
			if (action.contains("opponent sacrifices a " + thing)) {
				// Opponent sacrifices a THING
				out.println("\t\t\t\t<action ref='opponent-sacrifice-a-" + thing
						+ "' value='1'/>");
				return;
			}
			if (action.contains("opponent sacrifices two " + thing + "s")) {
				// Opponent sacrifices two THINGS
				out.println("\t\t\t\t<action ref='opponent-sacrifice-a-" + thing
						+ "' value='2'/>");
				return;
			}
			if (action.contains("opponent sacrifices ")
					&& action.indexOf(thing, action.indexOf("opponent sacrifices ")) != -1) {
				// Opponent sacrifices many THINGS
				out
						.println("\t\t\t\t<action ref='opponent-sacrifice-a-" + thing
								+ "'>");
				out.println("\t\t\t\t\t<!-- UPDATE THIS VALUE -->");
				out.println("\t\t\t\t\t<value register='stack' value='0'/>");
				out.println("\t\t\t\t</action>");
				return;
			}
			if (action.contains(thing)) {
				// Player sacrifice n THINGS
				out
						.println("\t\t\t\t<action ref='opponent-sacrifice-a-" + thing
								+ "'>");
				out.println("\t\t\t\t\t<value>");
				out.println("\t\t\t\t\t<!-- COMPLETE THE AMOUNT OF SACRIFICE -->");
				out.println("\t\t\t\t\t</value>");
				out.println("\t\t\t\t</action>");
				return;
			}
		}

		for (String sac : SACRIFICE_OTHER) {
			if (action.contains("sacrifice a " + sac)
					|| action.contains("sacrifice an " + sac)) {
				out.println("\t\t\t\t<action ref='sacrifice-a-permanent' value='1'/>");
				out.println("\t\t\t\t\t<test>");
				out.println("\t\t\t\t\t\t<has-idcard idcard='" + sac + "'/>");
				out.println("\t\t\t\t\t</test>");
				out.println("\t\t\t\t</action>");
				return;
			}
		}

		if (action.contains("sacrifice a")) {
			// Sacrifice a THING
			out.println("\t\t\t\t<action ref='sacrifice-a-permanent' value='1'>");
		} else if (action.contains("sacrifice two ")) {
			// Sacrifice 2 THINGS
			out.println("\t\t\t\t<action ref='sacrifice-a-permanent' value='2'>");
		} else {
			// Sacrifice X THINGS
			out.println("\t\t\t\t<action ref='sacrifice-a-permanent'>");
			out.println("\t\t\t\t\t<!-- UPDATE THIS VALUE -->");
			out.println("\t\t\t\t\t<value register='stack' value='0'/>");
		}
		out.println("\t\t\t\t\t<test>");
		String property = action.substring(action.indexOf("sacrifice ")).split(" ")[2];
		out.println("\t\t\t\t\t\t<has-property property='" + property + "'/>");
		out.println("\t\t\t\t\t</test>");
		out.println("\t\t\t\t</action>");
	}

	private String writeTarget(PrintWriter out, String action, boolean writeTarget) {
		String tmpString = action;
		if (tmpString.contains("target")) {

			while (tmpString.indexOf("target ") != -1) {
				if (tmpString.contains("target attacking or blocking creature")) {
					if (writeTarget) {
						out.println("\t\t\t\t<action ref='target-creature'>");
						out.println("\t\t\t\t\t<test>");
						out.println("\t\t\t\t\t\t<or>");
						out.println("\t\t\t\t\t\t\t<test ref='tested-is-attacking'/>");
						out.println("\t\t\t\t\t\t\t<test ref='tested-is-blocking'/>");
						out.println("\t\t\t\t\t\t</or>");
						out.println("\t\t\t\t\t</test>");
						out.println("\t\t\t\t</action>");
					}
					tmpString = StringUtils.replaceOnce(tmpString,
							"target attacking or blocking creature", "");
				} else if (tmpString.contains("target creature or player")) {
					if (writeTarget) {
						out.println("\t\t\t\t<action ref='target-dealtable'>");
						out.println("\t\t\t\t\t<test>");
						out
								.println("\t\t\t\t\t<!-- COMPLETE THIS TEST ON PLAYER/CREATURE TO TARGET -->");
						out.println("\t\t\t\t\t</test>");
						out.println("\t\t\t\t</action>");
					}
					tmpString = StringUtils.replaceOnce(tmpString,
							"target creature or player", "");
				} else if (tmpString.contains("target player")) {
					if (writeTarget) {
						out.println("\t\t\t\t<action ref='target-player'/>");
					}
					tmpString = StringUtils.replaceOnce(tmpString, "target player", "");
				} else if (tmpString.contains("target opponent")) {
					if (writeTarget) {
						out.println("\t\t\t\t<action ref='target-opponent'/>");
					}
					tmpString = StringUtils.replaceOnce(tmpString, "target opponent", "");
				} else if (tmpString.contains("target spell")) {
					if (writeTarget) {
						out.println("\t\t\t\t<action ref='target-spell'/>");
					}
					tmpString = StringUtils.replaceOnce(tmpString, "target spell", "");
				} else if (tmpString.contains("target activated ability")) {
					if (writeTarget) {
						out.println("\t\t\t\t<action ref='target-activated-ability'/>");
					}
					tmpString = StringUtils.replaceOnce(tmpString,
							"target activated ability", "");
				} else if (tmpString.contains("target triggered ability")) {
					if (writeTarget) {
						out.println("\t\t\t\t<action ref='target-triggered-ability'/>");
					}
					tmpString = StringUtils.replaceOnce(tmpString,
							"target triggered ability", "");
				} else if (tmpString.contains("target activated or triggered ability")) {
					if (writeTarget) {
						out
								.println("\t\t\t\t<action ref='target-activated-triggered-ability'/>");
					}
					tmpString = StringUtils.replaceOnce(tmpString,
							"target activated or triggered ability", "");
				} else {
					boolean found = false;
					for (int sacrificeI = SACRIFICE.length; sacrificeI-- > 0;) {
						final String thing = SACRIFICE[sacrificeI];
						if (tmpString.contains("target " + thing)) {
							if (writeTarget) {
								out.println("\t\t\t\t<action ref='target-" + thing + "'>");
								out.println("\t\t\t\t\t<test>");
								out.println("\t\t\t\t\t<!-- COMPLETE THIS TEST ON " + thing
										+ " TO TARGET -->");
								out.println("\t\t\t\t\t</test>");
								out.println("\t\t\t\t</action>");
							}
							tmpString = StringUtils.replaceOnce(tmpString, "target " + thing,
									"");
							found = true;
							break;
						}
					}
					if (!found) {
						if (writeTarget) {
							out.println("\t\t\t\t<action ref='target-permanent'>");
							out.println("\t\t\t\t\t<test>");
							String property = tmpString.substring(
									tmpString.indexOf("target ")).split(" ")[1];
							out.println("\t\t\t\t\t\t<has-property property='" + property
									+ "'/>");
							out.println("\t\t\t\t\t</test>");
							out.println("\t\t\t\t</action>");
						}
						tmpString = StringUtils.replaceOnce(tmpString, "target ", "");
					}
				}
			}
		}
		return tmpString;
	}

	/**
	 * Write the given actions comma separated, in the specified output.
	 * 
	 * @param out
	 *          the output were the pay-mana would be written.
	 * @param actionString
	 *          the string containg actions.
	 * @param targetString
	 *          the effects containing any target action.
	 */
	private String writeCost(PrintWriter out, String actionString,
			String targetString) {
		String res = "";
		String[] actions = actionString.replaceAll("--", "").trim().split(", ");

		for (String action : actions) {
			if (action.matches("[x]*[0-9]+[ubgrw]*")
					|| action.matches("[x]*[0-9]*[ubgrw]+")) {
				// pay-mana
				writeXmanaCost(out, extractManaCost(action), true);
			} else if ("t".equals(action.trim())) {
				// {T}
				out.println("\t\t\t\t<action ref='T'/>");
			} else if ("oq".equals(action.trim())) {
				// {Q}
				out.println("\t\t\t\t<action ref='Q'/>");
			} else if (action.contains("sacrifice")) {
				writeSacrifice(out, action);
			} else if (res.length() == 0) {
				res = action;
			} else {
				res += " " + action;
			}
		}

		if (targetString != null && targetString.contains("target")) {
			// Target actions
			return res + ":" + writeTarget(out, targetString, true);
		} else if (targetString != null) {
			if (targetString.contains("each player")) {
				out.println("\t\t\t\t<target type='player' mode='all'/>");
			}
			if (targetString.contains("each creature")) {
				out.println("\t\t\t\t<action ref='all-creatures'/>");
			}
			return res + ":" + targetString;
		}
		return res + ":";
	}

	private void writeActions(PrintWriter out, String pActions,
			boolean writeTarget, boolean ignoreCost) {
		String actions = pActions.replaceAll("--", "");
		boolean startWithSpc = actions.startsWith(" ");
		actions = actions.trim();

		if (actions.matches("add [x]*[0-9]+[ubgrw]* .*")
				|| actions.matches("add [x]*[0-9]*[ubgrw]+ .*")) {
			// Give mana
			actions = writeXmanaGive(out, actions, true);
		}

		// return to your hand
		if (actions.indexOf(lowerCard + " to your hand") != -1) {
			out.println("\t\t\t\t<target type='this' />");
			out.println("\t\t\t\t<action ref='return-to-hand'/>");
			actions = StringUtils.replaceOnce(actions, lowerCard + " to your hand",
					"");
		} else if (actions.indexOf("to your hand") != -1) {
			out.println("\t\t\t\t<action ref='return-to-hand'/>");
			actions = StringUtils.replaceOnce(actions, "to your hand", "");
		} else if (actions.indexOf(lowerCard + " to its owner's hand") != -1) {
			out.println("\t\t\t\t<target type='this' />");
			out.println("\t\t\t\t<action ref='return-to-hand'/>");
			actions = StringUtils.replaceOnce(actions, lowerCard
					+ " to its owner's hand", "");
		} else if (actions.indexOf("to its owner's hand") != -1) {
			out.println("\t\t\t\t<action ref='return-to-hand'/>");
			actions = StringUtils.replaceOnce(actions, "to its owner's hand", "");
		}

		if (actions.indexOf("regenerate " + lowerCard) != -1)
			out.println("\t\t\t\t<action ref='regenerate'/>");

		if (actions.indexOf("regenerate target") != -1)
			out.println("\t\t\t\t<action ref='regenerate-target'/>");

		// First perform Target action
		writeTarget(out, actions, writeTarget);

		if (actions.indexOf("enchanted creature gets ") != -1) {
			out.println("\t\t\t\t<target type='attachedto'/>");
			actions = StringUtils.replaceOnce(actions, "enchanted creature", "");
		} else if (actions.indexOf(lowerCard + " gets") != -1) {
			out.println("\t\t\t\t<target type='this'/>");
			actions = StringUtils.replaceOnce(actions, lowerCard, "");
		} else if (actions.indexOf(lowerCard + " gains") != -1) {
			out.println("\t\t\t\t<target type='this'/>");
			actions = StringUtils.replaceOnce(actions, lowerCard, "");
		}

		if (actions.indexOf("put") != -1 && actions.indexOf("puts") == -1
				&& actions.indexOf("creature token") != -1) {
			if (actions.lastIndexOf("put") < actions.indexOf("creature token")) {
				final int endIndex = actions.indexOf("creature token");
				String tokenAction = actions.substring(actions.lastIndexOf("put") + 4,
						endIndex - 1);
				String[] words = tokenAction.split(" ");

				if (words.length > 3) {
					out.print("\t\t\t\t<repeat value='");
					if (words[0].compareTo("a") == 0)
						out.println("1'/>");
					else if (words[0].compareTo("two") == 0)
						out.println("2'/>");
					else if (words[0].compareTo("x") == 0)
						out.println("x'/> <!-- NEEDS CODE FOR X TOKENS -->");
					else if (words[0].compareTo("three") == 0)
						out.println("3'/>");
					else if (words[0].compareTo("four") == 0)
						out.println("4'/>");
					else if (words[0].compareTo("five") == 0)
						out.println("5'/>");
					else if (words[0].compareTo("six") == 0)
						out.println("6'/>");
					else if (words[0].compareTo("seven") == 0)
						out.println("7'/>");
					out.println("\t\t\t\t<create-card>");
					if (words.length == 6)
						out.println("\t\t\t\t\t<card name='"
								+ StringUtils.capitalize(words[5]) + "'>");
					else
						out.println("\t\t\t\t\t<card name='"
								+ StringUtils.capitalize(words[3]) + "'>");
					out
							.println("\t\t\t\t\t\t<rules-author-comment></rules-author-comment>");
					out.println("\t\t\t\t\t\t<init>");
					out.println("\t\t\t\t\t\t\t<registers>");
					out.println("\t\t\t\t\t\t\t\t<register index='power' value='"
							+ words[1].charAt(0) + "'/>");
					out.println("\t\t\t\t\t\t\t\t<register index='toughness' value='"
							+ words[1].charAt(2) + "'/>");
					out.println("\t\t\t\t\t\t\t</registers>");
					out.print("\t\t\t\t\t\t\t<colors>" + words[2]);
					List<String> tokenProperties = new ArrayList<String>();
					tokenProperties.add("token");
					if (words.length == 6) {
						out.print(" "+ words[4]);
						tokenProperties.add(words[5]);
					} else {
						tokenProperties.add(words[3]);
					}
					out.println("</colors>");
					out.println("\t\t\t\t\t\t\t<idcards>creature</idcards>");
					if (actions.indexOf("with", endIndex) != -1
							&& actions.indexOf("into", endIndex) > actions.indexOf("with",
									endIndex)) {
						updateProperties(actions.substring(actions
								.indexOf("with", endIndex)
								+ "with".length() + 1, actions.indexOf("into", endIndex)),
								tokenProperties);
					}
					out.print("\t\t\t\t\t\t\t<properties>");
					for (String property : tokenProperties) {
						out.print(property);
						out.print(' ');
					}
					out.println("</properties>");
					out.println("\t\t\t\t\t\t</init>");
					out.println("\t\t\t\t\t</card>");
					out.println("\t\t\t\t</create-card>");
					String zone = actions.substring(actions.lastIndexOf("put") + 4);
					zone = zone.substring(zone.indexOf("into") + "into".length()).trim();
					int zoneIndex = zone.indexOf('.');
					if (zoneIndex > zone.indexOf(' ') && zone.indexOf(' ') >= 0) {
						zoneIndex = zone.indexOf(' ');
					}
					if (actions.indexOf("target opponent") != -1
							&& actions.indexOf("target opponent") < actions.indexOf('.')) {
						out
								.print("\t\t\t\t<move-card controller='target-list.first' destination='");
					} else {
						out.print("\t\t\t\t<move-card controller='you' destination='");
					}
					if (zoneIndex != -1) {
						out.print(zone.substring(0, zoneIndex).replaceAll(",", "").trim());
					} else {
						out.print("nowhere");
					}
					out.println("'/>");
				}
			}
		}
		if (startWithSpc) {
			actions = " " + actions;
		}

		while (actions.length() > 0) {
			if (actions.startsWith("add [x]*[0-9]+[ubgrw] ")
					|| actions.startsWith("add [x]*[0-9]*[ubgrw]+ ")) {
				// Give mana
				actions = writeXmanaGive(out, actions, true);
			} else if (actions.startsWith("add one mana of any color")) {
				out.println("\t\t\t\t<give-mana black='1'/>");
				out
						.println("\t\t\t\t<!-- DUPLICATE THIS ABILITY WITH THE FOLLOWING ACTIONS OR USE <action ref='tap-add-B'/> REF-ABILITY");
				out.println("\t\t\t\t<give-mana blue='1'/>");
				out.println("\t\t\t\t<give-mana green='1'/>");
				out.println("\t\t\t\t<give-mana red='1'/>");
				out.println("\t\t\t\t<give-mana white='1'/>");
				out.println("\t\t\t\t  -->");
				actions = actions.substring("add one mana of any color".length());
			} else if (actions.startsWith("add two mana of any ")) {
				out.println("\t\t\t\t<give-mana black='2'/>");
				out
						.println("\t\t\t\t<!-- DUPLICATE THIS ABILITY WITH THE FOLLOWING ACTIONS");
				out.println("\t\t\t\t<give-mana blue='2'/>");
				out.println("\t\t\t\t<give-mana green='2'/>");
				out.println("\t\t\t\t<give-mana red='2'/>");
				out.println("\t\t\t\t<give-mana white='2'/>");
				out.println("\t\t\t\t  -->");
				actions = actions.substring("add two mana of any ".length());
			} else if (actions.startsWith("add three mana of any ")) {
				out.println("\t\t\t\t<give-mana black='3'/>");
				out
						.println("\t\t\t\t<!-- DUPLICATE THIS ABILITY WITH THE FOLLOWING ACTIONS");
				out.println("\t\t\t\t<give-mana blue='3'/>");
				out.println("\t\t\t\t<give-mana green='3'/>");
				out.println("\t\t\t\t<give-mana red='3'/>");
				out.println("\t\t\t\t<give-mana white='3'/>");
				out.println("\t\t\t\t  -->");
				actions = actions.substring("add three mana of any ".length());
			} else if (actions.startsWith("remove it from the game at end of turn")) {
				out.println("\t\t\t\t<action ref-'remove-from-game-target-eot'/>");
				actions = actions.substring("remove it from the game at end of turn"
						.length());
			} else if (actions.startsWith("destroy it at end of turn")) {
				out.println("\t\t\t\t<action ref='destroy-target-eot'/>");
				actions = actions.substring("destroy it at end of turn".length());
			} else if (actions
					.startsWith("can attack this turn as though it didn't have defender")) {
				out.println("\t\t\t\t<action ref='wall-can-attack-until-eot'/>");
				actions = actions
						.substring("can attack this turn as though it didn't have defender"
								.length());
			} else if (actions.startsWith("look at ")
					&& actions.indexOf("hand") != -1) {
				// Look at target opponent's hand
				out.println("\t\t\t\t<show-zone zone='hand' for='you'/>");
				out
						.println("\t\t\t\t<!-- INSERT HERE ACTIONS PERFORMED WHILE HAND IS VISIBLE -->");
				out.println("\t\t\t\t<action ref='restore-hand-visibility'/>");
				actions = actions.substring(actions.indexOf("hand") + "hand".length());
				continue;
			} else if (actions.startsWith("choose a creature type")) {
				// Choose a creature type
				out
						.println("\t\t\t\t<input-property operation='set' index='free0' register='this' values='FIRST_SUB_TYPE..LAST_SUB_TYPE' />");
				actions = actions.substring("choose a creature type".length());
				continue;
			} else if (actions.startsWith("choose a color")) {
				// Choose a color
				out
						.println("\t\t\t\t<input-color operation='set' index='free0' register='this'/>");
				actions = actions.substring("choose a color".length());
				continue;
			} else if (actions.startsWith("search ")) {
				// Search in your library for...
				List<String> idCard = new ArrayList<String>();
				List<String> inIdCard = new ArrayList<String>();
				List<String> color = new ArrayList<String>();
				List<String> property = new ArrayList<String>();
				boolean upTo = actions.indexOf("up to ") != -1;
				if (actions.indexOf("any number of ") != -1) {
					out.println("\t\t\t\t<target-list operation='clear' name='%'/>");
					out.println("\t\t\t\t<target type='you' name='%'/> ");
					out.println("\t\t\t\t<show-zone zone='library' for='you' name='%'/>");
					out
							.println("\t\t\t\t<target mode='choose' type='card' raise-event='false' restriction-zone='library' hop='2' cancel='false' name='search-any'>");
					out.println("\t\t\t\t\t<test>");
					out.println("\t\t\t\t\t\t<controller player='target-list.first'/>");
					out.println("\t\t\t\t\t</test>");
					out.println("\t\t\t\t</target>");
					out.println("\t\t\t\t<hop value='-1' name='%'/>");
					out.println("\t\t\t\t<action ref='restore-library-visibility'/>");
					out.println("\t\t\t\t<shuffle zone='library'/>");
					out
							.println("\t\t\t\t<target-list operation='remove-first' name='%'/> ");
					actions = actions.substring("search ".length());
				}
				boolean isYou = actions.startsWith("search your ");
				if (actions.indexOf("white") != -1) {
					color.add("white");
				}
				if (actions.indexOf("black") != -1) {
					color.add("black");
				}
				if (actions.indexOf("blue") != -1) {
					color.add("blue");
				}
				if (actions.indexOf("green") != -1) {
					color.add("green");
				}
				if (actions.indexOf("red") != -1) {
					color.add("red");
				}
				if (actions.indexOf("artifact") != -1) {
					idCard.add("artifact");
				} else if (actions.indexOf("creature") != -1) {
					idCard.add("creature");
				}
				if (actions.indexOf("enchantment") != -1) {
					idCard.add("enchantment");
				}
				if (actions.indexOf("sorcery") != -1) {
					idCard.add("sorcery");
				}
				if (actions.indexOf("instant") != -1) {
					idCard.add("instant");
				}
				if (actions.indexOf("island") != -1) {
					idCard.add("island");
				}
				if (actions.indexOf("swamp") != -1) {
					idCard.add("swamp");
				}
				if (actions.indexOf("forest") != -1) {
					idCard.add("forest");
				}
				if (actions.indexOf("mountain") != -1) {
					idCard.add("mountain");
				}
				if (actions.indexOf("plains") != -1) {
					idCard.add("plains");
				}
				if (actions.indexOf("mercenary") != -1) {
					property.add("mercenary");
				}
				if (actions.indexOf("nonbasic land") != -1) {
					inIdCard.add("nonbasic-land");
				}
				if (actions.indexOf("basic land") != -1) {
					inIdCard.add("basic-land");
				} else if (actions.indexOf("land") != -1) {
					inIdCard.add("land");
				}
				if (actions.indexOf("legend") != -1) {
					property.add("legend");
				}
				if (actions.indexOf("zombie") != -1) {
					property.add("zombie");
				}
				if (actions.indexOf("rebel") != -1) {
					property.add("rebel");
				}
				if (actions.indexOf("dragon") != -1) {
					property.add("dragon");
				}
				if (actions.indexOf("goblin") != -1) {
					property.add("goblin");
				}
				if (actions.indexOf("dwarf") != -1) {
					property.add("dwarf");
				}
				if (isYou && upTo) {
					out.println("\t\t\t\t<action ref='search-lib-up-to' value='3'>");
				} else if (isYou) {
					out.println("\t\t\t\t<action ref='search-lib'>");
				} else {
					out.println("\t\t\t\t<action ref='target-player'/>");
					if (upTo) {
						out
								.println("\t\t\t\t<action ref='search-lib-up-to-player' value='3'>");
					} else {
						out.println("\t\t\t\t<action ref='search-lib-player' value='3'>");
					}
				}
				out.println("\t\t\t\t\t<test>");
				out.println("\t\t\t\t\t\t<and>");
				out.println("\t\t\t\t\t\t\t<controller player='target-list.first'/>");
				if (idCard.size() + inIdCard.size() > 1) {
					out.println("\t\t\t\t\t\t\t<or>");
				}
				for (String id : idCard) {
					out.println("\t\t\t\t\t\t\t\t<has-idcard idcard='" + id + "'/>");
				}
				for (String id : inIdCard) {
					out.println("\t\t\t\t\t\t\t\t<in-idcard idcard='" + id + "'/>");
				}
				if (idCard.size() + inIdCard.size() > 1) {
					out.println("\t\t\t\t\t\t\t</or>");
				}
				if (property.size() > 1) {
					out.println("\t\t\t\t\t\t\t<or>");
				}
				for (String id : property) {
					out.println("\t\t\t\t\t\t\t<has-property property='" + id + "'/>");
				}
				if (property.size() > 1) {
					out.println("\t\t\t\t\t\t\t</or>");
				}
				if (color.size() > 1) {
					out.println("\t\t\t\t\t\t\t<or>");
				}
				for (String id : color) {
					out.println("\t\t\t\t\t\t\t<has-color color='" + id + "'/>");
				}
				if (color.size() > 1) {
					out.println("\t\t\t\t\t\t\t</or>");
				}
				out.println("\t\t\t\t\t\t</and>");
				out.println("\t\t\t\t\t</test>");
				out.println("\t\t\t\t</action>");
				out
						.println("\t\t\t\t\t<!-- INSERT HERE ACTIONS PERFORMED WHEN CARDS HAVE BEEN CHOSEN -->");
				actions = actions.substring("search ".length());
			} else if (actions.startsWith("sacrifice ")) {
				// Sacrifice
				if (!ignoreCost)
					writeSacrifice(out, actions);
				actions = actions.substring("sacrifice ".length());
			} else

			if (actions.startsWith("untap ")) {
				// Tap
				actions = writeActionOnTarget(out, "untap ", actions);
				out.println("\t\t\t\t<untap/>");
			} else if (actions.startsWith("tap ")) {
				actions = writeActionOnTarget(out, "tap ", actions);
				out.println("\t\t\t\t<tap/>");
			} else

			if (actions.startsWith("discards")) {
				// Discard a card from your hand
				out
						.println("\t\t\t\t<!-- UPDATE THE NUMBER OF CARD PLAYER HAVE TO DISCARD, OR REMOVE REPEAT ACTION IF IS ONE -->");
				if (actions.contains("at random")) {
					out.println("\t\t\t\t<action ref='discard-random' value='1'/>");
				} else {
					out.print("\t\t\t\t<action ref='player-discard' ");
					if (actions.startsWith("discards two"))
						out.println("value='2'/>");
					else
						out.println("value='1'/>");
				}
				actions = actions.substring("discards ".length());
			} else if (actions.startsWith("discard ")) {
				// Discard a card from your hand
				out
						.println("\t\t\t\t<!-- UPDATE THE NUMBER OF CARD PLAYER HAVE TO DISCARD, OR REMOVE REPEAT ACTION IF IS ONE -->");
				if (actions.contains("at random")) {
					out.println("\t\t\t\t<action ref='discard-random' value='1'/>");
				} else {
					out.print("\t\t\t\t<action ref='discard' ");
					if (actions.startsWith("discard two"))
						out.println("value='2'/>");
					else
						out.println("value='1'/>");
				}
				actions = actions.substring("discard ".length());
			} else if (actions
					.startsWith("draw a card at the beginning of the next turn's upkeep.")) {

				out.println("\t\t\t\t<action ref='draw-a-card-next-upkeep'/>");
				actions = actions.replace(
						"draw a card at the beginning of the next turn's upkeep.", "");
			} else if (actions.startsWith(" draws")) {
				// Draw cards
				if (!actions.startsWith(" draws a card")) {
					if (actions.startsWith(" draws two cards")) {
						out.println("\t\t\t\t<repeat value='2'/>");
					} else if (actions.startsWith(" draws three card")) {
						out.println("\t\t\t\t<repeat value='3'/>");
					} else if (actions.startsWith(" draws ")
							&& actions.indexOf("cards") != -1) {
						out
								.println("\t\t\t\t<!-- UPDATE THE NUMBER OF CARD PLAYER HAVE TO DRAW -->");
						out.println("\t\t\t\t<repeat value='1'/>");
					}
				}
				out.println("\t\t\t\t<action ref='draw-a-card'/>");
				actions = actions.substring(" draws".length());
			} else if (actions.startsWith("draw ")) {
				out.println("\t\t\t\t<target type='you'/>");
				if (!actions.startsWith("draw a card")) {
					if (actions.startsWith("draw two cards")) {
						out.println("\t\t\t\t<repeat value='2'/>");
					} else if (actions.startsWith("draw three card")) {
						out.println("\t\t\t\t<repeat value='3'/>");
					} else if (actions.startsWith("draw ")
							&& actions.indexOf("cards") != -1) {
						out
								.println("\t\t\t\t<!-- UPDATE THE NUMBER OF CARD YOU HAVE TO DRAW -->");
						out.println("\t\t\t\t<repeat value='1'/>");
					}
				}
				out.println("\t\t\t\t<action ref='draw-a-card'/>");
				actions = actions.substring("draw ".length());
			} else if (actions.startsWith("destroy ")) {
				// Destroy
				actions = writeActionOnTarget(out, "destroy ", actions);
				if (actions.contains("can't be regenerated"))
					out.println("\t\t\t\t<action ref='bury'/>");
				else
					out.println("\t\t\t\t<action ref='destroy'/>");
			} else if (actions.startsWith("deals ")
					&& actions.indexOf("damage") != -1) {
				// Damage
				String toReplace = actions.substring(0, actions.indexOf("damage")
						+ "damage".length())
						+ " to ";
				String type = toReplace.indexOf("combat") != -1 ? "damage-combat"
						: "damage-normal";
				String amount = actions.substring(
						actions.indexOf("deals ") + "deals ".length(),
						actions.indexOf("damage")).trim();
				actions = writeActionOnTarget(out, toReplace, actions);
				try {
					out.println("\t\t\t\t<assign-damage value='"
							+ Integer.parseInt(amount) + "' type='" + type + "'/>");
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<assign-damage type='" + type + "'>");
					out.println("\t\t\t\t\t<value register='this' index='power'/>");
					out.println("\t\t\t\t</assign-damage>");
				}
			} else if (actions.startsWith("put ")
					&& actions.matches("put [^ ]+ [^ ]+ counter.*")) {
				// Add objects
				int counterIndex = actions.indexOf("counter");
				String[] counters = actions.substring(0, counterIndex).split(" ");
				String objectCount = counters[1].trim();
				String objectName = counters[2].trim();
				if (actions.matches("put [^ ]+ [^ ]+ counter? on " + lowerCard + ".*")) {
					out.println("\t\t\t\t<target-list operation='clear'/>");
					out.println("\t\t\t\t<target type='this'/>");
				}
				try {
					final int objectCountInt;
					if ("a".equals(objectCount)) {
						objectCountInt = 1;
					} else if ("two".equals(objectCount)) {
						objectCountInt = 2;
					} else if ("tree".equals(objectCount)) {
						objectCountInt = 3;
					} else if ("four".equals(objectCount)) {
						objectCountInt = 4;
					} else if ("five".equals(objectCount)) {
						objectCountInt = 5;
					} else {
						objectCountInt = Integer.parseInt(objectCount);
					}
					if (objectCountInt > 1) {
						out.print("\t\t\t\t<repeat value='");
						out.print(objectCountInt);
						out.println("/>");
					}
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<repeat>");
					out.println("\t\t\t\t\t<value register='stack' index='0'/>");
					out.println("\t\t\t\t</repeat>");
				}
				out.print("\t\t\t\t<add-object object-name='");
				out.print(objectName);
				out.println("'/>");
				actions = actions.substring(counterIndex + "counter".length() + 1);
			} else if (actions.startsWith("counter ")) {
				// counter a spell
				out.println("\t\t\t\t<action ref='counter'/>");
				actions = actions.substring("counter ".length());
			} else if (actions.startsWith("prevent the next ")) {
				int amount = 1;
				try {
					amount = Integer.valueOf(actions.substring("prevent the next "
							.length(), "prevent the next ".length() + 1));
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<!-- UPDATE THE DAMAGE PREVENTION NUMBER -->");
				}
				out.println("\t\t\t\t<!-- UPDATE THE DAMAGE PREVENTION OBJECTS -->");
				out.println("\t\t\t\t<action ref='prevent-" + amount + "'/>");
				actions = actions.substring("prevent the next ".length() + 1);
			} else if (actions.startsWith("prevent all")) {
				// Prevent all damage that would be dealt to ...
				out.println("\t\t\t\t<action ref='prevent-all'/>");
				actions = actions.substring("prevent all".length());
			} else if (actions.startsWith("redirect the next ")) {
				int amount = 1;
				try {
					amount = Integer.valueOf(actions.substring("redirect the next "
							.length(), "redirect the next ".length() + 1));
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<!-- UPDATE THE DAMAGE REDIRECTION NUMBER -->");
				}
				out.println("\t\t\t\t<!-- UPDATE THE DAMAGE REDIRECTION OBJECTS -->");
				out.println("\t\t\t\t<action ref='redirect-" + amount + "'/>");
				actions = actions.substring("redirect the next ".length() + 1);
			} else if (actions.startsWith("redirect all")) {
				// Redirect all damage that would be dealt to ...
				out.println("\t\t\t\t<action ref='redirect-all'/>");
				actions = actions.substring("redirect all".length());
			} else if (actions.startsWith("pay ") && actions.indexOf(" life") != -1) {
				// Pay 2 life
				try {
					int value = Integer.parseInt(actions.substring("pay ".length(),
							"pay ".length() + 2).trim());
					out.println("\t\t\t\t<action ref='pay-life' value='" + value + "'/>");
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<action ref='pay-life'>");
					out.println("\t\t\t\t\t<!-- UPDATE THE X VALUE OF LIFE TO PAY -->");
					out.println("\t\t\t\t\t<value register='stack' index='0'/>");
					out.println("\t\t\t\t</action>");
				}
				actions = actions
						.substring(actions.indexOf(" life") + " life".length());
			} else if (actions.startsWith(" gain ") && actions.indexOf(" life") != -1) {
				// You gain 2 life
				try {
					int value = Integer.parseInt(actions.substring(" gain ".length(),
							" gain ".length() + 2).trim());
					out
							.println("\t\t\t\t<action ref='gain-life' value='" + value
									+ "'/>");
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<action ref='gain-life'>");
					out.println("\t\t\t\t\t<!-- UPDATE THE X VALUE OF LIFE TO ADD -->");
					out.println("\t\t\t\t\t<value register='stack' index='0'/>");
					out.println("\t\t\t\t</action>");
				}
				actions = actions
						.substring(actions.indexOf(" life") + " life".length());
			} else if (actions.startsWith(" lose ") && actions.indexOf(" life") != -1) {
				// You lose 2 life
				try {
					int value = Integer.parseInt(actions.substring(" lose ".length(),
							" lose ".length() + 2).trim());
					out
							.println("\t\t\t\t<action ref='lose-life' value='" + value
									+ "'/>");
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<action ref='lose-life'>");
					out.println("\t\t\t\t\t<!-- UPDATE THE X VALUE OF LIFE TO LOSE -->");
					out.println("\t\t\t\t\t<value register='stack' index='0'/>");
					out.println("\t\t\t\t</action>");
				}
				actions = actions
						.substring(actions.indexOf(" life") + " life".length());
			} else if (actions.startsWith(" gains ")
					&& actions.indexOf(" life") != -1) {
				// Player gains 2 life
				try {
					int value = Integer.parseInt(actions.substring(" gains ".length(),
							" gains ".length() + 2).trim());
					out.println("\t\t\t\t<action ref='gain-life-target' value='" + value
							+ "'/>");
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<action ref='gain-life-target'>");
					out.println("\t\t\t\t\t<!-- UPDATE THE X VALUE OF LIFE TO ADD -->");
					out.println("\t\t\t\t\t<value register='stack' index='0'/>");
					out.println("\t\t\t\t</action>");
				}
				actions = actions
						.substring(actions.indexOf(" life") + " life".length());
			} else if (actions.startsWith(" loses ")
					&& actions.indexOf(" life") != -1) {
				// Player loses 2 life
				try {
					int value = Integer.parseInt(actions.substring(" loses ".length(),
							" loses ".length() + 2).trim());
					out.println("\t\t\t\t<action ref='lose-life-target' value='" + value
							+ "'/>");
				} catch (NumberFormatException e) {
					out.println("\t\t\t\t<action ref='lose-life-target'>");
					out.println("\t\t\t\t\t<!-- UPDATE THE X VALUE OF LIFE TO LOSE -->");
					out.println("\t\t\t\t\t<value register='stack' index='0'/>");
					out.println("\t\t\t\t</action>");
				}
				actions = actions
						.substring(actions.indexOf(" life") + " life".length());
			} else if (actions.startsWith(" gets ") || actions.startsWith(" gains ")) {
				// Gets ...
				if (actions.indexOf(" poison counter") != -1) {
					// poison counter
					out.println("\t\t\t\t<!-- UPDATE THE NB OF POISON TO ADD -->");
					out
							.println("\t\t\t\t<modify-register register='opponent' index='poison' operation='add' value=''/>");
					actions = actions.substring(actions.indexOf(" poison counter")
							+ " poison counter".length() + 1);
				} else {
					// Gets
					String power = null;
					boolean endOfTurn = actions.indexOf(" until end of turn") != -1;
					out.println("\t\t\t\t<add-modifier>");

					if (actions.indexOf("/") == -1) {
						out
								.println("\t\t\t\t\t<!-- UPDATE THE MODIFIER TYPE AND THE LINKED ATTRIBUTE -->");
						String property = "";
						if (actions.indexOf("gains") != -1)
							property = actions.substring(actions.indexOf("gains")).split(" ")[1];
						else
							property = "flying";
						out.println("\t\t\t\t\t<property-modifier property='" + property
								+ "' linked='false'>");
						if (endOfTurn) {
							out.println("\t\t\t\t\t\t<until>");
							out.println("\t\t\t\t\t\t\t<end-of-phase phase='cleanup'/>");
							out.println("\t\t\t\t\t\t</until>");
						}
						out.println("\t\t\t\t\t</property-modifier>");
					} else {
						if (actions.startsWith(" gets ")) {
							power = actions
									.substring(" gets ".length(), actions.indexOf("/")).trim();
						} else {
							power = actions.substring(" gains ".length(),
									actions.indexOf("/")).trim();
						}
						String tmpStr = actions.substring(actions.indexOf("/") + 1);
						int dot = tmpStr.indexOf(".");
						int space = tmpStr.indexOf(" ");
						String toughness = tmpStr.substring(0,
								(space < dot || dot == -1) && space != -1 ? space : dot)
								.replaceAll(",", "").trim();
						if (!power.startsWith("+0")) {
							out.println("\t\t\t\t\t<!-- CHECK THE LINKED ATTRIBUTE -->");
							if (power.indexOf("x") != -1) {
								out
										.println("\t\t\t\t\t<register-modifier index='power' operation='"
												+ (power.startsWith("-") ? "minus" : "add")
												+ "' linked='false'>");
								if (endOfTurn) {
									out.println("\t\t\t\t\t\t<until>");
									out.println("\t\t\t\t\t\t\t<end-of-phase phase='cleanup'/>");
									out.println("\t\t\t\t\t\t</until>");
								}
								out.println("\t\t\t\t\t\t<!-- UPDATE THE POWER X VALUE -->");
								out.println("\t\t\t\t\t\t<value register='stack' index='0'/>");
							} else {
								out
										.println("\t\t\t\t\t<register-modifier index='power' value='"
												+ power.substring(1)
												+ "' operation='"
												+ (power.startsWith("-") ? "minus" : "add")
												+ "' linked='false'>");
								if (endOfTurn) {
									out.println("\t\t\t\t\t\t<until>");
									out.println("\t\t\t\t\t\t\t<end-of-phase phase='cleanup'/>");
									out.println("\t\t\t\t\t\t</until>");
								}
							}
							out.println("\t\t\t\t\t</register-modifier>");
						}
						if (!toughness.startsWith("+0")) {
							out.println("\t\t\t\t\t<!-- CHECK THE LINKED ATTRIBUTE -->");
							if (toughness.indexOf("x") != -1) {
								out
										.println("\t\t\t\t\t<register-modifier index='toughness' operation='"
												+ (power.startsWith("-") ? "minus" : "add")
												+ "' linked='false'>");
								if (endOfTurn) {
									out.println("\t\t\t\t\t\t<until>");
									out.println("\t\t\t\t\t\t\t<end-of-phase phase='cleanup'/>");
									out.println("\t\t\t\t\t\t</until>");
								}
								out
										.println("\t\t\t\t\t\t<!-- UPDATE THE TOUGHNESS X VALUE -->");
								out.println("\t\t\t\t\t\t<value register='stack' index='0'/>");

							} else {
								out
										.println("\t\t\t\t\t<register-modifier index='toughness' value='"
												+ toughness.substring(1)
												+ "' operation='"
												+ (toughness.startsWith("-") ? "minus" : "add")
												+ "' linked='false'>");
								if (endOfTurn) {
									out.println("\t\t\t\t\t\t<until>");
									out.println("\t\t\t\t\t\t\t<end-of-phase phase='cleanup'/>");
									out.println("\t\t\t\t\t\t</until>");
								}
							}
							out.println("\t\t\t\t\t</register-modifier>");
						}
					}
					out.println("\t\t\t\t</add-modifier>");

					if (isAura) {
						out.println("\t\t\t\t<target type='this'/>");
						out.println("\t\t\t\t<attachlist>");
						out.println("\t\t\t\t\t\t<!-- UPDATE ATTACHMENT CONDITION -->");
						out
								.println("\t\t\t\t<valid-attachment ref='valid-enchant-creature'/>");
						out.println("\t\t\t\t</attachlist>");

					}
					actions = actions.substring(" gets ".length());
				}
			} else if (actions
					.matches("^remove .{1,5} cards? in your graveyard from the game")) {
				if (actions.startsWith("remove two"))
					out.println("\t\t\t\t<repeat value='2'/>");
				else if (actions.startsWith("remove three"))
					out.println("\t\t\t\t<repeat value='3'/>");
				else if (actions.startsWith("remove four"))
					out.println("\t\t\t\t<repeat value='4'/>");
				out.println("\t\t\t\t<action ref='remove-a-card-from-graveyard'/>");
			}
			if (actions.length() > 0) {
				actions = actions.substring(1);
			}
		}

		// TODO Enchanted creature has first strike and trample.
		// At the end of its controller's turn,

		// TODO Choose one -- You gain 5 life; or prevent the next 5 damage that
		// would be dealt to target creature this turn.

		// TODO Entwine {2} (Choose both if you pay the entwine cost.)

	}

	/**
	 * @param out
	 * @param actions
	 */
	private String writeActionOnTarget(PrintWriter out, String firstAction,
			String actions) {
		if (actions.startsWith(firstAction + "you")) {
			out.println("\t\t\t\t<target type='you'/>");
			return actions.substring((firstAction + "you").length());
		} else if (actions.startsWith(firstAction + "enchanted")) {
			out.println("\t\t\t\t<target type='attachedto'/>");
			return actions.substring((firstAction + "enchanted").length());
		} else if (actions.startsWith(firstAction + lowerCard)) {
			out.println("\t\t\t\t<target type='this'/>");
			return actions.substring((firstAction + lowerCard).length());
		} else if (actions.startsWith(firstAction + "all")) {
			out.println("\t\t\t\t<target-list operation='clear' name='%'/>");
			out.println("\t\t\t\t<!-- UPDATE TYPE, ZONE OF THIS TEST -->");
			out
					.println("\t\t\t\t<target type='card' mode='all' restriction-zone='play'>");
			out.println("\t\t\t\t\t<test>");
			out
					.println("\t\t\t\t<!-- COMPLETE THE TEST TO DETERMINE OBJECTS USED BY THE NEXT ACTION -->");
			out.println("\t\t\t\t\t</test>");
			out.println("\t\t\t\t</target>");
			return actions.substring((firstAction + "a").length());
		} else if (actions.startsWith(firstAction + "a")) {
			out.println("\t\t\t\t<target-list operation='clear' name='%'/>");
			out
					.println("\t\t\t\t<!-- UPDATE TYPE, ZONE, CONTROLLER, MODE OF THIS TEST -->");
			out
					.println("\t\t\t\t<target type='card' mode='choose' raise-event='false' restriction-zone='play'>");
			out.println("\t\t\t\t\t<test>");
			out
					.println("\t\t\t\t<!-- COMPLETE THE TEST TO DETERMINE OBJECT USED BY THE NEXT ACTION -->");
			out.println("\t\t\t\t\t</test>");
			out.println("\t\t\t\t</target>");
			return actions.substring((firstAction + "a").length());
		} else if (actions.length() <= firstAction.length()) {
			return "";
		} else {
			return actions.substring(firstAction.length());
		}
	}

	private List<String> extractProperties(String line, List<String> properties) {
		String[] propertiesSTR = line.split(" ");
		for (String property : propertiesSTR) {
			properties.add(property);
		}
		return properties;
	}

	/**
	 * @param line
	 * @param properties
	 */
	private String updateProperties(String pLine, List<String> properties) {
		String[] propertiesSTR = pLine.split(", ");

		// first : validation
		for (int i = propertiesSTR.length; i-- > 0;) {
			propertiesSTR[i] = propertiesSTR[i].trim();
			if (propertiesSTR[i].length() > 30 || propertiesSTR[i].indexOf(" ") != -1
					&& propertiesSTR[i].indexOf("strike") == -1
					&& !propertiesSTR[i].startsWith("protection from")) {
				return pLine;
			}
		}

		// then : update list
		String line = pLine;
		for (String property : propertiesSTR) {
			if (property.indexOf("cycling") == -1
					&& property.indexOf("buyback") == -1
					&& property.indexOf("flashback") == -1
					&& property.indexOf("echo") == -1
					&& property.indexOf("deathtouch") == -1
					&& property.indexOf("lifelink") == -1) {
				properties.add(property.trim().replaceAll(" ", "-"));
				line = MToolKit.replaceAll(
						StringUtils.replaceOnce(line, property + ",", ""), property, "")
						.trim();
			}
		}
		return line;
	}

	private String extractColor(int idcolors) {
		switch (idcolors) {
		case 0:
			return "colorless";
		case 1:
			return "black";
		case 2:
			return "blue";
		case 3:
			return "green";
		case 4:
			return "red";
		case 5:
			return "white";
		default:
			return "x";
		}
	}

	/**
	 * @param in
	 */
	private void skipCard(BufferedReader in) throws IOException {
		String line = in.readLine();
		while (line != null && line.length() > 0 && line.compareTo(" \t ") != 0) {
			line = in.readLine();
		}
	}

	/**
	 * Write the affinity XML code for a given idCard
	 * 
	 * @param out
	 * @param hasAffinityLine
	 */
	private void writeAffinity(PrintWriter out, String idCard) {
		assert idCard != null && idCard.length() > 0;
		out.println("\t\t\t\t<action ref='affinity'>");
		out.println("\t\t\t\t\t<counter-test>");
		out.print("\t\t\t\t\t\t<has-idcard idcard='");
		out.print(idCard.split(" ")[0].trim());
		out.println("'/>");
		out.println("\t\t\t\t\t</counter-test>");
		out.println("\t\t\t\t</action>");

	}

	private String lowerCard = null;

	private boolean isNonBasicLand = false;

	private boolean isLocalEnchantCreature = false;

	private boolean isLocalEnchantLand = false;

	private boolean isLocalEnchantArtifact = false;

	private boolean isEnchantWorld = false;

	private boolean isLocalEnchantCreatureArtifact = false;

	private boolean isLocalEnchantPermanent = false;

	private boolean isLocalEnchantEnchantment = false;

	private boolean isGlobalEnchant = false;

	private boolean isInstant = false;

	private boolean isCreature = false;

	private boolean isArtifact = false;

	private boolean isSorcery = false;

	private boolean isSwamp = false;

	private boolean isIsland = false;

	private boolean isForest = false;

	private boolean isMountain = false;

	private boolean isPlains = false;

	private boolean isAura = false;
	
	private boolean isScheme = false;
	
	private boolean isPlane = false;
	
	private boolean isPhenomenon = false;

}
