package com.app.sns_project.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.app.sns_project.ItemPagerAdapter
import com.app.sns_project.R
import com.app.sns_project.databinding.FragmentPostUpdateBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


class PostUpdateFragment : Fragment() {
    private lateinit var binding: FragmentPostUpdateBinding

    lateinit var auth: FirebaseAuth
    lateinit var storage : FirebaseStorage
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostUpdateBinding.inflate(inflater, container, false)

        //init
        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage
        firestore = FirebaseFirestore.getInstance()

        //postId 받기
        val args:PostUpdateFragmentArgs by navArgs()
        val postId = args.postId

        queryItem(postId)

        binding.updateButton.setOnClickListener {
            updateContent(postId)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        textWatcher()
    }

    private fun textWatcher(){
        binding.postEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d("textWatcher","beforeTextChanged")
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
            override fun afterTextChanged(p: Editable?) {
                Log.d("textWatcher","afterTextChanged")
                if(binding.postEdittext.text!!.isEmpty()){
                    binding.postTextview.error = "글을 입력해주세요"
                }else if(binding.postEdittext.text!!.length > 200){
                    binding.postTextview.error = "200자 이내로 작성해주세요"
                }else{
                    binding.postTextview.error = null
                }
                btnEnable()

            }
        })
    }

    private fun btnEnable(){
        binding.updateButton.isEnabled = binding.postEdittext.text!!.length in 1..200
    }

    private fun queryItem(itemId:String){
        firestore?.collection("post").document(itemId).get().addOnSuccessListener {
            binding.userName.text = it["userName"] as String
            binding.postEdittext.setText( it["content"] as String)
            val imageArray = it["imageUrl"] as ArrayList<String>

            val adapter = ItemPagerAdapter(requireActivity(),imageArray,2)
            binding.imageList.adapter = adapter
            val gridLayoutManager = GridLayoutManager(context,2)
            binding.imageList.layoutManager = gridLayoutManager

        }
    }

    private fun updateContent(itemId: String){
        val content = binding.postEdittext.text.toString()
        firestore?.collection("post").document(itemId).update("content",content).addOnSuccessListener {
            Snackbar.make(binding.root, "수정되었습니다!!", Snackbar.LENGTH_SHORT).show()
            //업데이트 후 메인화면
            findNavController().navigate(R.id.action_postUpdateFragment_to_profileFragment)
        }
    }
}