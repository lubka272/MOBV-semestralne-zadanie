package com.example.zadanie.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentFriendsBinding
import com.example.zadanie.helpers.Injection
import com.example.zadanie.helpers.PreferenceData
import com.example.zadanie.ui.viewmodels.FollowersViewModel
import com.example.zadanie.ui.viewmodels.FriendsViewModel

class FriendsFragment : Fragment() {
    private lateinit var binding: FragmentFriendsBinding
    private lateinit var viewmodel: FriendsViewModel
    private lateinit var followersViewModel: FollowersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        )[FriendsViewModel::class.java]

        followersViewModel = ViewModelProvider(
            this,
            Injection.provideViewModelFactory(requireContext())
        )[FollowersViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val x = PreferenceData.getInstance().getUserItem(requireContext())
        if ((x?.uid ?: "").isBlank()) {
            Navigation.findNavController(view).navigate(R.id.action_to_login)
            return
        }


        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            model = viewmodel
            followerModel = followersViewModel

            swiperefresh.setOnRefreshListener {
                viewmodel.refreshData()
            }

            swiperefreshFollower.setOnRefreshListener {
                followersViewModel.refreshData()
            }

            addFriendButton.setOnClickListener {
                val username = friendUsername.text.toString()
                if (username.trim().isNotEmpty()) {
                    viewmodel.addFriend(username)
                } else {
                    viewmodel.show("Ak chcete pridať priateľa, vyplňte jeho meno!")
                }

            }
        }

        viewmodel.loading.observe(viewLifecycleOwner) {
            binding.swiperefresh.isRefreshing = it
        }

        followersViewModel.loading.observe(viewLifecycleOwner) {
            binding.swiperefreshFollower.isRefreshing = it
        }

        viewmodel.message.observe(viewLifecycleOwner) {
            if (PreferenceData.getInstance().getUserItem(requireContext()) == null) {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
            }
        }

        followersViewModel.message.observe(viewLifecycleOwner) {
            if (PreferenceData.getInstance().getUserItem(requireContext()) == null) {
                Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
            }
        }
        setMenuBar()

    }

    private fun setMenuBar() {
        (activity as AppCompatActivity?)!!.supportActionBar!!.title = "Priatelia a sledujúci"

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.app_menu, menu)
                val backButton = menu.findItem(R.id.back_menu)
                backButton.isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.logout_menu -> {
                        PreferenceData.getInstance().clearData(requireContext())
                        Navigation.findNavController(requireView()).navigate(R.id.action_to_login)
                        return true
                    }
                    R.id.back_menu -> {
                        Navigation.findNavController(requireView()).navigate(R.id.action_to_bars)
                        return true
                    }
                    else -> false

                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}