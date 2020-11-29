package com.example.teamproject.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.teamproject.R;
import com.example.teamproject.model.FirebaseReviewVersion;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pdftron.common.PDFNetException;
import com.pdftron.fdf.FDFDoc;
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
import com.pdftron.pdf.utils.AppUtils;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.sdf.Obj;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

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
     *  Function for obtaining the PDF URL for a given document version.
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

        final FirebaseReviewVersion ReviewVersion = CurrentReviewVersion;
        mStorageReference.child(firebase_pdf_file).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                pdf_url = ReviewVersion.GetFirebasePublicUrl(downloadUrl.getEncodedPath());
                Log.d(TAG, "Decoded PDF Url: " + pdf_url);
                UpdatePDFView(pdf_url);
            }
        });
    }
    public void UpdatePDFView(String url) {
        try {
            AppUtils.setupPDFViewCtrl(mPdfViewCtrl);
            PDFViewZoomSettings(); // Setup the view settings.

            // Load the PDF URL into PDF Tron.
            Log.d(TAG, "Loading the PDF URL into PDF Tron: " + url);
            viewFromURL(url);


        } catch (PDFNetException e) {
            // This should NEVER occur when opening this fragment.
            Log.e(TAG, "Error setting up PDFViewCtrl");
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

    /*****************************************************************
     * Helper functions for loading a PDF from a resource file or URL
     *****************************************************************/
    public void viewFromResource(int resourceId, String fileName) throws PDFNetException {
        File file = Utils.copyResourceToLocal(getContext(), resourceId, fileName, ".pdf");
        mPdfDoc = new PDFDoc(file.getAbsolutePath());

        // Update the PDF Doc reference in Main Activity and set the PDF View Control.
        ((StartTheReview)getActivity()).CurrentPDFDoc = mPdfDoc;

        mPdfViewCtrl.setDoc(((StartTheReview)getActivity()).CurrentPDFDoc);
    }
    public void viewFromURL(String url) throws PDFNetException {
        String cachePDF_filename = ((StartTheReview)getActivity()).CacheDirectory + "/" + "upload.pdf";
        final String cache_annotation_file = ((StartTheReview)getActivity()).CacheDirectory + "/" + "upload.xfdf";

        Log.d(TAG, "viewFromURL: Cache file: " + cachePDF_filename);
        try {
            mPdfViewCtrl.openUrlAsync(url, cachePDF_filename, null, null);
            Log.w(TAG, "viewFromURL: Downloaded the PDF Url into cache.");

            // Use the cache file directly in order to work with current annotations.
            ((StartTheReview)getActivity()).CurrentPDFDoc = new PDFDoc(cachePDF_filename);

            // Only imports annotations if available from Firebase and if download was successful.
            if (!((StartTheReview)getActivity()).FirebaseAnnotationFilename.equals("none")) {
                Log.w(TAG, "viewFromURL: There is an XFDF file in firebase waiting to be downloaded.");

                // Get the firebase url for the XFDF file and save it to a cached file directly.
                CurrentReviewVersion.setAnnotationFile(((StartTheReview)getActivity()).FirebaseAnnotationFilename);
                final String firebase_xfdf_file = CurrentReviewVersion.AnnotationFile();
                final File annot_file = new File(cache_annotation_file);
                mStorageReference.child(firebase_xfdf_file).getFile(annot_file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Upon successfully downloading the XFDF file to the local file, load the annotations.
                        Log.d(TAG, "viewFromURL: Successfully downloaded the XFDF file from firebase storage to: " + cache_annotation_file);
                        try {
                            FDFDoc fdf_doc = FDFDoc.createFromXFDF(cache_annotation_file);
                            ((StartTheReview)getActivity()).CurrentPDFDoc.fdfMerge(fdf_doc);
                            Log.w(TAG, "viewFromURL: Merged annotations with currently viewable PDF.");

                            // Finally, load the cached PDF (with annotations).
                            mPdfViewCtrl.setDoc(((StartTheReview)getActivity()).CurrentPDFDoc);
                            Log.d(TAG, "viewFromURL: Loaded Cached PDF w/ Annotations.");
                        } catch (Exception e) {
                            Log.e(TAG, "viewFromURL: Could not load annotations from XFDF file!");
                            e.getStackTrace();
                        }
                    }
                });
                // Only on success do we load the annotations. Otherwise we do not load anything.
            } else {
                Log.w(TAG, "viewFromURL: No annotations found for the Review Version in firebase. Ignoring annotation merge.");
                // Finally, load the cached PDF (without annotations).
                mPdfViewCtrl.setDoc(((StartTheReview)getActivity()).CurrentPDFDoc);
                Log.d(TAG, "viewFromURL: Loaded Cached PDF w/out Annotations.");
            }
        } catch (Exception e) {
            Log.e(TAG, "viewFromURL: Some error occurred when trying to open the PDF Url!");
            e.printStackTrace();
        }
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
