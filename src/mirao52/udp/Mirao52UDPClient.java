package mirao52.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import mirao52.Mirao52Exception;
import mirao52.Mirao52Interface;

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

	public Mirao52UDPClient()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see mirao52.udp.Mirao52Interface#connect(java.lang.String)
	 */
	@Override
	public void connect(String pHostname) throws IOException
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

	/* (non-Javadoc)
	 * @see mirao52.udp.Mirao52Interface#close()
	 */
	@Override
	public void close() throws IOException
	{
		mDatagramSocket.close();
	}

	@Override
	public long getLastNumberOfReceivedShapes()
	{
		return mLastNumberOfShapesReceived;
	}


	/* (non-Javadoc)
	 * @see mirao52.udp.Mirao52Interface#sendSquareMirrorShapeVector(double[])
	 */
	@Override
	public boolean sendSquareMirrorShapeVector(double[] pSquareMirrorShapeVector) throws IOException
	{
		checkVectorDimensions(pSquareMirrorShapeVector,
													cMirao52SquareMirrorShapeVectorLength);
		double[] lRawMirrorShapeVector = removeUnusedElements(pSquareMirrorShapeVector);
		return sendRawMirrorShapeVector(lRawMirrorShapeVector);
	}

	/* (non-Javadoc)
	 * @see mirao52.udp.Mirao52Interface#sendSquareMirrorShapeVector(double[])
	 */
	@Override
	public boolean sendFlatMirrorShapeVector() throws IOException
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

	/* (non-Javadoc)
	 * @see mirao52.udp.Mirao52Interface#sendRawMirrorShapeVector(double[])
	 */
	@Override
	public boolean sendRawMirrorShapeVector(double[] pRawMirrorShapeVector) throws IOException
	{
		synchronized (mLock)
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
	}

	private byte[] convertDoubleArrayToByteArray(	byte[] pPrefix,
																								double[] pRawMirrorShapeVector)
	{

		int lVectorLength = pRawMirrorShapeVector.length;
		int lByteArrayLength = pPrefix.length + lVectorLength * 8;
		if (mByteArray == null || mByteArray.length != lByteArrayLength)
			mByteArray = new byte[lByteArrayLength];

		for (int j = 0; j < pPrefix.length; j++)
			mByteArray[j] = pPrefix[j];
		for (int i = 0, j = pPrefix.length; i < lVectorLength; i++)
		{
			long lDoubleAsLong = Double.doubleToLongBits(pRawMirrorShapeVector[i]);
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

	private long convertBytesToLong(byte[] pReceiveBuffer, int pOffset)
	{

		return (long) (pReceiveBuffer[pOffset + 7] & 0xFF << 0 * 8
										| (pReceiveBuffer[pOffset + 6] & 0xFF) << 1 * 8
										| (pReceiveBuffer[pOffset + 5] & 0xFF) << 2 * 8
										| (pReceiveBuffer[pOffset + 4] & 0xFF) << 3 * 8
										| (pReceiveBuffer[pOffset + 3] & 0xFF) << 4 * 8
										| (pReceiveBuffer[pOffset + 2] & 0xFF) << 5 * 8
										| (pReceiveBuffer[pOffset + 1] & 0xFF) << 6 * 8 | (pReceiveBuffer[pOffset + 0] & 0xFF) << 7 * 8);
	}

	private void checkVectorDimensions(	double[] pRawMirrorShapeVector,
																			int pExpectedVectorLength)
	{
		if (pRawMirrorShapeVector.length != pExpectedVectorLength)
		{
			String lExceptionMessage = String.format(	"Provided vector has wrong length %d should be %d",
																								pRawMirrorShapeVector.length,
																								pExpectedVectorLength);
			throw new Mirao52Exception(lExceptionMessage);
		}
	}

}
