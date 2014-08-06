package mirao52;

/**
 * Class Mirao52Exception
 * 
 * Instances of this exception are thrown when there is a problem with the
 * MIRAO52e deformable mirror.
 * 
 * @author Loic Royer 2014
 *
 */
public class Mirao52Exception extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance of Mirao52Exception class given an error message.
	 * 
	 * @param pExceptionMessage
	 */
	public Mirao52Exception(String pExceptionMessage)
	{
		super(pExceptionMessage);
	}

	/**
	 * Constructs an instance of the Mirao52Exception class given an error message
	 * and a cause.
	 * 
	 * @param pExceptionMessage
	 * @param pException
	 */
	public Mirao52Exception(String pExceptionMessage,
													Throwable pException)
	{
		super(pExceptionMessage, pException);
	}

}
