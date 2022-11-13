package com.app.sns_project

import android.Manifest
import android.app.Activity
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
import com.app.sns_project.databinding.FragmentPostAddBinding


class PostAddFragment : Fragment() {
    private lateinit var binding: FragmentPostAddBinding

    companion object {
        const val REQ_GALLERY = 1
    }

    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if(result.resultCode == Activity.RESULT_OK){
            val list = ArrayList<Uri>()
            Log.d("호출완료","checked")
            result?.data?.let { it ->
                Log.d("호출완료2",it.toString())
                if (it.clipData != null) {   // 사진을 여러개 선택한 경우
                    val count = it.clipData!!.itemCount
                    if (count > 4) {
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

        textWatcher()
        selectImage()
        return binding.root
    }

    //글 등록
    private fun addPost(){

    }

    //글자수 200이내
    private fun textWatcher(){
        binding.postEdittext.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(p: Editable?) {
                if(p.toString().isEmpty()){
                    binding.postTextview.error = "글을 입력해주세요"
                }else{
                    p.toString().toIntOrNull()?.let {
                        if (p != null) {
                            if (p.length > 200) {
                                binding.postTextview.error = "200자 이내로 작성해주세요"
                            } else {
                                binding.postTextview.error = ""
                            }
                        }
                    }
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

