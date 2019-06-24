package com.vannhat.firebasedemo_chat.login

import androidx.fragment.app.Fragment


open class BaseLoginFragment : Fragment() {
    fun getLogActivity() = requireActivity() as LoginRegisterActivity
}