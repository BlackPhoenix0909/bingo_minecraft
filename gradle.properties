package com.bingo.mod.client;

import com.bingo.mod.game.BingoCard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * The main Bingo Card GUI screen.
 * Displays a 5x5 grid of Minecraft item slots in classic Minecraft GUI style.
 * Collected items show a green checkmark overlay and strikethrough effect.
 */
public class BingoScreen extends Screen {

    // Minecraft GUI texture (the inventory/chest background)
    private static final Identifier WIDGETS_TEXTURE = new Identifier("textures/gui/widgets.png");

    // Layout constants
    private static final int CELL_SIZE = 36;       // Each cell is 36x36 pixels
    private static final int CELL_PADDING = 4;
    private static final int GRID_COLS = BingoCard.SIZE;
    private static final int GRID_ROWS = BingoCard.SIZE;
    private static final int GRID_TOTAL_W = GRID_COLS * (CELL_SIZE + CELL_PADDING) - CELL_PADDING;
    private static final int GRID_TOTAL_H = GRID_ROWS * (CELL_SIZE + CELL_PADDING) - CELL_PADDING;
    private static final int GUI_PADDING = 20;
    private static final int TITLE_HEIGHT = 30;
    private static final int BOTTOM_HEIGHT = 40;

    private static final int GUI_WIDTH = GRID_TOTAL_W + GUI_PADDING * 2;
    private static final int GUI_HEIGHT = GRID_TOTAL_H + GUI_PADDING * 2 + TITLE_HEIGHT + BOTTOM_HEIGHT;

    // Animation tracking
    private final Map<String, Long> animatingCells = new HashMap<>();
    private long openTime;

    public BingoScreen() {
        super(Text.translatable("bingo.card.title"));
    }

    @Override
    protected void init() {
        super.init();
        openTime = System.currentTimeMillis();

        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        // Close button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("✕ Schließen"),
            button -> this.close()
        ).dimensions(
            guiLeft + GUI_WIDTH / 2 - 60,
            guiTop + GUI_HEIGHT - BOTTOM_HEIGHT + 8,
            120, 20
        ).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim background
        this.renderBackground(context);

        int guiLeft = (this.width - GUI_WIDTH) / 2;
        int guiTop = (this.height - GUI_HEIGHT) / 2;

        // Draw main panel background (dark minecraft chest-style)
        drawPanel(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT);

        // Draw title
        String title = "✦ BINGO ✦";
        int titleX = guiLeft + GUI_WIDTH / 2 - textRenderer.getWidth(title) / 2;
        int titleY = guiTop + 8;
        // Shadow
        context.drawText(textRenderer, title, titleX + 1, titleY + 1, 0xFF_000000, false);
        // Gold gradient title
        context.drawText(textRenderer, title, titleX, titleY, 0xFF_FFD700, false);

        // Draw subtitle
        String subtitle = "Sammle Items – 3 in einer Reihe = Gewinn!";
        int subX = guiLeft + GUI_WIDTH / 2 - textRenderer.getWidth(subtitle) / 2;
        context.drawText(textRenderer, subtitle, subX, titleY + 14, 0xFF_AAAAAA, false);

        // Draw the bingo grid
        int gridLeft = guiLeft + GUI_PADDING;
        int gridTop = guiTop + GUI_PADDING + TITLE_HEIGHT;

        Item[][] grid = BingoClientMod.getClientGrid();
        boolean[][] collected = BingoClientMod.getClientCollected();

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int cellX = gridLeft + col * (CELL_SIZE + CELL_PADDING);
                int cellY = gridTop + row * (CELL_SIZE + CELL_PADDING);

                boolean isCenter = (row == BingoCard.FREE_CENTER && col == BingoCard.FREE_CENTER);
                boolean isColl = collected[row][col];
                Item item = grid[row][col];

                renderCell(context, cellX, cellY, item, isColl, isCenter, mouseX, mouseY);
            }
        }

        // Draw column letters (A-E) above grid
        String[] colLabels = {"A", "B", "C", "D", "E"};
        for (int col = 0; col < GRID_COLS; col++) {
            int cellX = gridLeft + col * (CELL_SIZE + CELL_PADDING) + CELL_SIZE / 2;
            context.drawText(textRenderer, colLabels[col],
                cellX - textRenderer.getWidth(colLabels[col]) / 2,
                gridTop - 12, 0xFF_FFAA00, false);
        }

        // Draw row numbers (1-5) to the left
        for (int row = 0; row < GRID_ROWS; row++) {
            int cellY = gridTop + row * (CELL_SIZE + CELL_PADDING) + CELL_SIZE / 2 - 4;
            String label = String.valueOf(row + 1);
            context.drawText(textRenderer, label,
                gridLeft - 12 - textRenderer.getWidth(label) / 2,
                cellY, 0xFF_FFAA00, false);
        }

        // Count collected (exclude center which is always collected)
        int totalCells = GRID_ROWS * GRID_COLS - 1; // -1 for free center
        int collectedCount = 0;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (!(r == BingoCard.FREE_CENTER && c == BingoCard.FREE_CENTER) && collected[r][c]) {
                    collectedCount++;
                }
            }
        }

        // Progress bar at bottom
        String progress = collectedCount + " / " + totalCells + " Items gesammelt";
        int progX = guiLeft + GUI_WIDTH / 2 - textRenderer.getWidth(progress) / 2;
        context.drawText(textRenderer, progress, progX, guiTop + GUI_HEIGHT - BOTTOM_HEIGHT + 4, 0xFF_AAFFAA, false);

        super.render(context, mouseX, mouseY, delta);

        // Render item tooltips on hover
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int cellX = gridLeft + col * (CELL_SIZE + CELL_PADDING);
                int cellY = gridTop + row * (CELL_SIZE + CELL_PADDING);
                boolean isCenter = (row == BingoCard.FREE_CENTER && col == BingoCard.FREE_CENTER);

                if (mouseX >= cellX && mouseX < cellX + CELL_SIZE &&
                    mouseY >= cellY && mouseY < cellY + CELL_SIZE) {

                    if (isCenter) {
                        context.drawTooltip(textRenderer, Text.literal("✦ FREI ✦"), mouseX, mouseY);
                    } else {
                        Item item = grid[row][col];
                        boolean isColl = collected[row][col];
                        String status = isColl ? " ✔ Gesammelt" : " ✗ Noch nicht";
                        context.drawTooltip(textRenderer, java.util.List.of(
                            item.getName().copy(),
                            Text.literal(status).formatted(
                                isColl ? net.minecraft.util.Formatting.GREEN : net.minecraft.util.Formatting.RED)
                        ), mouseX, mouseY);
                    }
                }
            }
        }
    }

    private void renderCell(DrawContext context, int x, int y, Item item, boolean collected, boolean isCenter, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + CELL_SIZE && mouseY >= y && mouseY < y + CELL_SIZE;

        // Cell background color
        int bgColor;
        if (isCenter) {
            bgColor = 0xFF_1A472A; // Dark green for FREE center
        } else if (collected) {
            bgColor = 0xFF_1C3A1C; // Darker green for collected
        } else if (hovered) {
            bgColor = 0xFF_3D3D3D; // Lighter for hover
        } else {
            bgColor = 0xFF_2A2A2A; // Default dark
        }

        // Draw cell background
        context.fill(x, y, x + CELL_SIZE, y + CELL_SIZE, bgColor);

        // Draw cell border
        int borderColor = isCenter ? 0xFF_32CD32 : (collected ? 0xFF_228B22 : 0xFF_555555);
        if (hovered) borderColor = 0xFF_FFD700;

        // Border: top, bottom, left, right
        context.fill(x, y, x + CELL_SIZE, y + 1, borderColor);
        context.fill(x, y + CELL_SIZE - 1, x + CELL_SIZE, y + CELL_SIZE, borderColor);
        context.fill(x, y, x + 1, y + CELL_SIZE, borderColor);
        context.fill(x + CELL_SIZE - 1, y, x + CELL_SIZE, y + CELL_SIZE, borderColor);

        if (isCenter) {
            // Draw "FREE" text centered
            String freeText = "FREE";
            int freeX = x + CELL_SIZE / 2 - textRenderer.getWidth(freeText) / 2;
            int freeY = y + CELL_SIZE / 2 - 4;
            context.drawText(textRenderer, "✦", x + CELL_SIZE / 2 - 3, freeY - 8, 0xFF_FFD700, false);
            context.drawText(textRenderer, freeText, freeX, freeY, 0xFF_32CD32, false);
        } else {
            // Draw item icon (16x16) centered in the cell
            int iconX = x + CELL_SIZE / 2 - 8;
            int iconY = y + CELL_SIZE / 2 - 8;
            context.drawItem(new net.minecraft.item.ItemStack(item), iconX, iconY);

            if (collected) {
                // Draw green checkmark overlay in corner
                context.fill(x + 1, y + 1, x + 10, y + 10, 0xAA_00FF00);
                context.drawText(textRenderer, "✔", x + 2, y + 2, 0xFF_FFFFFF, false);

                // Draw strikethrough line across cell
                int midY = y + CELL_SIZE / 2;
                context.fill(x + 2, midY - 1, x + CELL_SIZE - 2, midY + 1, 0xAA_FF4444);
            }
        }
    }

    private void drawPanel(DrawContext context, int x, int y, int w, int h) {
        // Outer border (golden)
        context.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0xFF_8B7536);
        // Main panel background
        context.fill(x, y, x + w, y + h, 0xFF_1D1D1D);
        // Inner highlight at top
        context.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFF_3A3A3A);
        context.fill(x + 1, y + 1, x + 2, y + h - 1, 0xFF_3A3A3A);
    }

    public void refreshCard() {
        // Called when card data is updated while screen is open
        // Screen auto-re-renders, so nothing special needed
    }

    public void playCollectAnimation(String itemId) {
        animatingCells.put(itemId, System.currentTimeMillis());
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause single-player when open
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
