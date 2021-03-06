/**
 * Copyright (c) 2018 MicroNova AG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this
 *        list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this
 *        list of conditions and the following disclaimer in the documentation and/or
 *        other materials provided with the distribution.
 *
 *     3. Neither the name of MicroNova AG nor the names of its
 *        contributors may be used to endorse or promote products derived from
 *        this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jenkins.task;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.plugins.exam.config.ExamModelConfig;
import jenkins.plugins.exam.config.ExamPluginConfig;
import jenkins.task._exam.Messages;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;

@XStreamAlias("testrun-filter") public class TestrunFilter extends AbstractDescribableImpl<TestrunFilter>{

    @CheckForNull protected String name;
    @CheckForNull protected String value;
    protected boolean adminCases;
    protected boolean activateTestcases = false;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean getAdminCases() {
        return adminCases;
    }

    public boolean getActivateTestcases() {
        return activateTestcases;
    }

    @Override public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @DataBoundConstructor public TestrunFilter(String name, String value, boolean adminCases, boolean activateTestcases) {
        this.name = name;
        this.value = value;
        this.adminCases = adminCases;
        this.activateTestcases = activateTestcases;
    }

    @Extension public static class DescriptorImpl extends Descriptor<TestrunFilter> {

        public DescriptorImpl() {
            load();
        }

        protected DescriptorImpl(Class<? extends TestrunFilter> clazz) {
            super(clazz);
            load();
        }

        @Override public String getDisplayName() {
            return "Testrun Filter";
        }

    }
}
