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

import org.spongepowered.launch.gui.LaunchInfoPanel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * Bootstrap class to provide some help for end users and manage headless
 * self-install deployment via Ivy
 */
public class Main {

    private static final File JAR = Main.getJar();
    public static final String JAR_NAME = Main.getJarName();

    private static Attributes manifestAttributes = null;

    public static void main(String[] argv) {
        if (System.console() == null && !Main.checkHeadlessState()) {
            Main.displayGraphicalMessage();
            return;
        }

        Main.printHeader();
        Main.printUsage();
    }

    private static void displayGraphicalMessage() {
        JOptionPane messagePane = new JOptionPane(new LaunchInfoPanel(), JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
        messagePane.setBorder(new EmptyBorder(0, 0, 20, 0));
        String dialogTitle = "Cannot start this jar directly - " + Main.getManifestAttribute("Implementation-Version", "DEV");
        JDialog dialog = messagePane.createDialog(null, dialogTitle);
        try {
            dialog.setIconImage(ImageIO.read(LaunchInfoPanel.class.getResourceAsStream("sponge_icon.png")));
        } catch (IOException ex) {
            // load icon failed :(
        }
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
    }

    private static void printHeader() {
        System.out.printf("\n%s v%s for Minecraft Forge %s\n   implementing %s version %s\n",
                Main.getManifestAttribute("Implementation-Name", "Sponge"),
                Main.getManifestAttribute("Implementation-Version", "DEV"),
                Main.getManifestAttribute("TargetForgeBuild", ""),
                Main.getManifestAttribute("Specification-Name", "SpongeAPI"),
                Main.getManifestAttribute("Specification-Version", "DEV")
        );
    }

    private static boolean checkHeadlessState() {
        try {
            Class<?> clGraphicsEnvironment = Class.forName("java.awt.GraphicsEnvironment");
            Method mdIsHeadless = clGraphicsEnvironment.getDeclaredMethod("isHeadless");
            return (Boolean)mdIsHeadless.invoke(null);
        } catch (Exception ex) {
            // ignore
        }
        return true;
    }

    private static void printUsage() {
        Scanner help = new Scanner(Main.class.getResourceAsStream("help.txt")).useDelimiter("\\Z");
        System.out.print(help.next().replace("${jarfile}", Main.JAR_NAME));
    }

    private static File getJar() {
        try {
            File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (jar.isFile()) {
                return jar;
            }
        } catch (IllegalArgumentException ex) {
            try {
                String uriFull = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
                int bangPos = uriFull.lastIndexOf('!');
                if (bangPos > -1 && uriFull.startsWith("jar:")) {
                    File jar = new File(URI.create(uriFull.substring(4, bangPos)));
                    if (jar.isFile()) {
                        return jar;
                    }
                }
            } catch (Exception ex1) {
                // ex1.printStackTrace();
            }
        } catch (Exception ex) {
            // derp
        }

        return null;
    }

    private static String getJarName() {
        return Main.JAR != null ? Main.JAR.getName() : "sponge.jar";
    }

    public static String getManifestAttribute(String key, String defaultValue) {
        if (Main.JAR == null) {
            return defaultValue;
        }

        Attributes manifestAttributes = Main.getManifestAttributes();
        return manifestAttributes != null ? manifestAttributes.getValue(key) : defaultValue;
    }

    private static Attributes getManifestAttributes() {
        if (Main.manifestAttributes == null) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(Main.JAR);
                Main.manifestAttributes = jarFile.getManifest().getMainAttributes();

            } catch (IOException ex) {
                // be quiet checkstyle
            } finally {
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (IOException ex) {
                        // this could be an issue later on :(
                    }
                }
            }
        }

        return Main.manifestAttributes;
    }

}
