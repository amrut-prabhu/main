package seedu.club.logic.commands.exceptions;

import static java.util.Objects.requireNonNull;
import static seedu.club.model.Model.PREDICATE_SHOW_ALL_POLLS;

import java.util.List;
import java.util.Objects;

import seedu.club.commons.core.Messages;
import seedu.club.commons.core.index.Index;
import seedu.club.logic.commands.CommandResult;
import seedu.club.logic.commands.UndoableCommand;
import seedu.club.model.poll.Poll;
import seedu.club.model.poll.exceptions.AnswerNotFoundException;
import seedu.club.model.poll.exceptions.PollNotFoundException;
import seedu.club.model.poll.exceptions.UserAlreadyVotedException;

/**
 * Votes in a poll of an existing poll in the club book.
 */
public class VoteCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "vote";
    public static final String COMMAND_FORMAT = "edit POLL_INDEX ANWER_INDEX";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Votes in the poll identified "
            + "by the index number used in the last poll listing. \n"
            + "Parameters: POLL_INDEX (must be a positive integer) QUESTION_INDEX (must be a positive integer)";

    public static final String MESSAGE_VOTE_SUCCESS = "Your vote has been received";
    public static final String MESSAGE_VOTE_FAIL_ALREADY_VOTED = "You have already voted in this poll";

    private final Index pollIndex;
    private final Index answerIndex;

    private Poll pollToVoteIn;

    /**
     * @param pollIndex   of the poll in the filtered poll list to vote in
     * @param answerIndex of the answer of the poll in the filtered poll list to vote in
     */
    public VoteCommand(Index pollIndex, Index answerIndex) {
        requireNonNull(pollIndex);
        requireNonNull(answerIndex);
        this.pollIndex = pollIndex;
        this.answerIndex = answerIndex;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        try {
            model.voteInPoll(pollToVoteIn, answerIndex);
        } catch (UserAlreadyVotedException userAlreadyVotedException) {
            throw new CommandException(MESSAGE_VOTE_FAIL_ALREADY_VOTED);
        } catch (PollNotFoundException questionNotFoundException) {
            throw new AssertionError("The target poll cannot be missing");
        } catch (AnswerNotFoundException answerNotFoundException) {
            throw new AssertionError("The target answer cannot be missing");
        }
        model.updateFilteredPollList(PREDICATE_SHOW_ALL_POLLS);
        return new CommandResult(String.format(MESSAGE_VOTE_SUCCESS));
    }

    @Override
    protected void preprocessUndoableCommand() throws CommandException {
        List<Poll> lastShownList = model.getFilteredPollList();

        if (pollIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_POLL_DISPLAYED_INDEX);
        }
        if (answerIndex.getZeroBased() >= pollToVoteIn.getAnswers().size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_ANSWER_DISPLAYED_INDEX);
        }
        pollToVoteIn = lastShownList.get(pollIndex.getZeroBased());
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof VoteCommand)) {
            return false;
        }

        // state check
        VoteCommand e = (VoteCommand) other;
        return pollIndex.equals(e.pollIndex)
                && answerIndex.equals(e.answerIndex)
                && Objects.equals(pollToVoteIn, e.pollToVoteIn);
    }

}
