package mirao52.bindings.demo;

import static org.junit.Assert.fail;
import mirao52.bindings.Mirao52eLibrary;

import org.bridj.Pointer;
import org.junit.Test;

public class Mirao52LibraryDemo
{

	@Test
	public void testOpenClose()
	{
		Pointer<Integer> status = Pointer.allocateInt();
		byte lMroOpen = Mirao52eLibrary.mroOpen(status);
		if (lMroOpen == Mirao52eLibrary.MRO_FALSE)
		{
			System.err.println("Could not open ");
			fail();
		}

		byte lMroClose = Mirao52eLibrary.mroClose(status);
		if (lMroClose == Mirao52eLibrary.MRO_FALSE)
		{
			System.err.println("Could not close ");
			fail();
		}
	}

}
