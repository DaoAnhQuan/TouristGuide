package com.android.touristguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class Helper {
    public static final int LOGIN_MODE = 0;
    public static final int SIGN_UP_MODE = 1;
    public static final String INDIVIDUAL_GROUP = "individual";
    public static final String MEMBER_GROUP="member";
    public static final String LEADER_GROUP = "leader";
    public static boolean isValidEmail(String email){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    public static void showEmailVerificationDialog(final Context context, String email, final FirebaseUser user, int mode){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View emailVerificationDialogView = layoutInflater.inflate(R.layout.activity_email_verification,null);
        final AlertDialog emailVerificationDialog = new AlertDialog.Builder(context).create();
        Button btnCancel = (Button) emailVerificationDialogView.findViewById(R.id.btn_edit_email);
        final Button btnResend = (Button) emailVerificationDialogView.findViewById(R.id.btn_resend);
        Button btnBackToLogin = (Button) emailVerificationDialogView.findViewById(R.id.btn_back_to_login);
        TextView tvEmailAddress = (TextView) emailVerificationDialogView.findViewById(R.id.tv_email_address);

        tvEmailAddress.setText(email);

        if (mode == Helper.LOGIN_MODE){
            btnBackToLogin.setVisibility(View.GONE);
        }

        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity signUpActivity = (Activity) context;
                signUpActivity.finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailVerificationDialog.cancel();
            }
        });

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnResend.setEnabled(false);
                user.sendEmailVerification();
                new CountDownTimer(60000,1000){
                    @Override
                    public void onTick(long l) {
                        btnResend.setText(getBoldString(String.valueOf(l/1000)));
                        btnResend.setTextColor(0xFF838181);
                    }

                    @Override
                    public void onFinish() {
                        btnResend.setText(getBoldString(context.getString(R.string.resend)));
                        btnResend.setTextColor(0xFF2196F3);
                        btnResend.setEnabled(true);
                    }
                }.start();
            }
        });

        emailVerificationDialog.setTitle(context.getString(R.string.email_verification));
        emailVerificationDialog.setView(emailVerificationDialogView);
        emailVerificationDialog.show();
    }

    public static SpannableString getBoldString(String text){
        SpannableString spanString = new SpannableString(text);
        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
        return spanString;
    }

    public static AlertDialog createLoadingDialog(Context context){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View loadingDialogView = layoutInflater.inflate(R.layout.loading_dialog,null);
        final AlertDialog loadingDialog = new AlertDialog.Builder(context).create();
        loadingDialog.setView(loadingDialogView);
        loadingDialog.setCanceledOnTouchOutside(false);
        return loadingDialog;
    }

    public static void finishActivityFromContext(Context context){
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.finish();
    }

    public static void setTextViewUI(TextView tv, String text, String backgroundColor, String textColor, boolean isBold){
        if (isBold){
            tv.setText(getBoldString(text));
        }else{
            tv.setText(text);
        }
        tv.setTextColor(Color.parseColor(textColor));
        tv.setBackgroundColor(Color.parseColor(backgroundColor));
    }

    public static void loadAvatar(String url, ImageView imv, View parent, Context context,int drawableId ){
        if (url != null){
            Glide.with(parent).load(url).into(imv);
        }else{
            Glide.with(parent).load(ContextCompat.getDrawable(context,drawableId)).into(imv);
        }
    }

    public static Task<HttpsCallableResult> signUp(String username,String email, String avatar, FirebaseFunctions mFuntions){
        Map<String, String> data = new HashMap<>();
        data.put("username",username);
        data.put("email",email);
        if (avatar != null){
            data.put("avatar",avatar);
        }
        return mFuntions
                .getHttpsCallable("signUp")
                .call(data);
    }

    public static FirebaseFunctions initFirebaseFunctions(){
        FirebaseFunctions functions = FirebaseFunctions.getInstance();
        functions.useEmulator("192.168.43.181",5001);
        return functions;
    }

    public static void setHtmlToTextView(TextView tv, String content){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
        } else {
            tv.setText(Html.fromHtml(content));
        }
    }

    public static String createFirebaseStorageFilename(Uri uri){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String timestamp = String.valueOf((new Date()).getTime());
        String fbFilename = uid+"_"+timestamp;
        return fbFilename;
    }

    public static Task<List<Member>> getMembersLocation(FirebaseFunctions mFunctions){
        return mFunctions.getHttpsCallable("getMembersLocation")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, List<Member>>() {
                    @Override
                    public List<Member> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String,Object> result = (Map<String,Object>)task.getResult().getData();
                        List<Member> members = new ArrayList<>();
                        for (Map.Entry<String,Object> member:result.entrySet()){
                            Map<String,Object> map = (HashMap<String,Object>) member.getValue();
                            Member mem = new Member((String)map.get("uid"),(String)map.get("url"),(Double)map.get("latitude"),
                                    (Double)map.get("longitude"));
                            members.add(mem);
                        }
                        return members;
                    }
                });
    }

    public static Bitmap getMapMarker(Uri uri,Context context) throws ExecutionException, InterruptedException {
        View customMarkerView = LayoutInflater.from(context).inflate(R.layout.map_marker,null);
        CircleImageView imv = customMarkerView.findViewById(R.id.imv_avatar);
        if (uri != null){
            FutureTarget<Bitmap> futureTarget = Glide.with(customMarkerView).asBitmap().load(uri).submit();
            Bitmap bitmap = futureTarget.get();
            imv.setImageBitmap(bitmap);
        }else{
            imv.setCircleBackgroundColor(Color.parseColor("#aaaaaa"));
            imv.setImageResource(R.drawable.ic_baseline_person_white_24);
        }
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    public static Task<List<User>> searchUsers(String query) throws Exception{
        FirebaseFunctions mFunctions = initFirebaseFunctions();
        Map<String,String> data = new HashMap<>();
        data.put("query",query);
        return  mFunctions.getHttpsCallable("searchUser")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, List<User>>() {
                    @Override
                    public List<User> then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        Map<String,Object> result = (Map<String,Object>) task.getResult().getData();
                        List<User> listUser = new ArrayList<>();
                        for (Map.Entry<String,Object> user:result.entrySet()){
                            Map<String,Object> map = (Map<String,Object>) user.getValue();
                            User rUser = new User((String)map.get("uid"),(String)map.get("username"),(String)map.get("url"));
                            listUser.add(rUser);
                        }
                        return listUser;
                    }
                });
    }
}
