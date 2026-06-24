package models.progress.chapter;

import models.progress.level.Level;
import models.progress.level.OrdinaryLevel;

import java.util.ArrayList;
import java.util.List;

public class FrostBite extends Chapter {

    public FrostBite() {
        this.name = "Frost Bite";
        this.levels = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            levels.add(new OrdinaryLevel(this, i));
        }
        this.currentUnlockedLevel = levels.get(0);
    }
}