package mirao52.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import mirao52.Mirao52Exception;
import mirao52.Mirao52Interface;

/**
 * Class Mirao52UDPClient
 * 
 * Instances of this class can connect o a UDP server providing control to the
 * MIRAO52e deformable mirror
 *
 * @author Loic Royer 2014
 *
 */
/**
 * Class Mirao52UDPClient 
 * 
 * Instances of this class ...
 *
 * @author Loic Royer
 * 2014
 *
 */
/**
 * Class Mirao52UDPClient
 * 
 * Instances of this class ...
 *
 * @author Loic Royer 2014
 *
 */
public class Mirao52UDPClient implements Mirao52Interface, Closeable
{
	private static final String cMirao52UDPPrefix = "MIRAO52E";
	private static final int cMirao52UDPPortNumber = 9876;
	private static final int cMirao52NumberOfActuators = 52;
	private static final int cMirao52SquareMirrorShapeVectorLength = 64;
	private DatagramSocket mDatagramSocket;
	private DatagramPacket mDatagramPacket;
	private byte[] mByteArray;

	private Object mLock = new Object();
	private double[] mRawMirrorShapeVectors;
	private volatile long mLastNumberOfShapesReceived;

	/**
	 * Constructs an instance of the Mirao52UDPClient class
	 */
	public Mirao52UDPClient()
	{
		super();
	}

	/**
	 * Opens the connection to the UDP server on a given machine. After the
	 * connection is established, miror shapes can be sent.
	 * 
	 * @param pHostname
	 *          hostname of machine on which the UDP server is running.
	 * @throws IOException
	 *           if the connection cannot be established.
	 */
	public void open(String pHostname) throws IOException
	{
		InetAddress lInetAddress = InetAddress.getByName(pHostname);
		mDatagramSocket = new DatagramSocket();

		byte[] lFlatRawMirrorShapeVectorBytes = convertDoubleArrayToByteArray(cMirao52UDPPrefix.getBytes(),
																																					new double[cMirao52NumberOfActuators]);

		mDatagramPacket = new DatagramPacket(	lFlatRawMirrorShapeVectorBytes,
																					lFlatRawMirrorShapeVectorBytes.length,
																					lInetAddress,
																					cMirao52UDPPortNumber);

		sendFlatMirrorShapeVector();
	}

	/**
	 * Disconnects from server. After this point mirror shapes cannot be sent
	 * anymore.
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException
	{
		mDatagramSocket.close();
	}

	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#getNumberOfReceivedShapes()
	 */
	@Override
	public long getNumberOfReceivedShapes()
	{
		return mLastNumberOfShapesReceived;
	}


	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#sendMatrixMirrorShapeVector(double[])
	 */
	@Override
	public boolean sendMatrixMirrorShapeVector(double[] pSquareMirrorShapeVector)
	{
		checkVectorDimensions(pSquareMirrorShapeVector,
													cMirao52SquareMirrorShapeVectorLength);
		double[] lRawMirrorShapeVector = removeUnusedElements(pSquareMirrorShapeVector);
		return sendRawMirrorShapeVector(lRawMirrorShapeVector);
	}

	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#sendFlatMirrorShapeVector()
	 */
	@Override
	public boolean sendFlatMirrorShapeVector()
	{
		return sendRawMirrorShapeVector(new double[cMirao52NumberOfActuators]);
	}

	private double[] removeUnusedElements(double[] pSquareMirrorShapeVector)
	{
		checkVectorDimensions(pSquareMirrorShapeVector,
													cMirao52SquareMirrorShapeVectorLength);
		if (mRawMirrorShapeVectors == null || mRawMirrorShapeVectors.length != cMirao52NumberOfActuators)
			mRawMirrorShapeVectors = new double[cMirao52NumberOfActuators];

		for (int i = 0, j = 0; i < cMirao52NumberOfActuators; i++)
		{
			if (i < 4)
			{
				j = 2 + i;
			}
			else if (i < 10)
			{
				j = 5 + i;
			}
			else if (i < 42)
			{
				j = 6 + i;
			}
			else if (i < 48)
			{
				j = 7 + i;
			}
			else if (i < 52)
			{
				j = 10 + i;
			}

			mRawMirrorShapeVectors[i] = pSquareMirrorShapeVector[j];
		}
		return mRawMirrorShapeVectors;
	}

	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#sendRawMirrorShapeVector(double[])
	 */
	@Override
	public boolean sendRawMirrorShapeVector(double[] pRawMirrorShapeVector)
	{
		synchronized (mLock)
		{
			try
			{
				checkVectorDimensions(pRawMirrorShapeVector,
															cMirao52NumberOfActuators);

				byte[] lByteBuffer = convertDoubleArrayToByteArray(	cMirao52UDPPrefix.getBytes(),
																														pRawMirrorShapeVector);
				mDatagramPacket.setData(lByteBuffer);
				mDatagramSocket.send(mDatagramPacket);

				byte[] lReceiveBuffer = new byte[9];

				DatagramPacket lDatagramPacket = new DatagramPacket(lReceiveBuffer,
																														lReceiveBuffer.length);

				mDatagramSocket.receive(lDatagramPacket);

				mLastNumberOfShapesReceived = convertBytesToLong(lReceiveBuffer,
																															0);
				boolean lSuccess = lReceiveBuffer[8] != 0;

				System.out.println("lLastNumberOfShapesReceived=" + mLastNumberOfShapesReceived);
				System.out.println("lSuccess=" + lSuccess);
				
				return lSuccess;
			}
			catch (IOException e)
			{
				throw new Mirao52Exception(	"Excepion while sending mirror shape: '" + e.getLocalizedMessage()
																				+ "'",
																		e);
			}
		}
	}

	/**
	 * Converts an array of doubles into a array of bytes plus a prefix
	 * 
	 * @param pPrefix
	 *          bytes to prepend
	 * @param pDoubleArray
	 *          array of doubles to convert
	 * @return
	 */
	private byte[] convertDoubleArrayToByteArray(	byte[] pPrefix,
																								double[] pDoubleArray)
	{

		int lVectorLength = pDoubleArray.length;
		int lByteArrayLength = pPrefix.length + lVectorLength * 8;
		if (mByteArray == null || mByteArray.length != lByteArrayLength)
			mByteArray = new byte[lByteArrayLength];

		for (int j = 0; j < pPrefix.length; j++)
			mByteArray[j] = pPrefix[j];
		for (int i = 0, j = pPrefix.length; i < lVectorLength; i++)
		{
			long lDoubleAsLong = Double.doubleToLongBits(pDoubleArray[i]);
			mByteArray[j++] = (byte) (lDoubleAsLong >>> 56);
			mByteArray[j++] = (byte) (lDoubleAsLong >>> 48);
			mByteArray[j++] = (byte) (lDoubleAsLong >>> 40);
			mByteArray[j++] = (byte) (lDoubleAsLong >>> 32);
			mByteArray[j++] = (byte) (lDoubleAsLong >>> 24);
			mByteArray[j++] = (byte) (lDoubleAsLong >>> 16);
			mByteArray[j++] = (byte) (lDoubleAsLong >>> 8);
			mByteArray[j++] = (byte) (lDoubleAsLong >>> 0);
		}
		return mByteArray;
	}

	/**
	 * Converts an array of bytes starting from some offset into a long
	 * 
	 * @param pByteArray
	 *          byte array
	 * @param pOffset
	 *          ofset into byte array
	 * @return corresponding long
	 */
	private long convertBytesToLong(byte[] pByteArray, int pOffset)
	{

		return (long) (pByteArray[pOffset + 7] & 0xFF << 0 * 8
										| (pByteArray[pOffset + 6] & 0xFF) << 1 * 8
										| (pByteArray[pOffset + 5] & 0xFF) << 2 * 8
										| (pByteArray[pOffset + 4] & 0xFF) << 3 * 8
										| (pByteArray[pOffset + 3] & 0xFF) << 4 * 8
										| (pByteArray[pOffset + 2] & 0xFF) << 5 * 8
										| (pByteArray[pOffset + 1] & 0xFF) << 6 * 8 | (pByteArray[pOffset + 0] & 0xFF) << 7 * 8);
	}

	/**
	 * Checks vector dimensions and throw an exception if the length is incorrect.
	 * 
	 * @param pVector
	 *          vector to check for correct length
	 * @param pExpectedVectorLength
	 *          expected corect length
	 */
	private void checkVectorDimensions(	double[] pVector,
																			int pExpectedVectorLength)
	{
		if (pVector.length != pExpectedVectorLength)
		{
			String lExceptionMessage = String.format(	"Provided vector has wrong length %d should be %d",
																								pVector.length,
																								pExpectedVectorLength);
			throw new Mirao52Exception(lExceptionMessage);
		}
	}

}
