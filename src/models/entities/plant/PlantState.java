package models.entities.plant;

public interface PlantState {
    void onTick(Plant plant);
    boolean canAttack();
}