package com.kingdoms.powers.powers;

import com.kingdoms.tags.TagManager;
import com.kingdoms.tags.Tags;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Powers extends JavaPlugin {
    private static final Reflections eventReflections = new Reflections("org.bukkit.event");
    private static final HashMap<String, Class<? extends Event>> eventMap = new HashMap<>();
    private static final Listener emptyListener = new Listener() {};

    public static TagManager tagManager;
    @Override
    public void onEnable() {
        loadScripts();
        Tags tags = (Tags) Bukkit.getPluginManager().getPlugin("KingdomsTags");
        if(tags != null)
            tagManager = tags.tagManager;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Powers(){
        super();
    }

    protected Powers(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file){
        super(loader, description, dataFolder, file);
    }

    public void loadClasses(){
        eventMap.clear();
        eventReflections.getSubTypesOf(Event.class).forEach(cl -> eventMap.put(cl.getSimpleName(), cl));
    }

    public void loadScripts(){
        if(eventMap.isEmpty()) loadClasses();

        // For script in scripts
        File[] files = getDataFolder().listFiles();
        if(files == null)
            return;


        // Gets all files with ".lua"
        List<File> scripts = Arrays.stream(files).filter(f -> f.getName().endsWith(".lua")).collect(Collectors.toList());

        for (File script : scripts) {
            String scriptString;
            Path path = script.toPath();

            try { scriptString = String.join("\n", Files.readAllLines(path));}
            catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            loadLuaEventListener(scriptString);
        }
    } 

    /**@param luaScript Script to load and turn into Event listeners
     * @see org.bukkit.event.Event*/
    public void loadLuaEventListener(String luaScript){
        Globals globals = JsePlatform.standardGlobals();
        globals.set("tags", CoerceJavaToLua.coerce(tagManager));
        globals.set("class", CoerceJavaToLua.coerce(new ClassLuaHelper()));

        LuaTable data = globals.load(luaScript).call().checktable();
        LuaTable events = data.get("events").checktable();

        // Register all events
        LuaValue eventName = LuaValue.NIL;
        while (true) {
            var next = events.next(eventName);

            // Set eventName to the next arg, if it's nil, break
            if ((eventName = next.arg1()).isnil()) break;

            LuaValue func = next.arg(2);

            // Get Event class from name
            String eventClassName = eventName.checkjstring();
            Class<? extends Event> targetClass = eventMap.get(eventClassName);

            // Convert to Closure for better performance
            LuaFunction function = func.checkfunction();
            LuaClosure closure = function.checkclosure();

            // Register unitary event
            Bukkit.getPluginManager().registerEvent(
                    targetClass,
                    emptyListener,
                    EventPriority.NORMAL,
                    ((listener, event) -> closure.call(CoerceJavaToLua.coerce(event))),
                    this
            );
        }
    }

    @SuppressWarnings("unused")
    static class ClassLuaHelper {
        public <T> T cast(Object object, Class<T> tClass) {
            return tClass.cast(object);
        }
        public boolean isInstanceOf(Object object, Class<?> cls){ return cls.isInstance(object); }
        public Class<?> fromName(String name) throws ClassNotFoundException { return Class.forName(name); }
    }
}
