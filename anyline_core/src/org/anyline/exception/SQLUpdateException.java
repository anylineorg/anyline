package org.anyline.exception;

public class SQLUpdateException extends SQLException{
	private static final long serialVersionUID = 1L;
	public SQLUpdateException(){
		super();
	}
	public SQLUpdateException(String title){
		super(title);
	}
}
