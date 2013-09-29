/*******************************************************************************
 * Coordinates.java
 * Copyright (c) 2013 WildBamaBoy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/

package mods.mca.core.util.object;

/**
 * Used to store a group of 3D coordinates and easily move them around.
 */
public class Coordinates
{
	/** The X coordinate value. */
    public double x;
    
    /** The Y coordinate value. */
    public double y;
    
    /** The Z coordinate value. */
    public double z;

    /**
     * Constructor
     * 
     * @param	x	The x coordinate value.
     * @param	y	The y coordinate value.
     * @param	z	The z coordinate value.
     */
    public Coordinates(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Gets string representation of the Coordinates object.
     * 
     * @return	"x, y, z" as string representation of the coordinates stored in this object.
     */
    public String toString()
    {
    	return x + ", " + y + ", " + z;
    }
}
