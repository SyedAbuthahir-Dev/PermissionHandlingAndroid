package com.example.permissionhandlingandroid

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class PermissionsViewModel:ViewModel() {

    val permissionQueue = mutableStateListOf<String>()

    fun dismissDialog(){
        permissionQueue.removeFirst()
    }

    fun onPermissionResult(
        permission:String,
        isGranted:Boolean
    ){
        if(!isGranted){
            permissionQueue.add(0,permission)
        }
    }
}