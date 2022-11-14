package com.app.sns_project

import android.Manifest
import android.app.Activity
import android.app.TaskInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.sns_project.DTO.PostDTO
import com.app.sns_project.databinding.FragmentPostAddBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PostAddFragment : Fragment() {
    private lateinit var binding: FragmentPostAddBinding

    private var list = ArrayList<Uri>()
    private var postId = ""

    lateinit var auth:FirebaseAuth
    lateinit var storage : FirebaseStorage
    lateinit var firestore: FirebaseFirestore

    companion object {
        const val REQ_GALLERY = 1
        const val UPLOAD_FOLDER_NAME = "images"
    }

    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == Activity.RESULT_OK){
            result?.data?.let { it ->
                if (it.clipData != null) {   // 사진을 여러개 선택한 경우
                    val count = it.clipData!!.itemCount
                    if (count > 10) {
                        Toast.makeText(context, "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_SHORT)
                            .show()
                        return@registerForActivityResult
                    }

                    for (i in 0 until count) {
                        val imageUri = it.clipData!!.getItemAt(i).uri
                        list.add(imageUri)
                    }
                } else {      // 1장 선택한 경우
                    val imageUri = it.data!!
                    list.add(imageUri)
                }
                setAdapater(list)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostAddBinding.inflate(inflater, container, false)

        //init
        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage
        firestore = FirebaseFirestore.getInstance()

        //TODO : 나중에 삭제할 로직 ------
        activity?.let {
            Firebase.auth.signInWithEmailAndPassword("ymjeong@hansung.ac.kr", "test1234!")
                .addOnCompleteListener(it) { view ->
                    if (view.isSuccessful) {
                        binding.userName.text = Firebase.auth.currentUser!!.email
                    } else {
                        Log.w("LoginActivity", "signInWithEmail", view.exception)
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        //-----------------

        textWatcher()
        selectImage()
        addPost()
        return binding.root
    }

    //글 등록
    private fun addPost(){
        binding.saveButton.setOnClickListener {
            val post = PostDTO()
            post.uid = auth?.currentUser?.uid
            post.userId = auth?.currentUser?.email // 임시로 저장해놓음
            post.content = binding.postEdittext.text.toString()
            post.timestamp = System.currentTimeMillis()

            if(list.isNotEmpty()){
                firestore?.collection("post").add(post).addOnSuccessListener {
                    Log.d("post_id",it.id)
                    postId = it.id
                    uploadImageAll()
                }.addOnFailureListener {
                    Snackbar.make(binding.root, "등록이 실패했습니다. 네트워크를 확인해주세요", Snackbar.LENGTH_SHORT).show()
                }
            }else{
                firestore?.collection("post").add(post).addOnSuccessListener {
                    Snackbar.make(binding.root, "글이 등록되었습니다.", Snackbar.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Snackbar.make(binding.root, "등록이 실패했습니다. 네트워크를 확인해주세요", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImageAll(){
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var saveImageList:ArrayList<String> = arrayListOf()
        for(i:Int in 0 until list.size){
            Log.d("index",i.toString())
            var imageFileName = "upload_images/"+timestamp+i+"_.png"
            var imageRef = storage.reference.child(UPLOAD_FOLDER_NAME)?.child(imageFileName)

            imageRef.putFile(list.get(i)).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri->
                    val imageUri = uri.toString()
                    saveImageList.add(imageUri)
                    firestore.collection("post").document(postId).update("imageUrl",saveImageList).addOnSuccessListener {
                        Snackbar.make(binding.root, "글이 등록되었습니다.", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    //글자수 200이내
    private fun textWatcher(){
        binding.postEdittext.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(p: Editable?) {
                if(binding.postEdittext.text!!.isEmpty()){
                    binding.postTextview.error = "글을 입력해주세요"
                }else if(binding.postEdittext.text!!.length > 200){
                    binding.postTextview.error = "200자 이내로 작성해주세요"
                }else{
                    binding.postTextview.error = null
                }
            }
        })
    }

    private fun showGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        imageResult.launch(intent)
    }

    private fun selectImage(){
        binding.addButton.setOnClickListener {
           val readPermission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            if(readPermission == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQ_GALLERY)
            }else{
                showGallery()
            }
        }
    }

    private fun setAdapater(list:ArrayList<Uri>){
        val adapter = ItemPagerAdapter(list)
        binding.imageList.adapter = adapter
        val gridLayoutManager = GridLayoutManager(context,2)
        binding.imageList.layoutManager = gridLayoutManager
    }
}

class ItemPagerAdapter(private val list: ArrayList<Uri>) :
    RecyclerView.Adapter<ItemPagerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_image_item, parent, false)
        )
    }

    override fun getItemCount(): Int = list.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.selectedImage.setImageURI(data)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val selectedImage : ImageView = view.findViewById(R.id.select_image)
    }

}

