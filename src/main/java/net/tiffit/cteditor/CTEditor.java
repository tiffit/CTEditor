package net.tiffit.cteditor;

import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.tiffit.cteditor.gui.EditorGui;

@Mod(modid = CTEditor.MODID, name = CTEditor.NAME, version = CTEditor.VERSION, clientSideOnly = true)
@Mod.EventBusSubscriber
public class CTEditor
{
    public static final String MODID = "cteditor";
    public static final String NAME = "CraftTweaker Editor";
    public static final String VERSION = "0.0.1";

    public static Logger logger;
    public static KeyBinding openEditor;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        logger = event.getModLog();
        openEditor = new KeyBinding("key.openeditor", Keyboard.KEY_NUMPAD4, "keycat." + MODID);
        openEditor.setKeyConflictContext(KeyConflictContext.IN_GAME);
		ClientRegistry.registerKeyBinding(openEditor);
    }

    @EventHandler
    public void init(FMLInitializationEvent event){}
    
    @SubscribeEvent
    public static void onKeyPress(KeyInputEvent e){
    	if(openEditor.isPressed()){
    		Minecraft.getMinecraft().displayGuiScreen(new EditorGui());
    	}
    }
}
