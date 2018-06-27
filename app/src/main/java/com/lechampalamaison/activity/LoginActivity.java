package com.lechampalamaison.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lechampalamaison.R;
import com.lechampalamaison.api.model.Login;
import com.lechampalamaison.api.model.apiResponse.AuthResponse;
import com.lechampalamaison.api.service.UserClient;
import com.lechampalamaison.api.utils.Configuration;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    public ProgressDialog progressDialog;

    Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(Configuration.urlApi)
            .addConverterFactory(GsonConverterFactory.create());

    Retrofit retrofit = builder.build();
    UserClient userClient = retrofit.create(UserClient.class);

    @BindView(R.id.input_login) EditText _loginText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Connexion...");
        progressDialog.show();

        String loginUser = _loginText.getText().toString();
        String password = _passwordText.getText().toString();

        Login login = new Login(loginUser, password);
        Call<AuthResponse> call = userClient.login(login);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if(response.body().getCode() == 0){

                    /*ArrayList<Cart> arrayListCart = new ArrayList<Cart>();
                    String jsonCart = new Gson().toJson(arrayListCart);
                    ArrayList<Paypal> arrayListPaypal = new ArrayList<Paypal>();
                    String jsonPaypal = new Gson().toJson(arrayListPaypal);*/

                    SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();

                    editor.putBoolean(getString(R.string.isLoggedin_key), true);
                    editor.putString(getString(R.string.login_key), response.body().getResult().getLoginUser());
                    editor.putInt(getString(R.string.type_key), response.body().getResult().getTypeUser());
                    editor.putString(getString(R.string.token_key), response.body().getResult().getToken());
                    editor.putString(getString(R.string.cart_key), null);
                    editor.putString(getString(R.string.paypal_key), null);

                    editor.commit();
                    onLoginSuccess();
                }else{
                    Toast.makeText(LoginActivity.this, "Erreur combinaison identifiant/mot de passe.", Toast.LENGTH_SHORT).show();
                    onLoginFailed();
                }
            }
            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Erreur combinaison identifiant/mot de passe.", Toast.LENGTH_SHORT).show();
                onLoginFailed();
            }
        });


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        //onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 2000);
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        progressDialog.dismiss();
        _loginButton.setEnabled(true);
        /*Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);*/
        finish();
    }

    public void onLoginFailed() {
        progressDialog.dismiss();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {

        boolean valid = true;
        String login = _loginText.getText().toString();
        String password = _passwordText.getText().toString();

        if (login.isEmpty() ) {
            _loginText.setError("Veuillez saisir un identifiant");
            valid = false;
        } else {
            _loginText.setError(null);
        }

        if (password.isEmpty()) {
            _passwordText.setError("Veuillez saisir un mot de passe");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}