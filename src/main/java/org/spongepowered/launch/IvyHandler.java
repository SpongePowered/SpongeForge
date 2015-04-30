/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.launch;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.retrieve.RetrieveReport;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.repository.url.URLResource;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;
import org.apache.ivy.util.filter.FilterHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * Base handler for the Ivy tasks
 */
public abstract class IvyHandler implements LaunchHandler {

    protected final IvySettings settings;
    protected final Ivy ivy;
    protected final URL ivyUrl;
    protected ModuleRevisionId revisionId;
    protected List<String> args;

    protected IvyHandler(URL settingsUrl, URL ivyUrl) {
        this.ivyUrl = ivyUrl;
        
        Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_ERR));
        
        this.settings = new IvySettings();
        
        try {
            this.settings.load(settingsUrl);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ModuleDescriptorParser parser = ModuleDescriptorParserRegistry.getInstance().getParser(new URLResource(ivyUrl));
        try {
            this.revisionId = parser.parseDescriptor(this.settings, ivyUrl, false).getModuleRevisionId();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        this.ivy = Ivy.newInstance(this.settings);
        this.ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_INFO));
    }
    
    @Override
    public void acceptArguments(List<String> args) {
        this.args = args;
    }

    protected boolean resolve() {
        try {
            ResolveReport report = this.ivy.resolve(this.ivyUrl);
            this.revisionId = report.getModuleDescriptor().getModuleRevisionId();
            return !report.hasError();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    protected Collection<File> retrieve() {
        try {
            RetrieveOptions options = new RetrieveOptions();
            options.setArtifactFilter(FilterHelper.getArtifactTypeFilter("jar,bundle"));
            options.setDestArtifactPattern("[conf]/[orgPath]/[artifact]/[revision]/[artifact]-[revision].[ext]");
            
            RetrieveReport report = this.ivy.retrieve(this.revisionId, options);
            return report.getRetrievedFiles();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
