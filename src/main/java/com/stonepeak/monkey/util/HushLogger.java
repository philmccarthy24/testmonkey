package com.stonepeak.monkey.util;

import org.eclipse.jetty.util.log.Logger;

/**
 * Stop default jetty logging
 * @author phil
 *
 */

public class HushLogger implements Logger {
	
    @Override
    public String getName() {
        return "HushLogger";
    }

    @Override
    public void warn(String msg, Object... args) {}

    @Override
    public void warn(Throwable thrown) {}

    @Override
    public void warn(String msg, Throwable thrown) {}

    @Override
    public void info(String msg, Object... args) {}

    @Override
    public void info(Throwable thrown) {}

    @Override
    public void info(String msg, Throwable thrown) {}

    @Override
    public boolean isDebugEnabled() {return false; }

    @Override
    public void setDebugEnabled(boolean enabled) {}

    @Override
    public void debug(String msg, Object... args) {}

    @Override
    public void debug(Throwable thrown) {}

    @Override
    public void debug(String msg, Throwable thrown) {}

    @Override
    public Logger getLogger(String name) {return this; }

    @Override
    public void ignore(Throwable ignored) {}

}

