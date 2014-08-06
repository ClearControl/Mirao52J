package mirao52.udp.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import mirao52.udp.Mirao52UDPClient;

import org.junit.Test;

public class Mirao52UDPClientTests
{

	@Test
	public void test() throws IOException, InterruptedException
	{
		Mirao52UDPClient lMirao52UDPClient = new Mirao52UDPClient();

		lMirao52UDPClient.connect("localhost");

		long lStartValueForLastNumberOfShapes = lMirao52UDPClient.getLastNumberOfReceivedShapes();

		for (int i = 1; i <= 10000; i++)
		{
			double[] lSquareMirrorShapeVector = generateRandomVector(64);
			assertTrue(lMirao52UDPClient.sendSquareMirrorShapeVector(lSquareMirrorShapeVector));
			assertEquals(	lStartValueForLastNumberOfShapes + i,
										lMirao52UDPClient.getLastNumberOfReceivedShapes());
			Thread.sleep(100);
		}

	}

	private double[] generateRandomVector(int pLength)
	{
		double[] lRawMirrorShapeVector = new double[pLength];

		final double lOffset = 0.005 * (2 * Math.random() - 1);
		for (int i = 0; i < lRawMirrorShapeVector.length; i++)
			lRawMirrorShapeVector[i] = lOffset;
		return lRawMirrorShapeVector;
	}

}
