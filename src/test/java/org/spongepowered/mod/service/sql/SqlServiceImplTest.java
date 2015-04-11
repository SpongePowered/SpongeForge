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
package org.spongepowered.mod.service.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.sql.SQLException;

public class SqlServiceImplTest {
    @Test
    public void testMysqlConnectionInfo() throws SQLException {
        final String jdbcUrl = "jdbc:mysql://zml:totallymypassword@localhost/sponge";
        final SqlServiceImpl.ConnectionInfo subject = SqlServiceImpl.ConnectionInfo.fromUrl(jdbcUrl);

        assertEquals("zml", subject.getUser());
        assertEquals("totallymypassword", subject.getPassword());
        assertEquals("jdbc:mysql://localhost/sponge", subject.getAuthlessUrl());
        assertEquals("org.mariadb.jdbc.Driver", subject.getDriverClassName());
    }

    @Test
    public void testH2ConnectionInfo() throws SQLException {
        final String jdbcUrl = "jdbc:h2:sparkles.db";
        final SqlServiceImpl.ConnectionInfo subject = SqlServiceImpl.ConnectionInfo.fromUrl(jdbcUrl);

        assertNull(subject.getUser());
        assertNull(subject.getPassword());
        assertEquals(jdbcUrl, subject.getAuthlessUrl());
        assertEquals("org.h2.Driver", subject.getDriverClassName());
    }

    @Test
    public void testSqliteConnectionInfo() throws SQLException {
        final String jdbcUrl = "jdbc:sqlite:glitter.db";
        final SqlServiceImpl.ConnectionInfo subject = SqlServiceImpl.ConnectionInfo.fromUrl(jdbcUrl);

        assertNull(subject.getUser());
        assertNull(subject.getPassword());
        assertEquals(jdbcUrl, subject.getAuthlessUrl());
        assertEquals("org.sqlite.JDBC", subject.getDriverClassName());
    }
}
