package ftd2xx;
import org.bridj.BridJ;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
/**
 * <i>native declaration : lib\ftd2xx\ftd2xx.helper.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Library("ftd2xx") 
public class SECURITY_ATTRIBUTES extends StructObject {
	static {
		BridJ.register();
	}
	@Field(0) 
	public int nLength() {
		return this.io.getIntField(this, 0);
	}
	@Field(0) 
	public SECURITY_ATTRIBUTES nLength(int nLength) {
		this.io.setIntField(this, 0, nLength);
		return this;
	}
	/** C type : LPVOID */
	@Field(1) 
	public Pointer<? > lpSecurityDescriptor() {
		return this.io.getPointerField(this, 1);
	}
	/** C type : LPVOID */
	@Field(1) 
	public SECURITY_ATTRIBUTES lpSecurityDescriptor(Pointer<? > lpSecurityDescriptor) {
		this.io.setPointerField(this, 1, lpSecurityDescriptor);
		return this;
	}
	@Field(2) 
	public int bInheritHandle() {
		return this.io.getIntField(this, 2);
	}
	@Field(2) 
	public SECURITY_ATTRIBUTES bInheritHandle(int bInheritHandle) {
		this.io.setIntField(this, 2, bInheritHandle);
		return this;
	}
	public SECURITY_ATTRIBUTES() {
		super();
	}
	public SECURITY_ATTRIBUTES(Pointer pointer) {
		super(pointer);
	}
}
