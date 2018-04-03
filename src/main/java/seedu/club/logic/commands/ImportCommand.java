//@@author amrut-prabhu
package seedu.club.logic.commands;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;

import seedu.club.commons.exceptions.DataConversionException;
import seedu.club.logic.commands.exceptions.CommandException;
import seedu.club.model.member.exceptions.DuplicateMemberException;

/**
 * Exports members' information from the specified file into Club Connect.
 */
public class ImportCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "import";
    public static final String COMMAND_FORMAT = "import FILE_PATH";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Imports the members' information from the specified CSV file into Club Connect. "
            + "Parameters: FILE_PATH (must be an absolute path to a CSV file )\n"
            + "Example: " + COMMAND_WORD + " C:/Users/John Doe/Downloads/members.csv";

    public static final String MESSAGE_DUPLICATE_MEMBER = "A member already exists in the club book.";
    public static final String MESSAGE_IMPORT_SUCCESS = "Successfully imported members from: %1$s";
    public static final String MESSAGE_IMPORT_FAILURE = "Error occurred while importing from the file: %1$s";
    public static final String MESSAGE_INCORRECT_FORMAT = "Data is not in the correct format in the file: %1$s";


    private final File importFile;

    /**
     * @param importFile CSV file to import members from.
     */
    public ImportCommand(File importFile) {
        requireNonNull(importFile);
        this.importFile = importFile;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        try {
            model.importMembers(importFile);
        } catch (IOException ioe) {
            throw new CommandException(String.format(MESSAGE_IMPORT_FAILURE, importFile));
        } catch (DataConversionException dce) {
            throw new CommandException(String.format(MESSAGE_INCORRECT_FORMAT, importFile));
        } catch (DuplicateMemberException dme) {
            throw new CommandException(MESSAGE_DUPLICATE_MEMBER);
        }

        return new CommandResult(String.format(MESSAGE_IMPORT_SUCCESS, importFile));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ImportCommand // instanceof handles nulls
                && this.importFile.equals(((ImportCommand) other).importFile)); // state check
    }
}