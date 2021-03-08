package com.conestoga.househunt;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    //Variables
    EditText txtFullName, txtEmail, txtPassword, txtcnfpassword, txtPhone;
    Button btnCreate;
    TextView txtLogIn;
    ImageView ivback;
    FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    private final String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler= new Handler();
    private static final int REQUEST_CODE_FULLNAME = 1;
    private static final int REQUEST_CODE_EMAIL = 2;
    private static final int REQUEST_CODE_PHONE = 3;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        txtFullName = findViewById(R.id.txtFullName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtcnfpassword = findViewById(R.id.txtcnfpassword);
        txtPhone = findViewById(R.id.txtPhone);
        btnCreate = findViewById(R.id.btnCreate);
        txtLogIn = findViewById(R.id.txtLogIn);
        ivback = findViewById(R.id.ivback);
        progressBar = findViewById(R.id.progressBar);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        mHandler.post(
                new Runnable() {
                    public void run() {
                        InputMethodManager inputMethodManager =  (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInputFromWindow(txtFullName.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                        txtFullName.requestFocus();
                    }
                });

        firebaseAuth = FirebaseAuth.getInstance();

        //If already Logged In
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        ivback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //OnCreate press
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullname = txtFullName.getText().toString().trim();
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();
                String cnfpassword = txtcnfpassword.getText().toString().trim();
                final String phone = txtPhone.getText().toString().trim();

                //Some Authentication
                if (TextUtils.isEmpty(fullname)) {
                    txtFullName.setError("Name Required.");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    txtEmail.setError("Email Required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    txtPassword.setError("Password Required.");
                    return;
                }

                if (password.length() < 5) {
                    txtPassword.setError("Password should be at least 6 characters.");
                }

                if (!password.equals(cnfpassword)) {
                    txtcnfpassword.setError("Confirm Password does not match with Password.");
                }

                if (TextUtils.isEmpty(phone)) {
                    txtPhone.setError("Phone Number Required.");
                    return;
                }

                //Progress
                progressBar.setVisibility(View.VISIBLE);

                //Register New User
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in is successful
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            String fullname = txtFullName.getText().toString().trim();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullname).build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                            }
                                        }
                                    });

                            Toast.makeText(SignUpActivity.this, "Registration Successful!.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        //Registered User
        txtLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity(int request_code)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        startActivityForResult(intent, request_code);
    }

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            assert matches != null;
            if (requestCode == REQUEST_CODE_FULLNAME){
                txtFullName.setText(matches.get(0));
            }else if (requestCode == REQUEST_CODE_EMAIL){
                txtEmail.setText(matches.get(0));
            }else if (requestCode == REQUEST_CODE_PHONE){
                txtPhone.setText(matches.get(0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void takevoiceinput(final View view) {

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                int Request_code = 1234;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (txtFullName.getRight() - txtFullName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (view.getId() == R.id.txtFullName) {
                            // FullName action
                            Request_code = REQUEST_CODE_FULLNAME;
                        } else if (view.getId() == R.id.txtEmail) {
                            //Email action
                            Request_code = REQUEST_CODE_EMAIL;
                        } else if (view.getId() == R.id.txtPhone) {
                            //Phone action
                            Request_code = REQUEST_CODE_PHONE;
                        }
                        startVoiceRecognitionActivity(Request_code);
                        return true;
                    }
                }
                return false;
            }
        });
    }
}