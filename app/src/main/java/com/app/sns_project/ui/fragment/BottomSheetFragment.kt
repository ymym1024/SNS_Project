package com.app.sns_project.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.sns_project.R
import com.app.sns_project.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class BottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    var postId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBottomSheetBinding.inflate(inflater,container,false)

        val args:BottomSheetFragmentArgs by navArgs()
        postId = args.postId

        firestore = FirebaseFirestore.getInstance()
        auth= FirebaseAuth.getInstance()

        //수정하기 버튼
        binding.postUpdate.setOnClickListener {

            findNavController().navigate(BottomSheetFragmentDirections.actionBottomSheetFragmentToPostUpdateFragment(postId))
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

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.custom_dialog_postdelete,null)

        var alertDialog = android.app.AlertDialog.Builder(context,R.style.CustomAlertDialog)
            .setView(view)
            .create()

        val deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)
        val cancelButton = view.findViewById<ImageButton>(R.id.cancelButton)

        deleteButton.setOnClickListener {
            dataDelete()
            alertDialog.dismiss()
        }
        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun dataDelete(){
        firestore.collection("post").document(postId).delete().addOnSuccessListener {
            findNavController().navigate(R.id.action_bottomSheetFragment_to_profileFragment)
            Toast.makeText(context,"삭제에 성공했습니다!",Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(context,"삭제에 실패했습니다!",Toast.LENGTH_LONG).show()
        }
        //TODO : 새로고침
    }
}