/*
 *  Copyright 2006-2007 Columbia University.
 *
 *  This file is part of MEAPsoft.
 *
 *  MEAPsoft is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  MEAPsoft is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MEAPsoft; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *
 *  See the file "COPYING" for the text of the license.
 */

package com.meapsoft;

/**
 * Standard interface for exception handlers.
 * 
 * Whenever an application catches an Exception that it wants to handle in a
 * standardized way should pass it along to the handleException method of a
 * class that implements this interface.
 * 
 * This is used to allow for a single interface to be used for exception
 * handling in text mode utilities and MEAPsoftGUI.
 * 
 * @author Ron Weiss (ronw@ee.columbia.edu)
 */
public class ExceptionHandler
{
	/**
	 * Simple ExceptionHandler that just prints a stack trace and exits.
	 */
	public void handleException(Exception e)
	{
		e.printStackTrace();
		System.exit(1);
	}
}
