package seedu.club.logic.commands;

import seedu.club.model.member.FieldContainsKeywordsPredicate;

/**
 * Finds and lists all members in club book whose field contains any of the argument keywords.
 * Partial match is acceptable.
 * Keyword matching is case insensitive.
 */
public class FindByCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all members whose field contain any of "
            + "the specified keywords (case-insensitive) and displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [] [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " alice bob charlie";

    private final FieldContainsKeywordsPredicate predicate;

    public FindByCommand(FieldContainsKeywordsPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute() {
        model.updateFilteredMemberList(predicate);
        return new CommandResult(getMessageForMemberListShownSummary(model.getFilteredMemberList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof FindByCommand // instanceof handles nulls
                && this.predicate.equals(((FindByCommand) other).predicate)); // state check
    }
}
