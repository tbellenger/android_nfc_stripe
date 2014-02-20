package com.bellsolutions.android.paymeback;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TerminalActivity extends Activity {
	private static final String TAG = "TerminalActivity";
	private static final String STATE_AMT = "com.abnote.termial.TerminalActivity.AMT";
	private static final String STATE_LOGIN = "com.abnote.termial.TerminalActivity.LOGIN";
	private Button mOne, mTwo, mThree, mFour, mFive, mSix, mSeven, mEight, mNine, mZero, mZeroes, mClear, mPay;
	private double mCurrAmt = 0;
	private TextView mAmt;
	private TextView mMerchant;
	private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Remove title bar
		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1){
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		}

		setContentView(R.layout.term);
		
		mHandler = new Handler();

		mOne = (Button) findViewById(R.id.b1);
		mTwo = (Button) findViewById(R.id.b2);
		mThree = (Button) findViewById(R.id.b3);
		mFour = (Button) findViewById(R.id.b4);
		mFive = (Button) findViewById(R.id.b5);
		mSix = (Button) findViewById(R.id.b6);
		mSeven = (Button) findViewById(R.id.b7);
		mEight = (Button) findViewById(R.id.b8);
		mNine = (Button) findViewById(R.id.b9);
		mZero = (Button) findViewById(R.id.b0);
		mZeroes = (Button) findViewById(R.id.b00);
		mClear = (Button) findViewById(R.id.bclear);
		mPay = (Button) findViewById(R.id.bpay);
		mAmt = (TextView) findViewById(R.id.tvamt);
		mMerchant = (TextView) findViewById(R.id.tvmerchant);
		
		mMerchant.setText(getString(R.string.version) + " " + getString(R.string.txt_logout));

		mOne.setOnClickListener(mButtonListener);
		mTwo.setOnClickListener(mButtonListener);
		mThree.setOnClickListener(mButtonListener);
		mFour.setOnClickListener(mButtonListener);
		mFive.setOnClickListener(mButtonListener);
		mSix.setOnClickListener(mButtonListener);
		mSeven.setOnClickListener(mButtonListener);
		mEight.setOnClickListener(mButtonListener);
		mNine.setOnClickListener(mButtonListener);
		mZero.setOnClickListener(mButtonListener);
		mZeroes.setOnClickListener(mButtonListener);
		mClear.setOnClickListener(mButtonListener);
		mPay.setOnClickListener(mButtonListener);
		
		if (savedInstanceState != null) {
			mCurrAmt = savedInstanceState.getDouble(STATE_AMT);
		}
		
		mAmt.setText(formatCurrency(mCurrAmt));
	}

	private OnClickListener mButtonListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.b1: appendDigit(.01); break;
			case R.id.b2: appendDigit(.02); break;
			case R.id.b3: appendDigit(.03); break;
			case R.id.b4: appendDigit(.04); break;
			case R.id.b5: appendDigit(.05); break;
			case R.id.b6: appendDigit(.06); break;
			case R.id.b7: appendDigit(.07); break;
			case R.id.b8: appendDigit(.08); break;
			case R.id.b9: appendDigit(.09); break;
			case R.id.b0: appendDigit(.0); break;
			case R.id.b00: appendDigit(.0); appendDigit(.0); break;
			case R.id.bclear: clearAmt(); break;
			case R.id.bpay: pay(); break;
			default: clearAmt();
			}
		}
	};
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	default: return super.onOptionsItemSelected(item);
    	}
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putDouble(STATE_AMT, mCurrAmt);
	}
	
	private void clearAmt() {
		mCurrAmt = 0;
		mAmt.setText(formatCurrency(mCurrAmt));
	}
	
	private void pay() {
		if (mCurrAmt == 0) {
			Toast.makeText(this, "Enter amount", Toast.LENGTH_LONG).show();
			return;
		}
		Intent i = new Intent(getApplicationContext(), NfcActivity.class);
		i.putExtra("amt", new DecimalFormat("#.##").format(mCurrAmt));
		startActivityForResult(i, NfcActivity.REQ_READ_CARD);
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode == NfcActivity.RSP_OK) {
    		clearAmt();
    		ReceiptDialog rd = new ReceiptDialog(this);
    		//rd.setSaleData(sale);
    		// TODO: set sale data from Stripe here
    		rd.show();
    	} else if (resultCode == NfcActivity.RSP_ERROR) {
    		Toast.makeText(this, "NFC Error", Toast.LENGTH_LONG).show();
    	} else if (resultCode == NfcActivity.RSP_CANCEL) {
    		Toast.makeText(this, "Payment cancelled", Toast.LENGTH_LONG).show();
    	}
    }
	
	private void appendDigit(double digit) {
		if (digit == 0 && mCurrAmt == 0) {
			// don't add 0 or 00 to an empty amount
		} else {
			mCurrAmt = (mCurrAmt * 10) + digit;
			mAmt.setText(formatCurrency(mCurrAmt));
		}
	}
	
	public static String formatCurrency(double amt) {
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(amt);
	}
	
	public static String formatCurrency(String amt) {
		return formatCurrency(Double.parseDouble(amt));
	}
}
