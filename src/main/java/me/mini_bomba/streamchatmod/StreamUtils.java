package me.mini_bomba.streamchatmod;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import me.mini_bomba.streamchatmod.asm.TransformerClass;
import me.mini_bomba.streamchatmod.asm.TransformerField;
import me.mini_bomba.streamchatmod.utils.ChatComponentTwitchMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int PURPLE = new Color(170, 0, 170).getRGB();
    public static final int GREEN = new Color(0, 255, 0).getRGB();
    public static final int RED = new Color(255, 0, 0).getRGB();
    public static final int BACKGROUND = new Color(0, 0, 0, 127).getRGB();

    // Reflection stuff for editing "private" & "final" fields on stuff
    // Vanilla fields
    private static final Field textComponentTextField = getVanillaAccessibleField(ChatComponentText.class, TransformerField.ChatComponentText_text);
    private static final Field guiNewChatChatLines = getVanillaAccessibleField(GuiNewChat.class, TransformerField.GuiNewChat_chatLines);
    // Vanilla Enhancements' fields
    private static final Field ve_BetterChatChatLines = getExternalAccessibleField(TransformerClass.VE_BetterChat, TransformerField.GuiNewChat_chatLines_deobf);
    private static final Field ve_BetterChatAllTab = getExternalAccessibleField(TransformerClass.VE_BetterChatWithTabs, TransformerField.VE_BetterChatWithTabs_allTab);
    private static final Field ve_ChatTabChatLines = getExternalAccessibleField(TransformerClass.VE_ChatTab, TransformerField.GuiNewChat_chatLines_deobf);

    private static Field getVanillaAccessibleField(Class<?> c, TransformerField f) {
        try {
            Field field = c.getDeclaredField(f.getReflectorName());
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to get DeclaredField '" + f.getReflectorName() + "' of " + c.getSimpleName() + " & set it accessible");
            e.printStackTrace();
            return null;
        }
    }

    private static Field getExternalAccessibleField(TransformerClass c, TransformerField f) {
        try {
            Class<?> c_reflection = Class.forName(c.getTransformerName());
            Field field = c_reflection.getDeclaredField(f.getReflectorName());
            field.setAccessible(true);
            return field;
        } catch (ClassNotFoundException ignored) {
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("Failed to get DeclaredField '" + f.getReflectorName() + "' from " + c.getTransformerName() + " & mark it accessible!");
            e.printStackTrace();
        }
        return null;
    }

    public static void addMessage(ICommandSender player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static void addMessage(EntityPlayerSP player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static void addMessage(EntityPlayerSP player, IChatComponent component) {
        player.addChatMessage(component);
    }

    public static void addMessage(ICommandSender player, IChatComponent component) {
        player.addChatMessage(component);
    }

    public static void addMessage(IChatComponent component) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) addMessage(player, component);
        }
    }

    public static void addMessage(String message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) addMessage(player, message);
        }
    }

    public static void addMessages(ICommandSender player, String[] messages) {
        for (String msg : messages) addMessage(player, msg);
    }

    public static void addMessages(EntityPlayerSP player, String[] messages) {
        for (String msg : messages) addMessage(player, msg);
    }

    public static void addMessages(String[] message) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) addMessages(player, message);
        }
    }

    public static void addMessages(ICommandSender player, IChatComponent[] components) {
        for (IChatComponent component : components) addMessage(player, component);
    }

    public static void addMessages(EntityPlayerSP player, IChatComponent[] components) {
        for (IChatComponent component : components) addMessage(player, component);
    }

    public static void addMessages(IChatComponent[] components) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) addMessages(player, components);
        }
    }

    public static String createPrefixedString(StreamConfig config, String message, @Nullable String channelName) {
        return (channelName == null ? config.getFullTwitchPrefix() : config.getTwitchPrefixWithChannel(channelName)) + " " + message;
    }

    public static String createPrefixedString(StreamConfig config, String message) {
        return config.getFullTwitchPrefix() + " " + message;
    }

    public static ChatComponentText createPrefixedComponent(StreamConfig config, String message, @Nullable String channelName) {
        return new ChatComponentText(createPrefixedString(config, message, channelName));
    }

    public static ChatComponentText createPrefixedComponent(StreamConfig config, String message) {
        return new ChatComponentText(createPrefixedString(config, message));
    }

    public static void addPrefixedMessage(StreamConfig config, String message, @Nullable String channelName) {
        addMessage(createPrefixedComponent(config, message, channelName));
    }

    public static void addPrefixedMessage(StreamConfig config, String message) {
        addMessage(createPrefixedComponent(config, message));
    }

    public static void queueAddPrefixedMessage(StreamConfig config, String message, @Nullable String channelName) {
        queueAddMessage(createPrefixedComponent(config, message, channelName));
    }

    public static void queueAddPrefixedMessage(StreamConfig config, String message) {
        queueAddMessage(createPrefixedComponent(config, message));
    }

    public static void addPrefixedMessages(StreamConfig config, String[] messages, @Nullable String channelName) {
        addMessages(Arrays.stream(messages).map(m -> createPrefixedComponent(config, m, channelName)).toArray(ChatComponentText[]::new));
    }

    public static void addPrefixedMessages(StreamConfig config, String[] messages) {
        addMessages(Arrays.stream(messages).map(m -> createPrefixedComponent(config, m)).toArray(ChatComponentText[]::new));
    }

    public static void queueAddPrefixedMessages(StreamConfig config, String[] messages, @Nullable String channelName) {
        queueAddMessages(Arrays.stream(messages).map(m -> createPrefixedComponent(config, m, channelName)).toArray(ChatComponentText[]::new));
    }

    public static void queueAddPrefixedMessages(StreamConfig config, String[] messages) {
        queueAddMessages(Arrays.stream(messages).map(m -> createPrefixedComponent(config, m)).toArray(ChatComponentText[]::new));
    }

    public static void queueAddMessage(String message) {
        Minecraft.getMinecraft().addScheduledTask(() -> addMessage(message));
    }

    public static void queueAddMessage(IChatComponent message) {
        Minecraft.getMinecraft().addScheduledTask(() -> addMessage(message));
    }

    public static void queueAddMessages(String[] messages) {
        Minecraft.getMinecraft().addScheduledTask(() -> addMessages(messages));
    }

    public static void queueAddMessages(IChatComponent[] components) {
        Minecraft.getMinecraft().addScheduledTask(() -> addMessages(components));
    }

    public static void queueRefreshChat() {
        Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().ingameGUI.getChatGUI().refreshChat());
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean editTextComponent(ChatComponentText component, String newText) {
        if (textComponentTextField == null) {
            LOGGER.warn("Failed to edit text component - static field lookup & set-accessible has failed");
            return false;
        }
        try {
            textComponentTextField.set(component, newText);
            return true;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to edit text component (exception below)");
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static List<ChatLine> getChatLines() {
        try {
            GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
            switch (chat.getClass().getName()) {
                case "com.orangemarshall.enhancements.modules.chat.BetterChat":
                    if (ve_BetterChatChatLines == null) return null;
                    return (List<ChatLine>) ve_BetterChatChatLines.get(chat);
                case "com.orangemarshall.enhancements.modules.chat.tab.BetterChatWithTabs":
                    if (ve_ChatTabChatLines == null || ve_BetterChatAllTab == null) return null;
                    return (List<ChatLine>) ve_ChatTabChatLines.get(ve_BetterChatAllTab.get(chat));
                default:
                    if (guiNewChatChatLines == null) return null;
                    return (List<ChatLine>) guiNewChatChatLines.get(chat);
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to get chatLines from GuiNewChat");
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteTwitchMessage(String messageId) {
        List<ChatLine> chatLines = getChatLines();
        if (chatLines == null) return;
        chatLines.removeIf(line -> {
            IChatComponent component = line.getChatComponent();
            return component instanceof ChatComponentTwitchMessage && messageId.equals(((ChatComponentTwitchMessage) component).messageId);
        });
        Minecraft.getMinecraft().ingameGUI.getChatGUI().refreshChat();
    }

    public static void queueDeleteTwitchMessage(String messageId) {
        Minecraft.getMinecraft().addScheduledTask(() -> deleteTwitchMessage(messageId));
    }

    public static void clearTwitchChat(String channelId) {
        List<ChatLine> chatLines = getChatLines();
        if (chatLines == null) return;
        chatLines.removeIf(line -> {
            IChatComponent component = line.getChatComponent();
            return component instanceof ChatComponentTwitchMessage && channelId.equals(((ChatComponentTwitchMessage) component).channelId);
        });
        Minecraft.getMinecraft().ingameGUI.getChatGUI().refreshChat();
    }

    public static void queueClearTwitchChat(String channelId) {
        Minecraft.getMinecraft().addScheduledTask(() -> clearTwitchChat(channelId));
    }

    public static void clearTwitchUserMessages(String channelId, String userId) {
        List<ChatLine> chatLines = getChatLines();
        if (chatLines == null) return;
        chatLines.removeIf(line -> {
            IChatComponent component = line.getChatComponent();
            return component instanceof ChatComponentTwitchMessage && channelId.equals(((ChatComponentTwitchMessage) component).channelId) && userId.equals(((ChatComponentTwitchMessage) component).userId);
        });
        Minecraft.getMinecraft().ingameGUI.getChatGUI().refreshChat();
    }

    public static void queueClearTwitchUserMessages(String channelId, String userId) {
        Minecraft.getMinecraft().addScheduledTask(() -> clearTwitchUserMessages(channelId, userId));
    }

    public static <T> List<T> singletonModifiableList(T obj) {
        return new ArrayList<>(Collections.singleton(obj));
    }

    public static void playSound(String sound, float volume, float pitch) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP player = mc.thePlayer;
            if (player != null) player.playSound(sound, volume, pitch);
        }
    }

    public static Boolean readStringAsBoolean(String str) {
        switch(str.toLowerCase()) {
            case "yes":
            case "true":
            case "1":
            case "y":
            case "enable":
            case "enabled":
            case "on":
                return true;
            case "false":
            case "no":
            case "0":
            case "n":
            case "disable":
            case "disabled":
            case "off":
                return false;
            default:
                return null;
        }
    }

    public static String capitalize(String str) {
        return str.substring(0,1).toUpperCase()+str.substring(1).toLowerCase();
    }

    @Nullable
    public static String getLatestVersion() {
        try {
            URL url = new URL("https://raw.githubusercontent.com/AquUpd/StreamChatMod/master/gradle.properties");
            Properties properties = new Properties();
            properties.load(url.openStream());
            return properties.getProperty("version");
        } catch (Exception e) {
            LOGGER.warn("Could not check for updates!");
            e.printStackTrace();
            return null;
        }
    }

    public static void drawChatWarning(GuiChat gui, int outlineColor, int backgroundColor, String warning) {
        drawChatOutline(gui, outlineColor);
        drawTextWithBackground(1, gui.height - 26, warning, outlineColor, backgroundColor);
    }

    public static void drawChatOutline(GuiChat gui, int color) {
        GuiScreen.drawRect(1, gui.height - 15, gui.width - 1, gui.height - 14, color);
        GuiScreen.drawRect(1, gui.height - 2, gui.width - 1, gui.height - 1, color);
        GuiScreen.drawRect(1, gui.height - 15, 2, gui.height - 1, color);
        GuiScreen.drawRect(gui.width - 2, gui.height - 15, gui.width - 1, gui.height - 1, color);
    }

    public static void drawTextWithBackground(int x, int y, String text, int textColor, int backgroundColor) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        GuiScreen.drawRect(x, y, x+fontRenderer.getStringWidth(text)+2, y+11, backgroundColor);
        fontRenderer.drawStringWithShadow(text, x+1, y+1, textColor);
    }

    public static class GitCommit {
        public final String hash;
        public final String shortHash;
        public final String message;
        public final String shortMessage;
        private static final Pattern shortMessagePattern = Pattern.compile("^.*");

        public GitCommit(String hash, String message) {
            this.hash = hash;
            shortHash = hash.substring(0, 8);
            this.message = message;
            Matcher matcher = shortMessagePattern.matcher(message);
            //noinspection ResultOfMethodCallIgnored
            matcher.find();
            shortMessage = matcher.group();
        }
    }

    @Nullable
    public static GitCommit getLatestCommit() {
        try {
            URL url = new URL("https://api.github.com/repos/AquUpd/StreamChatMod/commits/latest");
            Gson gson = new Gson();
            Map<?, ?> data = gson.fromJson(new InputStreamReader(url.openStream()), Map.class);
            return new GitCommit((String) data.get("sha"), (String) ((Map<?, ?>) data.get("commit")).get("message"));
        } catch (Exception e) {
            LOGGER.warn("Could not check for updates!");
            e.printStackTrace();
            return null;
        }
    }

    public static class TwitchOAuth2HandlerMain implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream file = getClass().getClassLoader().getResourceAsStream("token.html");
            byte[] readFile;
            int len;
            if (file == null) {
                readFile = "oof".getBytes();
                len = readFile.length;
            } else {
                readFile = new byte[file.available()];
                len = file.read(readFile);
                file.close();
            }
            exchange.sendResponseHeaders(200, len);
            OutputStream os = exchange.getResponseBody();
            os.write(readFile);
            exchange.close();
        }
    }

    public static class TwitchOAuth2HandlerSecondary implements HttpHandler {
        private final StreamChatMod mod;

        public TwitchOAuth2HandlerSecondary(StreamChatMod mod) {
            this.mod = mod;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = exchange.getRequestURI().getQuery();
            if (token != null) {
                mod.config.setTwitchToken(token);
                mod.config.twitchEnabled.set(true);
                mod.config.saveIfChanged();
                if (!mod.isImportantActionScheduled()) {
                    addMessage(EnumChatFormatting.GRAY + "Token set, restarting twitch chat...");
                    mod.asyncRestartTwitch();
                } else {
                    addMessage(EnumChatFormatting.RED + "There was an async action running, could not restart Twitch client. Run /twitch restart to finish setup.");
                }
            }
            exchange.sendResponseHeaders(token == null ? 400 : 200, 0);
            exchange.close();
            if (token == null) return;
            exchange.getHttpContext().getServer().stop(5);
            mod.httpServer = null;
            if (mod.httpShutdownScheduler != null) mod.httpShutdownScheduler.interrupt();
            mod.httpShutdownScheduler = null;
        }
    }

}
