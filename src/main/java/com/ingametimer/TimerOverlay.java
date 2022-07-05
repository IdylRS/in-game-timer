package com.ingametimer;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY;

@Slf4j
class TimerOverlay extends OverlayPanel
{
    private final InGameTimerConfig config;
    private final InGameTimerPlugin plugin;

    private final LineComponent timeRemainingComponent;

    private long secondsElapsed;

    private long lastUpdate;

    private boolean loggedIn;

    private boolean timeUp;

    private boolean isPaused;

    public static OverlayMenuEntry PAUSE_ENTRY = new OverlayMenuEntry(RUNELITE_OVERLAY, "Pause", "Timer");
    public static OverlayMenuEntry START_ENTRY = new OverlayMenuEntry(RUNELITE_OVERLAY, "Start", "Timer");
    public static OverlayMenuEntry RESET_ENTRY = new OverlayMenuEntry(RUNELITE_OVERLAY, "Reset", "Timer");

    public static TitleComponent PAUSE_TITLE = TitleComponent.builder().color(Color.YELLOW).text("Timer Paused").build();
    public static TitleComponent START_TITLE = TitleComponent.builder().color(Color.GREEN).text("Timer Running").build();
    public static TitleComponent TIME_EXPIRED_TITLE = TitleComponent.builder().color(Color.RED).text("Time Expired").build();

    @Inject
    private TimerOverlay(InGameTimerConfig config, InGameTimerPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.config = config;
        this.plugin = plugin;

        getMenuEntries().add(RESET_ENTRY);
        getMenuEntries().add(START_ENTRY);

        panelComponent.getChildren().add(PAUSE_TITLE);

        timeRemainingComponent = LineComponent.builder().left("Time Remaining:").right("").build();
        panelComponent.getChildren().add(timeRemainingComponent);
        
        secondsElapsed = getSecondsElapsed();
        lastUpdate = -1;
        loggedIn = false;
        timeUp = false;
        isPaused = true;

        setClearChildren(false);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        graphics.setFont(FontManager.getRunescapeFont());

        final long now = Instant.now().getEpochSecond();
        if(loggedIn && !isPaused && now - lastUpdate >= 1 && !timeUp) {
            lastUpdate = now;
            secondsElapsed++;

            if(secondsElapsed % 5 == 0) {
                plugin.saveSecondsElapsed(secondsElapsed);
            }
        }
        final long timeRemaining = this.config.countdown() * 60 - secondsElapsed;
        if(timeRemaining <= 0 && !timeUp) timeUp();
        final Color timeColor =  timeRemaining < 60 ? Color.RED : timeRemaining < 300 ? Color.YELLOW : Color.WHITE;

        timeRemainingComponent.setRightColor(timeColor);
        timeRemainingComponent.setRight(formatTime(timeRemaining));

        return super.render(graphics);
    }

    public void reset() {
        secondsElapsed = 0;
        timeUp = false;
        getMenuEntries().remove(START_ENTRY);
        getMenuEntries().remove(PAUSE_ENTRY);
        pauseTimer();
        timeRemainingComponent.setRight("");
    }

    public void pauseTimer() {
        isPaused = true;
        addOverlayEntry(START_ENTRY);
        updateOverlayTitle(PAUSE_TITLE);
    }

    public void resumeTimer() {
        if(timeUp) reset();

        isPaused = false;
        timeUp = false;
        lastUpdate = Instant.now().getEpochSecond();
        addOverlayEntry(PAUSE_ENTRY);
        updateOverlayTitle(START_TITLE);
    }

    private void timeUp() {
        addOverlayEntry(START_ENTRY);
        updateOverlayTitle(TIME_EXPIRED_TITLE);
        timeUp = true;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        loggedIn = isLoggedIn;
    }

    private void updateOverlayTitle(TitleComponent title) {
        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(title);
        panelComponent.getChildren().add(timeRemainingComponent);
    }

    private void addOverlayEntry(OverlayMenuEntry entry) {
        getMenuEntries().remove(PAUSE_ENTRY);
        getMenuEntries().remove(START_ENTRY);

        getMenuEntries().add(entry);
    }

    private long getSecondsElapsed() {
        final String savedSeconds = plugin.getSavedSecondsElapsed();

        if(savedSeconds == null ||  savedSeconds.isEmpty()) {
            return 0;
        }

        return Long.parseLong(savedSeconds);
    }

    private static String formatTime(final long remaining)
    {

        final long hours = TimeUnit.SECONDS.toHours(remaining);
        final long minutes = TimeUnit.SECONDS.toMinutes(remaining % 3600);
        final long seconds = remaining % 60;

        if(remaining < 60) {
            return String.format("%01ds", seconds);
        }
        if(remaining < 3600) {
            return String.format("%2dm %02ds", minutes, seconds);
        }

        return String.format("%1dh %02dm", hours, minutes);
    }
}