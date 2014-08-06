package mirao52;

import java.io.IOException;

/**
 * Interface Mirao52Interface
 * 
 * Classes that implement this interface provide methods that allow to retrieve
 * the umber of actuators, the matrix width and height, open and close
 * connection to the UDP port, and set the raw mirror shape
 * 
 * @author Loic Royer 2014
 *
 */
public interface Mirao52Interface
{
	/**
	 * Returns the number of actuators on the MIRAO52 (52!)
	 * 
	 * @return number of actuators (52)
	 */
	public static int getNumberOfActuators()
	{
		return 52;
	};

	/**
	 * Returns the width of the matrix
	 * 
	 * @return matrix width (8)
	 */
	public static int getMatrixWidth()
	{
		return 8;
	};

	/**
	 * Returns the height of the matrix
	 * 
	 * @return matrix height (8)
	 */
	public static int getMatrixHeight()
	{
		return getMatrixWidth();
	};

	/**
	 * Sends a flat mirror shape vector (0, ... , 0).
	 * 
	 * @return true if succeeded (Mirror accepts shape).
	 * @throws IOException
	 *           if shape could not be sent because of a connection issue
	 */
	public abstract boolean sendFlatMirrorShapeVector();

	/**
	 * Sends a 'full-matrix' mirror shape vector. This vector corresponds to a
	 * full 8x8 matrix. Corner actuator positions for which there is no actuator
	 * are simply ignored. This makes it easier to manipulate the mirror surface
	 * as a 2D image.
	 * 
	 * 
	 * @return true if succeeded.
	 */
	public abstract boolean sendMatrixMirrorShapeVector(double[] pSquareMirrorShapeVector);

	/**
	 * Sends a raw mirror shape vector. This vector values corresponds to each and
	 * every actuator on the mirror.
	 * 
	 * 
	 * @return true if succeeded.
	 */
	public abstract boolean sendRawMirrorShapeVector(double[] pRawMirrorShapeVector);

	/**
	 * @return number of mirror shapes received
	 */
	public abstract long getNumberOfReceivedShapes();

}