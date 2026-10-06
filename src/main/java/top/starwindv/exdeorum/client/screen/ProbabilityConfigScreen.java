/**
 * Copyleft (c) 2026 StarWindv
 * SPDX-License-Identifier: GPL-3-Clause
 */

package top.starwindv.exdeorum.client.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import top.starwindv.exdeorum.config.ProbabilityConfig;
import top.starwindv.exdeorum.recipe.ProbabilityRecipe;
import top.starwindv.exdeorum.recipe.ProbabilityTuner;
import top.starwindv.exdeorum.recipe.crook.CrookRecipe;
import top.starwindv.exdeorum.recipe.sieve.SieveRecipe;
import top.starwindv.exdeorum.util.TranslationKeys;

/**
 * Drop rate editor, opened from ModMenu's config button.
 * <p>
 * Rows come from the loaded recipes rather than a hand written list, so data pack added
 * drops appear here without any code change. Only values the player changed are written back
 * to {@code config/exdeorum-probabilities.json}; anything absent keeps the number the recipe
 * itself asks for.
 * <p>
 * A single {@link EditBox} is moved over whichever row is selected instead of giving every
 * one of the ~1700 rows its own widget, which would be wasteful given the list is
 * virtualised. Values are typed rather than dragged, so there is no snapping ambiguity.
 */
public class ProbabilityConfigScreen extends Screen {
private static final int ROW_HEIGHT = 20;
    private static final int LIST_TOP = 32;
    /** Status line plus the button row, plus the gaps around them. */
    private static final int LIST_BOTTOM = 48;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ITEM_SIZE = 16;
    private static final int VALUE_WIDTH = 52;
    private static final int RESET_WIDTH = 34;
    private static final int ROLLS_WIDTH = 20;
    private static final int MARGIN = 6;
    private static final int GAP = 5;
    private static final int MIN_LABEL_WIDTH = 40;
    private static final int SCROLLER_WIDTH = 10;

    private static final int COLOUR_LABEL = 0xFFFFFFFF;
    private static final int COLOUR_VALUE = 0xFFE0E0E0;
    private static final int COLOUR_ROLLS = 0xFF909090;
    private static final int COLOUR_HINT = 0xFFAAAAAA;
    private static final int COLOUR_ERROR = 0xFFFF5555;
    private static final int COLOUR_MODIFIED = 0x30FFC040;

    private final Screen parent;
    private final List<ProbabilityTuner.Entry> entries = new ArrayList<>();
    private final List<ProbabilityTuner.Entry> visible = new ArrayList<>();

    @Nullable
    private EditBox search;
    @Nullable
    private EditBox value;
    @Nullable
    private ProbabilityTuner.Entry selected;
    @Nullable
    private Component error;
    private String query = "";
    private int scroll;
    private int contentHeight;
    private boolean onlyModified;
    private boolean dirty;

    public ProbabilityConfigScreen(Screen parent) {
        super(Component.translatable(TranslationKeys.PROBABILITY_CONFIG_TITLE));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.entries.clear();
        this.entries.addAll(ProbabilityTuner.entries());
        this.entries.sort(Comparator.comparing(e -> e.id().identifier().toString()));
        this.selected = null;
        this.error = null;
        refilter();

        this.search = new EditBox(this.font, MARGIN, 10, Math.max(80, this.width / 2 - 90), 16,
                Component.translatable(TranslationKeys.PROBABILITY_CONFIG_SEARCH));
        this.search.setMaxLength(128);
        this.search.setValue(this.query);
        this.search.setResponder(value -> {
            this.query = value;
            this.scroll = 0;
            refilter();
        });
        addRenderableWidget(this.search);

        var toggle = addRenderableWidget(Button.builder(label(TranslationKeys.PROBABILITY_CONFIG_MODIFIED_ONLY), button -> {
            this.onlyModified = !this.onlyModified;
            this.scroll = 0;
            refilter();
            deselect(false);
        }).bounds(this.width - MARGIN - 96, 10, 96, 16).build());
        toggle.setMessage(label(TranslationKeys.PROBABILITY_CONFIG_MODIFIED_ONLY));

// Added as a real widget so focus, key handling and rendering go through the normal
        // widget pipeline. It is moved over whichever row is selected instead of giving all
        // ~1700 rows their own EditBox.
        this.value = new EditBox(this.font, 0, 0, VALUE_WIDTH, 14, Component.empty());
        this.value.setMaxLength(8);
        this.value.setHint(Component.translatable(TranslationKeys.PROBABILITY_CONFIG_ENTER_VALUE));
        addRenderableWidget(this.value);

        addRenderableWidget(Button.builder(label(TranslationKeys.PROBABILITY_CONFIG_RESET_SELECTED), button -> resetSelected())
                .bounds(MARGIN, buttonTop(), 110, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(label(TranslationKeys.PROBABILITY_CONFIG_RESET_ALL), button -> {
            ProbabilityConfig.clearAll();
            this.dirty = true;
            deselect(false);
            refilter();
        }).bounds(MARGIN + 114, buttonTop(), 86, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(label(TranslationKeys.PROBABILITY_CONFIG_DONE), button -> onClose())
                .bounds(this.width - MARGIN - 66, buttonTop(), 66, BUTTON_HEIGHT).build());
    }

    private int buttonTop() {
        return this.height - MARGIN - BUTTON_HEIGHT;
    }

    private int statusTop() {
        return buttonTop() - 12;
    }

    private static Component label(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private void refilter() {
        this.visible.clear();
        var needle = this.query.trim().toLowerCase(Locale.ROOT);

        for (var entry : this.entries) {
            if (this.onlyModified && !entry.modified()) {
                continue;
            }

            if (!needle.isEmpty() && !matches(entry, needle)) {
                continue;
            }

            this.visible.add(entry);
        }

        this.contentHeight = this.visible.size() * ROW_HEIGHT;
        this.scroll = Mth.clamp(this.scroll, 0, maxScroll());
    }

    /** Matches on the recipe id and on the translated names of everything involved. */
    private boolean matches(ProbabilityTuner.Entry entry, String needle) {
        if (entry.id().identifier().toString().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }

        var recipe = entry.recipe();

        if (recipe instanceof SieveRecipe sieve) {
            return matchesAny(sieve.result().item().value(), needle)
                    || matchesAny(sieve.ingredient(), needle)
                    || matchesAny(sieve.mesh(), needle);
        }

        if (recipe instanceof ProbabilityRecipe probability) {
            return matchesAny(probability.result().item().value(), needle)
                    || matchesAny(probability.ingredient(), needle);
        }

        if (recipe instanceof CrookRecipe crook) {
            return matchesAny(crook.result().item().value(), needle);
        }

        return false;
    }

    private boolean matchesAny(Ingredient ingredient, String needle) {
        return ingredient.items().anyMatch(holder -> matchesAny(holder.value(), needle));
    }

    private static boolean matchesAny(Item item, String needle) {
        return item.getDefaultInstance().getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle);
    }

    private int listHeight() {
        return this.height - LIST_TOP - LIST_BOTTOM;
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - listHeight());
    }

    private int rowRight() {
        return this.width - MARGIN - SCROLLER_WIDTH;
    }

    private int resetLeft() {
        return rowRight() - RESET_WIDTH;
    }

    private int valueLeft() {
        return resetLeft() - GAP - VALUE_WIDTH;
    }

    private int rollsLeft() {
        return valueLeft() - GAP - ROLLS_WIDTH;
    }

    private int labelLeft() {
        return MARGIN + ITEM_SIZE + GAP;
    }

/** Usable text width for a row, reserving the multiplier column only when it is used. */
    private int labelWidth(boolean hasRolls) {
        var right = hasRolls ? rollsLeft() : valueLeft() - GAP;
        return Math.max(MIN_LABEL_WIDTH, right - labelLeft() - GAP);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Park the row editor where it belongs BEFORE the widget pass renders it; set after,
        // it would draw at the previous frame's spot and lag behind the list while scrolling.
        var editing = this.selected != null && isRowVisible(this.selected);
        this.value.setVisible(editing);

        if (editing) {
            this.value.setX(valueLeft() - 1);
            this.value.setY(rowTop(this.selected) + 3);
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(this.font, this.title, this.width / 2, 1, COLOUR_LABEL);

        var top = LIST_TOP;
        var bottom = top + listHeight();
        graphics.fill(2, top, this.width - 2, bottom, 0x80000000);

        var first = Math.max(0, this.scroll / ROW_HEIGHT);
        var last = Math.min(this.visible.size(), first + listHeight() / ROW_HEIGHT + 2);

        for (var i = first; i < last; i++) {
            var y = top + i * ROW_HEIGHT - this.scroll;

            if (y + ROW_HEIGHT < top || y > bottom) {
                continue;
            }

            renderRow(graphics, this.visible.get(i), y);
        }

        if (this.visible.isEmpty()) {
            graphics.centeredText(this.font, label(TranslationKeys.PROBABILITY_CONFIG_EMPTY), this.width / 2, top + 10, COLOUR_HINT);
        }

        renderScroller(graphics, top, bottom);

        // Status line, kept clear of the buttons below it.
        var status = this.error != null ? this.error
                : this.dirty ? label(TranslationKeys.PROBABILITY_CONFIG_UNSAVED)
                : label(TranslationKeys.PROBABILITY_CONFIG_COUNT, this.visible.size(), this.entries.size());
        graphics.text(this.font, Component.literal(this.font.plainSubstrByWidth(status.getString(), this.width - 2 * MARGIN)), MARGIN, statusTop(),
                this.error != null ? COLOUR_ERROR : COLOUR_HINT, false);

        if (mouseY > top && mouseY < bottom) {
            var entry = entryAt(mouseX, mouseY);

            if (entry != null && entry != this.selected) {
                var tooltip = Component.empty()
                        .append(Component.literal(entry.id().identifier().toString()))
                        .append(Component.literal("\n"))
                        .append(Component.translatable(TranslationKeys.PROBABILITY_CONFIG_DEFAULT, format(entry.defaultValue())))
                        .append(Component.literal("\n"))
                        .append(Component.translatable(TranslationKeys.PROBABILITY_CONFIG_EXPECTED,
                                String.format(Locale.ROOT, "%.3f", entry.expected())));
                graphics.setTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
            }
        }
    }

private void renderRow(GuiGraphicsExtractor graphics, ProbabilityTuner.Entry entry, int y) {
        if (entry == this.selected) {
            // A thin outline rather than a fill; a fill made the whole row read as white and
            // swallowed the value being edited.
            var edge = entry.modified() ? 0xFF9BE07A : 0xFFFFFFFF;
            graphics.fill(MARGIN - 2, y, rowRight(), y, edge);
            graphics.fill(MARGIN - 2, y + ROW_HEIGHT - 1, rowRight(), y + ROW_HEIGHT, edge);
            graphics.fill(MARGIN - 2, y, MARGIN - 1, y + ROW_HEIGHT, edge);
            graphics.fill(rowRight(), y, rowRight() + 1, y + ROW_HEIGHT, edge);
        } else if (entry.modified()) {
            graphics.fill(MARGIN - 2, y, rowRight(), y + ROW_HEIGHT - 1, COLOUR_MODIFIED);
        }

        var stack = itemStackOf(entry);

        if (stack != null) {
            graphics.item(stack, MARGIN, y + 2);
        }

        graphics.text(this.font, entryLabel(entry, entry.rolls() > 1), labelLeft(), y + 6, COLOUR_LABEL, false);

        // Only rows that roll more than once need the multiplier column, so rows without one
        // get the extra space for their text instead of wasting it.
        if (entry.rolls() > 1) {
            graphics.text(this.font, Component.literal("x" + entry.rolls()), rollsLeft(), y + 6, COLOUR_ROLLS, false);
        }

        if (entry != this.selected) {
            graphics.text(this.font, format(entry.current()), valueLeft(), y + 6,
                    entry.modified() ? 0xFF9BE07A : COLOUR_VALUE, false);
        }

        graphics.text(this.font, label(TranslationKeys.PROBABILITY_CONFIG_RESET_SHORT), resetLeft(), y + 6,
                entry.modified() ? 0xFFFFCC55 : 0xFF555555, false);
    }

    /**
     * Builds {@code input / mesh → output}, trimming the input and mesh so the result item is
     * always the part that stays visible when the window is narrow.
     */
    private Component entryLabel(ProbabilityTuner.Entry entry, boolean hasRolls) {
        var recipe = entry.recipe();
        String input = null;
        String mesh = null;
        ItemStack output = null;

        if (recipe instanceof SieveRecipe sieve) {
            input = firstName(sieve.ingredient());
            mesh = firstName(sieve.mesh());
            output = sieve.result().create();
        } else if (recipe instanceof ProbabilityRecipe probability) {
            input = firstName(probability.ingredient());
            output = probability.result().create();
        } else if (recipe instanceof CrookRecipe crook) {
            output = crook.result().create();
        }

        var outputName = output == null ? "?" : output.getHoverName().getString();
        var available = labelWidth(hasRolls);

        if (input == null) {
            return Component.literal(ellipsise(outputName, available));
        }

        var prefix = mesh == null ? input : input + " / " + mesh;
        var room = available - this.font.width(" → " + outputName) - this.font.width("…");

        if (room <= 0) {
            return Component.literal(ellipsise(outputName, available));
        }

        return Component.literal(ellipsise(prefix, room) + " → " + outputName);
    }

    private String ellipsise(String text, int width) {
        if (width <= 0) {
            return "";
        }

        if (this.font.width(text) <= width) {
            return text;
        }

        return this.font.plainSubstrByWidth(text, Math.max(0, width - this.font.width("…"))) + "…";
    }

    private void renderScroller(GuiGraphicsExtractor graphics, int top, int bottom) {
        if (maxScroll() <= 0) {
            return;
        }

        var trackHeight = listHeight();
        var thumbHeight = Math.max(18, (int) ((long) trackHeight * trackHeight / this.contentHeight));
        var thumbTop = top + (trackHeight - thumbHeight) * this.scroll / maxScroll();
        graphics.fill(this.width - 2 - SCROLLER_WIDTH, top, this.width - 2, bottom, 0x30000000);
        graphics.fill(this.width - 2 - SCROLLER_WIDTH, thumbTop, this.width - 2, thumbTop + thumbHeight, 0xFF909090);
    }

    private static String format(float value) {
        if (value <= 0.0f) {
            return "0";
        }

        if (value >= 1.0f) {
            return "1";
        }

        return String.format(Locale.ROOT, "%.3f", value);
    }

    @Nullable
    private static ItemStack itemStackOf(ProbabilityTuner.Entry entry) {
        var recipe = entry.recipe();

        if (recipe instanceof ProbabilityRecipe probability) {
            return probability.result().create();
        }

        if (recipe instanceof CrookRecipe crook) {
            return crook.result().create();
        }

        return null;
    }

private static String firstName(Ingredient ingredient) {
        return ingredient.items()
                .map(holder -> holder.value().getDefaultInstance().getHoverName().getString())
                .findFirst()
                .orElse("?");
    }

    private int rowTop(ProbabilityTuner.Entry entry) {
        var index = this.visible.indexOf(entry);
        return index < 0 ? -1 : LIST_TOP + index * ROW_HEIGHT - this.scroll;
    }

    private boolean isRowVisible(ProbabilityTuner.Entry entry) {
        var y = rowTop(entry);
        return y >= LIST_TOP && y + ROW_HEIGHT <= LIST_TOP + listHeight();
    }

    @Nullable
    private ProbabilityTuner.Entry entryAt(double mouseX, double mouseY) {
        if (mouseY < LIST_TOP || mouseY > LIST_TOP + listHeight() || mouseX < MARGIN || mouseX > rowRight()) {
            return null;
        }

        var index = (int) ((mouseY - LIST_TOP + this.scroll) / ROW_HEIGHT);
        return index >= 0 && index < this.visible.size() ? this.visible.get(index) : null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (maxScroll() > 0 && event.x() >= this.width - 2 - SCROLLER_WIDTH && event.y() >= LIST_TOP
                && event.y() <= LIST_TOP + listHeight()) {
            this.draggingScrollbar = true;
            scrollToPointer(event.y());
            return true;
        }

        var entry = entryAt(event.x(), event.y());

        if (entry != null) {
            if (event.x() >= resetLeft()) {
                reset(entry);
                return true;
            }

            if (event.x() >= valueLeft() - GAP / 2) {
                select(entry);
            }

            return true;
        }

        // The buttons and the search box only receive their clicks through the normal widget
        // dispatch, so hand the click to the widgets first; clicking anything else focuses
        // the search box, letting the player type a filter without aiming at the small field.
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }

        if (this.search != null) {
            setFocused(this.search);
        }

        return true;
    }

    private boolean draggingScrollbar;

    private void scrollToPointer(double mouseY) {
        var trackHeight = listHeight();
        var fraction = Mth.clamp((float) (mouseY - LIST_TOP - 9) / Math.max(1, trackHeight - 18), 0.0f, 1.0f);
        this.scroll = Mth.clamp((int) (fraction * maxScroll()), 0, maxScroll());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingScrollbar) {
            scrollToPointer(event.y());
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scroll = Mth.clamp(this.scroll - (int) (scrollY * ROW_HEIGHT), 0, maxScroll());
        return true;
    }

private void select(ProbabilityTuner.Entry entry) {
        commit();
        this.selected = entry;
        this.error = null;

        if (this.value != null) {
            this.value.setValue(format(entry.current()));
            this.value.setTextColor(COLOUR_VALUE);
            this.value.setEditable(true);
            // Route focus through the screen so the search box lets go of it.
            setFocused(this.value);
        }
    }

    private void deselect(boolean keepError) {
        commit();
        this.selected = null;

        if (!keepError) {
            this.error = null;
        }

        if (this.value != null) {
            // Clear the screen's focus pointer too, not just the widget's own flag, or the
            // screen would keep routing keys and IME input to a hidden editor.
            if (getFocused() == this.value) {
                setFocused(null);
            }

            this.value.setFocused(false);
            this.value.setVisible(false);
        }
    }

    private void resetSelected() {
        if (this.selected != null) {
            reset(this.selected);
        }
    }

    private void reset(ProbabilityTuner.Entry entry) {
        ProbabilityConfig.clear(entry.id());
        this.dirty = true;
        this.error = null;

        if (this.value != null && entry == this.selected) {
            this.value.setValue(format(entry.current()));
        }
    }

    /**
     * Parses what the player typed. Anything that is not a number in {@code [0, 1]} is
     * rejected with a message rather than being silently clamped, so a typo cannot quietly
     * change a drop rate.
     */
    private void commit() {
        if (this.selected == null || this.value == null) {
            return;
        }

        var text = this.value.getValue().trim();

        if (text.isEmpty()) {
            this.error = Component.translatable(TranslationKeys.PROBABILITY_CONFIG_ERR_EMPTY);
            this.value.setTextColor(COLOUR_ERROR);
            return;
        }

        float parsed;

        try {
            parsed = Float.parseFloat(text);
        } catch (NumberFormatException e) {
            this.error = Component.translatable(TranslationKeys.PROBABILITY_CONFIG_ERR_NUMBER, text);
            this.value.setTextColor(COLOUR_ERROR);
            return;
        }

        if (!Float.isFinite(parsed) || parsed < 0.0f || parsed > 1.0f) {
            this.error = Component.translatable(TranslationKeys.PROBABILITY_CONFIG_ERR_RANGE, text);
            this.value.setTextColor(COLOUR_ERROR);
            return;
        }

        this.error = null;
        this.value.setTextColor(0xE0E0E0);
        this.value.setValue(format(parsed));

        if (Math.abs(parsed - this.selected.defaultValue()) > 1.0e-6f) {
            ProbabilityConfig.set(this.selected.id(), parsed);
            this.dirty = true;
        } else {
            // Back to the shipped number, so no override is written for it.
            ProbabilityConfig.clear(this.selected.id());
        }
    }

@Override
    public boolean keyPressed(KeyEvent event) {
        if (this.selected != null && this.value != null && this.value.isFocused()) {
            var key = event.key();

            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER || key == GLFW.GLFW_KEY_TAB) {
                commit();

                // Confirm and close: the row takes over and shows the committed value as text,
                // so nothing is left sitting in a focused editor. A rejected value keeps the
                // editor open with its error message so the typo can be corrected.
                if (this.error == null) {
                    deselect(false);
                }

                return true;
            }

            if (key == GLFW.GLFW_KEY_ESCAPE) {
                // Put the stored value back and drop the selection without writing.
                this.value.setValue(format(this.selected.current()));
                this.error = null;
                this.value.setTextColor(COLOUR_VALUE);
                deselect(false);
                return true;
            }

            // The widget gets the key either way; typing means the player is correcting a
            // value we rejected, so drop the complaint and the red text.
            this.value.keyPressed(event);
            this.error = null;
            this.value.setTextColor(COLOUR_VALUE);
            return true;
        }

        if (event.key() == GLFW.GLFW_KEY_DOWN || event.key() == GLFW.GLFW_KEY_UP) {
            moveSelection(event.key() == GLFW.GLFW_KEY_DOWN ? 1 : -1);
            return true;
        }

        return super.keyPressed(event);
    }

    /** Arrow keys walk the visible rows so the editor can be used without a mouse. */
    private void moveSelection(int delta) {
        if (this.visible.isEmpty()) {
            return;
        }

        var current = this.selected == null ? -1 : this.visible.indexOf(this.selected);
        var next = Mth.clamp(current + delta, 0, this.visible.size() - 1);
        select(this.visible.get(next));

        var top = rowTop(this.selected);
        var height = listHeight();

        if (top < this.scroll) {
            this.scroll = top;
        } else if (top + ROW_HEIGHT > this.scroll + height) {
            this.scroll = top + ROW_HEIGHT - height;
        }

        this.scroll = Mth.clamp(this.scroll, 0, maxScroll());
    }

    @Override
    public void onClose() {
        commit();

        if (this.dirty) {
            ProbabilityTuner.saveAndReloadRecipes();
            this.dirty = false;
        }

        this.minecraft.setScreenAndShow(this.parent);
    }
}