package com.example.teamproject.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.teamproject.R;
import com.example.teamproject.model.SingleComment;

import java.util.List;

public class CommentsListAdapter extends ArrayAdapter<SingleComment> {
    // TODO: Decide whether there should be a button in display_comment.xml for deleting a comment.
    Context context;
    int xml_layout;

    // This is a list of all comments for the document version.
    List<SingleComment> VersionComments;

    //constructor initializing the values
    public CommentsListAdapter(Context context, int xml_layout, List<SingleComment> version_comments) {
        super(context, xml_layout, version_comments);
        this.context = context;
        this.xml_layout = xml_layout;
        this.VersionComments = version_comments;
    }

    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Get the view of the layout XML corresponding to the ListView.
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(xml_layout, null, false);

        // Update the TextViews with the data stored in the Comment object.
        TextView CommentFullName = view.findViewById(R.id.comment_fullname);
        TextView CommentUsername = view.findViewById(R.id.comment_username);
        Button CommentDetails = view.findViewById(R.id.button_comment);
        TextView CommentDateTime = view.findViewById(R.id.comment_datetime);

        // Get the current comment object.
        SingleComment current_comment = VersionComments.get(position);

        // Update all the TextViews corresponding to the comment (see display_comment.xml)
        CommentFullName.setText(current_comment.getFullName());
        CommentUsername.setText("@" + current_comment.getUsername());
        CommentDetails.setText(current_comment.getComment());
        CommentDateTime.setText(current_comment.getCreationDate());

        // Finally update the view to show the Comment object on the list adapter.
        return view;
    }
}
