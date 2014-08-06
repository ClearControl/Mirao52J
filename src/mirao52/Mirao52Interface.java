package mirao52;

import java.io.IOException;

/**
 * Class Mirao52Interface
 * 
 * @author Loic Royer 2014
 *
 */
public interface Mirao52Interface
{

	public abstract void connect(String pHostname) throws IOException;

	public abstract void close() throws IOException;

	public abstract boolean sendFlatMirrorShapeVector() throws IOException;

	public abstract boolean sendSquareMirrorShapeVector(double[] pSquareMirrorShapeVector) throws IOException;

	public abstract boolean sendRawMirrorShapeVector(double[] pRawMirrorShapeVector) throws IOException;

	public abstract long getLastNumberOfReceivedShapes();

}