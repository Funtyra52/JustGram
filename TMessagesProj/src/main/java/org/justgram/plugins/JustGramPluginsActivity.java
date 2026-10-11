package org.justgram.plugins;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;
import java.util.List;

import uz.unnarsx.cherrygram.preferences.BaseCGPreferencesEntry;

public class JustGramPluginsActivity extends BaseCGPreferencesEntry {

    private final int safeModeToggleRow = 1000;
    private final int clearCacheRow = 1001;

    @Override
    protected CharSequence getTitle() {
        return "Плагины (Extera Engine)";
    }

    @Override
    protected String getKey() {
        return "justgram_plugins_screen";
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Безопасный режим"));

        boolean safeMode = JustGramPluginManager.INSTANCE.isSafeMode();
        items.add(UItem.asCheck(safeModeToggleRow, "Безопасный режим (Safe Mode)")
                .setChecked(safeMode));

        if (safeMode) {
            items.add(UItem.asShadow(
                    "Безопасный режим активен. Все сторонние плагины временно отключены для защиты от сбоев. Отключите его для возобновления работы плагинов."
            ));
        } else {
            items.add(UItem.asShadow(
                    "Движок плагинов портирован из ExteraGram. В случае сбоя плагина при старте JustGram автоматически предложит войти в безопасный режим."
            ));
        }

        items.add(UItem.asHeader("Установленные плагины"));

        List<PluginMetadata> plugins = JustGramPluginManager.INSTANCE.getAllPlugins();
        if (plugins.isEmpty()) {
            items.add(UItem.asShadow("Нет загруженных плагинов"));
        } else {
            for (int i = 0; i < plugins.size(); i++) {
                PluginMetadata meta = plugins.get(i);
                int rowId = i + 1;
                String subtitle = "v" + meta.getVersion() + " • " + meta.getAuthor();
                if (meta.isQuarantined()) {
                    subtitle += " [СБОЙ: " + (meta.getLastError() != null ? meta.getLastError() : "Изолирован") + "]";
                }
                UItem checkItem = UItem.asCheck(rowId, meta.getName())
                        .setChecked(meta.isEnabled() && !meta.isQuarantined());
                checkItem.subtext = subtitle;
                items.add(checkItem);
            }
        }

        items.add(UItem.asHeader("Производительность UI"));
        UItem clearBtn = UItem.asButton(clearCacheRow, "Очистить кэш UI-хуков и рефлексии");
        clearBtn.subtext = "Сброс внутренних кэшей инъекций для освобождения памяти";
        items.add(clearBtn);
        items.add(UItem.asShadow(
                "Кэширование рефлексии и dirty-checking оптимизируют FPS при прокрутке списков с UI-плагинами."
        ));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == safeModeToggleRow) {
            boolean next = !JustGramPluginManager.INSTANCE.isSafeMode();
            JustGramPluginManager.INSTANCE.setSafeMode(next);
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
        } else if (item.id == clearCacheRow) {
            PluginUIHookManager.INSTANCE.clearCache();
            if (getParentActivity() != null) {
                android.widget.Toast.makeText(getParentActivity(), "Кэш UI очищен", android.widget.Toast.LENGTH_SHORT).show();
            }
        } else {
            List<PluginMetadata> plugins = JustGramPluginManager.INSTANCE.getAllPlugins();
            int idx = item.id - 1;
            if (idx >= 0 && idx < plugins.size()) {
                PluginMetadata meta = plugins.get(idx);
                boolean next = !meta.isEnabled();
                JustGramPluginManager.INSTANCE.togglePlugin(meta.getId(), next);
                if (listView != null && listView.adapter != null) {
                    listView.adapter.update(true);
                }
            }
        }
    }
}
