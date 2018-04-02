//@@author amrut-prabhu
package seedu.club.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import seedu.club.commons.exceptions.IllegalValueException;
import seedu.club.commons.util.CsvUtil;
import seedu.club.model.member.UniqueMemberList;

/**
 * Stores clubBook data in an CSV file.
 */
public class CsvFileStorage {

    /**
     * Saves the given clubBook data to the specified file.
     */
    public static void saveDataToFile(File file, String data) throws IOException {
        try {
            CsvUtil.saveDataToFile(file, data);
        } catch (IOException ioe) {
            throw new IOException("Unexpected error " + ioe.getMessage());
        }
    }

    /**
     * Returns club book in the file or an empty club book
     */
    public static UniqueMemberList readClubBook(File file) throws FileNotFoundException, IllegalValueException {
        return CsvUtil.getDataFromFile(file);
    }
}
