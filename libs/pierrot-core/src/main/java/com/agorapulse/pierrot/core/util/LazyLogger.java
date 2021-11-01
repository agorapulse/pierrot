/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2021 Vladimir Orany.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agorapulse.pierrot.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class LazyLogger implements Logger {

    public static Logger create(Class<?> type) {
        return new LazyLogger(type);
    }

    private final Class<?> type;
    private Logger delegate;

    private LazyLogger(Class<?> type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return getDelegate().isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        getDelegate().trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        getDelegate().trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        getDelegate().trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        getDelegate().trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        getDelegate().trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return getDelegate().isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        getDelegate().trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        getDelegate().trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        getDelegate().trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        getDelegate().trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        getDelegate().trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return getDelegate().isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        getDelegate().debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        getDelegate().debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        getDelegate().debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        getDelegate().debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        getDelegate().debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return getDelegate().isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        getDelegate().debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        getDelegate().debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        getDelegate().debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        getDelegate().debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        getDelegate().debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return getDelegate().isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        getDelegate().info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        getDelegate().info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        getDelegate().info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        getDelegate().info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        getDelegate().info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return getDelegate().isInfoEnabled(marker);
    }

    @Override
    public void info(Marker marker, String msg) {
        getDelegate().info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        getDelegate().info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        getDelegate().info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        getDelegate().info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        getDelegate().info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return getDelegate().isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        getDelegate().warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        getDelegate().warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        getDelegate().warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        getDelegate().warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        getDelegate().warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return getDelegate().isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        getDelegate().warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        getDelegate().warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        getDelegate().warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        getDelegate().warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        getDelegate().warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return getDelegate().isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        getDelegate().error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        getDelegate().error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        getDelegate().error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        getDelegate().error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        getDelegate().error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return getDelegate().isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        getDelegate().error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        getDelegate().error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        getDelegate().error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        getDelegate().error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        getDelegate().error(marker, msg, t);
    }

    private Logger getDelegate() {
        if (delegate == null) {
            delegate = LoggerFactory.getLogger(type);
        }
        return delegate;
    }

}
