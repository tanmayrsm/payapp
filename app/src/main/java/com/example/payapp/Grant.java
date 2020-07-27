package com.example.payapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.payapp.Models.Paid;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Grant extends AppCompatActivity {
    String his_id, my_id;
    CircleImageView img;
    TextView Emails, Names, Balance, vTVGoogleStatus;
    LinearLayout LL1, LL2;
    Button FULL, PARTIAL;
    EditText Payable_amt;
    Button Done, vBtnGooglePay;
    EditText DESC;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    private PaymentsClient paymentsClient, mPaymentsClient;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private String mPrice = "0.0";
    int val, bala;
    ImageView Back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant);
        his_id = getIntent().getStringExtra("USERID");
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        img = findViewById(R.id.image);
        Emails = findViewById(R.id.emails);
        Names = findViewById(R.id.names);
        Balance = findViewById(R.id.balance);
        LL1 = findViewById(R.id.ll);
        LL2 = findViewById(R.id.ll2);
        FULL = findViewById(R.id.full);
        PARTIAL = findViewById(R.id.partial);
        Payable_amt = findViewById(R.id.payable_amt);
        Done = findViewById(R.id.done);
        DESC = findViewById(R.id.desc);
        vTVGoogleStatus = findViewById(R.id.tv_status);
        vBtnGooglePay = findViewById(R.id.btn_gateway_demo);
        Back = findViewById(R.id.back);

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Grant.this ,MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });


        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        my_id = firebaseUser.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(his_id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Names.setText(dataSnapshot.child("name").getValue().toString());
                Emails.setText(dataSnapshot.child("email").getValue().toString());
                Glide.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).into(img);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //set balance
        DatabaseReference reff = FirebaseDatabase.getInstance().getReference().
                child("Transactions").child("Grant").child(my_id).child(his_id).child("amount");
        if(my_id!=null && his_id!=null)
        reff.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    Balance.setText(dataSnapshot.getValue().toString());
                else{
                    Toast.makeText(Grant.this, "All transactions cleared", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        LL1.setVisibility(View.GONE);
        LL2.setVisibility(View.GONE);

        FULL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LL1.getVisibility() == View.GONE)    LL1.setVisibility(View.VISIBLE);
                if(LL2.getVisibility() == View.GONE)    LL2.setVisibility(View.VISIBLE);

                if(Payable_amt.isEnabled()) {
                    Payable_amt.setEnabled(false);
                    DatabaseReference reff = FirebaseDatabase.getInstance().getReference().
                            child("Transactions").child("Grant").child(my_id).child(his_id).child("amount");
                    reff.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Payable_amt.setText(dataSnapshot.getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        });

        PARTIAL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LL1.getVisibility() == View.GONE)    LL1.setVisibility(View.VISIBLE);
                if(LL2.getVisibility() == View.GONE)    LL2.setVisibility(View.VISIBLE);

                if(!Payable_amt.isEnabled()) {
                    Payable_amt.setEnabled(true);
                    Payable_amt.setText("");
                }

            }
        });

        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                val = Integer.parseInt(Payable_amt.getText().toString());
                bala = Integer.parseInt(Balance.getText().toString());
                if(val > bala) Toast.makeText(Grant.this, "ENtered amount shld be < payable amt", Toast.LENGTH_SHORT).show();
                else{
                    Toast.makeText(Grant.this, "Pay kar"+Payable_amt.getText().toString(), Toast.LENGTH_SHORT).show();
                    Log.e("val and bala:",val + " " + bala);
                    Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                            .build();

                    //paymentsClient = Wallet.getPaymentsClient(Grant.this,walletOptions);

                    mPaymentsClient =
                            Wallet.getPaymentsClient(
                                    Grant.this,
                                    new Wallet.WalletOptions.Builder()
                                            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                                            .build());

                    vBtnGooglePay.setVisibility(View.GONE);
                    vTVGoogleStatus.setVisibility(View.VISIBLE);
                    vTVGoogleStatus.setText(getText(R.string.googlepay_status_checking));
                    isReadyToPay();

                    //mPrice = price;
                    PaymentDataRequest request = createPaymentDataRequest();
                    if (request != null) {
                        AutoResolveHelper.resolveTask(
                                mPaymentsClient.loadPaymentData(request),
                                Grant.this,
                                // LOAD_PAYMENT_DATA_REQUEST_CODE is a constant value
                                // you define.
                                LOAD_PAYMENT_DATA_REQUEST_CODE);
                    }

                    // taking as if payments done

                    //if whole amount paid -
                        final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child("History");

                        final String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                        final String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        final String key = currentDate + " " + currentTime;

                        Paid p = new Paid(String.valueOf(val),DESC.getText().toString(),his_id,currentDate + " " + currentTime,"given","");
                        dataRef.child(my_id).child(key).setValue(p).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Paid p2 = new Paid(Payable_amt.getText().toString(),DESC.getText().toString(),my_id,currentDate + " " + currentTime,"received","");
                                dataRef.child(his_id).child(key).setValue(p2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Toast.makeText(Grant.this, "Payment successfully done", Toast.LENGTH_SHORT).show();
                                            if(val == bala){
                                                // TODO - delete the transcation node
//                                                Log.e("val and bala2:",val + " " + bala);
//                                                Log.e("AAgya","remo me");
                                                final DatabaseReference refr = FirebaseDatabase.getInstance().getReference("Transactions")
                                                        ;
                                                refr.child("Grant").child(my_id).child(his_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        refr.child("Request").child(his_id).child(my_id).removeValue();
                                                        Toast.makeText(Grant.this, "All transactions cleared", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }

                                                //deduct the remaining balance
                                                // TODO - add in notification list for receiver..that he received some money
                                                final DatabaseReference dataRef2 = FirebaseDatabase.getInstance().getReference().child("Notifications");
                                                Paid p3 = new Paid(Payable_amt.getText().toString(),DESC.getText().toString(),my_id,currentDate + " " + currentTime,"received","no");
                                                dataRef2.child(his_id).child(key).setValue(p3).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(!task.isSuccessful())
                                                            Toast.makeText(Grant.this, "Notification not sent to him", Toast.LENGTH_SHORT).show();
                                                    }
                                            });


                                        }
                                        else
                                            Toast.makeText(Grant.this, "Unsuccessful payment", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });



                    //startActivity(new Intent(Grant.this, Pay.class));
                }
            }
        });

        vBtnGooglePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vBtnGooglePay.setClickable(false);

                PaymentDataRequest request = createPaymentDataRequest();
                if (request != null) {
                    AutoResolveHelper.resolveTask(
                            mPaymentsClient.loadPaymentData(request),
                            Grant.this,
                            // LOAD_PAYMENT_DATA_REQUEST_CODE is a constant value
                            // you define.
                            LOAD_PAYMENT_DATA_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        String token = paymentData.getPaymentMethodToken().getToken();
                        Log.d("Token", token);
                        handlePaymentSuccess(paymentData);
                        final DatabaseReference refo = FirebaseDatabase.getInstance().getReference().child("Transactions");
                        HashMap<String, Object> m = new HashMap<>();
                        int rem = bala - val;
                        if(rem > 0){
                            m.put("amount",String.valueOf(rem));
                            refo.child("Grant").child(my_id).child(his_id).updateChildren(m);
                            refo.child("Request").child(his_id).child(my_id).updateChildren(m);
                        }


                        break;
                    case RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        final DatabaseReference refo2 = FirebaseDatabase.getInstance().getReference().child("Transactions");
                        HashMap<String, Object> m2 = new HashMap<>();
                        int rem2 = bala - val;
                        if(rem2>0){
                            m2.put("amount",String.valueOf(rem2));
                            refo2.child("Grant").child(my_id).child(his_id).updateChildren(m2);
                            refo2.child("Request").child(his_id).child(my_id).updateChildren(m2);
                        }

                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        // Log the status for debugging.
                        // Generally, there is no need to show an error to
                        // the user as the Google Pay API will do that.
                        Log.d("Status", status.toString());
                        handleError(status.getStatusCode());
                        final DatabaseReference refo3 = FirebaseDatabase.getInstance().getReference().child("Transactions");
                        HashMap<String, Object> m3 = new HashMap<>();
                        int rem3 = bala - val;
                        if(rem3  > 0){
                            m3.put("amount",String.valueOf(rem3));
                            refo3.child("Grant").child(my_id).child(his_id).updateChildren(m3);
                            refo3.child("Request").child(his_id).child(my_id).updateChildren(m3);
                        }

                        break;
                    default:
                        // Do nothing.
                }

                // Re-enables the Pay with Google button.
                vBtnGooglePay.setClickable(true);
                vBtnGooglePay.setVisibility(View.VISIBLE);
                break;
            default:
                // Do nothing.
        }
    }

    private void handleError(int statusCode) {
        // At this stage, the user has already seen a popup informing them an error occurred.
        // Normally, only logging is required.
        // statusCode will hold the value of any constant from CommonStatusCode or one of the
        // WalletConstants.ERROR_CODE_* constants.
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        //
        // Refer to your processor's documentation on how to proceed from here.
        PaymentMethodToken token = paymentData.getPaymentMethodToken();

        // getPaymentMethodToken will only return null if PaymentMethodTokenizationParameters was
        // not set in the PaymentRequest.
        if (token != null) {
            // If the gateway is set to example, no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (token.getToken().equals("examplePaymentMethodToken")) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Gateway name set to \"example\" - please modify " +
                                "Constants.java and replace it with your own gateway.")
                        .setPositiveButton("OK", null)
                        .create();
                alertDialog.show();
            }

            //String billingName = paymentData.getCardInfo().getBillingAddress().getName();
            Toast.makeText(this, getString(R.string.payments_show_name), Toast.LENGTH_LONG).show();

            // Use token.getToken() to get the token string.
            Log.d("PaymentData", "PaymentMethodToken received");
        }
    }

    private void isReadyToPay() {
        IsReadyToPayRequest request =
                IsReadyToPayRequest.newBuilder()
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                        .build();
        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(Task<Boolean> task) {
                        try {
                            boolean result = task.getResult(ApiException.class);
                            setGooglePayAvailable(result);
                        } catch (ApiException exception) {
                            Log.w("isReadyToPay failed", exception);
                        }
                    }
                });
    }

    private void setGooglePayAvailable(boolean available) {
        // If isReadyToPay returned true, show the button and hide the "checking" text. Otherwise,
        // notify the user that Pay with Google is not available.
        // Please adjust to fit in with your current user flow. You are not required to explicitly
        // let the user know if isReadyToPay returns false.
        if (available) {
            vTVGoogleStatus.setVisibility(View.GONE);
            vBtnGooglePay.setVisibility(View.GONE);
        } else {
            vTVGoogleStatus.setText(R.string.googlepay_status_unavailable);
        }
    }

    private PaymentDataRequest createPaymentDataRequest() {
        PaymentDataRequest.Builder request =
                PaymentDataRequest.newBuilder()
                        .setTransactionInfo(
                                // Supported countries for shipping (use ISO 3166-1 alpha-2 country codes).
                                // Relevant only when requesting a shipping address.
                                TransactionInfo.newBuilder()
                                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                        .setTotalPrice(mPrice)
                                        .setCurrencyCode("USD")
                                        .build())
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                        .setCardRequirements(
                                CardRequirements.newBuilder()
                                        .addAllowedCardNetworks(
                                                Arrays.asList(
                                                        WalletConstants.CARD_NETWORK_AMEX,
                                                        WalletConstants.CARD_NETWORK_DISCOVER,
                                                        WalletConstants.CARD_NETWORK_VISA,
                                                        WalletConstants.CARD_NETWORK_MASTERCARD))
                                        .build());

        // Custom parameters required by the processor / gateway.
        // In many cases, your processor / gateway will only require a gatewayMerchantId.
        // Please refer to your processor's documentation for more information. The number of parameters
        // required and their names vary depending on the processor.
        PaymentMethodTokenizationParameters params =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                        .addParameter("gateway", "payu")
                        .addParameter("gatewayMerchantId", "0fc52ed3fe43305549924570e64d5427 ")
                        .build();

        request.setPaymentMethodTokenizationParameters(params);
        return request.build();
    }

//    @Override
//    public void onBuyClick(String price) {
//        mPrice = price;
//        PaymentDataRequest request = createPaymentDataRequest();
//        if (request != null) {
//            AutoResolveHelper.resolveTask(
//                    mPaymentsClient.loadPaymentData(request),
//                    Grant.this,
//                    // LOAD_PAYMENT_DATA_REQUEST_CODE is a constant value
//                    // you define.
//                    LOAD_PAYMENT_DATA_REQUEST_CODE);
//        }
//    }
}
