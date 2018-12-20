/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.morlunk.ayandap.db;

import com.morlunk.jumble.model.Server;

import java.util.List;

/**
 * An interface for persistent storage services (i.e. databases, cloud) to implement.
 * Created by andrew on 13/08/13.
 */
public interface PlumbleDatabase {
    void open();
    void close();

    List<Server> getServers();
    void addServer(Server server);
    void updateServer(Server server);
    void removeServer(Server server);

    boolean isCommentSeen(String hash, byte[] commentHash);
    void markCommentSeen(String hash, byte[] commentHash);

    List<Integer> getPinnedChannels(long serverId);
    void addPinnedChannel(long serverId, int channelId);
    void removePinnedChannel(long serverId, int channelId);
    boolean isChannelPinned(long serverId, int channelId);

    List<String> getAccessTokens(long serverId);
    void addAccessToken(long serverId, String token);
    void removeAccessToken(long serverId, String token);

    List<Integer> getLocalMutedUsers(long serverId);
    void addLocalMutedUser(long serverId, int userId);
    void removeLocalMutedUser(long serverId, int userId);

    List<Integer> getLocalIgnoredUsers(long serverId);
    void addLocalIgnoredUser(long serverId, int userId);
    void removeLocalIgnoredUser(long serverId, int userId);

    /**
     * Adds the given certificate binary blob to the database.
     * @param name The user-readable certificate name.
     * @param certificate A PKCS12 binary blob.
     * @return A handle for the newly craeted certificate.
     */
    DatabaseCertificate addCertificate(String name, byte[] certificate);
    void deleteDatabaseCertificate(); //saeed Added
    List<DatabaseCertificate> getCertificates();

    /**
     * Obtains the certificate data associated with the given certificate ID.
     * @param id The certificate ID to fetch the data of.
     * @return A binary representation of a PKCS12 certificate.
     */
    byte[] getCertificateData(long id);

    /**
     * Removes the certificate with the given ID.
     * @param id The certificate's identifier.
     */
    void removeCertificate(long id);
}
