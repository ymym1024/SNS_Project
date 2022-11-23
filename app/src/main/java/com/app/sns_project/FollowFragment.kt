package com.app.sns_project

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FollowFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val tabTitleArray = arrayOf(
        "팔로워",
        "팔로잉"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view:View = inflater.inflate(R.layout.fragment_follow, container, false)
        viewPager = view.findViewById(R.id.pager)
        tabLayout = view.findViewById(R.id.tab_layout)
        return view
//        return inflater.inflate(R.layout.fragment_follower, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = PagerFragmentStateAdapter(requireActivity())
        // 2개의 fragment add
        pagerAdapter.addFragment(FollowerFragment())
        pagerAdapter.addFragment(FollowingFragment())

        // adapter 연결
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    0 -> {
                        pagerAdapter.refreshFragment(position,FollowerFragment())
                    }
                    1 -> {
                        pagerAdapter.refreshFragment(position,FollowingFragment())
                    }
                }
                Log.e("ViewPagerFragment", "Page ${position + 1}")
            }
        })

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitleArray[position]
        }.attach()

        val searchbutton = view.findViewById<ImageButton>(R.id.searchButton)
        searchbutton.setOnClickListener {
            println("search button clicked")
            val editTextSearchResult = view.findViewById<EditText>(R.id.editTextSearchPeople).text.toString()
//            Log.e("name",editTextSearchResult)
            if(editTextSearchResult=="" || editTextSearchResult == null){
                val layoutInflater = LayoutInflater.from(context)
                val view = layoutInflater.inflate(R.layout.custom_dialog_search,null)

                val alertDialog = AlertDialog.Builder(context,R.style.CustomAlertDialog)
                    .setView(view)
                    .create()

                val confirmButton = view.findViewById<ImageButton>(R.id.confirmButton)

                confirmButton.setOnClickListener {
                    alertDialog.dismiss()
                }
                alertDialog.show()
            }
            else{
                // bundle 이용
//                val bundle = Bundle()
//                bundle.putString("searchName",editTextSearchResult)
//                val searchFragment = SearchFragment()
//                searchFragment.arguments = bundle
////                Log.e("arguements check:",searchFragment.arguments?.getString("searchName").toString())
//
//                parentFragmentManager.commit {
//                    setReorderingAllowed(true)
//                    replace(R.id.nav_host_fragment, searchFragment, null)
//                    addToBackStack(null)
//                }
                val searchName = editTextSearchResult
                findNavController().navigate(FollowFragmentDirections.actionFollowFragmentToSearchFragment(searchName))
            }

        }
    }
}