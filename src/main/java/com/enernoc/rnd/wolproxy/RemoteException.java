package com.enernoc.rnd.wolproxy;

public class RemoteException extends RuntimeException {
	private static final long serialVersionUID = 13456302349823L;

	public RemoteException( String msg, Throwable cause ) {
		super( msg, cause );
	}
	
	public RemoteException( String msg ) { super( msg ); }
}
