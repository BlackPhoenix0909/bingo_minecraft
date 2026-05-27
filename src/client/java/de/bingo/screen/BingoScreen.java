package de.bingo.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BingoScreen extends Screen {

    private static final Identifier CHEST_GUI_TEXTURE =
        Identifier.of("minecraft", "textures/gui/container/generic_54.png");

    // Layout constants
    private static final int BOARD_SIZE = 3;
    private static final int CELL_SIZE = 54; // size of each cell in pixels
    private static final int BOARD_PIXEL_SIZE = BOARD_SIZE * CELL_SIZE; // 162px
    private static final int GUI_WIDTH = 260;
    private static final int GUI_HEIGHT = 240;

    // Colors (Minecraft palette)
    private static final int COLOR_BG_DARK        = 0xFF1D1D1D;
    private static final int COLOR_BG_PANEL       = 0xFF373737;
    private static final int COLOR_BORDER         = 0xFF555555;
    private static final int COLOR_BORDER_LIGHT   = 0xFFAAAAAA;
    private static final int COLOR_CELL_EMPTY     = 0xFF505050;
    private static final int COLOR_CELL_HOVER     = 0xFF636363;
    private static final int COLOR_CELL_COLLECTED = 0xFF2D5A1B;
    private static final int COLOR_CELL_COLLECTED_HOVER = 0xFF3A7322;
    private static final int COLOR_STRIKETHROUGH   = 0xAA55FF55;
    private static final int COLOR_TITLE_GOLD      = 0xFFFFD700;
    private static final int COLOR_WIN_OVERLAY     = 0xCC000000;
    private static final int COLOR_WIN_GOLD        = 0xFFFFD700;
    private static final int COLOR_TEXT_WHITE      = 0xFFFFFFFF;
    private static final int COLOR_TEXT_GRAY       = 0xFFAAAAAA;

    private Item[] board;
    private boolean[] progress;
    private String winnerName = null;

    // Animation
    private float winAnimTick = 0;
    private long openTime;
    private int hoveredCell = -1;

    public BingoScreen(Item[] board, boolean[] progress) {
        super(Text.literal("Bingo!"));
        this.board = board;
        this.progress = progress;
        this.openTime = System.currentTimeMillis();
    }

    public void refreshState(Item[] board, boolean[] progress) {
        this.board = board;
        this.progress = progress;
        this.winnerName = null;
        this.winAnimTick = 0;
    }

    public void refreshProgress(boolean[] progress) {
        this.progress = progress;
    }

    public void showWinner(String name) {
        this.winnerName = name;
        this.winAnimTick = 0;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw darkened background (Minecraft style)
        this.renderBackground(context, mouseX, mouseY, delta);

        long now = System.currentTimeMillis();
        float anim = Math.min(1.0f, (now - openTime) / 300.0f);

        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        // === MAIN PANEL ===
        // Outer border (lighter)
        context.fill(guiLeft - 2, guiTop - 2, guiLeft + GUI_WIDTH + 2, guiTop + GUI_HEIGHT + 2,
            COLOR_BORDER_LIGHT);
        // Inner border (dark)
        context.fill(guiLeft - 1, guiTop - 1, guiLeft + GUI_WIDTH + 1, guiTop + GUI_HEIGHT + 1,
            COLOR_BG_DARK);
        // Panel background
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT,
            COLOR_BG_PANEL);

        // === TITLE BAR ===
        int titleBarH = 28;
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + titleBarH, COLOR_BG_DARK);
        // Gold accent line under title
        context.fill(guiLeft, guiTop + titleBarH, guiLeft + GUI_WIDTH, guiTop + titleBarH + 2, 0xFFB8860B);

        // Title text
        String titleText = "§6§lBINGO §r§7- §eItem Sammel-Spiel";
        context.drawText(this.textRenderer, Text.literal("✦ BINGO ✦"), guiLeft + 10, guiTop + 10, COLOR_TITLE_GOLD, true);

        // Subtitle
        context.drawText(this.textRenderer, Text.literal("Sammle Items · 3 in einer Reihe = Sieg!"),
            guiLeft + 10, guiTop + 34, COLOR_TEXT_GRAY, false);

        // === BINGO BOARD ===
        int boardX = guiLeft + (GUI_WIDTH - BOARD_PIXEL_SIZE) / 2;
        int boardY = guiTop + 50;

        // Update hovered cell
        hoveredCell = -1;
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            int row = i / BOARD_SIZE;
            int col = i % BOARD_SIZE;
            int cellX = boardX + col * CELL_SIZE;
            int cellY = boardY + row * CELL_SIZE;
            if (mouseX >= cellX && mouseX < cellX + CELL_SIZE &&
                mouseY >= cellY && mouseY < cellY + CELL_SIZE) {
                hoveredCell = i;
            }
        }

        // Draw board border
        context.fill(boardX - 3, boardY - 3,
            boardX + BOARD_PIXEL_SIZE + 3, boardY + BOARD_PIXEL_SIZE + 3,
            0xFF222222);
        context.fill(boardX - 2, boardY - 2,
            boardX + BOARD_PIXEL_SIZE + 2, boardY + BOARD_PIXEL_SIZE + 2,
            0xFFAAAAAA);
        context.fill(boardX - 1, boardY - 1,
            boardX + BOARD_PIXEL_SIZE + 1, boardY + BOARD_PIXEL_SIZE + 1,
            0xFF333333);

        // Draw cells
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            int row = i / BOARD_SIZE;
            int col = i % BOARD_SIZE;
            int cellX = boardX + col * CELL_SIZE;
            int cellY = boardY + row * CELL_SIZE;

            boolean collected = progress != null && progress[i];
            boolean hovered = (hoveredCell == i);

            // Cell background
            int cellColor;
            if (collected) {
                cellColor = hovered ? COLOR_CELL_COLLECTED_HOVER : COLOR_CELL_COLLECTED;
            } else {
                cellColor = hovered ? COLOR_CELL_HOVER : COLOR_CELL_EMPTY;
            }
            context.fill(cellX + 1, cellY + 1, cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1, cellColor);

            // Cell border lines
            context.fill(cellX, cellY, cellX + CELL_SIZE, cellY + 1, COLOR_BORDER); // top
            context.fill(cellX, cellY + CELL_SIZE - 1, cellX + CELL_SIZE, cellY + CELL_SIZE, COLOR_BORDER); // bottom
            context.fill(cellX, cellY, cellX + 1, cellY + CELL_SIZE, COLOR_BORDER); // left
            context.fill(cellX + CELL_SIZE - 1, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, COLOR_BORDER); // right

            // Draw item
            if (board != null && i < board.length && board[i] != null) {
                ItemStack stack = new ItemStack(board[i]);
                int itemX = cellX + (CELL_SIZE - 16) / 2;
                int itemY = cellY + (CELL_SIZE - 16) / 2 - 4;
                context.drawItem(stack, itemX, itemY);

                // Item name (small)
                String name = board[i].getName().getString();
                if (name.length() > 8) name = name.substring(0, 7) + "…";
                int textColor = collected ? 0xFF88FF88 : 0xFFCCCCCC;
                // Small text under item
                context.getMatrices().push();
                context.getMatrices().translate(cellX + CELL_SIZE / 2f, cellY + CELL_SIZE - 10, 0);
                context.getMatrices().scale(0.6f, 0.6f, 1.0f);
                int textW = this.textRenderer.getWidth(name);
                context.drawText(this.textRenderer, Text.literal(name),
                    -textW / 2, 0, textColor, false);
                context.getMatrices().pop();
            }

            // Collected overlay: green checkmark + strikethrough diagonal
            if (collected) {
                // Semi-transparent green overlay
                context.fill(cellX + 1, cellY + 1,
                    cellX + CELL_SIZE - 1, cellY + CELL_SIZE - 1,
                    0x4400FF00);

                // Draw ✔ symbol
                context.drawText(this.textRenderer,
                    Text.literal("§a✔"),
                    cellX + CELL_SIZE - 12, cellY + 3, 0xFFFFFFFF, true);

                // Strikethrough diagonal lines (two lines for X)
                drawDiagonalLine(context,
                    cellX + 2, cellY + 2,
                    cellX + CELL_SIZE - 2, cellY + CELL_SIZE - 2,
                    COLOR_STRIKETHROUGH);
                drawDiagonalLine(context,
                    cellX + CELL_SIZE - 2, cellY + 2,
                    cellX + 2, cellY + CELL_SIZE - 2,
                    COLOR_STRIKETHROUGH);
            }

            // Hover tooltip
            if (hovered && board != null && i < board.length && board[i] != null) {
                context.drawItemTooltip(this.textRenderer, new ItemStack(board[i]), mouseX, mouseY);
            }
        }

        // === LEGEND ===
        int legendY = boardY + BOARD_PIXEL_SIZE + 10;

        // Progress count
        int collected = 0;
        if (progress != null) {
            for (boolean b : progress) if (b) collected++;
        }
        int total = BOARD_SIZE * BOARD_SIZE;

        context.drawText(this.textRenderer,
            Text.literal("§7Gesammelt: §e" + collected + "§7/§e" + total),
            guiLeft + 10, legendY, COLOR_TEXT_WHITE, false);

        // Legend icons
        context.fill(guiLeft + GUI_WIDTH - 110, legendY, guiLeft + GUI_WIDTH - 98, legendY + 9, COLOR_CELL_COLLECTED);
        context.drawText(this.textRenderer, Text.literal("§a= Gesammelt"),
            guiLeft + GUI_WIDTH - 96, legendY, COLOR_TEXT_GRAY, false);

        // Close hint
        context.drawText(this.textRenderer,
            Text.literal("§7[ESC] Schließen"),
            guiLeft + 10, guiTop + GUI_HEIGHT - 16, COLOR_TEXT_GRAY, false);

        // === WIN OVERLAY ===
        if (winnerName != null) {
            winAnimTick += delta * 2f;
            renderWinOverlay(context, guiLeft, guiTop, winnerName);
        }
    }

    private void renderWinOverlay(DrawContext context, int guiLeft, int guiTop, String winner) {
        // Darken everything
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, COLOR_WIN_OVERLAY);

        boolean isLocalPlayer = winner.equals(MinecraftClient.getInstance().player.getName().getString());

        // Pulsing gold border
        float pulse = (float)(Math.sin(winAnimTick * 0.15f) * 0.5f + 0.5f);
        int borderAlpha = (int)(128 + pulse * 127);
        int borderColor = (borderAlpha << 24) | 0xFFD700;

        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + 4, borderColor);
        context.fill(guiLeft, guiTop + GUI_HEIGHT - 4, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, borderColor);
        context.fill(guiLeft, guiTop, guiLeft + 4, guiTop + GUI_HEIGHT, borderColor);
        context.fill(guiLeft + GUI_WIDTH - 4, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, borderColor);

        // Stars
        String stars = "✦ ✦ ✦";

        // Main BINGO text
        String bingoText = isLocalPlayer ? "§6§lBINGO!" : "§c§lBINGO!";
        int bingoW = this.textRenderer.getWidth("BINGO!");
        context.getMatrices().push();
        context.getMatrices().translate(guiLeft + GUI_WIDTH / 2f, guiTop + GUI_HEIGHT / 2f - 30, 0);
        context.getMatrices().scale(2.5f, 2.5f, 1.0f);
        context.drawText(this.textRenderer, Text.literal(bingoText),
            -bingoW / 2, -8, COLOR_WIN_GOLD, true);
        context.getMatrices().pop();

        // Winner name
        String winMsg = isLocalPlayer ?
            "§a§lDu hast gewonnen!" :
            "§e" + winner + " §7hat gewonnen!";
        int winMsgW = this.textRenderer.getWidth(net.minecraft.text.Text.literal(winMsg));
        context.drawText(this.textRenderer, Text.literal(winMsg),
            guiLeft + GUI_WIDTH / 2 - winMsgW / 2,
            guiTop + GUI_HEIGHT / 2 + 5, COLOR_TEXT_WHITE, true);

        // Stars decoration
        int starsW = this.textRenderer.getWidth(stars);
        context.drawText(this.textRenderer, Text.literal("§6" + stars),
            guiLeft + GUI_WIDTH / 2 - starsW / 2,
            guiTop + GUI_HEIGHT / 2 - 52, COLOR_WIN_GOLD, true);
        context.drawText(this.textRenderer, Text.literal("§6" + stars),
            guiLeft + GUI_WIDTH / 2 - starsW / 2,
            guiTop + GUI_HEIGHT / 2 + 25, COLOR_WIN_GOLD, true);

        // Close hint
        context.drawText(this.textRenderer,
            Text.literal("§7[ESC] Schließen"),
            guiLeft + GUI_WIDTH / 2 - 38, guiTop + GUI_HEIGHT - 20, COLOR_TEXT_GRAY, false);
    }

    /**
     * Draws a 1-pixel-wide diagonal line using filled 1x1 rects.
     */
    private void drawDiagonalLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps == 0) return;
        for (int s = 0; s <= steps; s++) {
            int x = x1 + (x2 - x1) * s / steps;
            int y = y1 + (y2 - y1) * s / steps;
            context.fill(x, y, x + 2, y + 2, color);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // ESC closes
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
