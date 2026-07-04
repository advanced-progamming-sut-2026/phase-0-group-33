package models.progress.chapter;

import models.progress.level.Level;
import models.progress.level.OrdinaryLevel;

import java.util.ArrayList;
import java.util.List;

public class WaveyBeach extends Chapter {

    public WaveyBeach() {
        this.name = "Wavey Beach";
        this.levels = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            levels.add(new OrdinaryLevel(this, i));
        }
        this.currentUnlockedLevel = levels.get(0);
    }
}