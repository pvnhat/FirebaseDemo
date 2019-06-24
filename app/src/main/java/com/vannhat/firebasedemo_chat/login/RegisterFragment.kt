package com.vannhat.firebasedemo_chat.login

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.UserProfileChangeRequest
import com.vannhat.firebasedemo_chat.R
import com.vannhat.firebasedemo_chat.createToast
import kotlinx.android.synthetic.main.fragment_register.*


class RegisterFragment : BaseLoginFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleEvent()
    }

    private fun handleEvent() {
        btn_register.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val username = edt_username.text.trim().toString()
        val password = edt_password.text.trim().toString()
        getLogActivity().auth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createToast(requireContext(), "Registered successful !!!")
                    updateProfile()
                } else {
                    task.exception?.localizedMessage?.let { createToast(requireContext(), it) }
                }
                AuthUI.getInstance().signOut(requireContext())
            }
    }

    private fun updateProfile() {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("Nhat nhen")
            .setPhotoUri(Uri.parse(
                "http://images.phunucuocsong.vn/assets/catalog/2018-10/07/ngan/2018-10-07-1538916657-0.jpg"))
            .build()
        getLogActivity().auth.currentUser?.updateProfile(profileUpdates)
    }

    companion object {
        fun newInstance() = RegisterFragment()
    }
}
