package mirao52.udp.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import mirao52.udp.Mirao52UDPClient;

import org.junit.Test;

public class Mirao52UDPClientTests
{

	/**
	 * Start the Mirao52 UDP server on the localhost and fire this demo.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void test() throws IOException, InterruptedException
	{
		Mirao52UDPClient lMirao52UDPClient = new Mirao52UDPClient();

		lMirao52UDPClient.open("localhost");

		long lStartValueForLastNumberOfShapes = lMirao52UDPClient.getNumberOfReceivedShapes();

		for (int i = 1; i <= 10000; i++)
		{
			double[] lSquareMirrorShapeVector = generateRandomVector(64);
			assertTrue(lMirao52UDPClient.sendFullMatrixMirrorShapeVector(lSquareMirrorShapeVector));
			assertEquals(	lStartValueForLastNumberOfShapes + i,
										lMirao52UDPClient.getNumberOfReceivedShapes());
			Thread.sleep(100);
		}

	}

	/**
	 * generates a random shape vector
	 * 
	 * @param pLength
	 * @return
	 */
	private double[] generateRandomVector(int pLength)
	{
		double[] lRawMirrorShapeVector = new double[pLength];

		final double lOffset = 0.00 * (2 * Math.random() - 1);
		for (int i = 0; i < lRawMirrorShapeVector.length; i++)
			lRawMirrorShapeVector[i] = lOffset + 0.01
																	* (2 * Math.random() - 1);
		;
		return lRawMirrorShapeVector;
	}

}
