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
package jenkins.plugins.exam.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.ws.rs.core.Form;
import java.io.IOException;
import java.util.Random;

@XStreamAlias("exam-model-config") public class ExamModelConfig extends AbstractDescribableImpl<ExamModelConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExamModelConfig.class);

    public static final String TARGET_ENDPOINT = "http://examServer:8080/exam/ExamModelerService";

    /**
     * The optional display name of this server.
     */
    @CheckForNull protected String name;
    private String modelName;
    private int examVersion;
    private String targetEndpoint = TARGET_ENDPOINT;

    @DataBoundConstructor public ExamModelConfig(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Sets the optional display name.
     *
     * @param name the optional display name.
     */
    @DataBoundSetter public void setName(@CheckForNull String name) {
        this.name = Util.fixEmptyAndTrim(name);
    }

    /**
     * Sets the model name.
     *
     * @param modelName the model name.
     */
    @DataBoundSetter public void setModelName(@CheckForNull String modelName) {
        this.modelName = Util.fixEmptyAndTrim(modelName);
    }

    /**
     * Sets the exam version.
     *
     * @param examVersion exam version without delimiters.
     */
    @DataBoundSetter public void setExamVersion(int examVersion) {
        this.examVersion = examVersion;
    }

    /**
     * Set the target endpoint.
     *
     * @param targetEndpoint custom url if TE. Default value will be used in case of custom
     *                       is unchecked or value is blank
     */
    @DataBoundSetter public void setTargetEndpoint(@CheckForNull String targetEndpoint) {
        this.targetEndpoint = targetEndpoint.isEmpty() ? TARGET_ENDPOINT : targetEndpoint;
    }

    /**
     * Gets the optional display name of this server.
     *
     * @return the optional display name of this server, may be empty or
     * {@code null} but best effort is made to ensure that it has some
     * meaningful text.
     * @since 1.28.0
     */
    public String getName() {
        return name;
    }

    public int getExamVersion() {
        return examVersion;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    public String getDisplayName() {
        return Messages.ExamModelConfig_displayName(getName(), getModelName(), getTargetEndpoint());
    }

    @Extension public static class DescriptorImpl extends Descriptor<ExamModelConfig> {

        @Override public String getDisplayName() {
            return "EXAM Model";
        }

        // TODO Prüfungen für Modellnamen und TargetEndpoint ergänzen

        public FormValidation doCheckName(@QueryParameter String value) {

            ExamModelConfig[] modelConfigs = Jenkins.getInstance().getDescriptorByType(ExamPluginConfig.class)
                    .getModelConfigs().toArray(new ExamModelConfig[0]);

            // TODO prüfung einbauen
            return FormValidation.ok();
        }
    }
}
