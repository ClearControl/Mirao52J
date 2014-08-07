package mirao52.udp;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.LongBuffer;
import java.nio.channels.DatagramChannel;

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
	private static final int cMirao52UDPDefaultPortNumber = 9876;
	private static final int cMirao52NumberOfActuators = 52;
	private static final int cMirao52FullMatrixMirrorShapeVectorLength = 64;
	private DatagramChannel mDatagramChannel;
	private byte[] mByteArray;

	private Object mLock = new Object();
	private DoubleBuffer mRawMirrorShapeVectorsDoubleBuffer;
	private volatile long mLastNumberOfShapesReceived;
	private InetSocketAddress InetSocketAddress;
	private ByteBuffer mSendingByteBuffer;
	private ByteBuffer mReceivingByteBuffer;


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
		open(pHostname, cMirao52UDPDefaultPortNumber);
	}

	/**
	 * Opens the connection to the UDP server on a given machine. After the
	 * connection is established, miror shapes can be sent.
	 * 
	 * @param pHostname
	 *          hostname of machine on which the UDP server is running.
	 * @param pPort
	 *          UDP port number to connect to
	 * @throws IOException
	 *           if the connection cannot be established.
	 */
	public void open(String pHostname, int pPort) throws IOException
	{
		synchronized (mLock)
		{
			InetSocketAddress = new InetSocketAddress(pHostname, pPort);

			mDatagramChannel = DatagramChannel.open();
			mDatagramChannel.socket().bind(null);
			mDatagramChannel.configureBlocking(true);

			sendFlatMirrorShapeVector();
		}
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
		synchronized (mLock)
		{
			mDatagramChannel.close();
			mDatagramChannel = null;
		}
	}

	/**
	 * Returns whether this client is ready to send mirror shapes.
	 * 
	 * @return true if ready to send mirror shapes.
	 */
	public boolean isReady()
	{
		synchronized (mLock)
		{
			return mDatagramChannel != null;
		}
	}

	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#getNumberOfReceivedShapes()
	 */
	@Override
	public long getNumberOfReceivedShapes()
	{
		synchronized (mLock)
		{
			return mLastNumberOfShapesReceived;
		}
	}

	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#sendFullMatrixMirrorShapeVector(double[])
	 */
	@Override
	public boolean sendFullMatrixMirrorShapeVector(double[] pFullMatrixMirrorShapeVector)
	{
		DoubleBuffer lRawMirrorShapeVectorDoubleBuffer = removeUnusedElements(DoubleBuffer.wrap(pFullMatrixMirrorShapeVector));
		return sendRawMirrorShapeVector(lRawMirrorShapeVectorDoubleBuffer);
	}

	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#sendFullMatrixMirrorShapeVector(double[])
	 */
	@Override
	public boolean sendFullMatrixMirrorShapeVector(DoubleBuffer pFullMatrixMirrorShapeVectorDoubleBuffer)
	{
		checkVectorDimensions(pFullMatrixMirrorShapeVectorDoubleBuffer,
													cMirao52FullMatrixMirrorShapeVectorLength);
		DoubleBuffer lRawMirrorShapeVectorDoubleBuffer = removeUnusedElements(pFullMatrixMirrorShapeVectorDoubleBuffer);
		return sendRawMirrorShapeVector(lRawMirrorShapeVectorDoubleBuffer);
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


	private DoubleBuffer removeUnusedElements(DoubleBuffer pSquareMirrorShapeVectorDoubleBuffer)
	{
		checkVectorDimensions(pSquareMirrorShapeVectorDoubleBuffer,
													cMirao52FullMatrixMirrorShapeVectorLength);
		if (mRawMirrorShapeVectorsDoubleBuffer == null || mRawMirrorShapeVectorsDoubleBuffer.limit() != cMirao52NumberOfActuators)
			mRawMirrorShapeVectorsDoubleBuffer = ByteBuffer.allocateDirect(cMirao52NumberOfActuators * 8)
																					.asDoubleBuffer();

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

			mRawMirrorShapeVectorsDoubleBuffer.put(	i,
																							pSquareMirrorShapeVectorDoubleBuffer.get(j));
		}
		return mRawMirrorShapeVectorsDoubleBuffer;
	}


	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#sendRawMirrorShapeVector(double[])
	 */
	@Override
	public boolean sendRawMirrorShapeVector(double[] pRawMirrorShapeVector)
	{
		return sendRawMirrorShapeVector(DoubleBuffer.wrap(pRawMirrorShapeVector));
	}

	/**
	 * Interface method implementation
	 * 
	 * @see mirao52.Mirao52Interface#sendRawMirrorShapeVector(double[])
	 */
	public boolean sendRawMirrorShapeVector(DoubleBuffer pRawMirrorShapeVectorDoubleBuffer)
	{
		synchronized (mLock)
		{
			try
			{
				checkVectorDimensions(pRawMirrorShapeVectorDoubleBuffer,
															cMirao52NumberOfActuators);

				if (mSendingByteBuffer == null)
					mSendingByteBuffer = ByteBuffer.allocateDirect(cMirao52UDPPrefix.length() + pRawMirrorShapeVectorDoubleBuffer.limit()
																														* 8);
				mSendingByteBuffer.clear();
				mSendingByteBuffer.put(cMirao52UDPPrefix.getBytes());

				pRawMirrorShapeVectorDoubleBuffer.rewind();
				for (int i = 0; i < pRawMirrorShapeVectorDoubleBuffer.limit(); i++)
					mSendingByteBuffer.putDouble(pRawMirrorShapeVectorDoubleBuffer.get());
				mSendingByteBuffer.rewind();


				mSendingByteBuffer.rewind();
				int lBytesSent = mDatagramChannel.send(	mSendingByteBuffer,
																								InetSocketAddress);
				if (mReceivingByteBuffer == null)
					mReceivingByteBuffer = ByteBuffer.allocateDirect(8 + 1);
				mReceivingByteBuffer.rewind();


				mDatagramChannel.receive(mReceivingByteBuffer);
				

				mReceivingByteBuffer.rewind();
				LongBuffer lReceivingByteBufferAsLongBuffer = mReceivingByteBuffer.asLongBuffer();
				mLastNumberOfShapesReceived = lReceivingByteBufferAsLongBuffer.get();
				boolean lSuccess = mReceivingByteBuffer.get(8) != 0;

				// System.out.println("lLastNumberOfShapesReceived=" +
				// mLastNumberOfShapesReceived);
				// System.out.println("lSuccess=" + lSuccess);

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
	 *          vector to check for correct length (Java double array)
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

	/**
	 * Checks vector dimensions and throw an exception if the length is incorrect.
	 * 
	 * @param pVector
	 *          vector to check for correct length (DoubleBuffer)
	 * @param pExpectedVectorLength
	 *          expected corect length
	 */
	private void checkVectorDimensions(	DoubleBuffer pRawMirrorShapeVectorDoubleBuffer,
																			int pExpectedVectorLength)
	{
		if (pRawMirrorShapeVectorDoubleBuffer.limit() != pExpectedVectorLength)
		{
			String lExceptionMessage = String.format(	"Provided vector has wrong length %d should be %d",
																								pRawMirrorShapeVectorDoubleBuffer.limit(),
																								pExpectedVectorLength);
			throw new Mirao52Exception(lExceptionMessage);
		}
	}

}
