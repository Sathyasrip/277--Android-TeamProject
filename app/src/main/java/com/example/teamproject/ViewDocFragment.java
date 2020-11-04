package com.example.teamproject;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.PDFDoc;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.config.ToolManagerBuilder;
import com.pdftron.pdf.controls.AnnotationToolbar;
import com.pdftron.pdf.controls.AnnotationToolbarButtonId;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.pdf.utils.AppUtils;
import com.pdftron.pdf.utils.Utils;

import java.io.File;

import static android.content.ContentValues.TAG;

public class ViewDocFragment extends Fragment {

    private Boolean DebugMode = true;
    private View currentView;
    private PDFViewCtrl mPdfViewCtrl;
    private PDFDoc mPdfDoc;
    private ToolManager mToolManager;
    private AnnotationToolbar mAnnotationToolbar;
    private int DocumentVersion;
    private int pdf_resource;
    private String pdf_name = "document_under_review";

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

        /**************************************************************************************
         *  PDFView implementation (for viewing PDFs in a Fragment)
         *  Git source:
         *  https://github.com/PDFTron/pdftron-android-samples/tree/master/PDFViewCtrlViewer
         *************************************************************************************/
        mPdfViewCtrl = currentView.findViewById(R.id.pdfviewctrl);
        setupToolManager();
        setupAnnotationToolbar();

        if (DebugMode) {
            // TODO: Figure out a better way to load the PDF based on data stored on main activity.
            DocumentVersion = ((ReviewActivity)getActivity()).current_version;
            LoadSamplePDF(); // This loads a different pdf after each version selection.
        }

        try {
            AppUtils.setupPDFViewCtrl(mPdfViewCtrl);
            PDFViewZoomSettings(); // Setup the view settings.

            if (DebugMode) {
                // This will load the PDF into PDFView
                viewFromResource(pdf_resource, pdf_name);
            }

        } catch (PDFNetException e) {
            Log.e(TAG, "Error setting up PDFViewCtrl");
        }

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

    /******************************************************************
     *  Function for changing Test Sample PDFs. Replace after upload.
     *******************************************************************/
     public void LoadSamplePDF() {
         // Loads a sample PDF found in res/raw/
        if (DocumentVersion == 2) {
            pdf_resource = R.raw.sample2_datasheet;
        } else if (DocumentVersion == 3) {
            pdf_resource = R.raw.sample3_report;
        } else {
            pdf_resource = R.raw.sample1_ieee_article;
        }
     }
    /**************************************************************************************
     *  PDFTRON FUNCTIONS for PDFView
     *************************************************************************************/
    /**
     * Helper method to set up and initialize the ToolManager.
     */
    public void setupToolManager() {
        mToolManager = ToolManagerBuilder.from()
                .build(getActivity(), mPdfViewCtrl);
    }

    /**
     * Helper method to set up and initialize the AnnotationToolbar.
     */
    public void setupAnnotationToolbar() {
        mAnnotationToolbar = currentView.findViewById(R.id.annotationToolbar);
        // Remember to initialize your ToolManager before calling setup
        mAnnotationToolbar.setup(mToolManager);
        mAnnotationToolbar.hideButton(AnnotationToolbarButtonId.CLOSE);
        mAnnotationToolbar.show();
    }

    /**
     * Helper method to view a PDF document from resource
     *
     * @param resourceId of the sample PDF file
     * @param fileName   of the temporary PDF file copy
     * @throws PDFNetException if invalid document path is supplied to PDFDoc
     */
    public void viewFromResource(int resourceId, String fileName) throws PDFNetException {
        File file = Utils.copyResourceToLocal(getContext(), resourceId, fileName, ".pdf");
        mPdfDoc = new PDFDoc(file.getAbsolutePath());
        mPdfViewCtrl.setDoc(mPdfDoc);
        // Alternatively, you can open the document using Uri:
        // Uri fileUri = Uri.fromFile(file);
        // mPdfDoc = mPdfViewCtrl.openPDFUri(fileUri, null);
    }

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

        if (mPdfDoc != null) {
            try {
                mPdfDoc.close();
            } catch (Exception e) {
                // handle exception
            } finally {
                mPdfDoc = null;
            }
        }
    }
}
