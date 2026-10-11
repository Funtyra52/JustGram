package org.justgram.preferences;

import android.content.Context;
import android.view.View;

import org.justgram.badges.JustGramBadgesManager;
import org.justgram.ghost.JustGramGhostPreferencesEntry;
import org.justgram.plugins.JustGramPluginsActivity;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.SettingsActivity;

import java.util.ArrayList;

import uz.unnarsx.cherrygram.preferences.BaseCGPreferencesEntry;

public class JustGramPreferencesActivity extends BaseCGPreferencesEntry {

    private final int ghostModeRow = 1;
    private final int pluginsRow = 2;
    private final int badgesInfoRow = 3;
    private final int reloadSupportersRow = 4;
    private final int donateRow = 5;
    private final int githubRow = 6;
    private final int versionRow = 7;

    @Override
    protected CharSequence getTitle() {
        return "Настройки JustGram";
    }

    @Override
    protected String getKey() {
        return "justgram_settings";
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Функции JustGram"));

        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        ghostModeRow,
                        IconBackgroundColors.BLUE.top, IconBackgroundColors.BLUE.bottom,
                        R.drawable.ghost,
                        "Режим призрака",
                        "Скрытие онлайна, тайпинга и статусов прочтения",
                        true
                )
        );

        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        pluginsRow,
                        IconBackgroundColors.GREEN.top, IconBackgroundColors.GREEN.bottom,
                        R.drawable.formatting_code,
                        "Плагины",
                        "Extera Plugin Engine и безопасный режим",
                        true
                )
        );

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("Бейджи и сообщество"));

        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        badgesInfoRow,
                        0xFF00C0FF, 0xFF0072FF,
                        R.drawable.justgram_badge,
                        "Мульти-система бейджей",
                        "JustGram, Cherrygram, exteraGram и Premium",
                        true
                )
        );

        int supportersCount = JustGramBadgesManager.INSTANCE.getJustGramSupporters().size();
        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        reloadSupportersRow,
                        0xFF00C0FF, 0xFF0088FF,
                        R.drawable.msg_retry_solar,
                        "Обновить спонсоров JustGram",
                        "Загрузить Supporters.txt (" + supportersCount + " спонсоров)",
                        true
                )
        );

        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        donateRow,
                        0xFFF38B31, 0xFFE26314,
                        R.drawable.settings_gift,
                        "Получить бейдж спонсора",
                        "Закинуть сотку автору и попасть в Supporters.txt",
                        true
                )
        );

        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader("О проекте"));

        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        githubRow,
                        IconBackgroundColors.ORANGE_DEEP.top, IconBackgroundColors.RED.bottom,
                        R.drawable.settings_info_filled_solar,
                        "Репозиторий проекта",
                        "github.com/Funtyra52/JustGram",
                        true
                )
        );

        items.add(
                SettingsActivity.SettingCell.Factory.of(
                        versionRow,
                        IconBackgroundColors.BLUE_DEEP.top, IconBackgroundColors.BLUE_DEEP.bottom,
                        R.drawable.settings_support_filled_solar,
                        "Версия JustGram",
                        "0.1.0-beta.1 (база Cherrygram 11.3.0)",
                        false
                )
        );

        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == ghostModeRow) {
            presentFragment(new JustGramGhostPreferencesEntry());
        } else if (item.id == pluginsRow) {
            presentFragment(new JustGramPluginsActivity());
        } else if (item.id == badgesInfoRow) {
            showBadgesHelpDialog();
        } else if (item.id == reloadSupportersRow) {
            handleReloadSupporters();
        } else if (item.id == donateRow) {
            Browser.openUrl(getContext(), "https://github.com/Funtyra52/JustGram");
        } else if (item.id == githubRow) {
            Browser.openUrl(getContext(), "https://github.com/Funtyra52/JustGram");
        } else if (item.id == versionRow) {
            AndroidUtilities.addToClipboard("JustGram 0.1.0-beta.1 (Cherrygram 11.3.0)");
            BulletinFactory.of(this).createCopyBulletin("Версия скопирована в буфер").show();
        }
    }

    private void handleReloadSupporters() {
        if (getContext() == null) return;
        BulletinFactory.of(this).createSimpleBulletin(R.raw.chats_infotip, "Обновление списка спонсоров...").show();
        JustGramBadgesManager.INSTANCE.reloadSupporters(getContext(), count -> {
            if (isFinishing() || getContext() == null) return kotlin.Unit.INSTANCE;
            BulletinFactory.of(this).createSimpleBulletin(
                    R.drawable.justgram_badge,
                    "Спонсоры обновлены! Всего: " + count
            ).show();
            if (listView != null && listView.adapter != null) {
                listView.adapter.update(true);
            }
            return kotlin.Unit.INSTANCE;
        });
    }

    private void showBadgesHelpDialog() {
        if (getParentActivity() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder.setTitle("Мульти-система бейджей");
        builder.setMessage(
                "JustGram поддерживает одновременное отображение нескольких типов бейджей:\n\n" +
                "1. JustGram Supporter / Dev — участники и спонсоры из файла Supporters.txt репозитория JustGram.\n" +
                "2. CherryGram Supporter — официальные бейджи CherryGram.\n" +
                "3. exteraGram Supporter — бейджи сообщества exteraGram.\n" +
                "4. Telegram Premium — значок подписки Telegram Premium или кастомный статус.\n\n" +
                "Бейджи отрисовываются рядом с никнеймом в списках чатов, профиле и шапке диалога."
        );
        builder.setPositiveButton(LocaleController.getString(R.string.OK), null);
        showDialog(builder.create());
    }
}
