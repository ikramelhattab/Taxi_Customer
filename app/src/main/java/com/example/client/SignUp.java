package com.example.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.client.Common.Common;
import com.example.client.Model.Client;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {


    Button regtologinbtn,regbtn;
    TextInputLayout regname,regusername,regemail,regph,regpwd;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        regname = findViewById(R.id.reg_name);
       // regusername = findViewById(R.id.reg_username);
        regemail = findViewById(R.id.reg_email);
        regph = findViewById(R.id.reg_phone);
        regpwd = findViewById(R.id.reg_password);


        regtologinbtn = findViewById(R.id.reg_log);

        regtologinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SignUp.this,Login.class);
                startActivity(intent);
            }
        });
        regbtn = findViewById(R.id.reg_signup);

        //save data in firebase
        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth = FirebaseAuth.getInstance();
                db=FirebaseDatabase.getInstance("https://taxi-3b609-default-rtdb.firebaseio.com/");
                users=db.getReference(Common.user_client_tb1);

                if (!ValidateEmail()| !ValidatePassword()){
                    //get all values

                    auth.createUserWithEmailAndPassword(regemail.getEditText().getText().toString(),regpwd.getEditText().getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                                    Client c = new Client();
                                    c.setEmail(regemail.getEditText().getText().toString());
                                    c.setPassword(regpwd.getEditText().getText().toString());
                                    c.setName(regname.getEditText().getText().toString());
                                    c.setPhoneNo(regph.getEditText().getText().toString());

                                    users.child(regph.getEditText().getText().toString())
                                            .setValue(c)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Register success", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                }
                            });

                    /*
                    String name =regname.getEditText().getText().toString();
                    String username =regusername.getEditText().getText().toString();
                    String email =regemail.getEditText().getText().toString();
                    String phoneno =regph.getEditText().getText().toString();
                    String pwd =regpwd.getEditText().getText().toString();
*/


                    // Toast.makeText(this,"Your account has been created successfully!",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(),Login.class);
                    startActivity(intent);
                    finish();
                }



            }
        });



    }

    private Boolean ValidateEmail(){
        String val=regemail.getEditText().getText().toString();
        String noWhiteSpace ="\\A\\w{4,20}\\z";

        if (val.isEmpty()){
            regemail.setError("Field cannot be empty");
            return false;
        }

      /*  else if (val.length()>=15){
            regemail.setError("Username too long");
            return false;
        }*/
        else if (!val.matches(noWhiteSpace)){
            regemail.setError("White Space are not allowed");
            return false;
        }

        else {
            regemail.setError(null);
            regemail.setErrorEnabled(false);
            return true;
        }

    }

    private boolean ValidatePassword() {
        String val=regpwd.getEditText().getText().toString();
        String passwordVal ="^" +
                "(?=.*[a-zA-Z])" + //any letter
                "(?=.*[@#$%^&+=])" + //at least 1 special character
                "(?=\\S+$)" +  //no white spaces
                ".{6,}" + // at least 6 characters
                "$";

        if (val.isEmpty()){
            regpwd.setError("Field cannot be empty");
            return false;
        }
        else if (!val.matches(passwordVal)){
            regpwd.setError("Password is too weak");
            return false;}
        else {
            regpwd.setError(null);
            regpwd.setErrorEnabled(false);
            return true;
        }
    }


    public void loginUser(View view){
        if (!ValidateEmail()| !ValidatePassword()){
            return;
        }
        else {
            isUser();
        }
    }

    private void isUser() {
        final String userEnteredEmail = regemail.getEditText().getText().toString().trim();
        final String userEnteredPassword = regpwd.getEditText().getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1);

        Query checkUser=reference.orderByChild("email").equalTo(userEnteredEmail);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    regemail.setError(null);
                    regemail.setErrorEnabled(false);

                    String passwordFromDB = dataSnapshot.child(userEnteredEmail).child("password").getValue(String.class);

                    if (passwordFromDB.equals(userEnteredPassword)){

                        regemail.setError(null);
                        regemail.setErrorEnabled(false);

                        String nameFromDB = dataSnapshot.child(userEnteredEmail).child("name").getValue(String.class);
                        String usernameFromDB = dataSnapshot.child(userEnteredEmail).child("username").getValue(String.class);
                        String phoneNoFromDB = dataSnapshot.child(userEnteredEmail).child("phoneNo").getValue(String.class);
                        String emailFromDB = dataSnapshot.child(userEnteredEmail).child("email").getValue(String.class);

                      /*  Intent intent =new Intent(getApplicationContext(), UserProfile.class);

                        intent.putExtra("name",nameFromDB);
                        intent.putExtra("username",usernameFromDB);
                        intent.putExtra("email",emailFromDB);
                        intent.putExtra("phoneNo",phoneNoFromDB);
                        intent.putExtra("password",passwordFromDB);

                        startActivity(intent);*/
                    }

                    else {
                        regpwd.setError("Wrong Password");
                        regpwd.requestFocus();
                    }
                }

                else {
                    regemail.setError("No such User Exist");
                    regemail.requestFocus();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}