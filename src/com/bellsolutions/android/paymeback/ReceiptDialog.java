package com.bellsolutions.android.paymeback;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ReceiptDialog extends Dialog {
	private Button mOk;
	private double mAmt;
	private double mDiscount;
	private double mTotal;
	private Context ctx;
	
	public ReceiptDialog(Context context) {
		super(context);
		ctx = context;
	}
	
	/*public void setSaleData(SaleResponseBean sale) {
		mSale = sale;
		mAmt = Double.parseDouble(sale.getGrossAmt());
		mTotal = Double.parseDouble(sale.getNetAmt());
		mDiscount = mAmt - mTotal;
	}*/
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt_dialog);
        
        setTitle("Transaction Receipt");
        
        TextView dateTime = (TextView) findViewById(R.id.tvdatetime);
        TextView pan = (TextView) findViewById(R.id.tvpan);
        TextView amt = (TextView) findViewById(R.id.tvamt);
        TextView discount = (TextView) findViewById(R.id.tvdiscount);
        TextView total = (TextView) findViewById(R.id.tvtotal);
        mOk = (Button) findViewById(R.id.bok);
        
        //dateTime.setText(ctx.getString(R.string.txt_date) + " " + mSale.getDate() + " " + mSale.getTime());
        //pan.setText(ctx.getString(R.string.txt_pan) + " " + mSale.getCardNo());
        amt.setText(ctx.getString(R.string.txt_gross) + TerminalActivity.formatCurrency(mAmt));
        discount.setText(ctx.getString(R.string.txt_discount) + TerminalActivity.formatCurrency(mDiscount));
        total.setText(ctx.getString(R.string.txt_net) + TerminalActivity.formatCurrency(mTotal));
        
        mOk.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
}
