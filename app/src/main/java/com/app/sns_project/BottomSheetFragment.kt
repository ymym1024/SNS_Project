package com.app.sns_project

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.databinding.FragmentBottomSheetBinding
import com.app.sns_project.fragment.MainFragmentDirections
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore


class BottomSheetFragment(val postId:String) : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomSheetBinding.inflate(inflater,container,false)

        firestore = FirebaseFirestore.getInstance()

        //수정하기 버튼
        binding.postUpdate.setOnClickListener {
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToPostUpdateFragment(postId))
            dismiss()
        }

        //삭제하기 버튼
        binding.postDelete.setOnClickListener {
            delete()
        }

        return binding.root
    }

    private fun delete(){
        //삭제 다이얼로그 띄우기
        val builder = AlertDialog.Builder(requireActivity())
        builder
            .setTitle("이 게시물을 삭제 하시겠습니까?")
            .setMessage("게시물을 삭제한 후에 복원은 불가능합니다.")
            .setPositiveButton("네") { dialog , which ->
                // 기능구현
                dataDelete()
            }
            .setNegativeButton("아니요"){ dialog, which ->
                // 기능구현
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun dataDelete(){
        firestore.collection("post").document(postId).delete().addOnSuccessListener {
            dismiss()
            Toast.makeText(context,"삭제에 성공했습니다!",Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            dismiss()
            Toast.makeText(context,"삭제에 실패했습니다!",Toast.LENGTH_LONG).show()
        }
        //TODO : 새로고침
    }
}