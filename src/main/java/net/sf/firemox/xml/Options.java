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
package net.sf.firemox.xml;

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Options of XML builder.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
class Options {

	@Option(name = "-h", aliases = { "help" }, usage = "print this help message and exit")
	private boolean help;

	@Option(name = "-v", aliases = { "version" }, usage = "print product version and exit")
	private boolean version;

	@Option(name = "-f", aliases = { "force" }, usage = "force XML compile even if MDB is up to date")
	private boolean force;

	@Option(name = "-d", aliases = { "debug" }, usage = "debug activation")
	private boolean debug;

	@Option(name = "-x", aliases = { "xsd" }, usage = "enable XSD validation")
	private boolean xsdValidation;

	@Option(name = "-g", aliases = { "game" }, metaVar = "GAME", usage = "game short name like : mtg", required = true)
	private String mdb;

	@Option(name = "-n", aliases = { "nopaymana" }, usage = "ignore all 'paya-mana' actions")
	private boolean noPayMana;

	@Argument
	private List<String> arguments;

	/**
	 * Return
	 * 
	 * @return Returns the arguments.
	 */
	public List<String> getArguments() {
		return this.arguments;
	}

	/**
	 * Return
	 * 
	 * @return Returns the debug.
	 */
	public boolean isDebug() {
		return this.debug;
	}

	/**
	 * Return
	 * 
	 * @return Returns the force.
	 */
	public boolean isForce() {
		return this.force;
	}

	/**
	 * Return
	 * 
	 * @return Returns the help.
	 */
	public boolean isHelp() {
		return this.help;
	}

	/**
	 * Return the MDB name
	 * 
	 * @return the MDB name.
	 */
	public String getMdb() {
		return this.mdb;
	}

	/**
	 * Return version option.
	 * 
	 * @return the version option flag.
	 */
	public boolean isVersion() {
		return this.version;
	}

	/**
	 * Return the XSD validation option
	 * 
	 * @return the XSD validation option.
	 */
	public boolean isXsdValidation() {
		return this.xsdValidation;
	}

	/**
	 * Return the no-Pay-Mana option.
	 * 
	 * @return the no-Pay-Mana option.
	 */
	public boolean isNoPayMana() {
		return this.noPayMana;
	}

}
