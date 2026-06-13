package models.enums;

public enum SecurityQuestionData {
    Q1("What's your pet's name?"),
    Q2("What's your mother's name"),
    Q3("When's your birthday?"),
    Q4("In what city were you born?"),
    ;

    private String question;

    SecurityQuestionData(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
