package hr.fer.su.mgc.conv;

public class ConversionException extends Exception {
	private static final long serialVersionUID = 4303642818411177413L;
	
	public static final int EX_TYPE_GENERIC = 0;
	
	private int exceptionType;

	public int getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(int exceptionType) {
		this.exceptionType = exceptionType;
	}
	
	public ConversionException(String message) {
		super(message);
	}

	public ConversionException(int exceptionType, String message) {
		super(message);
		this.exceptionType = exceptionType;
	}
	

}
