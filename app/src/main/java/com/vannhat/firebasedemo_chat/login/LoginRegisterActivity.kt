package com.vannhat.firebasedemo_chat.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.vannhat.firebasedemo_chat.R

class LoginRegisterActivity : AppCompatActivity() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        val loginFrag = LoginFragment.newInstance()
        addFragment(supportFragmentManager, loginFrag, R.id.fl_container_full)
    }

    fun addFragment(fragmentManager: FragmentManager,
        fragment: Fragment, frameId: Int, addToBackStack: Boolean = false, tag: String? = null,
        bundle: Bundle? = null, haveAnimation: Boolean = false) {
        val transaction = fragmentManager.beginTransaction()
        if (haveAnimation)
            transaction.setCustomAnimations(R.anim.slide_bottom_up, R.anim.slide_top_down,
                R.anim.slide_bottom_up, R.anim.slide_top_down)
        if (fragment.arguments == null) {
            fragment.arguments = bundle
        }
        transaction.add(frameId, fragment, tag)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

}


