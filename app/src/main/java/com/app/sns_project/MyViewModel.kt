package com.app.sns_project

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Item(val username: String, val profileImageUrl: String)

class MyViewModel: ViewModel() {
    val itemsListData = MutableLiveData<ArrayList<Item>>()
    val items = ArrayList<Item>()

    val itemsListData2 = MutableLiveData<ArrayList<Item>>()
    val items2 = ArrayList<Item>()


    /* follower list 관련 */

    fun addItem(item: Item){
        items.add(item)
        itemsListData.value = items
    }

    fun deleteItem(pos: Int){
        items.removeAt(pos)
        itemsListData.value = items
    }


    /* following list 관련 */

    fun addItem2(item: Item){
        items2.add(item)
        itemsListData2.value = items2
    }

    fun deleteItem2(pos: Int){
        items2.removeAt(pos)
        itemsListData2.value = items2
    }
}