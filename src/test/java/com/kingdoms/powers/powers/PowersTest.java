package com.kingdoms.powers.powers;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PowersTest {
    private ServerMock server;
    private Powers plugin;
    private PlayerMock player;
    String script = """
                        EntityType = class:fromName("org.bukkit.entity.EntityType")
                        Player = class:fromName("org.bukkit.entity.Player")
                                    
                        function onEntityDamage(event)
                          entity = event:getEntity()
                          if tags:hasTag("powers.power")
                            if event:getEntityType():equals(EntityType.PLAYER) then
                              player = class:cast(event:getEntity(), Player)
                              player:setLevel(player:getLevel() + 5)
                            end
                          end
                        end
                                    
                                    
                        script_table = {
                          events = {
                            EntityDamageEvent = onEntityDamage
                          }
                        }
                                    
                        return script_table
                        """;


    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Powers.class);
        player = server.addPlayer();
        plugin.loadClasses();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void loadLuaEventListener() {
        plugin.loadLuaEventListener(script);

        player.setLevel(0);
        player.damage(1);
        assertEquals(0, player.getLevel());

        Powers.tagManager.addTag(player, "powers.power");
        player.setLevel(0);
        player.damage(1);
        assertEquals(5, player.getLevel());
    }

    @Test
    void testLoadScripts() throws IOException {
        PrintWriter writer = new PrintWriter(plugin.getDataFolder().getAbsolutePath() + "\\passiva.lua", StandardCharsets.UTF_8);
        writer.print(script);
        writer.close();
        plugin.loadScripts();
        player.setLevel(0);
        player.damage(1);
        assertEquals(5, player.getLevel());
    }

}