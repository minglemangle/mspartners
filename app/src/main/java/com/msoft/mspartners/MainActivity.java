package com.msoft.mspartners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private String URL = "http://www.mspartners.co.kr/";
    private static final int MY_PERMISSIONS_REQUESTS = 0;
    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; //웹뷰세팅
    private ProgressBar progressBar;
    private BackPressCloseHandler backPressCloseHandler;
    public ValueCallback<Uri> filePathCallbackNormal;
    public ValueCallback<Uri[]> filePathCallbackLollipop;
    public final static int FILECHOOSER_NORMAL_REQ_CODE = 2001;
    public final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2002;
    private Uri cameraImageUri = null;
    ValueCallback mFilePathCallback = null;

    private boolean goBackHome = false;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        progressBar.setVisibility(View.GONE);

        mWebView = (WebView) findViewById(R.id.webView1);

        checkVerify();
        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        mWebSettings.setAllowFileAccess(true); // 파일 접근 허용 여부
        mWebSettings.setAllowContentAccess(true); // 컨텐츠 접근 허용
//        mWebSettings.setDefaultTextEncodingName("UTF-8"); // encoding 설정
        mWebSettings.setDatabaseEnabled(true); // 데이터베이스 접근 허용 여부
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
//        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
        mWebSettings.setSupportMultipleWindows(true); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부


        mWebView.setWebChromeClient(new WebChromeClient());
//        mWebView.setWebViewClient(new WebViewClientClass());
        Intent intent = getIntent();
        String link = intent.getStringExtra("_LINK_");
        String loadUrl = URL;

        if(link != null && !link.equals("")) {
            goBackHome = true;

            loadUrl = link;
            if(loadUrl.contains("?")) {
                loadUrl = loadUrl + "&push=y";
            } else {
                loadUrl = loadUrl + "?push=y";
            }
        }
        mWebView.loadUrl(loadUrl);

        // TODO 테스트
//        mWebView.loadUrl("file:///android_asset/sample.html");

        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mFilePathCallback = filePathCallback;

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, 0);

                return true;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onCreateWindow(final WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                WebView newWebView = new WebView(MainActivity.this);
                WebView.WebViewTransport transport
                        = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();

                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                        browserIntent.setData(Uri.parse(url));
                        startActivity(browserIntent);
                        return true;
                    }

                    @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                        handler.proceed(); // SSL 에러가 발생해도 계속 진행
                    }
                });

                return true;
            }

        });

        backPressCloseHandler = new BackPressCloseHandler(this);

        // 2022-03-07 @Daniel - Add javascript interface
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "JavaScript");
    }


//    public class JavaScriptInterface {
//        Context mContext;
//        JavaScriptInterface(Context c) {
//            mContext = c;
//        }
//        @JavascriptInterface
//        public void showToast(String toast) {
//            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
//        }
//    }
    public class OOOWebChromeClient extends WebChromeClient {
        private final Activity thisActivity;

        public OOOWebChromeClient(Activity thisActivity) {
            super();
            this.thisActivity = thisActivity;
        }

    }

    private class WebViewClientClass extends WebViewClient {

        @SuppressLint("LongLogTag")
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            //Log.d("WebViewClient URL : " , request.getUrl().toString());

            String url = request.getUrl().toString();
            if (url.startsWith("tel:")) {
                Intent call_phone = new Intent(Intent.ACTION_CALL);
                call_phone.setData(Uri.parse(url));

                if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),"전화걸기 권한을 승인해 주셔야 정상적인 전화걸기 서비스가 가능합니다.",Toast.LENGTH_SHORT).show();
                    return true;
                }
                startActivity(call_phone);
            } else if (url.startsWith("sms:")){

                Intent intent = new Intent(Intent.ACTION_SENDTO,Uri.parse(url));
                startActivity(intent);

            } else if (url.startsWith("intent:kakaolink://")) {

                try {
                    Intent intent = Intent.parseUri(url,Intent.URI_INTENT_SCHEME);
                    Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                    if (existPackage != null) {
                        startActivity(intent);
                    } else {
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                        startActivity(marketIntent);
                    }

                    return true;
                } catch (Exception e) {
                    Log.d("shouldOverrideUrlLoading()","intent part Error");
                    e.printStackTrace();
                }

            } else {

                view.loadUrl(request.getUrl().toString());
            }

            return true;
            //return super.shouldOverrideUrlLoading(view, request);
        }
    }

    // 안드로이드 웹뷰에서 파일 첨부하기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //  Log.e(“resultCode:: “, String.valueOf(resultCode));
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

            // 파일 n개 선택한 경우
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && data != null && data.getClipData() != null && data.getClipData().getItemCount() > 0) {
                int count = data.getClipData().getItemCount();

                Uri[] uriArr = new Uri[count];
                for (int i = 0; i < count; i++) {
                    uriArr[i] = data.getClipData().getItemAt(i).getUri();
                }

                mFilePathCallback.onReceiveValue(uriArr);

            } else if (data != null && data.getData() != null) {
                // 파일 1개 선택한 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mFilePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                } else {
                    mFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
                }
            }

            mFilePathCallback = null;

        } else {
            mFilePathCallback.onReceiveValue(null);
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        String url = mWebView.getUrl();

        if(url.contains("index.php") || url.equals("https://www.mspartners.co.kr") || url.equals("https://www.mspartners.co.kr/")) {
            backPressCloseHandler.onBackPressed();
        } else {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                if (goBackHome) {
                    goBackHome = false;
                    mWebView.loadUrl(URL);
                    mWebView.clearHistory();
                    mWebView.clearCache(true);
                } else {
                    if (mWebView.getUrl().contains("index.php")) {
                        backPressCloseHandler.onBackPressed();
                    } else {
                        mWebView.loadUrl(URL);
                        mWebView.clearHistory();
                        mWebView.clearCache(true);
                    }
                }
            }
        }
    }

    //권한 획득 여부 확인
    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify() {

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            Log.d("checkVerify() : ","if문 들어옴");

            //카메라 또는 저장공간 권한 획득 여부 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {

                Toast.makeText(getApplicationContext(),"권한 관련 요청을 허용해 주셔야 카메라 캡처이미지 사용등의 서비스를 이용가능합니다.",Toast.LENGTH_SHORT).show();

            } else {
//                Log.d("checkVerify() : ","카메라 및 저장공간 권한 요청");
                // 카메라 및 저장공간 권한 요청
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET, Manifest.permission.CAMERA,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    //권한 획득 여부에 따른 결과 반환
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Log.d("onRequestPermissionsResult() : ","들어옴");

        if (requestCode == 1)
        {
            if (grantResults.length > 0)
            {
                for (int i=0; i<grantResults.length; ++i)
                {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                    {
                        // 카메라, 저장소 중 하나라도 거부한다면 앱실행 불가 메세지 띄움
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();

                        return;
                    }
                }
//                Toast.makeText(this, "Succeed Read/Write external storage !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 2022-03-07 @Daniel
     *
     * Javascript interface
     */
    final class JavaScriptInterface {
        JavaScriptInterface() { }

        // Get device token
        @JavascriptInterface
        public void getDeviceToken() {
            String token = PreferencesManager.getString(getApplicationContext(), "token");
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript:setAndroidDeviceToken('" + token + "')");
                }
            });
        }
    }
}
