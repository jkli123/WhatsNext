package com.snowdragon.whatsnext.database;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.snowdragon.whatsnext.model.Task;
import com.snowdragon.whatsnext.model.TaskChange;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class around Firebase Firestore class.
 * <p>
 *     This class wraps around Firestore to expose methods
 *     only necessary to the application. The database uses
 *     a fluent interface to chain method calls and supports the
 *     4 basic CRUD methods of a database. As all methods are
 *     to a cloud database, all database methods are asynchronous
 *     and as such, you have to register/set a DatabaseStateChangeListener
 *     to the instance of the database you are holding.
 * </p>
 * <p>
 *     The state change listener will be called when the appropriate
 *     methods have returned from their database operations along with
 *     some callback payload associated with the method call. Also, this
 *     database only allows registered users to gain access into the
 *     database. Currently, it does not support any form of unregistered
 *     user.
 * </p>
 */
public class Database {

    public static final String TASK_COLLECTION = "/tasks";
    public static final String DONE_COLLECTION = "/done";

    private static final String TAG = "Database";
    //This path is only for testing purposes only.
    private static final String DEV_PATH = "root/dev";
    private static final String RELEASE_PATH = "root/release";
    private static final String USERS_COLLECTION = "/users";

    private static final int ADD_EVENT = 0;
    private static final int GET_EVENT = 1;
    private static final int UPDATE_EVENT = 2;
    private static final int DELETE_EVENT = 3;

    private final DatabaseLogger mLogger;
    private final FirebaseFirestore mFirestore;
    private OnDatabaseStateChangeListener mStateChangeListener;

    private Database(Context context, FirebaseFirestore firestore) {
        mLogger = DatabaseLogger.getInstance(context);
        mFirestore = firestore;
    }

    /**
     * Gets an instance of the database.
     * <p>
     *     This method requires a context for string resources
     *     that have declared debug messages in it.
     * </p>
     * @param context The context to retrieve shared resources.
     * @return An instance of this database.
     */
    public static Database getInstance(Context context) {
        return new Database(context, FirebaseFirestore.getInstance());
    }

    /**
     * Deletes a certain task for the user.
     * <p>
     *     This method helps to delete a task which is
     *     identified by its Task UUID string from the
     *     database for this particular user. Note that
     *     neither arguments can be null as this will result
     *     in a IllegalArgumentException being thrown.
     * </p>
     * @param user The user currently signed in
     * @param task The task to delete based on UUID string.
     * @return An instance of this database.
     */
    public Database deleteTaskForUser(FirebaseUser user, Task task, String userCollection) {
        if(validateUserTask(user, task)) {
            String path = constructUsersDatabasePath(
                    DEV_PATH, "/" + user.getUid(), userCollection);
            return deleteTaskByPath(path, task);
        } else {
            throw new IllegalArgumentException(
                    "Cannot delete null user or task");
        }
    }

    /**
     * Updates a certain task for the user.
     * <p>
     *     This method updates a task referenced by the
     *     UUID string of the task and any fields that
     *     exist in the TaskChange object passed in
     *     as argument. Also, no arguments can be null
     *     as this will result in a IllegalArgumentException
     *     being thrown.
     * </p>
     * @param user The user currently logged in
     * @param taskId The task to update according to UUID string
     * @param taskChange The object holding list of changes to be made.
     * @return An instance of this database.
     */
    public Database updateTaskForUser(
            FirebaseUser user, String taskId, TaskChange taskChange, String userCollection) {
        if(validateUserTaskChange(user, taskChange)) {
            String path = constructUsersDatabasePath(
                    DEV_PATH, "/" + user.getUid(), userCollection);
            return updateTaskByPath(path, taskId, taskChange);
        } else {
            throw new IllegalArgumentException(
                            "Cannot update null user or task change");
        }

    }

    /**
     * Adds a certain task for the user.
     * <p>
     *     This method adds a task for the user. Do not
     *     call this method if you wish to update a task
     *     for the user as the behaviour of the database is not
     *     guaranteed under update operations.
     *     Also, no arguments can be null
     *     as this will result in a IllegalArgumentException
     *     being thrown.
     * </p>
     * @param user The user currently logged in
     * @param task The task to add
     * @return An instance of this database.
     */
    public Database addTaskForUser(FirebaseUser user, Task task, String userCollection) {
        if(validateUserTask(user, task)) {
            String path = constructUsersDatabasePath(
                    DEV_PATH, "/" + user.getUid(), userCollection);
            return addTaskByPath(path, task);
        } else {
            throw new IllegalArgumentException(
                    "Cannot add null user or task");
        }
    }

    /**
     * Retrieves all the tasks for the user.
     * <p>
     *     This method retrieves all tasks belonging to a user.
     *     Also, no arguments can be null
     *     as this will result in a IllegalArgumentException
     *     being thrown.
     * </p>
     * @param user The user currently logged in
     * @return An instance of this database.
     */
    public Database getAllTaskForUser(FirebaseUser user, String userCollection) {
        if(validateUser(user)) {
            String path = constructUsersDatabasePath(
                    DEV_PATH, "/" + user.getUid(), userCollection);
            return getAllTaskByPath(path);
        } else {
            throw new IllegalArgumentException("Unable to get null user");
        }
    }

    /**
     * Registers a DatabaseStateChangeListener to this instance.
     * <p>
     *     Attaches a listener to an instance of the database.
     *     Take careful when calling this method multiple times
     *     as the database only supports having one registered
     *     listener currently and new calls to this method will
     *     override any listener already attached making the old
     *     listener unusable.
     * </p>
     * @param listener The state change listener to attach this instance to.
     * @return An instance of this database.
     */
    public Database setOnDatabaseStateChangeListener(
            OnDatabaseStateChangeListener listener) {
        mStateChangeListener = listener;
        return this;
    }

    /**
     * Utility method to update a database according to path.
     * <p>
     *     Helper method to update a database according to the
     *     path and taskId. Path is the colleciton path of the
     *     user's own task collection and taskId is the actual
     *     task to update.
     * </p>
     * @param path The collection path belonging to the user
     * @param taskId The task to update
     * @param taskChange The fields to change.
     * @return An instance of this database.
     */
    private Database updateTaskByPath(
            final String path, final String taskId, final TaskChange taskChange) {
        mFirestore
                .collection(path)
                .document(taskId)
                .update(taskChange.getFieldValueMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(
                            @NonNull com.google.android.gms.tasks.Task<Void> task) {
                        informListener(UPDATE_EVENT, taskId);
                    }
                });
        return this;
    }

    /**
     * Utility method to delete a task according to path.
     * <p>
     *     Helper method to delete a database according to the
     *     path and taskId. Path is the collection path of the
     *     user's own task collection and taskId is the actual
     *     task to delete.
     * </p>
     * @param path The collection path belonging to the user
     * @param taskToDelete The task to delete
     * @return An instance of this database.
     */
    private Database deleteTaskByPath(String path, final Task taskToDelete) {
        mFirestore
                .collection(path)
                .document(taskToDelete.getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(
                            @NonNull com.google.android.gms.tasks.Task<Void> task) {
                        informListener(DELETE_EVENT, taskToDelete);
                    }
                });
        return this;
    }

    /**
     * Utility method to add a task according to user's collection of tasks.
     * <p>
     *     Helper method to add a task according to the
     *     user's own path where the user has permission to
     *     read and write.
     * </p>
     * @param path The collection path belonging to the user
     * @param taskToAdd The task to add
     * @return An instance of this database.
     */
    private Database addTaskByPath(String path, final Task taskToAdd) {
        mFirestore
                .collection(path)
                .document(taskToAdd.getId())
                .set(taskToAdd)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(
                            @NonNull com.google.android.gms.tasks.Task<Void> task) {
                        informListener(ADD_EVENT, taskToAdd);
                    }
                });
        return this;
    }

    /**
     * Utility method to get the collection of a user's tasks
     * <p>
     *     Helper method to get the collection of a
     *     user's tasks in the database.
     * </p>
     * @param path The collection path belonging to the user
     * @return An instance of this database.
     */
    private Database getAllTaskByPath(String path) {
        final List<Task> result = new ArrayList<>();
        mFirestore.collection(path)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot r : task.getResult()) {
                            result.add(r.toObject(Task.class));
                        }
                        informListener(GET_EVENT, result);
                    }
                });
        return this;
    }

    /**
     * Utility method to inform any listeners about completion of events.
     * <p>
     *     Helper method to inform listeners that some kind of
     *     event has been completed. The event is defined by the
     *     status code and any arguments to be passed in is
     *     passed in as the payload in the arguments. If
     *     there are no listeners currently registered, then
     *     this method does nothing, otherwise it will inform
     *     the listener of any event that has completed with
     *     the appropriate payload.
     * </p>
     * @param statusCode The type of event being informed.
     * @param payload The data being passed back.
     */
    private void informListener(int statusCode,
                                @NonNull Object payload) {
        if(!isAnyoneListeningForEvents()) {
            return;
        }
        switch(statusCode) {
            case ADD_EVENT :
                Task task = (Task) payload;
                mLogger.logAddMessage(task.getId());
                mStateChangeListener.onAdd(task);
                break;
            case GET_EVENT :
                /*
                 * Do ensure that payload being passed into this method is
                 * of type List<Task>. Due to java's erasure of types,
                 * it is troublesome to safely cast it to List<Task> without
                 * additional steps. Anyway since this method is private and
                 * only used here in this class, it is quite easy to
                 * find the culprit
                 */
                //noinspection unchecked
                mLogger.logGetMessage((List<Task>) payload);
                //noinspection unchecked
                mStateChangeListener.onGet((List<Task>) payload);
                break;
            case UPDATE_EVENT :
                mLogger.logUpdateMessage((String)payload);
                mStateChangeListener.onUpdate((String) payload);
                break;
            case DELETE_EVENT :
                Task deleted = (Task) payload;
                mLogger.logDeleteMessage(deleted.getId());
                mStateChangeListener.onDelete(deleted);
                break;
            default :
                //If you ever get this message, go die. What are you even doing?
                mLogger.logMessage("Fatal error. Unrecognized status code in listeners");
        }
    }

    /**
     * Checks if any listeners are registered to this instance.
     *
     * @return True if there is a listener attached, false otherwise.
     */
    private boolean isAnyoneListeningForEvents() {
        return mStateChangeListener != null;
    }

    private boolean validateUser(FirebaseUser user) {
        return user != null;
    }

    private boolean validateUserTask(FirebaseUser user, Task task) {
        return validateUser(user) && task != null;
    }

    private boolean validateUserTaskChange(FirebaseUser user, TaskChange taskChange) {
        return validateUser(user) && taskChange != null;
    }

    /**
     * Constructs the collection path leading to the collection owned by user.
     * <p>
     *     This method constructs the collection path that
     *     wil lead to the collection of tasks that are owned by
     *     the user. By default, if the user is signed in properly,
     *     the user is allowed full read and write access to that
     *     portion of the database.
     * </p>
     * <p>
     *     In order to change debug and release build, pass in a different
     *     root paths into the arguments.
     * </p>
     * @param rootPath The starting root of the path determines dev or release build.
     * @param userPath The path of the user, usually the user's firebase UID.
     * @return The string describing the path to user's tasks collection
     */
    private String constructUsersDatabasePath(String rootPath, String userPath, String userCollectionPath) {
        if(userPath == null) {
            return rootPath + USERS_COLLECTION;
        } else {
            return rootPath + USERS_COLLECTION + userPath + userCollectionPath;
        }
    }

    /**
     * A callback interface for event completion for async firestore events.
     * <p>
     *     This interface describes the callback methods that
     *     are available to listeners of the database events.
     *     Generally, these methods follow the 4 CRUD methods
     *     for a basic database and each of the 4 methods have
     *     their own payload data that is passed in the callback
     *     after async function has finished.
     * </p>
     *
     */
    public interface OnDatabaseStateChangeListener {
        void onAdd(Task task);
        void onUpdate(String taskId);
        void onDelete(Task task);
        void onGet(List<Task> task);
    }
}
