package com.example.teamproject.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.teamproject.R;
import com.example.teamproject.model.SingleComment;
import com.example.teamproject.ui.CommentsFragment;
import com.example.teamproject.ui.StartTheReview;

import java.util.List;

public class CommentsListAdapter extends ArrayAdapter<SingleComment> {
    // TODO: Decide whether there should be a button in display_comment.xml for deleting a comment.
    Context context;
    View view;
    int xml_layout;
    Button CommentDetails;
    String logged_in_user;

    // This is a list of all comments for the document version.
    SingleComment current_comment;
    List<SingleComment> VersionComments;

    //constructor initializing the values
    public CommentsListAdapter(Context context, int xml_layout, String logged_in_user, List<SingleComment> version_comments) {
        super(context, xml_layout, version_comments);
        this.context = context;
        this.xml_layout = xml_layout;
        this.logged_in_user = logged_in_user;
        this.VersionComments = version_comments;
    }

    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the view of the layout XML corresponding to the ListView.
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(xml_layout, null, false);

        // Update the TextViews with the data stored in the Comment object.
        TextView CommentNumber = view.findViewById(R.id.comment_number);
        TextView CommentFullName = view.findViewById(R.id.comment_fullname);
        TextView CommentUsername = view.findViewById(R.id.comment_username);
        CommentDetails = view.findViewById(R.id.button_comment);
        TextView CommentDateTime = view.findViewById(R.id.comment_datetime);

        // Get the current comment object.
        current_comment = VersionComments.get(position);

        // Update all the TextViews corresponding to the comment (see display_comment.xml)
        CommentNumber.setText("#" + String.valueOf(position + 1));
        CommentFullName.setText(current_comment.FullName());
        CommentUsername.setText("@" + current_comment.Username());
        CommentDetails.setText(current_comment.Comment());
        CommentDateTime.setText(current_comment.CreationDate());

        // Finally update the view to show the Comment object on the list adapter.
        return view;
    }
}
