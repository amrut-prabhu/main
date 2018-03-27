package seedu.club.model;

import static java.util.Objects.requireNonNull;
import static seedu.club.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.club.commons.core.ComponentManager;
import seedu.club.commons.core.LogsCenter;
import seedu.club.commons.events.model.ClubBookChangedEvent;
import seedu.club.commons.events.model.ProfilePhotoChangedEvent;
import seedu.club.commons.events.ui.SendEmailRequestEvent;
import seedu.club.model.email.Body;
import seedu.club.model.email.Client;
import seedu.club.model.email.Subject;
import seedu.club.model.group.Group;
import seedu.club.model.group.exceptions.GroupCannotBeRemovedException;
import seedu.club.model.group.exceptions.GroupNotFoundException;
import seedu.club.model.member.Member;
import seedu.club.model.member.exceptions.DuplicateMemberException;
import seedu.club.model.member.exceptions.MemberNotFoundException;
import seedu.club.model.poll.Poll;
import seedu.club.model.poll.exceptions.DuplicatePollException;
import seedu.club.model.poll.exceptions.PollNotFoundException;
import seedu.club.model.tag.Tag;
import seedu.club.model.tag.exceptions.TagNotFoundException;
import seedu.club.model.task.Assignee;
import seedu.club.model.task.Assignor;
import seedu.club.model.task.Status;
import seedu.club.model.task.Task;
import seedu.club.model.task.exceptions.DuplicateTaskException;
import seedu.club.model.task.exceptions.TaskCannotBeDeletedException;
import seedu.club.model.task.exceptions.TaskNotFoundException;
import seedu.club.storage.ProfilePhotoStorage;

/**
 * Represents the in-memory model of the club book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final ClubBook clubBook;
    private final FilteredList<Member> filteredMembers;
    private final FilteredList<Tag> filteredTags;
    private final FilteredList<Poll> filteredPolls;
    private Member loggedInMember;
    private final FilteredList<Task> filteredTasks;

    /**
     * Initializes a ModelManager with the given clubBook and userPrefs.
     */
    public ModelManager(ReadOnlyClubBook clubBook, UserPrefs userPrefs) {
        super();
        requireAllNonNull(clubBook, userPrefs);

        logger.fine("Initializing with club book: " + clubBook + " and user prefs " + userPrefs);

        this.clubBook = new ClubBook(clubBook);
        filteredMembers = new FilteredList<>(this.clubBook.getMemberList());
        filteredTags = new FilteredList<>(this.clubBook.getTagList());
        filteredPolls = new FilteredList<>(this.clubBook.getPollList());
        loggedInMember = getLoggedInMember();
        filteredTasks = new FilteredList<>(this.clubBook.getTaskList());
    }

    public ModelManager() {
        this(new ClubBook(), new UserPrefs());
    }

    @Override
    public Member getLoggedInMember() {
        loggedInMember = null;
        if (!getFilteredMemberList().isEmpty()) {
            loggedInMember = getFilteredMemberList().get(0);
        }
        return loggedInMember;
    }

    @Override
    public void resetData(ReadOnlyClubBook newData) {
        clubBook.resetData(newData);
        indicateClubBookChanged();
    }

    @Override
    public ReadOnlyClubBook getClubBook() {
        return clubBook;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateClubBookChanged() {
        raise(new ClubBookChangedEvent(clubBook));
    }

    //@@author amrut-prabhu
    /** Raises an event to indicate the profile photo of a member has changed */
    private boolean indicateProfilePhotoChanged(String originalPath, String newFileName) {
        ProfilePhotoChangedEvent profilePhotoChangedEvent = new ProfilePhotoChangedEvent(originalPath, newFileName);
        raise(profilePhotoChangedEvent);
        return profilePhotoChangedEvent.isPhotoChanged();
    }

    @Override
    public boolean addProfilePhoto(String originalPhotoPath) {
        String newFileName = getLoggedInMember().getMatricNumber().toString();
        if (!indicateProfilePhotoChanged(originalPhotoPath, newFileName)) {
            return false;
        }

        String newProfilePhotoPath = ProfilePhotoStorage.getCurrentDirectory()
                + ProfilePhotoStorage.SAVE_PHOTO_DIRECTORY + newFileName + ProfilePhotoStorage.FILE_EXTENSION;

        loggedInMember.setProfilePhotoPath(newProfilePhotoPath);
        indicateClubBookChanged();
        return true;
    }
    //@@author

    @Override
    public synchronized void deleteMember(Member target) throws MemberNotFoundException {
        clubBook.removeMember(target);
        indicateClubBookChanged();
    }

    @Override
    public synchronized void addMember(Member member) throws DuplicateMemberException {
        //updateTagList(member.getTags());
        clubBook.addMember(member);
        updateFilteredMemberList(PREDICATE_SHOW_ALL_MEMBERS);
        indicateClubBookChanged();
    }

    @Override
    public void updateMember(Member target, Member editedMember)
            throws DuplicateMemberException, MemberNotFoundException {
        requireAllNonNull(target, editedMember);

        clubBook.updateMember(target, editedMember);
        deleteUnusedTags();
        indicateClubBookChanged();
    }

    @Override
    public synchronized void addPoll(Poll poll) throws DuplicatePollException {
        clubBook.addPoll(poll);
        updateFilteredPollList(PREDICATE_SHOW_ALL_POLLS);
        indicateClubBookChanged();
    }

    @Override
    public synchronized void deletePoll(Poll target) throws PollNotFoundException {
        clubBook.removePoll(target);
        indicateClubBookChanged();
    }

    @Override
    public boolean logInMemberSuccessful(String username, String password) {
        requireAllNonNull(username, password);

        return clubBook.logInMember(username, password);
    }

    //@@author yash-chowdhary
    @Override
    public void removeGroup(Group toRemove) throws GroupNotFoundException, GroupCannotBeRemovedException {
        requireNonNull(toRemove);

        clubBook.removeGroup(toRemove);
        indicateClubBookChanged();
    }
    //@@author


    @Override
    public void deleteTag(Tag tag) throws TagNotFoundException {
        clubBook.deleteTag(tag);
        updateFilteredMemberList(PREDICATE_SHOW_ALL_MEMBERS);
        indicateClubBookChanged();
    }

    /**
     * Removes those tags from the master tag list that no members in the club book are tagged with.
     */
    private void deleteUnusedTags() {
        List<Tag> tags = new ArrayList<>(clubBook.getTagList());

        for (Tag tag: tags) {
            deleteTagIfUnused(tag);
        }
    }

    /**
     * Removes {@code tag} from the master tag list if no members in the club book are tagged with it.
     *
     * @param tag Tag to remove if no members are tagged with it
     */
    private void deleteTagIfUnused(Tag tag) {
        if (isNotTaggedInMembers(tag)) {
            try {
                deleteTag(tag);
            } catch (TagNotFoundException tnfe) {
                throw new AssertionError("The tag cannot be missing.");
            }
        }
    }

    /**
     * Returns true is no member in the club book is tagged with {@code tag}.
     */
    private boolean isNotTaggedInMembers(Tag tag) {
        List<Member> members = new ArrayList<>(clubBook.getMemberList());

        for (Member member : members) {
            if (member.getTags().contains(tag)) {
                return false;
            }
        }
        return true;
    }

    //@@author yash-chowdhary
    @Override
    public String generateEmailRecipients(Group group, Tag tag) throws GroupNotFoundException, TagNotFoundException {
        if (group != null) {
            return generateGroupEmailRecipients(group);
        }
        return generateTagEmailRecipients(tag);
    }

    /**
     * Generates recipient list of all members part of {@code Tag toSendEmailTo}
     * @throws TagNotFoundException if {@code Tag toSendEmailTo} doesn't exist in the club book
     */
    private String generateTagEmailRecipients(Tag toSendEmailTo) throws TagNotFoundException {
        List<Member> members = new ArrayList<>(clubBook.getMemberList());

        List<String> emailRecipients = new ArrayList<>();
        Boolean tagFound = false;
        for (Member member : members) {
            Set<Tag> memberTags = member.getTags();
            if (memberTags.contains(toSendEmailTo)) {
                emailRecipients.add(member.getEmail().toString());
                tagFound = true;
            }
        }
        if (!tagFound) {
            throw new TagNotFoundException();
        }

        return String.join(",", emailRecipients);
    }

    /**
     * Generates recipient list of all members part of {@code Group toSendEmailTo}
     * @throws GroupNotFoundException if {@code Group toSendEmailTo} doesn't exist in the club book
     */
    private String generateGroupEmailRecipients(Group toSendEmailTo) throws GroupNotFoundException {
        List<Member> members = new ArrayList<>(clubBook.getMemberList());

        List<String> emailRecipients = new ArrayList<>();
        Boolean groupFound = false;
        for (Member member : members) {
            if (member.getGroup().equals(toSendEmailTo)) {
                emailRecipients.add(member.getEmail().toString());
                groupFound = true;
            }
        }
        if (!groupFound) {
            throw new GroupNotFoundException();
        }
        return String.join(",", emailRecipients);
    }

    @Override
    public void sendEmail(String recipients, Client client, Subject subject, Body body) {
        raise(new SendEmailRequestEvent(recipients, subject, body, client));
    }

    @Override
    public void addTaskToTaskList(Task toAdd) throws DuplicateTaskException {
        try {
            Assignor assignor = new Assignor(loggedInMember.getName().fullName);
            Assignee assignee = new Assignee(loggedInMember.getName().fullName);
            Status status = new Status(Status.NOT_STARTED_STATUS);
            toAdd.setAssignor(assignor);
            toAdd.setAssignee(assignee);
            toAdd.setStatus(status);
            clubBook.addTaskToTaskList(toAdd);
            indicateClubBookChanged();
        } catch (DuplicateTaskException dte) {
            throw new DuplicateTaskException();
        }
    }

    @Override
    public void deleteTask(Task targetTask) throws TaskNotFoundException, TaskCannotBeDeletedException {
        Assignor assignor = targetTask.getAssignor();
        Assignee assignee = targetTask.getAssignee();
        String currentMember = loggedInMember.getName().toString();
        if (!currentMember.equalsIgnoreCase(assignor.getAssignor())
                || !currentMember.equalsIgnoreCase(assignee.getAssignee())) {
            throw new TaskCannotBeDeletedException();
        }
        clubBook.deleteTask(targetTask);
        indicateClubBookChanged();
    }

    //@@author

    //=========== Filtered member List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code member} backed by the internal list of
     * {@code clubBook}
     */
    @Override
    public ObservableList<Member> getFilteredMemberList() {
        return FXCollections.unmodifiableObservableList(filteredMembers);
    }

    @Override
    public ObservableList<Task> getFilteredTaskList() {
        return FXCollections.unmodifiableObservableList(filteredTasks);
    }

    @Override
    public void updateFilteredMemberList(Predicate<Member> predicate) {
        requireNonNull(predicate);
        filteredMembers.setPredicate(predicate);
    }

    @Override
    public ObservableList<Poll> getFilteredPollList() {
        return FXCollections.unmodifiableObservableList(filteredPolls);
    }

    @Override
    public void updateFilteredPollList(Predicate<Poll> predicate) {
        requireNonNull(predicate);
        filteredPolls.setPredicate(predicate);
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return clubBook.equals(other.clubBook)
                && filteredMembers.equals(other.filteredMembers);
    }

    //=========== Filtered Tag List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Tag} backed by the internal list of
     * {@code clubBook}
     */
    @Override
    public ObservableList<Tag> getFilteredTagList() {
        return FXCollections.unmodifiableObservableList(filteredTags);
    }

    @Override
    public void updateFilteredTagList(Predicate<Tag> predicate) {
        requireNonNull(predicate);
        filteredTags.setPredicate(predicate);
    }

}
