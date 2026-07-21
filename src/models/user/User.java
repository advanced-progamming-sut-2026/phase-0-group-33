package models.user;

import models.asset.Coin;
import models.asset.Diamond;
import models.asset.Pot;
import models.entities.zombie.ZombieType;
import models.enums.DifficultyLevel;
import models.enums.Gender;
import models.progress.level.Level;

import java.util.Set;
import java.util.HashSet;

public class User {

    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private Gender gender;
    private Coin coins = new Coin();
    private Diamond diamonds = new Diamond();
    private Pot pots = new Pot();
    private DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
    private SecurityQuestion securityQuestion;
    private Set<UserPlant> unlockedPlants = new HashSet<>();
    private Set<ZombieType> seenZombies = new HashSet<>();
    private Set<Level> completedLevels = new HashSet<>();
    private int highestScore = 0;
    private int numberOfGames;

    public User(String username, String passwordHash, String nickname, String email,
                Gender gender, SecurityQuestion securityQuestion) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
    }

    public User() {

    }

    public void changePasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public void changeUsername(String newUsername) { this.username = newUsername; }

    public void completeLevel(Level level) {
        if(level != null) this.completedLevels.add(level);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Coin getCoins() {
        return coins;
    }

    public void setCoins(Coin coins) {
        this.coins = coins;
    }

    public Diamond getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(Diamond diamonds) {
        this.diamonds = diamonds;
    }

    public Pot getPots() {
        return pots;
    }

    public int getNumberOfGames() {
        return numberOfGames;
    }

    public void setNumberOfGames(int numberOfGames) {
        this.numberOfGames = numberOfGames;
    }

    public void setPots(Pot pots) {
        this.pots = pots;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public SecurityQuestion getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(SecurityQuestion securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public Set<UserPlant> getUnlockedPlants() {
        return unlockedPlants;
    }

    public void setUnlockedPlants(Set<UserPlant> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public Set<ZombieType> getSeenZombies() {
        return seenZombies;
    }

    public void setSeenZombies(Set<ZombieType> seenZombies) {
        this.seenZombies = seenZombies;
    }

    public Set<Level> getCompletedLevels() {
        return completedLevels;
    }

    public void setCompletedLevels(Set<Level> completedLevels) {
        this.completedLevels = completedLevels;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }
}
