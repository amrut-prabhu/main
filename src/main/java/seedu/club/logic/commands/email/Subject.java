package seedu.club.logic.commands.email;

import static java.util.Objects.requireNonNull;

/**
 * Refers to the subject of an email
 */
public class Subject {

    public static final String EMPTY_SUBJECT_STRING = "";
    private String subject;

    public Subject(String subject) {
        requireNonNull(subject);
        this.subject = subject.trim();
    }

    @Override
    public int hashCode() {
        return subject.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other == this    //short circuit if same object
                || (other instanceof Subject    //handles nulls
                && this.subject.equalsIgnoreCase(((Subject) other).subject));   //state check
    }

    @Override
    public String toString() {
        return this.subject;
    }
}
