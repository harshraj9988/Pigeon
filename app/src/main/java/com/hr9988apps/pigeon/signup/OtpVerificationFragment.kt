package com.hr9988apps.pigeon.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.FragmentOtpVerificationBinding
import com.hr9988apps.pigeon.utils.ConnectionLiveData
import java.util.concurrent.TimeUnit

class OtpVerificationFragment : Fragment() {

    /*********************** Global Variables *********************************************************/

    private lateinit var binding: FragmentOtpVerificationBinding

    private lateinit var phoneNumber: String

    //instance for firebase authentication
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var storedVerificationId: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var connectionLiveData: ConnectionLiveData

    private lateinit var args: OtpVerificationFragmentArgs

    /**************************************************************************************************/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_otp_verification, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // checking if the device has a network connection or not
        connectionLiveData = ConnectionLiveData(requireContext())
        connectionLiveData.observe(viewLifecycleOwner) { isNetworkAvailable ->
            if (isNetworkAvailable) {
                binding.progressBar.visibility = View.INVISIBLE
                binding.noInternet.visibility = View.INVISIBLE
                binding.mainLayout.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                binding.noInternet.visibility = View.VISIBLE
                binding.mainLayout.visibility = View.INVISIBLE
            }
        }

        args = OtpVerificationFragmentArgs.fromBundle(requireArguments())
        phoneNumber = args.phoneNumber

        binding.otpVarificationTitle.text = "Verify $phoneNumber"
        binding.invalidateAll()

        sendVerificationCode()

        binding.verifyBtn.setOnClickListener {
            binding.otpProgressBar.visibility = View.VISIBLE
            binding.verifyBtn.visibility = View.INVISIBLE
            verifyCode(binding.otpBox.text.toString())
        }

        binding.backBtn.setOnClickListener {

            Navigation.findNavController(it)
                .navigate(OtpVerificationFragmentDirections.actionOtpVerificationFragmentToPhoneRegFragment())
        }
    }

    /**********************************  helper methods  **********************************************/

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            val code: String? = credential.smsCode

            if (code != null) {
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            println("Failed verification")
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(verificationId, token)
            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
            binding.verifyBtn.isEnabled = true
            binding.otpProgressBar.visibility = View.INVISIBLE
            binding.verifyBtn.visibility = View.VISIBLE
        }
    }

    private fun sendVerificationCode() {
        try {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity())                 // Activity (for callback binding)
                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: IllegalStateException) {
            Toast.makeText(context, "Failed! Please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyCode(code: String) {

        val credential: PhoneAuthCredential =
            PhoneAuthProvider.getCredential(storedVerificationId, code)
        signInByCredential(credential)

    }

    private fun signInByCredential(credential: PhoneAuthCredential) {
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Navigation.findNavController(requireView())
                        .navigate(OtpVerificationFragmentDirections.actionOtpVerificationFragmentToSetupUserProfileFragment(true))
                } else {
                    Toast.makeText(context, "Wrong OTP", Toast.LENGTH_SHORT).show()
                    binding.otpProgressBar.visibility = View.INVISIBLE
                    binding.verifyBtn.visibility = View.VISIBLE
                }
            }
    }
}
