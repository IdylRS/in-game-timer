/*
 * Copyright (c) 2020, Jordan Atwood <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

    @Inject
    private TimerOverlay(InGameTimerConfig config, InGameTimerPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.BOTTOM_LEFT);
        this.config = config;
        this.plugin = plugin;

        getMenuEntries().add(RESET_ENTRY);
        getMenuEntries().add(START_ENTRY);

        panelComponent.getChildren().add(TitleComponent.builder().text("In Game Timer").build());

        timeRemainingComponent = LineComponent.builder().left("Time Remaining:").right("").build();
        panelComponent.getChildren().add(timeRemainingComponent);


        secondsElapsed = getSecondsElapsed();
        lastUpdate = -1;
        loggedIn = false;
        timeUp = false;
        isPaused = true;

        setClearChildren(false);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        graphics.setFont(FontManager.getRunescapeFont());

        final long now = Instant.now().getEpochSecond();
        if(loggedIn && !isPaused && now - lastUpdate >= 1 && !timeUp) {
            lastUpdate = now;
            secondsElapsed++;

            if(secondsElapsed % 10 == 0) {
                plugin.saveSecondsElapsed(secondsElapsed);
            }
        }
        final long timeRemaining = this.config.countdown() * 60 - secondsElapsed;
        if(timeRemaining <= 0) timeUp();
        final Color timeColor =  timeRemaining < 60 ? Color.RED : timeRemaining < 300 ? Color.YELLOW : Color.WHITE;

        timeRemainingComponent.setRightColor(timeColor);
        timeRemainingComponent.setRight(formatTime(timeRemaining));

        return super.render(graphics);
    }

    public void reset() {
        secondsElapsed = 0;
        timeUp = false;
        pauseTimer();
        timeRemainingComponent.setRight("");
    }

    public void pauseTimer() {
        isPaused = true;
        getMenuEntries().remove(PAUSE_ENTRY);
        getMenuEntries().add(START_ENTRY);
    }

    public void resumeTimer() {
        isPaused = false;
        timeUp = false;
        lastUpdate = Instant.now().getEpochSecond();
        getMenuEntries().add(PAUSE_ENTRY);
        getMenuEntries().remove(START_ENTRY);
    }

    private void timeUp() {
        getMenuEntries().remove(PAUSE_ENTRY);
        getMenuEntries().remove(START_ENTRY);
        timeUp = true;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        loggedIn = isLoggedIn;
    }

    private long getSecondsElapsed() {
        final String savedSeconds = plugin.getSavedSecondsElapsed();

        if(savedSeconds.isEmpty() || savedSeconds == null) {
            return 0;
        }

        return Long.parseLong(savedSeconds);
    }

    private static String formatTime(final long remaining)
    {

        final long hours = TimeUnit.SECONDS.toHours(remaining);
        final long minutes = TimeUnit.SECONDS.toMinutes(remaining % 3600);
        final long seconds = remaining % 60;

        if(remaining <= 0) {
            return "Time's up!";
        }
        if(remaining < 60) {
            return String.format("%02ds", seconds);
        }
        if(remaining < 3600) {
            return String.format("%02dm%02ds", minutes, seconds);
        }

        return String.format("%01dh%1dm", hours, minutes);
    }
}