package com.example.zadanie.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.zadanie.R
import com.example.zadanie.data.datastore.AppSettingsRepository
import com.example.zadanie.databinding.FragmentBarsBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.viewmodels.BarsViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class BarsFragment : Fragment() {
    private lateinit var binding: FragmentBarsBinding
    private lateinit var viewmodel: BarsViewModel
    private lateinit var appSettingsRepository: AppSettingsRepository

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_locate)
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewmodel.show("Bol udelený iba prístup k približnej polohe.")
            }
            else -> {
                viewmodel.show("Prístup k vašej polohe nieje povolený.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        ).get(BarsViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBarsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appSettingsRepository = AppSettingsRepository(requireContext())

        val preferenceDataInstance = PreferenceData.getInstance()
        val user = preferenceDataInstance.getUserItem(requireContext())
        if ((user?.uid ?: "").isBlank()) {
            Navigation.findNavController(view).navigate(R.id.action_to_login)
            return
        }

        makeSortIconsClicable()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewmodel
        }.also { bnd ->
            bnd.swiperefresh.setOnRefreshListener {
                viewmodel.refreshData()
            }

            bnd.findBar.setOnClickListener {
                if (checkPermissions()) {
                    it.findNavController().navigate(R.id.action_to_locate)
                } else {
                    locationPermissionRequest.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        }

        viewmodel.loading.observe(viewLifecycleOwner) {
            binding.swiperefresh.isRefreshing = it
        }

        viewmodel.message.observe(viewLifecycleOwner) {
            if (PreferenceData.getInstance().getUserItem(requireContext()) == null) {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
            }
        }
        setMenuBar()
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun makeSortIconsClicable() {
        binding.sortByName.setOnClickListener {
            runBlocking {
                val isDataSortedByName = appSettingsRepository.getIsSortedByName.asLiveData()
                isDataSortedByName.observe(viewLifecycleOwner) { isSortedAsc ->
                    if (isSortedAsc == 1) {
                        launch {
                            binding.sortByName.setCompoundDrawablesWithIntrinsicBounds(
                                0,0,R.drawable.arrow_up, 0
                            )
                            binding.sortByPocet.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.sort_numbers, 0
                            )
                            appSettingsRepository.saveIsSortedByName(0)
                            appSettingsRepository.saveIsSortedByPocet(2)
                        }
                    } else {
                        launch {
                            binding.sortByName.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.arrow_down, 0
                            )
                            binding.sortByPocet.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.sort_numbers, 0
                            )
                            appSettingsRepository.saveIsSortedByName(1)
                            appSettingsRepository.saveIsSortedByPocet(2)
                        }
                    }
                }
            }

        }

        binding.sortByPocet.setOnClickListener {
            runBlocking {
                val isDataSortedByPocet = appSettingsRepository.getIsSortedByPocet.asLiveData()
                isDataSortedByPocet.observe(viewLifecycleOwner) { isSortedAsc ->
                    if (isSortedAsc == 1) {
                        launch {
                            binding.sortByPocet.setCompoundDrawablesWithIntrinsicBounds(
                                0,0,R.drawable.arrow_up, 0
                            )
                            binding.sortByName.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.sort, 0
                            )
                            appSettingsRepository.saveIsSortedByPocet(0)
                            appSettingsRepository.saveIsSortedByName(2)
                        }
                    } else {
                        launch {
                            binding.sortByPocet.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.arrow_down, 0
                            )
                            binding.sortByName.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.sort, 0
                            )
                            appSettingsRepository.saveIsSortedByPocet(1)
                            appSettingsRepository.saveIsSortedByName(2)
                        }
                    }
                }
            }

        }
    }

    private fun setMenuBar() {
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Zoznam podnikov"
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.app_menu, menu)

                val friendButton = menu.findItem(R.id.friend_menu)
                friendButton.isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.logout_menu -> {
                        PreferenceData.getInstance().clearData(requireContext())
                        Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
                        return true
                    }
                    R.id.friend_menu -> {
                        Navigation.findNavController(requireView()).navigate(R.id.action_to_friends)
                        return true
                    }
                    else -> false

                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}