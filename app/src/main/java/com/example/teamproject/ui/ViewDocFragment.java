package com.example.teamproject.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.teamproject.R;
import com.example.teamproject.model.CommentsListAdapter;
import com.example.teamproject.model.FirebaseReviewVersion;
import com.example.teamproject.model.SingleComment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pdftron.common.PDFNetException;
import com.pdftron.fdf.FDFDoc;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.ColorPt;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFNet;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.Page;
import com.pdftron.pdf.Rect;
import com.pdftron.pdf.annots.Highlight;
import com.pdftron.pdf.config.ToolManagerBuilder;
import com.pdftron.pdf.controls.AnnotationToolbar;
import com.pdftron.pdf.controls.AnnotationToolbarButtonId;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.pdf.tools.UndoRedoManager;
import com.pdftron.pdf.utils.AppUtils;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.sdf.Obj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Single;

import static android.content.ContentValues.TAG;

public class ViewDocFragment extends Fragment {
    private static final String TAG = "ViewPDF";

    // Firebase
    StorageReference mStorageReference;

    // PDFTron variables.
    private View currentView;
    PDFViewCtrl mPdfViewCtrl;
    PDFDoc mPdfDoc;
    private ToolManager mToolManager;
    private AnnotationToolbar mAnnotationToolbar;

    // Variables used to load the PDF file & annotations.
    private int DocumentVersion;
    private String pdf_url; // The URL accessible with a tokenized link.
    private FirebaseReviewVersion CurrentReviewVersion;

    public ViewDocFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_view, container, false);

        /***************************
         *  Firebase Authentication.
         ***************************/
        mStorageReference = FirebaseStorage.getInstance().getReference();

        /**************************************************************************************
         *  PDFView implementation (for viewing PDFs in a Fragment)
         *  Git source:
         *  https://github.com/PDFTron/pdftron-android-samples/tree/master/PDFViewCtrlViewer
         *************************************************************************************/
        mPdfViewCtrl = currentView.findViewById(R.id.pdfviewctrl);
        setupToolManager();
        setupAnnotationToolbar();

        /******************************
         *  Get PDF Url from Firebase.
         *****************************/
        DocumentVersion = ((StartTheReview)getActivity()).current_version;
        LoadVersionPDF(String.valueOf(DocumentVersion));

        return currentView;
    }

    /***************************************************************
     *  When selecting the tab, pull variable from the Host Activity.
     ***************************************************************/
    @Override
    public void onAttach(Context context)
    {
        // Obtain the Document Version from the main activity.
        DocumentVersion = ((StartTheReview)getActivity()).current_version;

        super.onAttach(context);
    }

    /*******************************************************************
     *  Function for obtaining the PDF URL for a given Review Version
     *  and saving it to a Local Cache to add annotations.
     *******************************************************************/
    public void LoadVersionPDF(String version) {
        // Loads the Version PDF based on the version provided in the host activity.
        ArrayList<FirebaseReviewVersion> ReviewVersions = ((StartTheReview)getActivity()).SelectedReview.getAllVersions();
        CurrentReviewVersion = new FirebaseReviewVersion();
        for (int i = 0; i < ReviewVersions.size(); ++i) {
            if (ReviewVersions.get(i).Version().equals(version)) {
                CurrentReviewVersion = ReviewVersions.get(i);
                break;
            }
        }

        // There should ALWAYS be a valid PDF that is returned here.
        Log.d(TAG, "The Review Version is: " + CurrentReviewVersion.Version());
        String firebase_pdf_file = CurrentReviewVersion.PDF();
        Log.d(TAG, "The Firebase PDF File found: " + firebase_pdf_file);

        // Destination local cached PDF file.
        final String cachePDF_filename = ((StartTheReview)getActivity()).CacheDirectory + "/" + "upload.pdf";
        Log.d(TAG, "The Destination Cache PDF File: " + cachePDF_filename);

        // Download the firebase PDF as a local cached PDF file.
        final File CachePDFFile = new File(cachePDF_filename);
        mStorageReference.child(firebase_pdf_file).getFile(CachePDFFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // After successfully downloading the PDF File, get the annotations.
                UpdatePDFView(cachePDF_filename);
            }
        });
    }
    public void UpdatePDFView(String local_pdf_filename) {
        try {
            AppUtils.setupPDFViewCtrl(mPdfViewCtrl);
            PDFViewZoomSettings(); // Setup the view settings.

            // Load the PDF URL into PDF Tron.
            Log.d(TAG, "Loading the PDF from a Cached PDF file: " + local_pdf_filename);
            viewFromCachedFile(local_pdf_filename);
        } catch (PDFNetException e) {
            // This should NEVER occur when opening this fragment.
            Log.e(TAG, "Error setting up PDFViewCtrl");
        }
    }

    /*****************************************************************
     * Helper functions for loading a PDF from a resource file or URL
     *****************************************************************/
    public void viewFromCachedFile(String cached_pdf_filename) throws PDFNetException {
        final String cache_annotation_file = ((StartTheReview)getActivity()).CacheDirectory + "/" + "upload.xfdf";

        Log.d(TAG, "viewFromCachedFile: Cache PDF file: " + cached_pdf_filename);
        try {
            // Use the cache file directly in order to work with current annotations.
            ((StartTheReview)getActivity()).CurrentPDFDoc = new PDFDoc(cached_pdf_filename);

            // Only imports annotations if available from Firebase and if download was successful.
            if (!((StartTheReview)getActivity()).FirebaseAnnotationFilename.equals("none")) {
                Log.w(TAG, "viewFromCachedFile: There is an XFDF file in firebase waiting to be downloaded.");

                // Get the firebase url for the XFDF file and save it to a cached file directly.
                CurrentReviewVersion.setAnnotationFile(((StartTheReview)getActivity()).FirebaseAnnotationFilename);
                final String firebase_xfdf_file = CurrentReviewVersion.AnnotationFile();
                final File annot_file = new File(cache_annotation_file);
                mStorageReference.child(firebase_xfdf_file).getFile(annot_file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Upon successfully downloading the XFDF file to the local file, load the annotations.
                        Log.d(TAG, "viewFromCachedFile: Successfully downloaded the XFDF file from firebase storage to: " + cache_annotation_file);
                        try {
                            FDFDoc fdf_doc = FDFDoc.createFromXFDF(cache_annotation_file);
                            ((StartTheReview)getActivity()).CurrentPDFDoc.fdfMerge(fdf_doc);
                            Log.w(TAG, "viewFromCachedFile: Merged annotations with currently viewable PDF.");

                            // Finally, load the cached PDF (with annotations).
                            mPdfViewCtrl.setDoc(((StartTheReview)getActivity()).CurrentPDFDoc);
                            Log.d(TAG, "viewFromCachedFile: Loaded Cached PDF w/ Annotations.");
                        } catch (Exception e) {
                            Log.e(TAG, "viewFromCachedFile: Could not load annotations from XFDF file!");
                            e.getStackTrace();
                        }
                    }
                });
                // Only on success do we load the annotations. Otherwise we do not load anything.
            } else {
                Log.w(TAG, "viewFromCachedFile: No annotations found for the Review Version in firebase. Ignoring annotation merge.");
                // Finally, load the cached PDF (without annotations).
                mPdfViewCtrl.setDoc(((StartTheReview)getActivity()).CurrentPDFDoc);
                Log.d(TAG, "viewFromCachedFile: Loaded Cached PDF w/out Annotations.");
            }
        } catch (Exception e) {
            Log.e(TAG, "viewFromCachedFile: Some error occurred when trying to open the PDF Url!");
            e.printStackTrace();
        }
    }

    /**************************************************************************************
     *  PDFTRON FUNCTIONS for PDFView
     *************************************************************************************/

    /****************************************************************
     * Helper function to set up and initialize the ToolManager.
     ****************************************************************/
    public void setupToolManager() {
        mToolManager = ToolManagerBuilder.from()
                .build(getActivity(), mPdfViewCtrl);

        /************************************************************************************
         *  Listeners for PDFTron Annotations.
         *  AnnotationModificationListener is used to react to new annotation actions.
         *  In our case we are interested in AFTER user highlights text or makes a comment.
         *  We want to load either a custom dialog box for making a comment, or simply
         *  modify the contents of the annotation.
         ***********************************************************************************/
        ToolManager.AnnotationModificationListener annotationModificationListener = new ToolManager.AnnotationModificationListener() {
            @Override
            public void onAnnotationsAdded(Map<Annot, Integer> map) {
                // Lets make a "Comment" generate a SingleComment that is added to the
                // List of comments.

                // Parse through the entire map and look for the Annotation.
                for (Map.Entry<Annot, Integer> entry : map.entrySet()) {
                    Annot current_annot = entry.getKey();
                    Log.d(TAG, "AnnotationsAdded: Key: " + String.valueOf(current_annot));
                    Log.d(TAG, "AnnotationsAdded: Page: " + entry.getValue());

                    // Generate a unique ID for this annotation to keep track of it.
                    String fixed_annotation_uuid = UUID.randomUUID().toString().replace("-","");
                    try {
                        // Only set on first time adding.
                        current_annot.setCustomData("FixedID", fixed_annotation_uuid);
                        String fixed_uuid = current_annot.getCustomData("FixedID");
                        Log.d(TAG, "AnnotationsAdded: Fixed UUID: " + fixed_uuid);
                        if (current_annot.getType() == Annot.e_Text) {
                            Log.d(TAG, "AnnotationsAdded: This is a Comment Text Box.");
                            String comment_text = current_annot.getContents();
                            Log.d(TAG, "AnnotationsAdded: Comment Text: " + comment_text);
                        }
                    } catch (PDFNetException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAnnotationsPreModify(Map<Annot, Integer> map) {
                for (Map.Entry<Annot, Integer> entry : map.entrySet()) {
                    Annot current_annot = entry.getKey();
                    Log.d(TAG, "AnnotationsPreModify: Key: " + String.valueOf(current_annot));
                    Log.d(TAG, "AnnotationsPreModify: Page: " + entry.getValue());
                    try {
                        String fixed_uuid = current_annot.getCustomData("FixedID");
                        Log.d(TAG, "AnnotationsPreModify: Fixed UUID: " + fixed_uuid);
                        if (current_annot.getType() == Annot.e_Text) {
                            Log.d(TAG, "AnnotationsPreModify: This is a Comment Text Box.");
                            String comment_text = current_annot.getContents();
                            Log.d(TAG, "AnnotationsPreModify: Comment Text: " + comment_text);
                        }
                    } catch (PDFNetException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAnnotationsModified(Map<Annot, Integer> map, Bundle bundle) {
                for (Map.Entry<Annot, Integer> entry : map.entrySet()) {
                    Annot current_annot = entry.getKey();
                    Log.d(TAG, "AnnotationsModified: Key: " + String.valueOf(current_annot));
                    Log.d(TAG, "AnnotationsModified: Page: " + entry.getValue());
                    try {
                        String fixed_uuid = current_annot.getCustomData("FixedID");
                        Log.d(TAG, "AnnotationsModified: Fixed UUID: " + fixed_uuid);

                        // Only on modified Text Boxes, do something.
                        if (current_annot.getType() == Annot.e_Text) {
                            Log.d(TAG, "AnnotationsModified: This is a Comment Text Box.");
                            String comment_text = current_annot.getContents();
                            Log.d(TAG, "AnnotationsModified: Comment Text: " + comment_text);

                            // Check the list of comments. Add new comment after latest comment found.
                            Log.d(TAG, "AnnotationsModified: Checking for existing comments...");
                            if (((StartTheReview)getActivity()).listOfComments.size() > 0) {
                                // Find the last comment number and increment by 1.
                                int comment_count = 0;
                                Boolean existing_comment = false;
                                int found_comment_index = 0;
                                for (int idx = 0; idx < ((StartTheReview)getActivity()).listOfComments.size(); ++idx) {
                                    SingleComment current_comment = ((StartTheReview)getActivity()).listOfComments.get(idx);
                                    comment_count = Integer.parseInt(current_comment.CommentNumber());

                                    // Check if the comment exists, and if it does, stop counting.
                                    if (current_comment.AnnotationID().equals(fixed_uuid)) {
                                        Log.d(TAG, "AnnotationsModified: The Comment already exists in Loaded Comments list!");
                                        existing_comment = true;
                                        found_comment_index = idx;
                                        break;
                                    }
                                }

                                // If the comment already existed, simply update the comment.
                                if (existing_comment) {
                                    // Since the UUID matched, we need to grab existing comment info and update it.
                                    // Before updating, make sure the username matches!
                                    if (((StartTheReview)getActivity()).listOfComments.get(found_comment_index).Username().equals(((StartTheReview)getActivity()).logged_in_username)) {
                                        ((StartTheReview)getActivity()).listOfComments.get(found_comment_index).setComment(comment_text);
                                        ((StartTheReview)getActivity()).listOfComments.get(found_comment_index).setCreation_date();
                                        Log.d(TAG, "AnnotationsModified: Updated Live list of comments for Comments Fragment.");

                                        // Update the UpdatedComments list for Firebase.
                                        // If the comment is already in the the list, update the entries only.
                                        Boolean updated_comment_exists = false;
                                        for (int child_idx = 0; child_idx < ((StartTheReview)getActivity()).UpdatedComments.size(); ++child_idx) {
                                            if (((StartTheReview)getActivity()).UpdatedComments.get(child_idx).CommentNumber().equals(String.valueOf(comment_count))) {
                                                // If the comment already exists, just edit the values.
                                                updated_comment_exists = true;
                                                ((StartTheReview)getActivity()).UpdatedComments.get(child_idx).setComment(comment_text);
                                                ((StartTheReview)getActivity()).UpdatedComments.get(child_idx).setCreation_date();
                                                Log.d(TAG, "AnnotationsModified: UpdatedComments already was updated before. Therefore, simply update the entry.");
                                                break;
                                            }
                                        }
                                        if (!updated_comment_exists) {
                                            // If there is no entry in updated comments. Just add to the UpdatedComments list.
                                            Log.d(TAG, "AnnotationsModified: UpdatedComments new entry! Adding the entry.");
                                            ((StartTheReview)getActivity()).UpdatedComments.add(((StartTheReview)getActivity()).listOfComments.get(found_comment_index));
                                        }
                                    } else {
                                        // The comment doesn't belong to the logged in user. Return the comment back to it's original value.
                                        current_annot.setContents(((StartTheReview)getActivity()).listOfComments.get(found_comment_index).Comment());
                                        Log.d(TAG, "AnnotationsModified: Ownership conflict! Restored comment text back to the original value.");
                                        Toast.makeText(getContext(), "That's not your comment! Can't let you modify it!", Toast.LENGTH_LONG).show();
                                        // TODO: Set the date back as well?
                                    }

                                } else {
                                    // This is a brand new comment. So add it to the list.
                                    Log.d(TAG, "AnnotationsModified: Brand new comment in a list of already loaded comments.");
                                    ++comment_count; // Increment by 1.
                                    SingleComment new_comment = new SingleComment(
                                            String.valueOf(comment_count),
                                            ((StartTheReview)getActivity()).CurrentUser.FullName(),
                                            ((StartTheReview)getActivity()).CurrentUser.Username(),
                                            comment_text, "");
                                    new_comment.setCreation_date();
                                    new_comment.setAnnotationID(fixed_uuid);

                                    // Update the list of new comments.
                                    ((StartTheReview)getActivity()).listOfComments.add(new_comment);
                                    ((StartTheReview)getActivity()).NewComments.add(new_comment);
                                }
                            } else {
                                // No comments currently exist, therefore add the first comment.
                                Log.d(TAG, "AnnotationsModified: Brand new comment in a an empty list.");
                                SingleComment new_comment = new SingleComment(
                                        "1",
                                        ((StartTheReview)getActivity()).CurrentUser.FullName(),
                                        ((StartTheReview)getActivity()).CurrentUser.Username(),
                                        comment_text, "");
                                new_comment.setCreation_date();
                                new_comment.setAnnotationID(fixed_uuid);

                                // Update the list of new comments.
                                ((StartTheReview)getActivity()).listOfComments.add(new_comment);
                                ((StartTheReview)getActivity()).NewComments.add(new_comment);
                            }

                            // Finally, Update the adapter.
                            Log.d(TAG, "AnnotationsModified: Updating the Comments List Adapter.");
                            CommentsListAdapter adapter = new CommentsListAdapter(((StartTheReview) getActivity()).CommentsContext, R.layout.display_comment, ((StartTheReview)getActivity()).logged_in_username, ((StartTheReview) getActivity()).listOfComments);
                            ((StartTheReview) getActivity()).CommentsListView.setAdapter(adapter);
                        }
                    } catch (PDFNetException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAnnotationsPreRemove(Map<Annot, Integer> map) {
                for (Map.Entry<Annot, Integer> entry : map.entrySet()) {
                    Annot current_annot = entry.getKey();
                    Log.d(TAG, "AnnotationsPreRemove: Key: " + String.valueOf(current_annot));
                    Log.d(TAG, "AnnotationsPreRemove: Page: " + entry.getValue());

                    try {
                        String fixed_uuid = current_annot.getCustomData("FixedID");
                        Log.d(TAG, "AnnotationsPreRemove: Fixed UUID: " + fixed_uuid);
                        if (current_annot.getType() == Annot.e_Text) {
                            Log.d(TAG, "AnnotationsPreRemove: This is a Comment Text Box.");
                            String comment_text = current_annot.getContents();
                            Log.d(TAG, "AnnotationsPreRemove: Comment Text: " + comment_text);
                        }
                    } catch (PDFNetException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAnnotationsRemoved(Map<Annot, Integer> map) {
                for (Map.Entry<Annot, Integer> entry : map.entrySet()) {
                    Annot current_annot = entry.getKey();
                    Log.d(TAG, "AnnotationsRemoved: Key: " + String.valueOf(current_annot));
                    Log.d(TAG, "AnnotationsRemoved: Page: " + entry.getValue());
                    try {
                        String fixed_uuid = current_annot.getCustomData("FixedID");
                        Log.d(TAG, "AnnotationsRemoved: Fixed UUID: " + fixed_uuid);

                        // If the annotation was removed, find the comment, and delete it, if you are the owner.
                        if (current_annot.getType() == Annot.e_Text) {
                            Log.d(TAG, "AnnotationsRemoved: This is a Comment Text Box.");
                            String comment_text = current_annot.getContents();
                            Log.d(TAG, "AnnotationsRemoved: Comment Text: " + comment_text);

                            // Check the list of comments. If found, delete the comment.
                            Log.d(TAG, "AnnotationsRemoved: Checking for existing comments...");
                            if (((StartTheReview)getActivity()).listOfComments.size() > 0) {
                                Log.d(TAG, "AnnotationsRemoved: The List of Comments exists!");

                                // Look for a match in listOfComments for UUID, and if found, delete the commment.
                                int comment_count = 0;
                                Boolean existing_comment = false;
                                int found_comment_index = 0;
                                for (int idx = 0; idx < ((StartTheReview)getActivity()).listOfComments.size(); ++idx) {
                                    SingleComment current_comment = ((StartTheReview)getActivity()).listOfComments.get(idx);
                                    comment_count = Integer.parseInt(current_comment.CommentNumber());

                                    // Check if the comment exists, and if it does, stop counting.
                                    if (current_comment.AnnotationID().equals(fixed_uuid)) {
                                        Log.d(TAG, "AnnotationsRemoved: Found UUID: " + current_comment.AnnotationID());
                                        Log.d(TAG, "AnnotationsRemoved: The Comment already exists in Loaded Comments list!");
                                        existing_comment = true;
                                        found_comment_index = idx;
                                        break;
                                    }
                                }

                                // If the comment was found matching the UUID, remove the comment.
                                if(existing_comment) {
                                    if (((StartTheReview)getActivity()).listOfComments.get(found_comment_index).Username().equals(((StartTheReview)getActivity()).logged_in_username)) {
                                        Log.d(TAG, "AnnotationsRemoved: Removing the comment from the loaded list of comments.");
                                        ((StartTheReview)getActivity()).DeletedComments.add(((StartTheReview)getActivity()).listOfComments.get(found_comment_index));
                                        ((StartTheReview)getActivity()).listOfComments.remove(found_comment_index);
                                    } else {
                                        // The user is not authorized to delete. So undo the deletion.
                                        Log.d(TAG, "AnnotationsRemoved: User is not authorized to delete the comment. Undoing...");
                                        Toast.makeText(getContext(), "That's not your comment! Can't let you delete it!", Toast.LENGTH_LONG).show();
                                        try {
                                            String undoInfo = mToolManager.getUndoRedoManger().undo();
                                            UndoRedoManager.jumpToUndoRedo(mPdfViewCtrl, undoInfo, true);
                                            Log.d(TAG, "AnnotationsRemoved: Successfully undo'd the comment deletion.");
                                        } catch (Exception e) {
                                            Log.e(TAG, "AnnotationsRemoved: Could not undo the deleted comment.");
                                            e.getStackTrace();
                                        }
                                    }
                                }
                            } else {
                                Log.d(TAG, "AnnotationsRemoved: There are no existing comments to delete!");
                                // There currently is no entries in the list of comments, so don't do anything.
                            }

                            // Finally, Update the adapter.
                            Log.d(TAG, "AnnotationsRemoved: Updating the Comments List Adapter.");
                            CommentsListAdapter adapter = new CommentsListAdapter(((StartTheReview) getActivity()).CommentsContext, R.layout.display_comment, ((StartTheReview)getActivity()).logged_in_username, ((StartTheReview) getActivity()).listOfComments);
                            ((StartTheReview) getActivity()).CommentsListView.setAdapter(adapter);
                        }
                    } catch (PDFNetException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onAnnotationsRemovedOnPage(int i) {

            }

            @Override
            public void annotationsCouldNotBeAdded(String s) {

            }
        };
        mToolManager.addAnnotationModificationListener(annotationModificationListener);

    }

            /****************************************************************
     * Helper function to set up and initialize the AnnotationToolbar.
     ****************************************************************/
    public void setupAnnotationToolbar() {
        mAnnotationToolbar = currentView.findViewById(R.id.annotationToolbar);
        // Remember to initialize your ToolManager before calling setup
        mAnnotationToolbar.setup(mToolManager);
        mAnnotationToolbar.hideButton(AnnotationToolbarButtonId.CLOSE);
        mAnnotationToolbar.show();
    }

    /************************************************************
     * Configures the Zoom control options for the PDF Viewer.
     ************************************************************/
    private void PDFViewZoomSettings() throws PDFNetException {
        // Enable maintain zoom level
        mPdfViewCtrl.setMaintainZoomEnabled(true);
        // Set preferred view mode to PageViewMode.FIT_PAGE
        mPdfViewCtrl.setPreferredViewMode(PDFViewCtrl.PageViewMode.FIT_WIDTH);
        // Set PageViewMode.FIT_PAGE to current page view mode
        mPdfViewCtrl.setPageViewMode(PDFViewCtrl.PageViewMode.FIT_WIDTH);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Handle configuration changes from the toolbar here
        mAnnotationToolbar.onConfigurationChanged(newConfig);
    }

    /**
     * We need to clean up and handle PDFViewCtrl based on Android lifecycle callback.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mPdfViewCtrl != null) {
            mPdfViewCtrl.pause();
            mPdfViewCtrl.purgeMemory();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPdfViewCtrl != null) {
            mPdfViewCtrl.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPdfViewCtrl != null) {
            mPdfViewCtrl.destroy();
            mPdfViewCtrl = null;
        }

        if (((StartTheReview)getActivity()).CurrentPDFDoc != null) {
            try {
                ((StartTheReview)getActivity()).CurrentPDFDoc.close();
            } catch (Exception e) {
                // handle exception
            } finally {
                ((StartTheReview)getActivity()).CurrentPDFDoc = null;
            }
        }
    }
}
