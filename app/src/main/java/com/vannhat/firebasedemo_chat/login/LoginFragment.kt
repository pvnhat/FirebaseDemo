package com.vannhat.firebasedemo_chat.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.vannhat.firebasedemo_chat.MainActivity
import com.vannhat.firebasedemo_chat.R
import com.vannhat.firebasedemo_chat.createLog
import com.vannhat.firebasedemo_chat.createToast
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : BaseLoginFragment() {

    private var googleSignInClient: GoogleApiClient? = null
    private var callbackManager = CallbackManager.Factory.create()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initGoogleLogIn()
        initFacebookLogIn()
        handleEvent()
        checkLoggedIn()
    }

    private fun initFacebookLogIn() {
        btn_facebook_login.setReadPermissions("email", "public_profile")
        btn_facebook_login.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    result?.accessToken?.let { handleFacebookAccessToken(it) }
                }

                override fun onCancel() {
                    createLog("Canceled")
                }

                override fun onError(error: FacebookException?) {
                    createLog("Fail: " + error?.message)
                }
            })

    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        getLogActivity().auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createToast(requireContext(), "Logged In with Facebook successful !")
                    val user = getLogActivity().auth.currentUser
                    startActivity(MainActivity.newInstance(requireContext(),
                        "user : ${user?.displayName} , ${user?.email}",
                        user?.photoUrl.toString()))
                } else {
                    createLog("Fail: " + task.exception?.message)
                }
            }
    }

    private fun initGoogleLogIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                "946649962472-ukjtca0g373j9t66kd0iqv696ccdgns7.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleApiClient.Builder(requireContext())
            .enableAutoManage(requireActivity()) {
                createLog("Connection failed !")
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    private fun checkLoggedIn() {
        if (getLogActivity().auth.currentUser?.email == null)
            return
        else
            startActivity(Intent(requireActivity(), MainActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN_RC && resultCode == Activity.RESULT_OK && data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                createLog("Fail: " + e.message)
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        getLogActivity().auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createToast(requireContext(), "Logged In with Google successful !")
                    startActivity(MainActivity.newInstance(requireContext(),
                        "user : ${account.displayName} , ${account.email}",
                        account.photoUrl.toString()))
                } else {
                    createLog("Fail: " + task.exception?.message)
                }
            }
    }

    private fun handleEvent() {
        tv_register.setOnClickListener {
            (requireActivity() as LoginRegisterActivity).addFragment(
                requireActivity().supportFragmentManager, RegisterFragment.newInstance(),
                R.id.fl_container_full, true
            )
        }

        btn_login.setOnClickListener {
            logInSystem()
        }

        tv_forgot_pass.setOnClickListener {
            sendPassResetMail()
        }

        sign_in_gg_button.setOnClickListener {
            googleSignIn()
        }
    }

    private fun googleSignIn() {
        startActivityForResult(
            Auth.GoogleSignInApi.getSignInIntent(googleSignInClient),
            GOOGLE_SIGN_IN_RC
        )
    }

    private fun sendPassResetMail() {
        getLogActivity().auth.sendPasswordResetEmail(edt_username.text.trim().toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) createToast(requireContext(), "Sent !")
            }
    }

    private fun logInSystem() {
        val username = edt_username.text.trim().toString()
        val password = edt_password.text.trim().toString()
        getLogActivity().auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    startActivity(Intent(getLogActivity(), MainActivity::class.java))
                else
                    task.exception?.message?.let { createToast(requireContext(), it) }

                getLogActivity().auth.currentUser?.apply {
                    createLog("email: $email, $username, $password, $displayName")
                }
            }

    }

    companion object {
        private const val GOOGLE_SIGN_IN_RC = 96

        fun newInstance() = LoginFragment()
    }
}
