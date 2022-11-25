package com.app.sns_project.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.app.sns_project.databinding.FragmentProfileUpdateBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class ProfileUpdateFragment : Fragment() {
    private lateinit var binding: FragmentProfileUpdateBinding

    private lateinit var uid :String
    private lateinit var userName : String

    private var imageUri = ""

    companion object {
        const val REQ_UPDATE_GALLERY = 2
    }

    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == Activity.RESULT_OK){
            result.data?.data?.let {
                val imagePath = getRealPathFromURI(it)
                Log.d("imge url",imagePath)
                //binding.userImageImageView.setImageURI(it)
                Glide.with(this).clear(binding.userImageImageView)
                Glide.with(this).load(imagePath).apply(RequestOptions().circleCrop()).into(binding.userImageImageView)
                imageUri = imagePath
            }
        }else{
            return@registerForActivityResult
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileUpdateBinding.inflate(inflater, container, false)
        uid = FirebaseAuth.getInstance().currentUser!!.uid

        updateData()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initData(uid)
        getImage()
    }

    private fun initData(uid : String){
        FirebaseFirestore.getInstance().collection("user").document(uid).get().addOnSuccessListener {
            val name = it["userName"] as String
            val profile = it["profileImage"] as String

            //뷰 갱신
            userName = name // 이름 초기화
            binding.nameEdittext.setText(name)
            Glide.with(binding.userImageImageView.context).load(profile).apply(RequestOptions().circleCrop()).into(binding.userImageImageView)

            binding.emailEdittext.setText(FirebaseAuth.getInstance().currentUser!!.email)
        }
    }

    private fun getImage(){
        binding.profileUpdateTextview.setOnClickListener {
            val readPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            val writePermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if(readPermission == PackageManager.PERMISSION_DENIED || writePermission == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQ_UPDATE_GALLERY)
            }else{
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
                imageResult.launch(intent)
            }
        }
    }

    private fun updateData(){
        binding.saveButton.setOnClickListener {
            var userName = binding.nameEdittext.text.toString()

            if(!imageUri.isEmpty()){
                var imageFileName = "profile_images/"+userName+".png"
                val fileName = File(imageUri).toUri()

                var imageRef = FirebaseStorage.getInstance().reference.child("images").child(imageFileName)

                imageRef.putFile(fileName).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri->
                        val imageUri = uri.toString()

                        FirebaseFirestore.getInstance().collection("user").document(uid)
                            .update("profileImage",imageUri)
                            .addOnSuccessListener {
                                Snackbar.make(binding.root, "프로필이 수정되었습니다", Snackbar.LENGTH_SHORT).show()

                            }.addOnFailureListener {
                                Snackbar.make(binding.root, "등록이 실패했습니다. 네트워크를 확인해주세요", Snackbar.LENGTH_SHORT).show()
                            }
                    }
                }
            }else{
                Snackbar.make(binding.root, "수정을 원하시지 않으면 뒤로가기를 눌러주세요", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun getRealPathFromURI(uri : Uri):String{
        val buildName = Build.MANUFACTURER

        var columnIndex = 0
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity?.contentResolver?.query(uri, proj, null, null, null)
        if(cursor!!.moveToFirst()){
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        val result = cursor.getString(columnIndex)
        cursor.close()
        return result
    }
}