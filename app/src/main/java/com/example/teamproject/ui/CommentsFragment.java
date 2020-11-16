package com.example.teamproject.ui;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.teamproject.R;
import com.example.teamproject.model.CommentsListAdapter;
import com.example.teamproject.model.SingleComment;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends Fragment {

    private Boolean DebugMode = true;
    List<SingleComment> listOfComments;
    ListView commentsListView;
    int DocumentVersion = 0;

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

        // Since this Fragment contains a ListView we must set the adapter and load it.
        commentsListView = (ListView) currentView.findViewById(R.id.listview_comments);
        listOfComments = new ArrayList<SingleComment>();

        // Load the comment list.
        if (DebugMode) {
            // TODO: Replace this with a return from the host activity, or database call.
            DocumentVersion = ((ReviewActivity)getActivity()).current_version;
            listOfComments = GetCommentTestSamples(DocumentVersion);
        }

        // Create the Comments List Adapter and then set the adapter to the Comments List View.
        CommentsListAdapter adapter = new CommentsListAdapter(getContext(), R.layout.display_comment, listOfComments);
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
            DocumentVersion = ((ReviewActivity)getActivity()).current_version;
        }
        super.onAttach(context);
    }

    /*******************************************************
     *  When swapping between tabs, reload the fragment.
     *******************************************************/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }


    public List<SingleComment> GetCommentTestSamples(int version) {
        // Returns a Test Sample list based on the version.
        List<SingleComment> SampleComments = new ArrayList<SingleComment>();

        // Versions 1-3 always display 3 comments.
        if (version == 1) {
            SingleComment comment1 = new SingleComment(
                    "John Doe",
                    "johndoe",
                    "Make sure to include the correct date.",
                    "10/31/2020 05:34"
            );
            SingleComment comment2 = new SingleComment(
                    "Mary Sue",
                    "marysue",
                    "You forgot to add a period at the end of the sentence.",
                    "11/01/2020 12:25"
            );
            SingleComment comment3 = new SingleComment(
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
                    "Sarah Page",
                    "sarahpage",
                    "The formatting of the table is wrong, please fix it.",
                    "11/03/2020 06:59"
            );
            SingleComment comment2 = new SingleComment(
                    "Harper Yue",
                    "harperyue",
                    "Excellent work on the sourcing!",
                    "11/03/2020 14:44"
            );
            SingleComment comment3 = new SingleComment(
                    "Felix Monger",
                    "felixmonger",
                    "You forgot to explain how to enable Firebase through Gradle!",
                    "11/04/2020 00:15"
            );
            SampleComments.add(comment1);
            SampleComments.add(comment2);
            SampleComments.add(comment3);
        } else if (version == 3) {
            SingleComment comment1 = new SingleComment(
                    "Samuel Radcliff",
                    "sradcliff",
                    "Coding standards are well documented. Keep up the good work!",
                    "11/04/2020 15:53"
            );
            SingleComment comment2 = new SingleComment(
                    "Jim Que",
                    "jimque",
                    "Please shorten your summary statement. It is far too long.",
                    "11/05/2020 19:32"
            );
            SingleComment comment3 = new SingleComment(
                    "Carrey Fisher",
                    "cfisher",
                    "Don't forget to include more pictures. It adds more spice!",
                    "11/05/2020 23:44"
            );
            SampleComments.add(comment1);
            SampleComments.add(comment2);
            SampleComments.add(comment3);
        }
        return SampleComments;
    }
}