package seedu.club.model.poll;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.club.commons.core.index.Index;
import seedu.club.model.member.MatricNumber;
import seedu.club.model.poll.exceptions.AnswerNotFoundException;
import seedu.club.model.poll.exceptions.UserAlreadyVotedException;

/**
 * Represents a Poll in the club book.
 */
public class Poll {

    private final Question question;
    private final ObservableList<Answer> answers;
    private final Set<MatricNumber> polleesMatricNumbers;

    /**
     * Constructs a {@code Poll}.
     */
    public Poll(Question question, List<Answer> answers) {
        this(question, answers, null);
    }

    public Poll(Question question, List<Answer> answers,
                Set<MatricNumber> polleesMatricNumbers) {
        requireNonNull(question);
        requireNonNull(answers);

        this.question = question;
        this.answers = FXCollections.observableArrayList(answers);
        this.polleesMatricNumbers = new HashSet<>();
        this.polleesMatricNumbers.addAll(polleesMatricNumbers);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Poll // instanceof handles nulls
                && this.question.equals(((Poll) other).question) // state check
                && this.answers.equals(((Poll) other).answers));
    }

    public Question getQuestion() {
        return question;
    }

    public int getTotalVoteCount() {
        return answers.stream().collect(Collectors.reducing(0, Answer::getVoteCount, Integer::sum));
    }

    /**
     * Returns an immutable answer list, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public ObservableList<Answer> getAnswers() {
        return FXCollections.unmodifiableObservableList(answers);
    }

    /**
     * Returns an immutable poll set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<MatricNumber> getPolleesMatricNumbers() {
        return Collections.unmodifiableSet(polleesMatricNumbers);
    }

    public void vote(Index answerIndex, MatricNumber polleeMatricNumber) throws
            AnswerNotFoundException, UserAlreadyVotedException {
        if (polleesMatricNumbers.contains(polleeMatricNumber)) {
            throw new UserAlreadyVotedException();
        } else {
            try {
                answers.get(answerIndex.getZeroBased()).voteThisAnswer();
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                throw new AnswerNotFoundException();
            }
            polleesMatricNumbers.add(polleeMatricNumber);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(question, answers);
    }

    /**
     * Format state as text for viewing.
     */
    @Override
    public String toString() {
        return "[ " + question + " ]"
                + answers.stream().map(Answer::toString).collect(Collectors.joining(","));
    }
}
