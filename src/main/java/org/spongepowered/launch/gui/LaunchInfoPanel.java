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
package org.spongepowered.launch.gui;

import org.spongepowered.launch.Main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class LaunchInfoPanel extends JPanel {

    protected static final URI FORGE_URI = URI.create("http://files.minecraftforge.net/");

    private static final long serialVersionUID = 1L;

    private JPanel panelBanner;
    private JPanel panelInfo;
    private JLabel lblInfo1;
    private JLabel lblIcon;
    private JLabel lblTitle;
    private JPanel panelVLayout;
    private JLabel lblForgeURL;
    private JLabel lblInfo2;

    /**
     * Create the panel.
     */
    public LaunchInfoPanel() {

        setPreferredSize(new Dimension(500, 420));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        try {
            if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                setPreferredSize(new Dimension(500, 340));
            }
        } catch (Throwable th) {
            // well, crap
        }

        this.panelBanner = new JPanel();
        this.panelBanner.setBorder(new EmptyBorder(24, 24, 24, 24));
        this.panelBanner.setPreferredSize(new Dimension(500, 120));
        this.panelBanner.setMinimumSize(new Dimension(100, 120));
        this.panelBanner.setMaximumSize(new Dimension(700, 120));
        this.panelBanner.setBackground(new Color(0x3A3A3A));
        add(this.panelBanner);
        this.panelBanner.setLayout(new BorderLayout(0, 0));

        this.lblTitle = new JLabel("Sponge");
        this.lblTitle.setForeground(Color.WHITE);
        this.lblTitle.setBorder(new EmptyBorder(0, 24, 0, 0));
        this.panelBanner.add(this.lblTitle, BorderLayout.CENTER);

        this.lblIcon = new JLabel("");
        this.lblIcon.setIcon(new ImageIcon(LaunchInfoPanel.class.getResource("sponge_logo.png")));
        this.lblIcon.setMinimumSize(new Dimension(72, 72));
        this.lblIcon.setPreferredSize(new Dimension(72, 72));
        this.panelBanner.add(this.lblIcon, BorderLayout.WEST);

        this.panelInfo = new JPanel();
        this.panelInfo.setBorder(new EmptyBorder(24, 24, 24, 24));
        add(this.panelInfo);
        this.panelInfo.setLayout(new GridLayout(0, 1, 0, 0));

        this.panelVLayout = new JPanel();
        this.panelInfo.add(this.panelVLayout);
        this.panelVLayout.setLayout(new BoxLayout(this.panelVLayout, BoxLayout.Y_AXIS));

        this.lblInfo1 = new JLabel("<html><h3><font color=\"#806600\">Oops! You attempted to run the Sponge Forge Mod directly!</font></h3>"
                + "<p>This jar file is a mod for <b>Minecraft Forge</b>. You can run this file as a <b>Forge mod</b> by installing <b>Minecraft "
                + "Forge</b> and simply dropping the jar file into the Forge \"mods\" directory. You can get <b>Minecraft Forge</b> from:</p>"
                + "</html>");
        this.panelVLayout.add(this.lblInfo1);
        this.lblInfo1.setVerticalAlignment(SwingConstants.TOP);

        this.lblForgeURL = new JLabel("<html><ul><li> &nbsp; <a href=\"" + LaunchInfoPanel.FORGE_URI + "\"><font color=\"#806600\">"
                + LaunchInfoPanel.FORGE_URI + "</font></a></li></ul></html>\r\n");
        this.lblForgeURL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.lblForgeURL.setVerticalAlignment(SwingConstants.TOP);
        this.lblForgeURL.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                LaunchInfoPanel.openURI(LaunchInfoPanel.FORGE_URI);
            }
        });

        this.panelVLayout.add(this.lblForgeURL);

        this.lblInfo2 = new JLabel("<html><p>To run this version of Sponge we recommend that you download <b><font color=\"#806600\">Minecraft Forge"
                + " version " + Main.getManifestAttribute("TargetForgeVersion", "") + "</font></b>. Once you have downloaded and installed <b>Minecraft"
                + " Forge</b>, you can load <b>Sponge</b> as a mod.<br /><br />Click <b>OK</b> to close this window.</p></html>");
        this.lblInfo2.setVerticalAlignment(SwingConstants.TOP);
        this.panelVLayout.add(this.lblInfo2);

        Font font = this.lblTitle.getFont();
        this.lblTitle.setFont(font.deriveFont(font.getSize() + 12.0F));

    }

    static void openURI(URI uri) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        try {
            Desktop.getDesktop().browse(uri);
        } catch (IOException e) {
            // TODO Maybe display a message that it failed
        }
    }

}
