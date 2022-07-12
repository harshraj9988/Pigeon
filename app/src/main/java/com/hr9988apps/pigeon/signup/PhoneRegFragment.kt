package com.hr9988apps.pigeon.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.hr9988apps.pigeon.R
import com.hr9988apps.pigeon.databinding.FragmentPhoneRegBinding
import com.hr9988apps.pigeon.utils.ConnectionLiveData

class PhoneRegFragment : Fragment() {

/************************ Global Variables *******************************************************/

    private lateinit var binding: FragmentPhoneRegBinding
    private lateinit var connectionLiveData: ConnectionLiveData
    private lateinit var phoneNumber: String

/*************************************************************************************************/
    private lateinit var viewModel: PhoneRegViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_phone_reg, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[PhoneRegViewModel::class.java]

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

        binding.continueBtn.setOnClickListener {
            if (binding.phoneBox.text.toString().length < 10) {
                Toast.makeText(context, "Please enter a valid number", Toast.LENGTH_LONG).show()
            } else {
                phoneNumber = "${binding.countryCodeBox.text}${binding.phoneBox.text}"
                confirmPhoneNumber(it)
            }
        }
    }

    private fun confirmPhoneNumber(view: View){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Is this number correct?")
        builder.setMessage(phoneNumber)
        builder.setPositiveButton(
            "correct"
        ) { _, _ -> goToOtpVerification(view) }
        builder.setNegativeButton("change") { _, _ ->
            return@setNegativeButton
        }
        builder.show()
    }

    private fun goToOtpVerification(view: View){
        Navigation.findNavController(view).navigate(PhoneRegFragmentDirections.actionPhoneRegFragmentToOtpVerificationFragment(phoneNumber))
    }

}
