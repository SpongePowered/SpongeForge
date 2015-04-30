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
package org.spongepowered.launch.handlers;

import org.spongepowered.launch.Main;

import org.spongepowered.launch.IvyHandler;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

/**
 * Perform Ivy retrieve (from cache) then run the server
 */
public class RunHandler extends IvyHandler {

    private static final ClassLoader classLoader = RunHandler.class.getClassLoader();
    
    private static final Method mAddUrl = RunHandler.getAddUrlMethod();
    
    public RunHandler(URL settingsUrl, URL ivyUrl) {
        super(settingsUrl, ivyUrl);
    }
    
    @Override
    public String getName() {
        return "run";
    }
    
    @Override
    public String getDescription() {
        return "Launches the server, must run install task first";
    }
    
    private static Method getAddUrlMethod() {
        try {
            Method mAddUrl = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            mAddUrl.setAccessible(true);
            return mAddUrl;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        Collection<File> libraries = this.retrieve();
        if (libraries == null) {
            return;
        }
        
        this.addLibrariesToClassPath(libraries);
        
        String coreMods = System.getProperty("fml.coreMods.load", "");
        System.setProperty("fml.coreMods.load", coreMods + "," + Main.getManifestAttribute("FMLCorePlugin", ""));

        try {
            Class<?> launch = Class.forName("net.minecraftforge.fml.relauncher.ServerLaunchWrapper");
            Method main = launch.getDeclaredMethod("main", new Class<?>[] {String[].class} );
            main.invoke(null, new Object[] { this.args.toArray(new String[0]) });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addLibrariesToClassPath(Collection<File> libraries) {
        for (File library : libraries) {
            try {
                RunHandler.mAddUrl.invoke(RunHandler.classLoader, library.toURI().toURL());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
