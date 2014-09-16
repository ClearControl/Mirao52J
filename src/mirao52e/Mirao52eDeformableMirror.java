package mirao52e;

import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.IOException;

import mirao52e.bindings.Mirao52eLibrary;

import org.bridj.Pointer;

/**
 * 
 * Instances of this class can directly connect to the MIRAO52e deformable
 * mirror via the Mirao52eLibrary bindings. This is really a convenience object
 * oriented wrapper around the automatically generated BridJ bindings.
 *
 * @author Loic Royer 2014
 *
 */

public class Mirao52eDeformableMirror implements Closeable
{
	private Object mLock = new Object();

	private Pointer<Double> mRawMirrorShapeVector;

	private volatile boolean mOutputTrigger = false;

	private volatile boolean mIsOpen = false;

	/**
	 * Constructs an instance of the Mirao52eDeformableMirror class
	 */
	public Mirao52eDeformableMirror()
	{
		super();
	}

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
	public int getMatrixWidth()
	{
		return 8;
	};

	/**
	 * Returns the height of the matrix
	 * 
	 * @return matrix height (8)
	 */
	public int getMatrixHeight()
	{
		return getMatrixWidth();
	};

	public boolean open()
	{
		if (mIsOpen)
			return true;
		synchronized (mLock)
		{
			Pointer<Integer> lPointerToStatus = Pointer.allocateInt();
			byte lMroOpen = Mirao52eLibrary.mroOpen(lPointerToStatus);
			byte lStatusByte = lPointerToStatus.getByte();
			lPointerToStatus.release();
			if (lMroOpen == Mirao52eLibrary.MRO_FALSE)
			{
				System.err.println("Could not open status=" + lStatusByte);
				return false;
			}
			mIsOpen = true;
			return true;
		}
	}

	public boolean isOpen()
	{
		synchronized (mLock)
		{
			return mIsOpen;
		}
	}

	/**
	 * Disconnects from the driver
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException
	{
		if (!mIsOpen)
			return;
		synchronized (mLock)
		{
			Pointer<Integer> lPointerToStatus = Pointer.allocateInt();
			byte lMroClose = Mirao52eLibrary.mroClose(lPointerToStatus);
			byte lStatusByte = lPointerToStatus.getByte();
			lPointerToStatus.release();

			if (lMroClose == Mirao52eLibrary.MRO_FALSE)
			{
				System.err.println("Could not close status=" + lStatusByte);
				fail();
			}
		}
	}

	/**
	 * Sends a flat mirror shape vector (0, ... , 0).
	 * 
	 * @return true if succeeded (Mirror accepts shape).
	 * @throws IOException
	 *           if shape could not be sent because of a connection issue
	 */
	public boolean sendFlatMirrorShapeVector()
	{
		return sendRawMirrorShapeVector(new double[Mirao52eSpecifications.cMirao52NumberOfActuators]);
	}

	/**
	 * Sends a 'full-matrix' mirror shape vector. This vector corresponds to a
	 * full 8x8 matrix. Corner actuator positions for which there is no actuator
	 * are simply ignored. This makes it easier to manipulate the mirror surface
	 * as a 2D image.
	 * 
	 * @param pFullMatrixMirrorShapeVector
	 *          flat vector of 8x8 double values as double[]
	 * @return true if succeeded.
	 */
	public boolean sendFullMatrixMirrorShapeVector(double[] pFullMatrixMirrorShapeVector)
	{
		Pointer<Double> lPointerToDoubles = Pointer.pointerToDoubles(pFullMatrixMirrorShapeVector);
		Pointer<Double> lRawMirrorShapeVectorDoubleBuffer = removeNonExistantCornerActuators(lPointerToDoubles);
		boolean lReturnValue = sendRawMirrorShapeVector(lRawMirrorShapeVectorDoubleBuffer);
		lPointerToDoubles.release();
		return lReturnValue;
	}

	/**
	 * Sends a 'full-matrix' mirror shape vector. This vector corresponds to a
	 * full 8x8 matrix. Corner actuator positions for which there is no actuator
	 * are simply ignored. This makes it easier to manipulate the mirror surface
	 * as a 2D image.
	 * 
	 * 
	 * @param pFullMatrixMirrorShapeVectorDoubleBuffer
	 *          flat vector of 8x8 double values as Pointer<Double>
	 * @return true if succeeded.
	 */
	public boolean sendFullMatrixMirrorShapeVector(Pointer<Double> pFullMatrixMirrorShapeVectorDoubleBuffer)
	{
		checkVectorDimensions(pFullMatrixMirrorShapeVectorDoubleBuffer,
													Mirao52eSpecifications.cMirao52FullMatrixMirrorShapeVectorLength);
		Pointer<Double> lRawMirrorShapeVectorDoubleBuffer = removeNonExistantCornerActuators(pFullMatrixMirrorShapeVectorDoubleBuffer);
		return sendRawMirrorShapeVector(lRawMirrorShapeVectorDoubleBuffer);
	}

	/**
	 * Sends a raw mirror shape vector. This vector values corresponds to each and
	 * every actuator on the mirror.
	 * 
	 * 
	 * @return true if succeeded.
	 */
	public boolean sendRawMirrorShapeVector(double[] pRawMirrorShapeVector)
	{
		Pointer<Double> lPointerToDoubles = Pointer.pointerToDoubles(pRawMirrorShapeVector);
		boolean lResultValue = sendRawMirrorShapeVector(lPointerToDoubles);
		return lResultValue;
	}

	/**
	 * Sends a raw mirror shape vector. This vector values corresponds to each and
	 * every actuator on the mirror.
	 * 
	 * 
	 * @return true if succeeded.
	 */
	public boolean sendRawMirrorShapeVector(Pointer<Double> pRawMirrorShapeVectorDoubleBuffer)
	{
		synchronized (mLock)
		{
			try
			{
				checkVectorDimensions(pRawMirrorShapeVectorDoubleBuffer,
															Mirao52eSpecifications.cMirao52NumberOfActuators);

				byte lMroApplyCommand = Mirao52eLibrary.mroApplyCommand(pRawMirrorShapeVectorDoubleBuffer,
																																(byte) (mOutputTrigger ? Mirao52eLibrary.MRO_TRUE
																																											: Mirao52eLibrary.MRO_FALSE),
																																null);

				boolean lSuccess = lMroApplyCommand == Mirao52eLibrary.MRO_TRUE;
				return lSuccess;
			}
			catch (Throwable e)
			{
				throw new Mirao52Exception(	"Excepion while sending mirror shape: '" + e.getLocalizedMessage()
																				+ "'",
																		e);
			}
		}
	}

	private Pointer<Double> removeNonExistantCornerActuators(Pointer<Double> pSquareMirrorShapeVectorDoubleBuffer)
	{
		checkVectorDimensions(pSquareMirrorShapeVectorDoubleBuffer,
													Mirao52eSpecifications.cMirao52FullMatrixMirrorShapeVectorLength);
		if (mRawMirrorShapeVector == null || mRawMirrorShapeVector.getValidElements() != Mirao52eSpecifications.cMirao52NumberOfActuators)
			mRawMirrorShapeVector = Pointer.allocateDoubles(Mirao52eSpecifications.cMirao52NumberOfActuators);

		Mirao52eSpecifications.convertLayout(	pSquareMirrorShapeVectorDoubleBuffer,
																					mRawMirrorShapeVector);
		return mRawMirrorShapeVector;
	}

	/**
	 * Checks vector dimensions and throw an exception if the length is incorrect.
	 * 
	 * @param pVector
	 *          vector to check for correct length (Java double array)
	 * @param pExpectedVectorLength
	 *          expected correct length
	 */
	private void checkVectorDimensions(	Pointer<Double> pVector,
																			int pExpectedVectorLength)
	{
		if (pVector.getValidElements() != pExpectedVectorLength)
		{
			String lExceptionMessage = String.format(	"Provided vector has wrong length %d should be %d",
																								pVector.getValidElements(),
																								pExpectedVectorLength);
			throw new Mirao52Exception(lExceptionMessage);
		}
	}

}
