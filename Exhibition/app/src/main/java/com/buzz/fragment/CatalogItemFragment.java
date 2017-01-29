package com.buzz.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.buzz.activity.R;
import com.buzz.impl.VideoEnabledWebChromeClient;
import com.buzz.impl.VideoEnabledWebView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CatalogItemFragment extends Fragment {
    private static final String TAG = CatalogItemFragment.class.getSimpleName();
    private static final String ARG_IMAGE_PATH = "image_path";
    private static final String ARG_DESC = "desc";

    private String mImagePath;
    private String mDesc;
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    private View footBar;

    public interface OnEventCallBackListener {
        public void OnEventCallBack(String param);
    }
    private OnEventCallBackListener mListener;

    public static CatalogItemFragment newInstance(String imagePath, String desc) {
        CatalogItemFragment fragment = new CatalogItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        args.putString(ARG_DESC, desc);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mImagePath = getArguments().getString(ARG_IMAGE_PATH);
            mDesc = getArguments().getString(ARG_DESC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_catalog_item, container, false);

            RelativeLayout background = (RelativeLayout) view.findViewById(R.id.catalog_fragment_exhibition_background);
            background.setBackground(Drawable.createFromPath(mImagePath));

            webView = (VideoEnabledWebView) view.findViewById(R.id.catalog_fragment_exhibition_desc);
            // Initialize the VideoEnabledWebChromeClient and set event handlers
            View nonVideoLayout = view.findViewById(R.id.nonVideoLayout);
            ViewGroup videoLayout = (ViewGroup) getActivity().findViewById(R.id.videoLayout);
            footBar = getActivity().findViewById(R.id.foot_bar);
            //noinspection all
            View loadingView = getLayoutInflater(savedInstanceState).inflate(R.layout.view_loading_video, null);
            webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) {
                @Override
                public void onProgressChanged(WebView view, int progress) {

                }
            };
            webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
                @Override
                public void toggledFullscreen(boolean fullscreen) {
                    // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                    if (fullscreen) {
                        WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
                        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                        getActivity().getWindow().setAttributes(attrs);
                        if (android.os.Build.VERSION.SDK_INT >= 14) {
                            //noinspection all
                            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                        }
                        footBar.setVisibility(View.INVISIBLE);
                    } else {
                        WindowManager.LayoutParams attrs = getActivity().getWindow().getAttributes();
                        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                        getActivity().getWindow().setAttributes(attrs);
                        if (android.os.Build.VERSION.SDK_INT >= 14) {
                            //noinspection all
                            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        }
                        footBar.setVisibility(View.VISIBLE);
                    }

                }
            });
            webView.setWebChromeClient(webChromeClient);
            // Call private class InsideWebViewClient
            webView.setWebViewClient(new InsideWebViewClient());
            Pattern p = Pattern.compile("<video>(.*)</video>");
            Matcher m = p.matcher(mDesc);
            if (m.find()) {
                webView.loadUrl(m.group(1));
            } else {
                String finalDesc = mDesc;
                Pattern p1 = Pattern.compile("url[(](.*)[)]");
                Matcher m1 = p1.matcher(mDesc);
                while (m1.find()) {
                    finalDesc = finalDesc.replace(m1.group(1)
                            , "file://"
                            .concat(mImagePath.substring(0, mImagePath.lastIndexOf("/") + 1))
                            .concat(m1.group(1)));
                }
                webView.loadDataWithBaseURL("", finalDesc, "text/html", "utf-8", null);
            }
            webView.setBackgroundColor(getResources().getColor(R.color.bg_color));

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnEventCallBackListener) {
            mListener = (OnEventCallBackListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventCallBackListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        // Force links to be opened inside WebView and not in Default Browser
        // Thanks http://stackoverflow.com/a/33681975/1815624
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        }
    }
}
