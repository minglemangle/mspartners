package com.msoft.mspartners;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GoogleBillingImpl implements PurchasesUpdatedListener {
    private static final String TAG = "GoogleBillingImpl";

    private final BillingClient mBillingClient;
    private List<SkuDetails> skuDetailsList = new ArrayList<>();

    private Context mContext;

    private MainActivity mMainActivity;

    public GoogleBillingImpl(@NonNull final Context applicationContext, MainActivity mainActivity) {
        mMainActivity = mainActivity;
        mContext = applicationContext;
        mBillingClient = BillingClient.newBuilder(applicationContext).enablePendingPurchases().setListener(this).build();
    }

    public void init() {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    List<String> strList = new ArrayList<>();
                    strList.add("noad_30000");

                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(strList).setType(BillingClient.SkuType.INAPP);

                    mBillingClient.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                            // Process the result.
                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                if(list.isEmpty()) {
                                    Log.d(TAG, "list is zero");
                                } else {
                                    skuDetailsList = list;
                                }
                            }
                        }
                    });
                }else{
                    Log.d(TAG, "google purchase error");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "onBillingServiceDisconnected");
            }
        });
    }

    public void purchase(Activity activity, String productId) {
        BillingFlowParams flowParams = null;
        BillingResult billingResult;

        SkuDetails sku = getSkuDetail(productId);
        if( sku != null ) {
            flowParams = BillingFlowParams.newBuilder().setSkuDetails(sku).build();
            billingResult = mBillingClient.launchBillingFlow(activity, flowParams);
        } else {
            Log.d(TAG, "sku is null");
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
            for (Purchase purchase : list) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(mContext, "결제를 취소하셨습니다.", Toast.LENGTH_LONG).show();
        } else {
            // Handle any other error codes.
            Toast.makeText(mContext, "결제 중 오류가 발생했습니다.\n" + billingResult.getDebugMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "google purchase error");
        }
    }

    private void handlePurchase(Purchase purchase) {
        String purchaseToken, payLoad;
        purchaseToken = purchase.getPurchaseToken();
        payLoad = purchase.getDeveloperPayload();

        if(purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build();

            mBillingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        String orderId = purchase.getOrderId();
                        String json = purchase.getOriginalJson();

                        try {
                            JSONObject jObject = new JSONObject(json);
                            String productId = jObject.getString("productId");

                            mMainActivity.mWebView.post(new Runnable(){
                                @Override
                                public void run(){
                                    mMainActivity.mWebView.loadUrl("javascript:proc_purchase('" + orderId + "', '" + productId + "');");
                                }
                            });
                        } catch(JSONException ex) {
                            Toast.makeText(mContext, "결제실패\n" + ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(mContext, "결제실패\n" + billingResult.getDebugMessage(), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "google purchase consume error");
                    }
                }
            });
        }
    }

    private SkuDetails getSkuDetail(String productId) {
        for(SkuDetails item : skuDetailsList) {
            if(item.getSku().equals(productId)) {
                return item;
            }
        }
        return null;
    }
}