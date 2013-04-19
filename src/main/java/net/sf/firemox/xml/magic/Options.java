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

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * Options of Oracle to XML builder.
 * 
 * @author <a href="mailto:fabdouglas@users.sourceforge.net">Fabrice Daugan </a>
 * @since 0.95
 */
public class Options {

	@Option(name = "-h", aliases = { "help" }, usage = "print this help message and exit")
	private boolean help;

	@Option(name = "-v", aliases = { "version" }, usage = "print product version and exit")
	private boolean version;

	@Option(name = "-f", aliases = { "file" }, metaVar = "FILE", usage = "Oracle file downloaded from sites like 'http://www.yawgatog.com/resources/oracle'", required = true)
	private String oracleFile;

	@Option(name = "-d", aliases = { "destination" }, metaVar = "DESTINATION", usage = "destination directory of generated XML files", required = true)
	private String destination;

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
	 * Return the help flag
	 * 
	 * @return the help flag.
	 */
	public boolean isHelp() {
		return this.help;
	}

	/**
	 * Return the Oracle file
	 * 
	 * @return the Oracle file.
	 */
	public String getOracleFile() {
		return this.oracleFile;
	}

	/**
	 * Return Destination directory of generated XML files
	 * 
	 * @return Destination directory of generated XML files.
	 */
	public String getDestination() {
		return this.destination;
	}

	/**
	 * Return version option.
	 * 
	 * @return the version option flag.
	 */
	public boolean isVersion() {
		return this.version;
	}

}
