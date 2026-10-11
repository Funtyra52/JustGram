package org.justgram.ghost;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.preferences.BaseCGPreferencesEntry;

public class JustGramGhostPreferencesEntry extends BaseCGPreferencesEntry {

    private final int masterRow = 1;
    private final int hideOnlineRow = 2;
    private final int hideTypingRow = 3;
    private final int hideReadMessagesRow = 4;
    private final int hideReadStoriesRow = 5;

    @Override
    protected CharSequence getTitle() {
        return "Режим призрака";
    }

    @Override
    protected String getKey() {
        return "justgram_ghost_settings";
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Основные настройки призрака"));

        items.add(UItem.asCheck(masterRow, "Режим призрака (Master Switch)")
                .setChecked(JustGramGhostConfig.INSTANCE.getGhostModeMaster()));

        items.add(UItem.asShadow(
                "При включении главного переключателя все параметры скрытия активируются автоматически."
        ));

        items.add(UItem.asHeader("Гранулярные переключатели"));

        items.add(UItem.asCheck(hideOnlineRow, "Скрывать статус «В сети»")
                .setChecked(JustGramGhostConfig.INSTANCE.getHideOnline()));

        items.add(UItem.asCheck(hideTypingRow, "Скрывать набор текста")
                .setChecked(JustGramGhostConfig.INSTANCE.getHideTyping()));

        items.add(UItem.asCheck(hideReadMessagesRow, "Не отправлять статус прочтения сообщений")
                .setChecked(JustGramGhostConfig.INSTANCE.getHideReadMessages()));

        items.add(UItem.asCheck(hideReadStoriesRow, "Не отправлять статус просмотра историй")
                .setChecked(JustGramGhostConfig.INSTANCE.getHideReadStories()));

        items.add(UItem.asShadow(
                "Внимание: В соответствии с принципами приватности и техническим заданием JustGram, функция сохранения удалённых сообщений (анти-удалялка) полностью исключена из клиента."
        ));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == masterRow) {
            boolean next = !JustGramGhostConfig.INSTANCE.getGhostModeMaster();
            JustGramGhostConfig.INSTANCE.setGhostModeMaster(next);
        } else if (item.id == hideOnlineRow) {
            boolean next = !JustGramGhostConfig.INSTANCE.getHideOnline();
            JustGramGhostConfig.INSTANCE.setHideOnline(next);
        } else if (item.id == hideTypingRow) {
            boolean next = !JustGramGhostConfig.INSTANCE.getHideTyping();
            JustGramGhostConfig.INSTANCE.setHideTyping(next);
        } else if (item.id == hideReadMessagesRow) {
            boolean next = !JustGramGhostConfig.INSTANCE.getHideReadMessages();
            JustGramGhostConfig.INSTANCE.setHideReadMessages(next);
        } else if (item.id == hideReadStoriesRow) {
            boolean next = !JustGramGhostConfig.INSTANCE.getHideReadStories();
            JustGramGhostConfig.INSTANCE.setHideReadStories(next);
        }
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }
}
