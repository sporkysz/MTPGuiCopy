package net.mtproject.copymenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(modid = CopyMenu.MODID, version = CopyMenu.VERSION)
public class CopyMenu {
    public static final String MODID = "copymenu";
    public static final String VERSION = "1.0";

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiContainer) {
            int buttonId = 9999;
            int buttonWidth = 100;
            int buttonHeight = 20;
            int buttonX = event.gui.width - buttonWidth - 10;
            int buttonY = 10;

            event.buttonList.add(new GuiButton(buttonId, buttonX, buttonY, buttonWidth, buttonHeight, "Copy Menu"));
        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.button.id == 9999 && event.gui instanceof GuiContainer) {
            copyMenuToJson((GuiContainer) event.gui);
            event.gui.mc.thePlayer.closeScreen();
        }
    }

    private void copyMenuToJson(GuiContainer gui) {
        Map<String, Object> menuData = new HashMap<>();
        Container container = gui.inventorySlots;

        String menuName = gui.getClass().getSimpleName();
        menuData.put("menuName", menuName);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Slot slot : container.inventorySlots) {
            ItemStack itemStack = slot.getStack();
            if (itemStack != null) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("slot", slot.slotNumber);
                itemData.put("itemName", itemStack.getDisplayName());
                itemData.put("itemId", itemStack.getItem().getRegistryName());

                List<String> lore = new ArrayList<>();
                if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("display", 10)) {
                    net.minecraft.nbt.NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");
                    if (display.hasKey("Lore", 9)) {
                        net.minecraft.nbt.NBTTagList loreList = display.getTagList("Lore", 8);
                        for (int i = 0; i < loreList.tagCount(); i++) {
                            lore.add(loreList.getStringTagAt(i));
                        }
                    }
                }
                itemData.put("lore", lore);

                items.add(itemData);
            }
        }
        menuData.put("items", items);

        try {
            File configDir = new File("config");
            if (!configDir.exists()) {
                configDir.mkdir();
            }

            File outputFile = new File(configDir, "copymenu_data.json");
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                gson.toJson(menuData, writer);
            }

            gui.mc.thePlayer.addChatMessage(new ChatComponentText("Menu copied to config/copymenu_data.json"));
        } catch (Exception e) {
            e.printStackTrace();
            gui.mc.thePlayer.addChatMessage(new ChatComponentText("Error copying menu: " + e.getMessage()));
        }
    }
}