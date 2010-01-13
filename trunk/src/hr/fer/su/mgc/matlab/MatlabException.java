package hr.fer.su.mgc.matlab;

public class MatlabException extends Exception {
	private static final long serialVersionUID = 1687200595982655087L;
	
	public MatlabException(String errorMessage) {
		super(errorMessage);
	}
}