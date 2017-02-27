package com.ems.server;

import java.util.ArrayList;
import java.util.Iterator;

public class EmsShutdownHandler {
	
	private ArrayList<EmsShutdownObserver> observers = new ArrayList<EmsShutdownObserver>();

	public void addShutdownObserver(EmsShutdownObserver o) {
		
		observers.add(o);
		
	}
	
	public void removeShutdownObserver(EmsShutdownObserver o) {
		
		observers.remove(o);
		
	}
	
	public void notifyObjservers() {
		
		Iterator<EmsShutdownObserver> i = observers.iterator();
        while( i.hasNext() ) {
        	EmsShutdownObserver o = i.next();
              o.cleanUp();
        }

		
	}

}
