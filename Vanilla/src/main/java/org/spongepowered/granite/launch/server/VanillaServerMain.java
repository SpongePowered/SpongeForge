/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered.org <http://www.spongepowered.org>
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
package org.spongepowered.granite.launch.server;

import net.minecraft.launchwrapper.Launch;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public final class VanillaServerMain {

    private static final String MINECRAFT_SERVER_LOCAL = "minecraft_server.1.8.jar";
    private static final String MINECRAFT_SERVER_REMOTE = "https://s3.amazonaws.com/Minecraft.Download/versions/1.8/minecraft_server.1.8.jar";

    private static final String LAUNCHWRAPPER_LOCAL = "launchwrapper-1.11.jar";
    private static final String LAUNCHWRAPPER_REMOTE = "https://libraries.minecraft.net/net/minecraft/launchwrapper/1.11/launchwrapper-1.11.jar";

    private VanillaServerMain() {
    }

    public static void main(String[] args) throws Exception {
        if (!checkMinecraft()) {
            return;
        }

        Launch.main(join(args,
                "--tweakClass", "org.spongepowered.granite.launch.server.VanillaServerTweaker"
        ));
    }

    private static String[] join(String[] args, String... prefix) {
        String[] result = new String[prefix.length + args.length];
        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(args, 0, result, prefix.length, args.length);
        return result;
    }

    private static boolean checkMinecraft() throws Exception {
        File bin = new File("bin");
        if (!bin.isDirectory() && !bin.mkdirs()) {
            throw new IOException("Failed to create folder at " + bin);
        }

        // First check if Minecraft is already downloaded :D
        File file = new File(bin, MINECRAFT_SERVER_LOCAL);

        // Download the server first
        if (!file.isFile() && !downloadVerified(MINECRAFT_SERVER_REMOTE, file)) {
            return false;
        }

        file = new File(bin, LAUNCHWRAPPER_LOCAL);
        return file.isFile() || downloadVerified(LAUNCHWRAPPER_REMOTE, file);
    }


    private static boolean downloadVerified(String remote, File file) throws Exception {
        String name = file.getName();
        URL url = new URL(remote);

        System.out.println("Downloading " + name + "... This can take a while.");
        System.out.println(url);
        URLConnection con = url.openConnection();
        MessageDigest md5 = MessageDigest.getInstance("MD5");

        InputStream in = null;
        FileOutputStream out = null;
        ReadableByteChannel source = null;
        FileChannel target = null;
        try {
            in = con.getInputStream();
            out = new FileOutputStream(file);
            source = Channels.newChannel(new DigestInputStream(in, md5));
            target = out.getChannel();

            target.transferFrom(source, 0, Long.MAX_VALUE);
        } finally {
            close(in, out, source, target);
        }

        String expected = getETag(con);
        if (!expected.isEmpty()) {
            String hash = toHexString(md5.digest());
            if (hash.equals(expected)) {
                System.out.println("Successfully downloaded " + name + " and verified checksum!");
            } else {
                if (!file.delete()) {
                    throw new IOException("Failed to delete " + file);
                }

                System.err.println("Failed to download " + name + " (failed checksum verification).");
                System.err.println("Please try again later.");
                return false;
            }
        }

        return true;
    }

    private static String getETag(URLConnection con) {
        String hash = con.getHeaderField("ETag");
        if (hash == null || hash.isEmpty()) {
            return "";
        }

        if (hash.startsWith("\"") && hash.endsWith("\"")) {
            hash = hash.substring(1, hash.length() - 1);
        }

        return hash;
    }

    private static void close(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public static String toHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
