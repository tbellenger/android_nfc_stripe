package com.bellsolutions.android.paymeback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;

import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import au.com.bellsolutions.android.nfc.activity.BaseNfcActivity;

public class NfcActivity extends BaseNfcActivity {
	private static final String TAG = "NfcActivity";
	public static final String PUBLISHABLE_KEY = "pk_test_o9vV2tx5D7xcPssL973f9dIZ";
	private static final String STATE_AMT = "com.bellsolutions.android.termial.NfcActivity.AMT";
	private static final String STATE_INFO = "com.bellsolutions.android.termial.NfcActivity.INFO";
	private static final String STATE_INFO_AMT = "com.bellsolutions.android.termial.NfcActivity.INFO_AMT";
	public static final int REQ_READ_CARD = 1;
	public static final int RSP_OK = 0;
	public static final int RSP_ERROR = 1;
	public static final int RSP_CANCEL = 2;
	public static final int STATUS_READING_CARD = 1;
	public static final int STATUS_ONLINE = 2;
	private Button mCancel;
	private TextView mInfo, mInfoAmt;
	private AtomicBoolean mIsCancelClicked = new AtomicBoolean();
	private String mAmt;
	private PerformTransactionTask mRunningTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.nfc);
		
		if (getIntent().getExtras() != null) {
			Log.d(TAG, "with extras");
			mAmt = getIntent().getExtras().getString("amt");
		}
		
		if( (mRunningTask = (PerformTransactionTask)getLastNonConfigurationInstance()) != null) {
			Log.d(TAG, "re-attach to previous task"); 
			mRunningTask.attach(this);
		} else {
			Log.d(TAG, "new task");
			mRunningTask = new PerformTransactionTask(this, mAmt);
		}

		mInfo = (TextView) findViewById(R.id.tvinfo);
		mInfo.setText(getString(R.string.txt_tap_card));
		mInfoAmt = (TextView) findViewById(R.id.tvamt);
		mInfoAmt.setText(TerminalActivity.formatCurrency(mAmt));
		mCancel = (Button) findViewById(R.id.bcancel);
		mCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
			if (mRunningTask != null && mRunningTask.getStatus().compareTo(AsyncTask.Status.RUNNING) == 0) {
					mIsCancelClicked.set(true);
				} else {
		            setResult(RSP_CANCEL);
		            finish();
				}
				
			}
		});
		
		if (savedInstanceState != null) {
			Log.d(TAG, "update from saved instance");
			mAmt = savedInstanceState.getString(STATE_AMT);
			mInfo.setText(savedInstanceState.getString(STATE_INFO));
			mInfoAmt.setText(savedInstanceState.getCharSequence(STATE_INFO_AMT));
		}
		
		setResult(RSP_CANCEL);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_AMT, mAmt);
		outState.putString(STATE_INFO, mInfo.getText().toString());
		outState.putCharSequence(STATE_INFO_AMT, mInfoAmt.getText());
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.d(TAG, "config change. detach from task");
		mRunningTask.detach();
		return mRunningTask;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}
	
	public void onNfcDiscovered(Tag tag) {
		// Make processing a transaction one shot for this activity
		if (mRunningTask.getStatus().equals(AsyncTask.Status.PENDING)) {
			Log.d(TAG, "task pending so execute");
			mRunningTask.execute(tag);
		} else {
			Log.d(TAG, "no task pending");
		}
	}
	
	public void onProgressUpdate(Integer status) {
		switch (status) {
			case STATUS_READING_CARD: mInfo.setText(getString(R.string.txt_reading_card)); break;
			case STATUS_ONLINE: mInfo.setText(getString(R.string.txt_online_processing)); break;
		}
	}
	
	public void onCancelled() {
		setResult(RSP_CANCEL);
        finish();
	}
	
	public void onTaskFinished(Bundle result) {
		if (result != null) {
			// card pan and expiry read now charge
			String pan = result.getString("pan");
			String exp = result.getString("exp");
			final String amt = result.getString("amt");
			
			Log.d(TAG, "Expiry: " + exp.substring(2,4) + "/" + "20" + exp.substring(0,2));
			
			Card card = new Card(pan,
					Integer.parseInt(exp.substring(2,4)), // MM
					Integer.parseInt("20" + exp.substring(0,2)), // YYYY
					"000");

	        boolean validation = true; //card.validateCard();
	        if (validation) {
	            new Stripe().createToken(
	                    card,
	                    PUBLISHABLE_KEY,
	                    new TokenCallback() {
	                    public void onSuccess(Token token) {
	                    	// TODO: move this to a server for security
		                    	//com.stripe.Stripe.apiKey = "sk_test_yh3FflMgzASfsIulCHN1QmSa";

		                    	Map<String, Object> chargeParams = new HashMap<String, Object>();
		                    	chargeParams.put("amount", amt);
		                    	chargeParams.put("currency", "aud");
		                    	chargeParams.put("card", token.getId()); // obtained with Stripe.js
		                    	chargeParams.put("description", "Charge for test@bellsolutions.com.au");
		                    	//chargeParams.put("metadata", null);

		                    	// TODO: Yeah I know this is bad!!! Just playing around 
								CreateCharge(chargeParams, "sk_test_yh3FflMgzASfsIulCHN1QmSa", new ChargeCallback() {

									@Override
									public void onError(Exception error) {
										// TODO Auto-generated method stub
										
									}

									@Override
									public void onSuccess(Charge charge) {
										// TODO Auto-generated method stub
										
									}
									
								});
	                        }
	                    public void onError(Exception error) {
	                            try {
									throw error;
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
	                        }
	                    });
	        } else {
	            //handleError("You did not enter a valid card");
	        }
            setResult(RSP_OK);
            finish();
		} else {
			setResult(RSP_ERROR);
            finish();
		}
	}
	
	private class ResponseWrapper {
        public final Charge charge;
        public final Exception error;

        private ResponseWrapper(Charge charge, Exception error) {
            this.error = error;
            this.charge = charge;
        }
    }
	
	public void CreateCharge(Map<String, Object> chargeParams, String secretKey, ChargeCallback callback) {
		chargeCreator.create(chargeParams, secretKey, null, callback);
	}
	
	interface ChargeCreator {
        public void create(Map<String, Object> chargeParams, String secretKey, Executor executor, ChargeCallback callback);
    }
	
	ChargeCreator chargeCreator = new ChargeCreator() {
        @Override
        public void create(final Map<String, Object> chargeParams, final String secretKey, final Executor executor,
                final ChargeCallback callback) {
            AsyncTask<Void, Void, ResponseWrapper> task = new AsyncTask<Void, Void, ResponseWrapper>() {
                protected ResponseWrapper doInBackground(Void... params) {
                    try {
                    	com.stripe.Stripe.apiKey = secretKey;
                    	Charge charge = com.stripe.model.Charge.create(chargeParams);
                        return new ResponseWrapper(charge, null);
                    } catch (Exception e) {
                        return new ResponseWrapper(null, e);
                    }
                }

                protected void onPostExecute(ResponseWrapper result) {
                    chargePostExecution(result, callback);
               }
            };

            executeChargeTask(executor, task);
        }
    };
    
    private void executeChargeTask(Executor executor, AsyncTask<Void, Void, ResponseWrapper> task) {
        if (executor != null)
            task.executeOnExecutor(executor);
        else
            task.execute();
    }
    
    private void chargePostExecution(ResponseWrapper result, ChargeCallback callback) {
        if (result.charge != null)
            callback.onSuccess(result.charge);
        else if (result.error != null)
            callback.onError(result.error);
        else
            callback.onError(new RuntimeException("Somehow got neither a charge response or an error response"));
    }
	
    public abstract class ChargeCallback {
        public abstract void onError(Exception error);
        public abstract void onSuccess(Charge charge);
    }
}
