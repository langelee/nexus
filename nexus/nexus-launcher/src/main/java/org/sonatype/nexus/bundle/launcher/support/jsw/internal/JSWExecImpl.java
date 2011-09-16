/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bundle.launcher.support.jsw.internal;

import com.google.common.base.Preconditions;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Commandline;
import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.bundle.launcher.support.ant.AntHelper;
import org.sonatype.nexus.bundle.launcher.support.jsw.JSWExec;

import java.io.File;
import java.io.IOException;

/**
 * Default {@link JSWExec} implementation.
 *
 * @since 1.9.3
 */
class JSWExecImpl
        implements JSWExec {

    private Logger logger = LoggerFactory.getLogger(JSWExecImpl.class);

    private final String appName;
    private final File controlScript;
    private final String controlScriptCanonicalPath;
    private final AntHelper ant;

    /**
     * Constructor.
     *
     * @param binDir  the bin directory where the jsw control scripts are located
     * @param appName the app name managed by JSW
     * @throws NullPointerException     if params are null
     * @throws IllegalArgumentException if the JSW exec script cannot be found for this platform
     * @since 1.9.3
     */
    public JSWExecImpl(final File binDir, final String appName, final AntHelper ant) {
        Preconditions.checkNotNull(binDir);
        Preconditions.checkNotNull(appName);
        Preconditions.checkNotNull(ant);

        this.ant = ant;

        if (!binDir.isDirectory()) {
            throw new IllegalArgumentException("binDir is not a directory:" + binDir.getAbsolutePath());
        }

        if (appName.trim().equals("")) {
            throw new IllegalArgumentException("appName must contain at least one character");
        }

        this.appName = appName;

        boolean windows = Os.isFamily(Os.FAMILY_WINDOWS);
        final String extension = windows ? ".bat" : "";
        this.controlScript = new File(binDir, appName + extension);

        ant.chmod(binDir, "**/*", "u+x");

        if (!this.controlScript.isFile() || !this.controlScript.canExecute()) {
            throw new IllegalArgumentException("jsw script is not an executable file: " + this.controlScript.getAbsolutePath());
        }

        try {
            this.controlScriptCanonicalPath = this.controlScript.getCanonicalPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem getting canonical path to jsw control script: " + this.controlScript.getAbsolutePath(), e);
        }

    }

    /**
     * {@inheritDoc}
     *
     * @since 1.9.3
     */
    public JSWExecImpl start() {
        //need console since on windows we would first need a service installed if start cmd was used
        executeJSWScript("start");
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.9.3
     */
    public JSWExecImpl stop() {
        executeJSWScript("stop", false);
        return this;
    }

    protected File getControlScript() {
        return this.controlScript;
    }

    private void executeJSWScript(final String command) {
        executeJSWScript(command, true);
    }

    private void executeJSWScript(final String command, final boolean spawn) {
        File script = getControlScript();

        ExecTask exec = ant.createTask(ExecTask.class);
        exec.setExecutable(this.controlScriptCanonicalPath);

        if (spawn) {
            exec.setSpawn(true);
        } else {
            exec.setFailIfExecutionFails(true);
            exec.setFailonerror(true);
        }
        Commandline.Argument arg = exec.createArg();
        arg.setValue(command);
        exec.setDir(script.getParentFile());
        logger.info("Executing {} script cmd {} {}", new Object[]{appName, this.controlScriptCanonicalPath, command});
        exec.execute();
    }


}
