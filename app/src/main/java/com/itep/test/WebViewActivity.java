package com.itep.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wagaranai on 2018/11/12/0012.
 */
public class WebViewActivity extends Activity {
    private static final String TAG = WebViewActivity.class.getSimpleName();

    WebView webView;
    ProgressBar progressBar;

    private boolean isPost = true;
    private String cookieStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        initWebView();

    }

    private void initWebView() {
        webView = findViewById(R.id.web_view);
        progressBar = findViewById(R.id.progress_bar);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //支持javascript
        webView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        webView.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        webView.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        webView.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);

        WebSettings settings = webView.getSettings();
        settings.setAppCacheEnabled(false); //启用应用缓存
        settings.setDomStorageEnabled(false); //启用或禁用DOM缓存。
        settings.setDatabaseEnabled(false); //启用或禁用DOM缓存。

        webView.canGoBack();

        //解决跨域访问的问题
        try {
            if (Build.VERSION.SDK_INT >= 16) {
                Class<?> clazz = webView.getSettings().getClass();
                Method method = clazz.getMethod(
                        "setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(webView.getSettings(), true);
                }
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                return super.shouldOverrideKeyEvent(view, event);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }


            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                setTitle(title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                //                setIcon(icon);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                Log.e(TAG, "onJsAlert:" + url + "\n" + message);

                //保存一下cookie，后面httpclient使用
                //CookieManager cookieManager = CookieManager.getInstance();
                //cookieStr = cookieManager.getCookie(HttpUtils.BASE_URL);

                return super.onJsAlert(view, url, message, result);
                //                AlertDialog.Builder b = new AlertDialog.Builder(WebViewActivity.this);
                //                b.setTitle("");
                //                b.setMessage(message);
                //                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                //                    @Override
                //                    public void onClick(DialogInterface dialog, int which) {
                //                        result.confirm();
                //                    }
                //                });
                //                b.setCancelable(false);
                //                b.create().show();
                //                return true;
                //                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                Log.e(TAG, "onJsConfirm:" + url + "\n" + message);
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                Log.e(TAG, "onJsPrompt:" + url + "\n" + message + "\n" + defaultValue);
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        //        webView.evaluateJavascript("test://", new ValueCallback<String>() {
        //            @Override
        //            public void onReceiveValue(String value) {
        //                Log.e(TAG,"evaluateJavascript:"+value);
        //            }
        //        });
        //        webView.loadUrl("http://www.baidu.com");
        loadWebPage();
    }

    private void loadWebPage() {
        //一般手动取出cookie的目的只是交给 webview 等等，非必要情况不要自己操作
        //CookieStore cookieStore = OkGo.getInstance().getCookieJar().getCookieStore();
        //HttpUrl httpUrl = HttpUrl.parse(HttpUtils.BASE_URL);
        //List<Cookie> cookies = cookieStore.getCookie(httpUrl);
        //Log.e(TAG,httpUrl.host() + "对应的cookie如下：" + cookies.toString());
        ////            ToastUtils.show(httpUrl.host() + "对应的cookie如下：" + cookies.toString(),WebViewActivity.this);
        //synchronousWebCookies(this, HttpUtils.BASE_URL, getCookie(cookies));
        //
        //if (getIntent().getExtras().getBoolean("isGet",false)){
        //    webView.loadUrl(HttpUtils.BASE_URL+"/manager/membercenter/phonesubmitnew?viewType=2");
        //    return;
        //}else if (getIntent().getExtras().getBoolean("isPlan",false)){
        //    String planId=getIntent().getExtras().getString("id");
        //    if (planId.equals("111")){
        //        webView.postUrl(HttpUtils.addPlan(),getParams(planId).getBytes());
        //    }else {
        //        webView.postUrl(HttpUtils.editPlan(),getParams(planId).getBytes());
        //    }
        //}else {
        //    webView.postUrl(HttpUtils.appViewServer(),getParams(getIntent().getExtras().getString("id")).getBytes());
        //}
        webView.loadUrl("https://www.baidu.com");
    }

    //private String getParams(String id){
    //   return  "username="
    //            + AppSharedPreferences.getInstance(this).get("username")
    //            + "&token="
    //            + AppSharedPreferences.getInstance(this).get("token")
    //            + "&uuid="
    //            + Utils.getSerial()
    //            + "&id="
    //            + id;
    //}


    //private String getCookie(List<Cookie> allCookie){
    //    for (int i = 0; i<allCookie.size();i++){
    //        return  allCookie.get(i).toString();
    //    }
    //    return "";
    //}

    /**
     * 把cookie交给webview
     *
     * @param context
     * @param url
     * @param cookies
     */
    public static void synchronousWebCookies(Context context, String url, String cookies) {
        if (!TextUtils.isEmpty(url))
            if (!TextUtils.isEmpty(cookies)) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.createInstance(context);
                }
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.removeSessionCookie();// 移除
                cookieManager.removeAllCookie();
                StringBuilder sbCookie = new StringBuilder();
                sbCookie.append(cookies);
                String cookieValue = sbCookie.toString();
                Log.e("synchronousWebCookies: ", cookieValue);
                cookieManager.setCookie(url, cookieValue);//为url设置cookie

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    cookieManager.flush();
                }


            }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
