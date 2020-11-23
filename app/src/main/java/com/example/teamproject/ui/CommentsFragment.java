package com.example.teamproject.ui;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.teamproject.R;
import com.example.teamproject.model.CommentsListAdapter;
import com.example.teamproject.model.ProfileSettings;
import com.example.teamproject.model.SingleComment;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends Fragment {

    private Boolean DebugMode = true;
    List<SingleComment> listOfComments;
    ListView commentsListView;
    int DocumentVersion = 0;

    String currentUser_username;

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

        // Load these crucial variables for use by the List Adapter.
        currentUser_username = ((StartTheReview)getActivity()).logged_in_username;
        DocumentVersion = ((StartTheReview)getActivity()).current_version;

        // Since this Fragment contains a ListView we must set the adapter and load it.
        commentsListView = (ListView) currentView.findViewById(R.id.listview_comments);
        listOfComments = new ArrayList<SingleComment>();

        // Load the comment list.
        if (DebugMode) {
            // TODO: Replace this with a return from the host activity, or database call.
            listOfComments = GetCommentTestSamples(DocumentVersion);
        }

        // Create the Comments List Adapter and then set the adapter to the Comments List View.
        CommentsListAdapter adapter = new CommentsListAdapter(getContext(), R.layout.display_comment, currentUser_username, listOfComments);
        commentsListView.setAdapter(adapter);

        return currentView;
    }

    /***************************************************************
     *  When selecting the tab, pull variable from the Host Activity.
     ***************************************************************/
    @Override
    public void onAttach(Context context)
    {
        if (DebugMode) {
            DocumentVersion = ((StartTheReview)getActivity()).current_version;
            currentUser_username = ((StartTheReview)getActivity()).logged_in_username;
        }
        super.onAttach(context);
    }

    public List<SingleComment> GetCommentTestSamples(int version) {
        // Returns a Test Sample list based on the version.
        List<SingleComment> SampleComments = new ArrayList<SingleComment>();

        // Versions 1-3 always display 3 comments.
        if (version == 1) {
            SingleComment comment1 = new SingleComment(
                    "1",
                    "John Doe",
                    "johndoe",
                    "Make sure to include the correct date.",
                    "10/31/2020 05:34"
            );
            SingleComment comment2 = new SingleComment(
                    "2",
                    "Mary Sue",
                    "marysue",
                    "You forgot to add a period at the end of the sentence.",
                    "11/01/2020 12:25"
            );
            SingleComment comment3 = new SingleComment(
                    "3",
                    "Alex Mac",
                    "alexmac",
                    "You should probably expand on the details here some more.",
                    "11/02/2020 18:22"
            );
            SampleComments.add(comment1);
            SampleComments.add(comment2);
            SampleComments.add(comment3);
        } else if (version == 2) {
            SingleComment comment1 = new SingleComment(
                    "1",
                    "Sarah Page",
                    "sarahpage",
                    "The formatting of the table is wrong, please fix it.",
                    "11/03/2020 06:59"
            );
            SingleComment comment2 = new SingleComment(
                    "2",
                    "Harper Yue",
                    "harperyue",
                    "Excellent work on the sourcing!",
                    "11/03/2020 14:44"
            );
            SampleComments.add(comment1);
            SampleComments.add(comment2);
        } else if (version == 3) {
            SingleComment comment1 = new SingleComment(
                    "1",
                    "Samuel Radcliff",
                    "sradcliff",
                    "Coding standards are well documented. Keep up the good work!",
                    "11/04/2020 15:53"
            );
            SampleComments.add(comment1);
        }
        return SampleComments;
    }
}