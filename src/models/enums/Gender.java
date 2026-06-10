package models.enums;

public enum Gender {
    MALE, FEMALE, PREFER_NOT_TO_SAY;

    public static Gender getByName(String name) {
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(name)) {
                return gender;
            }
        }
        return PREFER_NOT_TO_SAY;
    }

    @Override
    public String toString() {
        return this.name().replaceAll("_", " ");
    }
}
