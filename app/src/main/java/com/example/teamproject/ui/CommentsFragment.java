package com.example.teamproject.ui;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.teamproject.R;
import com.example.teamproject.model.CommentsListAdapter;
import com.example.teamproject.model.FirebaseReview;
import com.example.teamproject.model.FirebaseReviewVersion;
import com.example.teamproject.model.SingleComment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends Fragment {
    private static final String TAG = "CommentsFragment";
    ListView commentsListView;
    String currentUser_username;
    int DocumentVersion = 0;

    // Firebase
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;

    public CommentsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View currentView =  inflater.inflate(R.layout.fragment_comments, container, false);

        /***************************
         *  Firebase Authentication.
         ***************************/
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        /************************************
         *  Configure Views for List Adapter.
         ************************************/
        currentUser_username = ((StartTheReview)getActivity()).logged_in_username;
        DocumentVersion = ((StartTheReview)getActivity()).current_version;
        ((StartTheReview)getActivity()).CommentsView = currentView;
        commentsListView = (ListView) currentView.findViewById(R.id.listview_comments);
        ((StartTheReview)getActivity()).CommentsListView = commentsListView;

        /************************************
         *  Load the Comments from Firebase.
         ************************************/
        RetrieveComments(String.valueOf(DocumentVersion));

        return currentView;
    }

    /***************************************************************
     *  When selecting the tab, pull variable from the Host Activity.
     ***************************************************************/
    @Override
    public void onAttach(Context context)
    {
        // Required when switching through versions.
        DocumentVersion = ((StartTheReview)getActivity()).current_version;
        currentUser_username = ((StartTheReview)getActivity()).logged_in_username;

        super.onAttach(context);
    }

    /***************************************
     *  Obtains the comments from Firebase.
     ***************************************/
    public void RetrieveComments(String version) {
        // Obtain the context for the Comments Fragment. This is necessary to use the listener on the host activity.
        ((StartTheReview) getActivity()).CommentsContext = getContext();

        // Find the review version
        FirebaseReview CurrentReview = ((StartTheReview) getActivity()).SelectedReview;
        ArrayList<FirebaseReviewVersion> ReviewVersions = CurrentReview.getAllVersions();
        FirebaseReviewVersion CurrentReviewVersion = new FirebaseReviewVersion();
        for (int i = 0; i < ReviewVersions.size(); ++i) {
            if (ReviewVersions.get(i).Version().equals(version)) {
                CurrentReviewVersion = ReviewVersions.get(i);
                break;
            }
        }
        Log.d(TAG, "The Review UUID is: " + CurrentReview.UUID());
        Log.d(TAG, "The Review Version is: " + CurrentReviewVersion.Version());

        // Assume that the storage reference is already set and try to obtain list of comments.
        DatabaseReference comments_db = mDatabaseReference.child("open_reviews").child(CurrentReview.UUID()).child("versions").child(CurrentReviewVersion.Version()).child("comments");
        comments_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SingleComment> existing_comments = new ArrayList<SingleComment>();

                // Grab all the Comments.
                for (DataSnapshot data : snapshot.getChildren()) {
                    String comment_number = data.getKey();
                    String details = data.child("details").getValue(String.class);
                    String full_name = data.child("full_name").getValue(String.class);
                    String timestamp = data.child("timestamp").getValue(String.class);
                    String username = data.child("username").getValue(String.class);
                    String annotation_id = data.child("annotation_id").getValue(String.class);

                    Log.d(TAG, "Comment Found: " + comment_number);
                    SingleComment user_comment = new SingleComment(comment_number, full_name,
                            username, details, timestamp);
                    user_comment.setAnnotationID(annotation_id);

                    Log.d(TAG, "Username: " + user_comment.Username());
                    Log.d(TAG, "Full Name: " + user_comment.FullName());
                    Log.d(TAG, "Timestamp: " + user_comment.CreationDate());
                    Log.d(TAG, "Details: " + user_comment.Comment());
                    Log.d(TAG, "Annotation ID: " + user_comment.Comment());

                    // Add the comment to the list of comments.
                    existing_comments.add(user_comment);
                }

                // Finally, update with the complete list of comments.
                // We must add a try because this will be called again when we do an upload and may cause a crash.
                try {
                    ((StartTheReview) getActivity()).listOfComments = existing_comments;

                    // Check if there were actually any comments found. If not throw a snackbar message.
                    String snackbar_msg = "No comments were found, Add one in View tab.";
                    final Snackbar snackBar = Snackbar.make(getView(), snackbar_msg, Snackbar.LENGTH_INDEFINITE);
                    if (existing_comments.size() < 1) {
                        snackBar.setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Dismiss the snackbar when clicking on 'Dismiss'
                                snackBar.dismiss();
                            }
                        });
                        snackBar.show();
                        Log.d(TAG, "SnackBar: " + snackbar_msg);
                    } else {
                        Log.d(TAG, "New comments found, dismissing the Snackbar.");
                        snackBar.dismiss(); // In case user doesn't dismiss the dialog.
                    }
                    // Update the adapter.
                    CommentsListAdapter adapter = new CommentsListAdapter(((StartTheReview) getActivity()).CommentsContext, R.layout.display_comment, currentUser_username, ((StartTheReview) getActivity()).listOfComments);
                    ((StartTheReview) getActivity()).CommentsListView.setAdapter(adapter);

                } catch (Exception e) {
                    Log.d(TAG, "RetrieveComments: ERROR: Unable to update the List of comments. New updates were found on firebase after doing an upload!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // If the list doesn't exist or there is some error, just use show an empty list.
                Log.d(TAG, "No comments were found in the database!");

                List<SingleComment> existing_comments = new ArrayList<SingleComment>();

                // Generate an empty list.
                // We must add a try because this will be called again when we do an upload and may cause a crash.
                try {
                    ((StartTheReview) getActivity()).listOfComments = existing_comments;

                    // Update the adapter.
                    CommentsListAdapter adapter = new CommentsListAdapter(((StartTheReview) getActivity()).CommentsContext, R.layout.display_comment, currentUser_username, ((StartTheReview) getActivity()).listOfComments);
                    ((StartTheReview) getActivity()).CommentsListView.setAdapter(adapter);

                    // Display a snackbar message indicating no comments were found.
                    String snackbar_msg = "No comments were found, Add one in View tab.";
                    final Snackbar snackBar = Snackbar.make(getView(), snackbar_msg, Snackbar.LENGTH_INDEFINITE);
                    snackBar.setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Dismiss the snackbar when clicking on 'Dismiss'
                            snackBar.dismiss();
                        }
                    });
                    snackBar.show();
                    Log.d(TAG, "SnackBar: " + snackbar_msg);
                } catch (Exception e) {
                    Log.d(TAG, "RetrieveComments: ERROR: Unable to update the List of comments. Could be we are leaving the activity after modifying comments.");
                }
            }
        });
    }
}