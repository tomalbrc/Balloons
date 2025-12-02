package de.tomalbrc.balloons.configui.api;

import de.tomalbrc.balloons.util.TextUtil;
import de.tomalbrc.balloons.util.Util;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ConfiguredGui<T extends GuiElementData, E> extends SimpleGui {
    protected final String guiId;
    protected final GuiData<T> data;

    protected final Map<String, List<Integer>> regionSlots = new LinkedHashMap<>();
    protected final Map<String, Integer> currentPage = new HashMap<>();

    public ConfiguredGui(String guiId, GuiData<T> data, ServerPlayer player, boolean manipulatePlayerSlots) {
        super(Util.menuTypeForHeight(data.layout().size()), player, manipulatePlayerSlots);
        this.guiId = guiId;
        this.data = data;
        build();
    }

    protected void build() {
        this.setTitle(TextUtil.parse(data.title()));
        scanLayout();
        buildStaticAndPaged();
    }

    public final String getGuiId() {
        return guiId;
    }

    protected List<E> getElementsForType(String typeName, ServerPlayer player) {
        return Collections.emptyList();
    }

    private void scanLayout() {
        regionSlots.clear();
        List<String> layout = data.layout();
        int cols = getWidth();
        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                char ch = line.charAt(col);
                GuiElementData el = data.keys().get(ch);
                if (el == null) continue;
                String typeName = el.type();
                int absIdx = row * cols + col;
                regionSlots.computeIfAbsent(typeName, k -> new ArrayList<>()).add(absIdx);
            }
        }
    }

    private void buildStaticAndPaged() {
        int cols = getWidth();
        List<String> layout = data.layout();

        for (int row = 0; row < layout.size(); row++) {
            String line = layout.get(row);
            for (int col = 0; col < line.length(); col++) {
                char ch = line.charAt(col);
                int slotIdx = row * cols + col;
                T el = data.keys().get(ch);
                if (el == null) {
                    setSlot(slotIdx, new GuiElementBuilder(ItemStack.EMPTY));
                    continue;
                }

                GuiElementType<T, E> handler = GuiTypeRegistry.get(guiId, el.type());
                if (handler == null) {
                    clearSlot(slotIdx);
                    continue;
                }

                if (handler instanceof ListGuiElementType) {
                    clearSlot(slotIdx);
                } else {
                    GuiElementBuilder b = handler.build(this, el);
                    setSlot(slotIdx, (b == null) ? new GuiElementBuilder(ItemStack.EMPTY) : b);
                }
            }
        }

        for (Map.Entry<String, List<Integer>> e : regionSlots.entrySet()) {
            String typeName = e.getKey();
            GuiElementType<T, E> handler = GuiTypeRegistry.get(guiId, typeName);
            if (handler instanceof ListGuiElementType<T, E> listHandler) {
                populatePagedRegion(typeName, listHandler);
            }
        }
    }

    private void populatePagedRegion(String typeName, ListGuiElementType<T, E> handler) {
        List<Integer> slots = regionSlots.getOrDefault(typeName, Collections.emptyList());
        if (slots.isEmpty()) return;

        GuiElementData sample = findAnyElementDataForType(typeName);
        if (sample == null) return;

        List<E> elements = getElementsForType(typeName, getPlayer());
        if (elements == null) elements = Collections.emptyList();

        int perPage = slots.size();
        int page = currentPage.getOrDefault(typeName, 0);

        // clamp page if it points past the last page
        int maxPage = Math.max(0, Math.floorDiv(Math.max(0, elements.size() - 1), perPage));
        if (page > maxPage) {
            page = maxPage;
            currentPage.put(typeName, page);
        }

        for (int pos = 0; pos < slots.size(); pos++) {
            int slotIndex = slots.get(pos);
            int elementIndex = page * perPage + pos;
            E element = elementIndex < elements.size() ? elements.get(elementIndex) : null;
            GuiElementBuilder builder = element == null ? null : handler.buildEntry(this, sample, element);
            if (builder != null) setSlot(slotIndex, builder.build());
            else clearSlot(slotIndex);
        }
    }

    private GuiElementData findAnyElementDataForType(String typeName) {
        for (GuiElementData d : data.keys().values()) {
            if (typeName.equals(d.type())) return d;
        }
        return null;
    }

    public void nextPage(String typeName) {
        Util.clickSound(this.getPlayer());

        List<Integer> slots = regionSlots.getOrDefault(typeName, Collections.emptyList());
        if (slots.isEmpty()) return;

        List<?> elements = getElementsForType(typeName, getPlayer());
        if (elements == null) elements = Collections.emptyList();

        int perPage = slots.size();
        int page = currentPage.getOrDefault(typeName, 0);
        int maxPage = Math.max(0, Math.floorDiv(Math.max(0, elements.size() - 1), perPage));

        if (page < maxPage) {
            currentPage.put(typeName, page + 1);
            // only re-populate that region to avoid rebuilding entire GUI
            GuiElementType<T, E> handler = GuiTypeRegistry.get(guiId, typeName);
            if (handler instanceof ListGuiElementType<T, E> listHandler) populatePagedRegion(typeName, listHandler);
            Util.clickSound(getPlayer());
        }
    }

    public void previousPage(String typeName) {
        Util.clickSound(this.getPlayer());

        int page = currentPage.getOrDefault(typeName, 0);
        if (page <= 0) return;
        List<Integer> slots = regionSlots.getOrDefault(typeName, Collections.emptyList());
        if (slots.isEmpty()) return;

        currentPage.put(typeName, page - 1);
        GuiElementType<T, E> handler = GuiTypeRegistry.get(guiId, typeName);
        if (handler instanceof ListGuiElementType<T, E> listHandler) populatePagedRegion(typeName, listHandler);
        Util.clickSound(getPlayer());
    }

    public void setPage(String typeName, int pageIndex) {
        List<Integer> slots = regionSlots.getOrDefault(typeName, Collections.emptyList());
        if (slots.isEmpty()) return;

        List<?> elements = getElementsForType(typeName, getPlayer());
        if (elements == null) elements = Collections.emptyList();

        int perPage = slots.size();
        int maxPage = Math.max(0, Math.floorDiv(Math.max(0, elements.size() - 1), perPage));
        int clamped = Math.max(0, Math.min(pageIndex, maxPage));
        currentPage.put(typeName, clamped);

        GuiElementType<T, E> handler = GuiTypeRegistry.get(guiId, typeName);
        if (handler instanceof ListGuiElementType<T, E> listHandler) populatePagedRegion(typeName, listHandler);
    }

    public int maxPage(String typeName) {
        List<Integer> slots = regionSlots.getOrDefault(typeName, Collections.emptyList());
        if (slots.isEmpty()) return 0;

        List<?> elements = getElementsForType(typeName, getPlayer());
        if (elements == null) elements = Collections.emptyList();

        int perPage = slots.size();
        return Math.max(0, Math.floorDiv(Math.max(0, elements.size() - 1), perPage));
    }

    public int getCurrentPage(String typeName) {
        return currentPage.getOrDefault(typeName, 0);
    }

    public int getPageSize(String typeName) {
        return regionSlots.getOrDefault(typeName, Collections.emptyList()).size();
    }

    public void back() {
        Util.clickSound(this.getPlayer());
        this.close();
    }
}
