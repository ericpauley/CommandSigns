package org.zonedabone.commandsigns;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CommandSignsMessagingProxy implements InvocationHandler {
    
	// This class does not currently support ConsoleCommandSender as the proxy
	// Further experimentation must be carried out to make this class the one-stop-shop
	
    private Object sender;
    private Object receiver;
    private boolean silent;
    
    public static Object newInstance(Object proxy) {
    	return newInstance(proxy, proxy, false);
    }
    public static Object newInstance(Object proxy, boolean silent) {
    	return newInstance(proxy, proxy, silent);
    }
    public static Object newInstance(Object sender, Object receiver) {
    	return newInstance(sender, receiver, false);
    }
    public static Object newInstance(Object sender, Object receiver, boolean silent) {
    	return Proxy.newProxyInstance(
    		    sender.getClass().getClassLoader(),
    		    sender.getClass().getInterfaces(),
    		    new CommandSignsMessagingProxy(sender, receiver, silent));
    }

    private CommandSignsMessagingProxy(Object sender, Object receiver, boolean silent) {
    	this.sender = sender;
        this.receiver = receiver;
        this.silent = silent;
    }

    // Is called whenever a method is invoked
    public Object invoke(Object p, Method m, Object[] args) throws Throwable {
        Object result = null;
    	try {
    		String name = m.getName();
    		// If the receiver is being sent a message, only do so if the silent flag is not set
    		if (name == "sendMessage" || name == "sendRawMessage") {
    			if (!silent && receiver != null)
    				result = m.invoke(receiver, args);
    		} else {
    			result = m.invoke(sender, args);
    		}
        } catch (InvocationTargetException e) {
    	    throw e.getTargetException();
        } catch (Exception e) {
    	    throw new RuntimeException("Unexpected invocation exception: " + e.getMessage());
    	}
    	return result;
    }
    
}
