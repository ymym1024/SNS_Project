package com.app.sns_project

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Item(val username: String, val profileImageUrl: String)
data class chatItem(val time: Long, val username: String, val message: String)
data class chatTitleItem(val username: String, val profileImageUrl: String, val lastMessage: String)

class MyViewModel: ViewModel() {
    val itemsListData = MutableLiveData<ArrayList<Item>>()
    val items = ArrayList<Item>()

    val itemsListData2 = MutableLiveData<ArrayList<Item>>()
    val items2 = ArrayList<Item>()

    val chatTitleItemListData = MutableLiveData<ArrayList<chatTitleItem>>()
    val chatTitleItemList = ArrayList<chatTitleItem>()

    val chatItemsListData = MutableLiveData<ArrayList<chatItem>>()
    val chatItemsList = ArrayList<chatItem>()


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


    /* chat 관련 */

    fun addChatTitleItem(item: chatTitleItem){
        chatTitleItemList.add(item)
        chatTitleItemListData.value = chatTitleItemList
    }

    fun deleteChatTitleItem(){
        chatTitleItemList.clear()
        chatTitleItemListData.value = chatTitleItemList
    }

    /* chatroom 관련 */

    fun addChatItem(item: chatItem){
        chatItemsList.add(item)
        chatItemsListData.value = chatItemsList
    }

    fun deleteAllChatItem(){
        chatItemsList.clear()
        chatItemsListData.value = chatItemsList
    }
}