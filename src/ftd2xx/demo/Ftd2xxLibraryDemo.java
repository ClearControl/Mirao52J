package ftd2xx.demo;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import org.bridj.Pointer;
import org.junit.Test;

import ftd2xx.Ftd2xxLibrary;

public class Ftd2xxLibraryDemo
{
	// usb.transfer_type == 0x3 && usb.endpoint_number.direction == 0

	@Test
	public void test() throws UnsupportedEncodingException,
										InterruptedException
	{
		/*Pointer<Pointer<?>> lDescriptionsList = Pointer.allocatePointers(4);
		lDescriptionsList.set(0, Pointer.allocateArray(Byte.class, 64));
		lDescriptionsList.set(1, Pointer.allocateArray(Byte.class, 64));
		lDescriptionsList.set(2, Pointer.allocateArray(Byte.class, 64));
		lDescriptionsList.set(3, Pointer.NULL);

		Pointer<Integer> lPNumberOfDevices = Pointer.allocateInt();
		Ftd2xxLibrary.fTListDevices(lDescriptionsList,
																lPNumberOfDevices,
																Ftd2xxLibrary.FT_LIST_ALL | Ftd2xxLibrary.FT_OPEN_BY_DESCRIPTION);

		System.out.println("Total number of devices: " + lPNumberOfDevices.getInt());

		for (int i = 0; i < 3; i++)
		{
			System.out.println("DeviceNumber:" + i);
			Pointer<?> lPointer = lDescriptionsList.get(i);
			if (lPointer == Pointer.NULL)
				continue;
			byte[] lBytesArray = (byte[]) (lPointer.as(Byte.class).getArray(64));
			String lDescription = new String(lBytesArray, "UTF-8");
			System.out.println(lDescription);
		}/**/

		int lBaudRate = 9600;
		for (; lBaudRate <= 2457600; lBaudRate *= 2)
		{
			System.out.println("lBaudRate = " + lBaudRate);
			Pointer<Pointer<?>> lPointerToHandle = open(lBaudRate);

			sendSyncCommand(lPointerToHandle);
			sendMonitoringCommand(lPointerToHandle);

			for (int i = 0; i < 10; i++)
			{
				sendFlatRE(lPointerToHandle);
				sendUnknownCommand(lPointerToHandle);
				Thread.sleep(100);
			}

			close(lPointerToHandle);
		}
	}

	private void close(Pointer<Pointer<?>> lPointerToHandle)
	{
		long lStatusClose = Ftd2xxLibrary.fTClose(lPointerToHandle.get());
		if (lStatusClose != Ftd2xxLibrary.FT_OK)
		{
			System.err.println("Cannot close device - Status not ok: " + lStatusClose);
			fail();
		}
	}

	private Pointer<Pointer<?>> open(int pBaudRate)
	{
		String lDeviceName = "mirao52e" + (char) (0);
		Pointer<?> lDeviceDescription = Pointer.allocateBytes(lDeviceName.length());
		lDeviceDescription.setBytes(lDeviceName.getBytes());
		Pointer<Pointer<?>> lPointerToHandle = Pointer.allocatePointer();
		lPointerToHandle.set(Pointer.allocatePointer());

		long lStatusOpen = Ftd2xxLibrary.fTOpenEx(lDeviceDescription,
																							Ftd2xxLibrary.FT_OPEN_BY_DESCRIPTION,
																							lPointerToHandle);

		if (lStatusOpen == Ftd2xxLibrary.FT_DEVICE_NOT_FOUND)
		{
			System.err.println("Cannot open device - Device not found! ");
			fail();
		}
		else if (lStatusOpen != Ftd2xxLibrary.FT_OK)
		{
			System.err.println("Cannot open device - Status not ok: " + lStatusOpen);
			fail();
		}

		long lStatusResetDevice = Ftd2xxLibrary.fTResetDevice(lPointerToHandle.get());
		System.out.println("lStatusResetDevice=" + lStatusResetDevice);

		// Reset FT Device
		long lStatusSetBitMode = Ftd2xxLibrary.fTSetBitMode(lPointerToHandle.get(),
																												(byte) 0,
																												(byte) 0);
		System.out.println("lStatusSetBitMode=" + lStatusSetBitMode);

		// Set Baud Rate
		long lStatusSetBaudRate = Ftd2xxLibrary.fTSetBaudRate(lPointerToHandle.get(),
																													pBaudRate);
		System.out.println("lStatusSetBaudRate=" + lStatusSetBaudRate);

		// Set Data Bit , Stop Bit , Parity Bit
		long lStatusSetDataCharacteristics = Ftd2xxLibrary.fTSetDataCharacteristics(lPointerToHandle.get(),
																																								(byte) 8,
																																								(byte) 0,
																																								(byte) 0);
		System.out.println("lStatusSetDataCharacteristics=" + lStatusSetDataCharacteristics);

		// Set Flow Control
		long lStatusSetFlowControl = Ftd2xxLibrary.fTSetFlowControl(lPointerToHandle.get(),
																																(short) 0x0000,
																																(byte) 0x0b,
																																(byte) 0x0d);
		System.out.println("lStatusSetFlowControl=" + lStatusSetFlowControl);

		// long lStatusReset = Ftd2xxLibrary.fTResetDevice(lPointerToHandle.get());
		// System.out.println("lStatusReset=" + lStatusReset);

		long lStatusSetLatencyTimer = Ftd2xxLibrary.fTSetLatencyTimer(lPointerToHandle.get(),
																																	(byte) 16);
		System.out.println("lStatusSetLatencyTimer=" + lStatusSetLatencyTimer);

		long lStatusSetUSBParameters = Ftd2xxLibrary.fTSetUSBParameters(lPointerToHandle.get(),
																																		(long) 22,
																																		(long) 112);
		System.out.println("lStatusSetUSBParameters=" + lStatusSetUSBParameters);

		long lStatusSetTimeouts = Ftd2xxLibrary.fTSetTimeouts(lPointerToHandle.get(),
																													(long) 100,
																													(long) 100);
		System.out.println("lStatusSetTimeouts=" + lStatusSetTimeouts);

		return lPointerToHandle;
	}

	private void sendFlat(Pointer<Pointer<?>> pPointerToHandle)
	{
		char[] lMessage = prepareMessage(new double[52]);
		sendMessage(pPointerToHandle, lMessage);
	}

	private void sendFlatRE(Pointer<Pointer<?>> pPointerToHandle)
	{
		char[] lFlat = new char[52];
		for (int i = 0; i < lFlat.length; i++)
			lFlat[i] = 0x1FFF;
		char[] lMessage = prepareMessage(lFlat);
		sendMessage(pPointerToHandle, lMessage);
	}

	private void sendMessage(	Pointer<Pointer<?>> pPointerToHandle,
														char[] pMessage)
	{
		Pointer<Character> lMessagePointer = Pointer.allocateChars(pMessage.length);
		for (int i = 0; i < pMessage.length; i++)
			lMessagePointer.set(i, pMessage[i]);

		Pointer<Integer> lNumberOfBytesSentPointer = Pointer.allocateInt();
		long lStatusSendMessage = Ftd2xxLibrary.fTWrite(pPointerToHandle.get(),
																										lMessagePointer,
																										2 * pMessage.length,
																										lNumberOfBytesSentPointer);
		System.out.println("Number of bytes sent:" + lNumberOfBytesSentPointer.get());
		lMessagePointer.release();
		lNumberOfBytesSentPointer.release();

		System.out.println("lStatusSendMessage=" + lStatusSendMessage);
	}

	private char[] prepareMessage(double[] pShape)
	{
		char[] lShapeChar = new char[52];

		for (int i = 0; i < 52; i++)
		{
			short lShortValue = (short) (16383 * ((pShape[i] + 1) / 2));
			char lCharValue = (char) lShortValue;
			lShapeChar[i] = lCharValue;
		}

		return prepareMessage(lShapeChar);
	}

	private char[] prepareMessage(char[] pShape)
	{
		char[] lMessage = new char[56];

		lMessage[0] = 0xF600;
		lMessage[1] = 0x4C00;
		for (int i = 1; i < 26; i++)
			lMessage[i + 2] = pShape[i];
		lMessage[28] = 0x4C20;
		for (int i = 26; i < 52; i++)
			lMessage[i + 3] = pShape[i];
		lMessage[55] = 0xF100;
		lMessage[55] = (char) (lMessage[55] | checksum(lMessage));

		return lMessage;
	}

	private char checksum(char[] pMessage)
	{
		char lChecksum = 0;
		for (int i = 0; i < pMessage.length; i++)
		{
			lChecksum += (byte) (pMessage[i] & 0x00FF);
			lChecksum += (byte) (pMessage[i] >> 8 & 0x00FF);
		}
		lChecksum = (char) ~lChecksum;
		lChecksum++;
		lChecksum = (char) (lChecksum & 0x00FF);
		return lChecksum;
	}

	private void sendSyncCommand(Pointer<Pointer<?>> lPointerToHandle)
	{
		Pointer<Character> lSyncCommandPointer = Pointer.allocateChars(2);
		lSyncCommandPointer.set(0, (char) 0xF0F0);
		lSyncCommandPointer.set(1, (char) 0xF0F0);
		Pointer<Integer> lNumberOfBytesSentPointer = Pointer.allocateInt();
		long lStatusSyncCommand = Ftd2xxLibrary.fTWrite(lPointerToHandle.get(),
																										lSyncCommandPointer,
																										2 * 2,
																										lNumberOfBytesSentPointer);
		lSyncCommandPointer.release();
		lNumberOfBytesSentPointer.release();
		System.out.println("lStatusSyncCommand=" + lStatusSyncCommand);
	}

	private void sendMonitoringCommand(Pointer<Pointer<?>> lPointerToHandle)
	{
		Pointer<Character> lMonitoringCommandPointer = Pointer.allocateChars(1);
		lMonitoringCommandPointer.set(0, (char) 0xE000);
		Pointer<Integer> lNumberOfBytesSentPointer = Pointer.allocateInt();
		long lStatusSyncCommand = Ftd2xxLibrary.fTWrite(lPointerToHandle.get(),
																										lMonitoringCommandPointer,
																										1 * 2,
																										lNumberOfBytesSentPointer);
		lMonitoringCommandPointer.release();
		lNumberOfBytesSentPointer.release();
		System.out.println("lStatusSyncCommand=" + lStatusSyncCommand);
	}

	private void sendUnknownCommand(Pointer<Pointer<?>> lPointerToHandle)
	{
		Pointer<Character> lSyncCommandPointer = Pointer.allocateChars(1);
		lSyncCommandPointer.set(0, (char) 0x8000);
		Pointer<Integer> lNumberOfBytesSentPointer = Pointer.allocateInt();
		long lStatusSyncCommand = Ftd2xxLibrary.fTWrite(lPointerToHandle.get(),
																										lSyncCommandPointer,
																										1 * 2,
																										lNumberOfBytesSentPointer);
		lSyncCommandPointer.release();
		lNumberOfBytesSentPointer.release();
		System.out.println("lStatusSyncCommand=" + lStatusSyncCommand);
	}

}
