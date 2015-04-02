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
package org.spongepowered.mod.mixin.core.entity.player;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.world.World;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.net.PlayerConnection;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.Titles;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mod.effect.particle.SpongeParticleEffect;
import org.spongepowered.mod.effect.particle.SpongeParticleHelper;
import org.spongepowered.mod.interfaces.Subjectable;
import org.spongepowered.mod.text.SpongeChatComponent;
import org.spongepowered.mod.text.SpongeText;
import org.spongepowered.mod.text.chat.SpongeChatType;
import org.spongepowered.mod.text.title.SpongeTitle;
import org.spongepowered.mod.util.VecHelper;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityPlayerMP.class)
@Implements(@Interface(iface = Player.class, prefix = "playermp$"))
public abstract class MixinEntityPlayerMP extends EntityPlayer implements CommandSource, Subjectable {

    @Shadow
    private String translator;

    @Shadow
    public NetHandlerPlayServer playerNetServerHandler;

    @Shadow
    public int lastExperience;

    public MixinEntityPlayerMP(World worldIn, com.mojang.authlib.GameProfile gameprofile) {
        super(worldIn, gameprofile);
    }

    public GameProfile playermp$getProfile() {
        return (GameProfile) getGameProfile();
    }

    public String playermp$getName() {
        return getGameProfile().getName();
    }

    public boolean playermp$isOnline() {
        return true;
    }

    public Optional<Player> playermp$getPlayer() {
        return Optional.of((Player) this);
    }

    public Text playermp$getDisplayName() {
        return ((SpongeChatComponent) getDisplayName()).toText();
    }

    public Locale playermp$getLocale() {
        return new Locale(this.translator);
    }

    public void playermp$sendMessage(Text... messages) {
        playermp$sendMessage(ChatTypes.CHAT, messages);
    }

    public void playermp$sendMessage(Iterable<Text> messages) {
        playermp$sendMessage(ChatTypes.CHAT, messages);
    }

    public void playermp$sendMessage(ChatType type, Text... messages) {
        for (Text text : messages) {
            this.playerNetServerHandler.sendPacket(new S02PacketChat(((SpongeText) text).toComponent(), ((SpongeChatType) type).getId()));
        }
    }

    public void playermp$sendMessage(ChatType type, Iterable<Text> messages) {
        for (Text text : messages) {
            this.playerNetServerHandler.sendPacket(new S02PacketChat(((SpongeText) text).toComponent(), ((SpongeChatType) type).getId()));
        }
    }

    public void playermp$sendTitle(Title title) {
        ((SpongeTitle) title).send((EntityPlayerMP) (Object) this);
    }

    public void playermp$resetTitle() {
        playermp$sendTitle(Titles.RESET);
    }

    public void playermp$clearTitle() {
        playermp$sendTitle(Titles.CLEAR);
    }

    public void playermp$spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.playermp$spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    public void playermp$spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            double dx = this.posX - position.getX();
            double dy = this.posY - position.getY();
            double dz = this.posZ - position.getZ();

            if (dx * dx + dy * dy + dz * dz < radius * radius) {
                for (Packet packet : packets) {
                    this.playerNetServerHandler.sendPacket(packet);
                }
            }
        }
    }

    public PlayerConnection playermp$getConnection() {
        return (PlayerConnection) this.playerNetServerHandler;
    }

    public void playermp$setBedLocation(@Nullable Location location) {
        super.spawnChunk = location != null ? VecHelper.toBlockPos(location.getPosition()) : null;
    }

    // this needs to be overridden from EntityPlayer so we can force a resend of the experience level
    public void playermp$setLevel(int level) {
        this.experienceLevel = level;
        this.lastExperience = -1;
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public String getIdentifier() {
        return getUniqueID().toString();
    }

    @Override
    public Tristate permDefault(String permission) {
        return Tristate.FALSE;
    }

}
